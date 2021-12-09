package Quick.Protocol.Listeners;

public interface NoticePackageReceivedListener {
	/**
	 * 执行
	 * 
	 * @param typeName 类型名称
	 * @param contentModel  内容模型
	 */
	public abstract void Invoke(String typeName, Object contentModel);
}
