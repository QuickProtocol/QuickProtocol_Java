package Quick.Protocol.WebSocket.Client;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.client.WebSocketClient;

import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.QpClient;

public class QpWebSocketClient extends QpClient {

	private QpWebSocketClientOptions options;
	private WebSocketClient client;
	private WebSocketHandler handler;

	public QpWebSocketClient(QpWebSocketClientOptions options) {
		super(options);
		this.options = options;
	}

	@Override
	protected ConnectionStreamInfo InnerConnectAsync() {
		client = new WebSocketClient();
		handler = new WebSocketHandler();
		try {
			client.start();
			client.connect(handler, new URI(options.Url));
			Date beginTime = new Date();
			while (!handler.getIsConnected()) {
				long usedTime = new Date().getTime() - beginTime.getTime();
				if (usedTime > options.ConnectionTimeout)
					throw new TimeoutException();
				Thread.sleep(100);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return handler.getConnectionStreamInfo();
	}

	@Override
	protected void Disconnect() {
		if (client != null) {
			try {
				client.stop();
			} catch (Exception ex) {
			}
			client = null;
		}

		super.Disconnect();
	}
}
