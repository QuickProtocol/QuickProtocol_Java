package Quick.Protocol;

import java.util.HashMap;

public class CommandExecuterManager {
	private HashMap<String, ICommandExecuter<?, ?>> commandExecuterDict = new HashMap<String, ICommandExecuter<?, ?>>();

	public CommandExecuterManager() {
	}

	/**
	 * 获取全部注册的命令请求类型名称
	 * 
	 * @return
	 */
	public String[] GetRegisterCommandRequestTypeNames() {
		return commandExecuterDict.keySet().toArray(new String[0]);
	}

	public void Register(String cmdRequestTypeName, ICommandExecuter<?, ?> commandExecuter) {
		commandExecuterDict.put(cmdRequestTypeName, commandExecuter);
	}

	public void Register(Class<?> cmdRequestType, ICommandExecuter<?, ?> commandExecuter) {
		Register(cmdRequestType.getName(), commandExecuter);
	}

	/**
	 * 执行命令
	 * 
	 * @param handler
	 * @param cmdRequestTypeName
	 * @param cmdRequestModel
	 * @return
	 */
	public Object ExecuteCommand(QpChannel handler, String cmdRequestTypeName, Object cmdRequestModel) {
		if (!CanExecuteCommand(cmdRequestTypeName))
			throw new RuntimeException(String.format("Command Request Type[{0}] has no executer.", cmdRequestTypeName));
		ICommandExecuter<?, ?> commandExecuter = commandExecuterDict.get(cmdRequestTypeName);
		return commandExecuter.Execute(handler, cmdRequestModel);
	}

	/**
	 * 能否执行指定类型的命令
	 * 
	 * @param cmdRequestTypeName
	 * @return
	 */
	public Boolean CanExecuteCommand(String cmdRequestTypeName) {
		return commandExecuterDict.containsKey(cmdRequestTypeName);
	}
}