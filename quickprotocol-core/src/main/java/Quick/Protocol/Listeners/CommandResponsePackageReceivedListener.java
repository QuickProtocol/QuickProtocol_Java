package Quick.Protocol.Listeners;

public interface CommandResponsePackageReceivedListener {
	/**
	 * 执行
	 * 
	 * @param commandId 命令编号
	 * @param code      响应码
	 * @param message   错误消息
	 * @param typeName  类型名称
	 * @param content   内容
	 */
	public abstract void Invoke(String commandId, byte code, String message, String typeName, String content);
}
