package Quick.Protocol;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import Quick.Protocol.Exceptions.CommandException;
import Quick.Protocol.Exceptions.ProtocolException;
import Quick.Protocol.Listeners.AuchenticatedListener;
import Quick.Protocol.Listeners.DisconnectedListener;
import Quick.Protocol.Utils.BitConverter;
import Quick.Protocol.Utils.CancellationToken;
import Quick.Protocol.Utils.CryptographyUtils;
import Quick.Protocol.Utils.LogUtils;

public class QpServerChannel extends QpChannel {
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private QpServer server;
	private ConnectionStreamInfo connectionStreamInfo;
	private CancellationToken cts;
	private QpServerOptions options;
	private String question;
	// 通过认证后，才允许使用的命令执行管理器列表
	private List<CommandExecuterManager> authedCommandExecuterManagerList = null;

	public String ChannelName;

	private ArrayList<AuchenticatedListener> AuchenticatedListeners = new ArrayList<AuchenticatedListener>();

	public void addAuchenticatedListener(AuchenticatedListener listener) {
		AuchenticatedListeners.add(listener);
	}

	public void removeAuchenticatedListener(AuchenticatedListener listener) {
		AuchenticatedListeners.remove(listener);
	}

	private ArrayList<DisconnectedListener> DisconnectedListeners = new ArrayList<DisconnectedListener>();

	public void addDisconnectedListener(DisconnectedListener listener) {
		DisconnectedListeners.add(listener);
	}

	public void removeDisconnectedListener(DisconnectedListener listener) {
		DisconnectedListeners.remove(listener);
	}

	public QpServerChannel(QpServer server, ConnectionStreamInfo connectionStreamInfo, String channelName,
			CancellationToken cancellationToken, QpServerOptions options) {
		super(options);

		this.server = server;
		this.connectionStreamInfo = connectionStreamInfo;
		this.ChannelName = channelName;
		this.options = options;
		this.authedCommandExecuterManagerList = options.CommandExecuterManagerList;

		cts = new CancellationToken();
		cancellationToken.Register(new Runnable() {

			public void run() {
				Stop();
			}
		});
		// 修改缓存大小
		ChangeBufferSize(options.BufferSize);
		IsConnected = true;

		// 初始化连接相关指令处理器
		CommandExecuterManager connectAndAuthCommandExecuterManager = new CommandExecuterManager();
		connectAndAuthCommandExecuterManager.Register(Quick.Protocol.Commands.Connect.Request.class,
				new ICommandExecuter<Quick.Protocol.Commands.Connect.Request, Quick.Protocol.Commands.Connect.Response>() {
					public Quick.Protocol.Commands.Connect.Response Execute(QpChannel handler, Object request) {
						return connect(handler, (Quick.Protocol.Commands.Connect.Request) request);
					}
				});
		connectAndAuthCommandExecuterManager.Register(Quick.Protocol.Commands.Authenticate.Request.class,
				new ICommandExecuter<Quick.Protocol.Commands.Authenticate.Request, Quick.Protocol.Commands.Authenticate.Response>() {
					public Quick.Protocol.Commands.Authenticate.Response Execute(QpChannel handler, Object request) {
						return authenticate(handler, (Quick.Protocol.Commands.Authenticate.Request) request);
					}
				});
		connectAndAuthCommandExecuterManager.Register(Quick.Protocol.Commands.HandShake.Request.class,
				new ICommandExecuter<Quick.Protocol.Commands.HandShake.Request, Quick.Protocol.Commands.HandShake.Response>() {
					public Quick.Protocol.Commands.HandShake.Response Execute(QpChannel handler, Object request) {
						return handShake(handler, (Quick.Protocol.Commands.HandShake.Request) request);
					}
				});
		connectAndAuthCommandExecuterManager.Register(Quick.Protocol.Commands.GetQpInstructions.Request.class,
				new ICommandExecuter<Quick.Protocol.Commands.GetQpInstructions.Request, Quick.Protocol.Commands.GetQpInstructions.Response>() {
					public Quick.Protocol.Commands.GetQpInstructions.Response Execute(QpChannel handler,
							Object request) {
						return getQpInstructions(handler, (Quick.Protocol.Commands.GetQpInstructions.Request) request);
					}
				});
		options.CommandExecuterManagerList = new ArrayList<CommandExecuterManager>();
		options.CommandExecuterManagerList.add(connectAndAuthCommandExecuterManager);

		InitQpPackageHandler_Stream(connectionStreamInfo);
		// 开始读取其他数据包
		BeginReadPackage(cts);
	}

