package Quick.Protocol.WebSocket.Client;

import Quick.Protocol.QpClientOptions;

public class QpWebSocketClientOptions extends QpClientOptions {
	/**
	 * WebSocket的URL地址
	 */
	public String Url = "ws://127.0.0.1:3011/qp_test";

	@Override
	public void Check() {
		super.Check();
		if (Url == null)
			throw new RuntimeException("Url is null");
		if (!Url.startsWith("ws://"))
			throw new RuntimeException("Url must start with ws://");
	}

	@Override
	public String GetConnectionInfo() {
		return Url;
	}
}
