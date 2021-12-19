package Quick.Protocol.WebSocket.Client;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.common.message.MessageInputStream;
import org.eclipse.jetty.websocket.common.message.MessageOutputStream;
import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.Utils.LogUtils;

@WebSocket
public class WebSocketHandler {
	private WebSocketSession session;
	private ConnectionStreamInfo csi = null;
	private MessageInputStream inputStream = null;

	public boolean getIsConnected() {
		return session != null;
	}

	public ConnectionStreamInfo getConnectionStreamInfo() {

		if (csi == null) {
			csi = new ConnectionStreamInfo();
			csi.ConnectionOutputStream = new MessageOutputStream(session);
			inputStream = new MessageInputStream(session);
			csi.ConnectionInputStream = new java.io.BufferedInputStream(inputStream);
		}
		return csi;
	}

	@OnWebSocketConnect
	public void OnWebSocketConnect(Session session) {
		if (LogUtils.LogConnection)
			LogUtils.Log("[Connection] %s connected.", session.getRemoteAddress().toString());
		this.session = (WebSocketSession) session;
	}

	@OnWebSocketMessage
	public void onMessage(String msg) {
		System.out.printf("Got msg: %s%n", msg);
	}

	@OnWebSocketMessage
	public void OnWebSocketMessage(byte buf[], int offset, int length) {
		try {
			inputStream.appendFrame(ByteBuffer.wrap(buf, offset, length), false);
		} catch (Exception e) {
		}
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		if (LogUtils.LogConnection)
			LogUtils.Log("[Connection] %s disconnected.statusCode:%s,reason:%s", session.getRemoteAddress().toString(),
					statusCode, reason);

		this.session = null;
		if (csi != null) {
			try {
				inputStream.close();
			} catch (Exception ex) {
			}
			try {
				csi.ConnectionInputStream.close();
			} catch (Exception ex) {
			}
			try {
				csi.ConnectionOutputStream.close();
			} catch (Exception ex) {
			}
		}
	}
}