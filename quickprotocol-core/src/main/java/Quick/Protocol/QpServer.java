package Quick.Protocol;

import java.util.ArrayList;
import java.util.List;

import Quick.Protocol.Listeners.AuchenticateTimeoutListener;
import Quick.Protocol.Listeners.AuchenticatedListener;
import Quick.Protocol.Listeners.DisconnectedListener;
import Quick.Protocol.Listeners.ServerChannelAuchenticateTimeoutListener;
import Quick.Protocol.Listeners.ServerChannelConnectedListener;
import Quick.Protocol.Listeners.ServerChannelDisconnectedListener;
import Quick.Protocol.Utils.CancellationToken;
import Quick.Protocol.Utils.LogUtils;

public abstract class QpServer {
	private CancellationToken cts;
	private QpServerOptions options;
	private List<QpServerChannel> channelList = new ArrayList<QpServerChannel>();

	/**
	 * 增加Tag属性，用于引用与QpServer相关的对象
	 */
	public Object Tag;

	/**
	 * 获取全部的通道
	 */
	public QpServerChannel[] Channels = new QpServerChannel[0];

	private ArrayList<ServerChannelConnectedListener> ChannelConnectedListeners = new ArrayList<ServerChannelConnectedListener>();

	public void addChannelConnectedListener(ServerChannelConnectedListener listener) {
		ChannelConnectedListeners.add(listener);
	}

	public void removeChannelConnectedListener(ServerChannelConnectedListener listener) {
		ChannelConnectedListeners.remove(listener);
	}

	private ArrayList<ServerChannelDisconnectedListener> ChannelDisconnectedListeners = new ArrayList<ServerChannelDisconnectedListener>();

	public void addChannelDisconnectedListener(ServerChannelDisconnectedListener listener) {
		ChannelDisconnectedListeners.add(listener);
	}

	public void removeChannelDisconnectedListener(ServerChannelDisconnectedListener listener) {
		ChannelDisconnectedListeners.remove(listener);
	}

	private ArrayList<ServerChannelAuchenticateTimeoutListener> ChannelAuchenticateTimeoutListeners = new ArrayList<ServerChannelAuchenticateTimeoutListener>();

	public void addChannelAuchenticateTimeoutListener(ServerChannelAuchenticateTimeoutListener listener) {
		ChannelAuchenticateTimeoutListeners.add(listener);
	}

	public void removeChannelAuchenticateTimeoutListener(ServerChannelAuchenticateTimeoutListener listener) {
		ChannelAuchenticateTimeoutListeners.remove(listener);
	}

	public QpServer(QpServerOptions options) {
		options.Check();
		this.options = options;
	}

	public void Start() {
		cts = new CancellationToken();
		beginAccept(cts);
	}

	protected void RemoveChannel(QpServerChannel channel) {
		synchronized (channelList) {
			if (channelList.contains(channel)) {
				channelList.remove(channel);
				Channels = channelList.toArray(new QpServerChannel[0]);
			}
		}
	}

	protected void OnNewChannelConnected(final ConnectionStreamInfo connectionStreamInfo, final String channelName,
			CancellationToken token) {
		final QpServerChannel channel = new QpServerChannel(this, connectionStreamInfo, channelName, token,
				options.Clone());

		// 认证超时
		channel.addAuchenticateTimeoutListener(new AuchenticateTimeoutListener() {
			@Override
			public void Invoke() {
				if (LogUtils.LogConnection)
					LogUtils.Log("[Connection]{0} auchenticate timeout.", channelName);

				if (ChannelAuchenticateTimeoutListeners.size() > 0)
					for (ServerChannelAuchenticateTimeoutListener listener : ChannelAuchenticateTimeoutListeners)
						listener.Invoke(channel);
			}
		});

		channel.addAuchenticatedListener(new AuchenticatedListener() {
			public void Invoke() {
				// 将通道加入到全部通道列表里面
				synchronized (channelList) {
					channelList.add(channel);
					Channels = channelList.toArray(new QpServerChannel[0]);
				}
				channel.addDisconnectedListener(new DisconnectedListener() {

					public void Invoke() {
						if (LogUtils.LogConnection)
							LogUtils.Log("[Connection]%s Disconnected.", channelName);
						RemoveChannel(channel);
						try {
							connectionStreamInfo.ConnectionInputStream.close();
							connectionStreamInfo.ConnectionOutputStream.close();
						} catch (Exception ex) {
						}
						if (ChannelDisconnectedListeners.size() > 0)
							for (ServerChannelDisconnectedListener listener : ChannelDisconnectedListeners)
								listener.Invoke(channel);
					}

				});
				if (ChannelConnectedListeners.size() > 0)
					for (ServerChannelConnectedListener listener : ChannelConnectedListeners)
						listener.Invoke(channel);
			}
		});
	}

	protected abstract void InnerAcceptAsync(CancellationToken token);

	private void beginAccept(final CancellationToken token) {
		new Thread(new Runnable() {

			public void run() {
				while (true) {
					if (token.IsCancellationRequested())
						return;
					try {
						InnerAcceptAsync(token);
					} catch (Exception ex) {
						return;
					}
				}
			}
		}).start();
	}

	public void Stop() {
		if (cts != null)
			cts.Cancel();
		cts = null;
	}
}
