package Quick.Protocol.Tcp;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.QpServer;
import Quick.Protocol.Utils.CancellationToken;
import Quick.Protocol.Utils.ExceptionUtils;
import Quick.Protocol.Utils.LogUtils;

public class QpTcpServer extends QpServer {
	private ServerSocket tcpListener;
	private QpTcpServerOptions options;
	public SocketAddress ListenEndPoint;

	public QpTcpServer(QpTcpServerOptions options) {
		super(options);
		this.options = options;
	}

	@Override
	public void Start() {
		try {
			if (options.Address == null)
				tcpListener = new ServerSocket(options.Port);
			else
				tcpListener = new ServerSocket(options.Port, 50, options.Address);
			ListenEndPoint = tcpListener.getLocalSocketAddress();
			super.Start();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void Stop() {
		if (tcpListener != null)
			try {
				tcpListener.close();
			} catch (Exception ex) {
			}
		tcpListener = null;
		super.Stop();
	}

	@Override
	protected void InnerAcceptAsync(CancellationToken token) {
		Socket socket = null;
		try {
			socket = tcpListener.accept();
			String remoteEndPointStr = "TCP:" + socket.getRemoteSocketAddress().toString();
			if (LogUtils.LogConnection)
				LogUtils.Log("[Connection]%s connected.", remoteEndPointStr);
			ConnectionStreamInfo connectionStreamInfo = new ConnectionStreamInfo();
			connectionStreamInfo.ConnectionInputStream = socket.getInputStream();
			connectionStreamInfo.ConnectionOutputStream = socket.getOutputStream();
			OnNewChannelConnected(connectionStreamInfo, remoteEndPointStr, token);
		} catch (Exception ex) {
			if (LogUtils.LogConnection)
				LogUtils.Log("[Connection]Init&Start Channel error,reason:%s", ExceptionUtils.GetExceptionString(ex));
			try {
				if (socket != null)
					socket.close();
			} catch (Exception ex2) {
			}
		}
	}
}
