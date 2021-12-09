package Quick.Protocol.Listeners;

public interface RawCommandRequestPackageReceivedListener {
	/**
	 * 执行
	 * 
	 * @param commandId 命令编号
	 * @param typeName  类型名称
	 * @param content   内容
	 * 
	 * @return 是否已处理
	 */
	public abstract boolean Invoke(String commandId, String typeName, String content);
}
