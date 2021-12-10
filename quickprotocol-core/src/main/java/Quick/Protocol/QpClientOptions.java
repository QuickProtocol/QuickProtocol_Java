package Quick.Protocol;

import Quick.Protocol.Annotations.Category;
import Quick.Protocol.Annotations.DisplayName;

public abstract class QpClientOptions extends QpChannelOptions {
	/**
	 * 连接超时(默认为5秒)
	 */
	@DisplayName("连接超时")
	@Category("高级")
	public int ConnectionTimeout = 5 * 1000;

	/**
	 * 获取传输超时
	 * 
	 * @return
	 */
	public int getTransportTimeout() {
		return InternalTransportTimeout;
	}

	/**
	 * 设置传输超时
	 * 
	 * @param value
	 */
	public void setTransportTimeout(int value) {
		InternalTransportTimeout = value;
	}

	/**
	 * 启用加密(默认为false)
	 */
	@DisplayName("启用加密")
	@Category("高级")
	public boolean EnableEncrypt = false;
	/// <summary>
	/// 启用压缩(默认为false)
	/// </summary>
	@DisplayName("启用压缩")
	@Category("高级")
	public boolean EnableCompress = false;

	/// <summary>
	/// 当认证通过时
	/// </summary>
	public void OnAuthPassed() {
		InternalCompress = EnableCompress;
		InternalEncrypt = EnableEncrypt;
	}

	/**
	 * 初始化
	 */
	public void Init() {
		InternalCompress = false;
		InternalEncrypt = false;
	}

	/**
	 * 获取连接信息
	 * 
	 * @return
	 */
	public abstract String GetConnectionInfo();

	@Override
	public String toString() {
		return GetConnectionInfo();
	}
}
