package Quick.Protocol.Tcp;

import Quick.Protocol.QpClientOptions;
import Quick.Protocol.Annotations.Category;
import Quick.Protocol.Annotations.DisplayName;

public class QpTcpClientOptions extends QpClientOptions {
	/**
	 * 主机
	 */
	@DisplayName("主机")
	@Category("常用")
	public String Host = "127.0.0.1";
	/**
	 * 端口
	 */
	@DisplayName("端口")
	@Category("常用")
	public int Port = 3011;
	/**
	 * 本地主机
	 */
	@DisplayName("本地主机")
	@Category("高级")
	public String LocalHost;
	/**
	 * 本地端口
	 */
	@DisplayName("本地端口")
	@Category("高级")
	public int LocalPort;

	@Override
	public void Check() {
		super.Check();
		if (Host == null || Host == "")
			throw new RuntimeException("Argument 'Host' is null.");
		if (Port < 0 || Port > 65535)
			throw new RuntimeException("'Port' must between 0 and 65535");
	}

	@Override
	public String GetConnectionInfo() {
		return String.format("%s:%s", Host, Port);
	}
}
