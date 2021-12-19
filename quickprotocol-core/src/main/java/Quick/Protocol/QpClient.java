package Quick.Protocol;

import java.util.ArrayList;
import Quick.Protocol.Utils.CancellationToken;
import Quick.Protocol.Utils.CryptographyUtils;

public abstract class QpClient extends QpChannel {
	private CancellationToken cts = null;
	public QpClientOptions Options;

	public QpClient(QpClientOptions options) {
		super(options);
		options.Check();
		this.Options = options;
	}

	protected abstract ConnectionStreamInfo InnerConnectAsync();

	/// <summary>
	/// 连接
	/// </summary>
	public void ConnectAsync() {
		// 清理
		Close();
		cts = new CancellationToken();

		ConnectionStreamInfo connectionStreamInfo;
		try {
			connectionStreamInfo = InnerConnectAsync();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// 初始化网络
		InitQpPackageHandler_Stream(connectionStreamInfo);

		// 开始读取其他数据包
		BeginReadPackage(cts);

		Quick.Protocol.Commands.Connect.Request connectRequest = new Quick.Protocol.Commands.Connect.Request();
		ArrayList<String> connectRequestInstructionIdList = new ArrayList<String>();
		for (QpInstruction item : Options.getInstructionSet()) {
			connectRequestInstructionIdList.add(item.Id);
		}
		connectRequest.InstructionIds = connectRequestInstructionIdList.toArray(new String[0]);
		Quick.Protocol.Commands.Connect.Response repConnect = super.SendCommand(connectRequest,
				Quick.Protocol.Commands.Connect.Response.class);

		// 如果服务端使用的缓存大小与客户端不同，则设置缓存大小为与服务端同样的大小
		if (BufferSize != repConnect.BufferSize)
			ChangeBufferSize(repConnect.BufferSize);

		// 认证
		Quick.Protocol.Commands.Authenticate.Request authenticateRequest = new Quick.Protocol.Commands.Authenticate.Request();
		authenticateRequest.Answer = CryptographyUtils.ComputeMD5Hash(repConnect.Question + Options.Password);
		super.SendCommand(authenticateRequest, Quick.Protocol.Commands.Authenticate.Response.class);

		// 握手
		Quick.Protocol.Commands.HandShake.Request handshakeRequest = new Quick.Protocol.Commands.HandShake.Request();
		handshakeRequest.EnableCompress = Options.EnableCompress;
		handshakeRequest.EnableEncrypt = Options.EnableEncrypt;
		handshakeRequest.TransportTimeout = Options.getTransportTimeout();

		super.SendCommand(handshakeRequest, Quick.Protocol.Commands.HandShake.Response.class, 5000, new Runnable() {
			public void run() {
				Options.OnAuthPassed();
				IsConnected = true;
			}
		});

		// 开始心跳
		if (Options.getHeartBeatInterval() > 0) {
			// 定时发送心跳包
			BeginHeartBeat(cts);
		}
	}

	@Override
	protected void OnReadError(Exception exception) {
		super.OnReadError(exception);
		Options.Init();
		cancellAll();
		Disconnect();
	}

	private void cancellAll() {
		if (cts != null) {
			cts.Cancel();
			cts = null;
		}
	}

	/// <summary>
	/// 关闭连接
	/// </summary>
	public void Close() {
		cancellAll();
		InitQpPackageHandler_Stream(null);
		Disconnect();
	}
}