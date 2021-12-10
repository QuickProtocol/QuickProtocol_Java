package Quick.Protocol.Listeners;

import Quick.Protocol.QpServerChannel;

/**
 * 服务通道已连接监听器
 */
public interface ServerChannelConnectedListener {
	/**
	 * 执行
	 */
	public abstract void Invoke(QpServerChannel channel);
}
