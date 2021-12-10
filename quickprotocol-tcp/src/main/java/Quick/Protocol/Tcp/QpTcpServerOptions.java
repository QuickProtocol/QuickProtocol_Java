package Quick.Protocol.Tcp;

import java.net.InetAddress;

import com.fasterxml.jackson.annotation.JsonIgnore;

import Quick.Protocol.QpServerOptions;

public class QpTcpServerOptions extends QpServerOptions {
	/**
	 * IP地址，设置为null代表全部IP地址
	 */
	@JsonIgnore
	public InetAddress Address;
	/**
	 * 端口
	 */
	public int Port;

	@Override
	public void Check() {
		super.Check();
		if (Port < 0 || Port > 65535)
			throw new RuntimeException("Port must between 0 and 65535");
	}
}
