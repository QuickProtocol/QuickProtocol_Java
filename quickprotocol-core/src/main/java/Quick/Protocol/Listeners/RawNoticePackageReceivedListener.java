package Quick.Protocol.Listeners;

/**
 * 原始通知数据包接收监听器
 */
public interface RawNoticePackageReceivedListener {
	/**
	 * 执行
	 * 
	 * @param typeName 类型名称
	 * @param content  内容
	 */
	public abstract void Invoke(String typeName, String content);
}
