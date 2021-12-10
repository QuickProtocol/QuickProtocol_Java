package Quick.Protocol.Tcp;

import java.net.InetSocketAddress;
import java.net.Socket;

import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.QpClient;
import Quick.Protocol.Annotations.DisplayName;

@DisplayName("TCP")
public class QpTcpClient extends QpClient {
	private Socket tcpClient;
	private QpTcpClientOptions options;

	public QpTcpClient(QpTcpClientOptions options) {
		super(options);
		this.options = options;
	}

	@Override
	protected ConnectionStreamInfo InnerConnectAsync() {
		try {
			if (tcpClient != null)
				Close();

			// 开始连接
			tcpClient = new Socket(options.Host, options.Port);
			if (options.LocalHost != null && !"".equals(options.LocalHost))
				tcpClient.bind(InetSocketAddress.createUnresolved(options.LocalHost, options.LocalPort));

			if (!tcpClient.isConnected())
				throw new RuntimeException(String.format("Failed to connect to %s:%s.", options.Host, options.Port));
			ConnectionStreamInfo ret = new ConnectionStreamInfo();
			ret.ConnectionInputStream = tcpClient.getInputStream();
			ret.ConnectionOutputStream = tcpClient.getOutputStream();
			return ret;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void Disconnect() {
		if (tcpClient != null) {
			try {
				tcpClient.close();
			} catch (Exception ex) {
			}
			tcpClient = null;
		}

		super.Disconnect();
	}
}
