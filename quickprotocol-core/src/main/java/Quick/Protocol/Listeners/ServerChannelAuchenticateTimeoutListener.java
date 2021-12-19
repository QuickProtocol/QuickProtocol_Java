package Quick.Protocol.Listeners;

import Quick.Protocol.QpServerChannel;

/**
 * 服务通道认证超时监听器
 */
public interface ServerChannelAuchenticateTimeoutListener {
	/**
	 * 执行
	 */
	public abstract void Invoke(QpServerChannel channel);
}