	private Quick.Protocol.Commands.Connect.Response connect(QpChannel handler,
			Quick.Protocol.Commands.Connect.Request request) {
		if (request.InstructionIds != null) {
			for (String id : request.InstructionIds) {
				boolean exists = false;
				for (QpInstruction item : options.getInstructionSet()) {
					if (item.Id.equals(id)) {
						exists = true;
						break;
					}
				}
				if (!exists)
					throw new CommandException((byte) 255, String.format("Unknown instruction: %s", id));
			}
		}

		question = Hex.encodeHexString(BitConverter.GetBytes(UUID.randomUUID()));
		Quick.Protocol.Commands.Connect.Response rep = new Quick.Protocol.Commands.Connect.Response();
		rep.BufferSize = options.BufferSize;
		rep.Question = question;
		return rep;
	}

	private Quick.Protocol.Commands.Authenticate.Response authenticate(QpChannel handler,
			Quick.Protocol.Commands.Authenticate.Request request) {
		if (!CryptographyUtils.ComputeMD5Hash(question + options.Password).equals(request.Answer)) {
			new Thread(new Runnable() {

				public void run() {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					Stop();
				}

			}).start();
			throw new CommandException((byte) 1, "认证失败！");
		}
		if (AuchenticatedListeners.size() > 0)
			for (AuchenticatedListener listener : AuchenticatedListeners)
				listener.Invoke();

		return new Quick.Protocol.Commands.Authenticate.Response();
	}

	private Quick.Protocol.Commands.HandShake.Response handShake(QpChannel handler,
			Quick.Protocol.Commands.HandShake.Request request) {
		options.CommandExecuterManagerList.addAll(authedCommandExecuterManagerList);
		options.InternalCompress = request.EnableCompress;
		options.InternalEncrypt = request.EnableEncrypt;
		options.InternalTransportTimeout = request.TransportTimeout;

		// 开始心跳
		if (options.getHeartBeatInterval() > 0)
			BeginHeartBeat(cts);
		return new Quick.Protocol.Commands.HandShake.Response();
	}

	private Quick.Protocol.Commands.GetQpInstructions.Response getQpInstructions(QpChannel handler,
			Quick.Protocol.Commands.GetQpInstructions.Request request) {
		Quick.Protocol.Commands.GetQpInstructions.Response rep = new Quick.Protocol.Commands.GetQpInstructions.Response();
		rep.Data = options.getInstructionSet();
		return rep;
	}

	/// <summary>
	/// 停止
	/// </summary>
	public void Stop() {
		try {
			if (cts != null && !cts.IsCancellationRequested())
				cts.Cancel();

			connectionStreamInfo.ConnectionInputStream.close();
			connectionStreamInfo.ConnectionOutputStream.close();
		} catch (Exception ex) {
		}
	}

	@Override
	protected void OnReadError(Exception exception) {
		if (options.ProtocolErrorHandler != null) {
			if (ProtocolException.class.isInstance(exception)) {
				ProtocolException protocolException = (ProtocolException) exception;
				server.RemoveChannel(this);
				if (LogUtils.LogConnection)
					LogUtils.Log("[ProtocolErrorHandler]%s: Begin ProtocolErrorHandler invoke...",
							dateFormat.format(new Date()));

				options.ProtocolErrorHandler.Invoke(connectionStreamInfo, protocolException.ReadBuffer);
				return;
			}
		}
		Stop();
		super.OnReadError(exception);
		if (IsConnected) {
			IsConnected = false;
			if (DisconnectedListeners.size() > 0)
				for (DisconnectedListener listener : DisconnectedListeners)
					listener.Invoke();
		}
	}
}
