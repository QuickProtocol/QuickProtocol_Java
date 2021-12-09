package Quick.Protocol.Listeners;

public interface CommandRequestPackageReceivedListener {
	/**
	 * 执行
	 * 
	 * @param commandId    命令编号
	 * @param typeName     类型名称
	 * @param contentModel 内容模型
	 */
	public abstract void Invoke(String commandId, String typeName, Object contentModel);
}
