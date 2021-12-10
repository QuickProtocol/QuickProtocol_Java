package Quick.Protocol.Listeners;

import Quick.Protocol.QpServerChannel;

/**
 * 服务通道已断开监听器
 */
public interface ServerChannelDisconnectedListener {
	/**
	 * 执行
	 */
	public abstract void Invoke(QpServerChannel channel);
}
