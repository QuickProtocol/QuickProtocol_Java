package com.github.quickprotocol;

import java.util.HashMap;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommandExecuterManager {
	private HashMap<String, ICommandExecuter> commandExecuterDict = new HashMap<String, ICommandExecuter>();

	public CommandExecuterManager() {
	}

	/**
	 * 获取全部注册的命令请求类型名称
	 * 
	 * @return
	 */
	public String[] getRegisterCommandRequestTypeNames() {
		return commandExecuterDict.keySet().toArray(new String[0]);
	}

	public void register(String cmdRequestTypeName, ICommandExecuter commandExecuter) {
		commandExecuterDict.put(cmdRequestTypeName, commandExecuter);
	}

	public void register(Class cmdRequestType, ICommandExecuter commandExecuter) {
		register(cmdRequestType.getName(), commandExecuter);
	}

	/**
	 * 执行命令
	 * 
	 * @param handler
	 * @param cmdRequestTypeName
	 * @param cmdRequestModel
	 * @return
	 */
	public Object executeCommand(QpChannel handler, String cmdRequestTypeName, Object cmdRequestModel) {
		if (!canExecuteCommand(cmdRequestTypeName))
			throw new RuntimeException(String.format("Command Request Type[{0}] has no executer.", cmdRequestTypeName));
		ICommandExecuter commandExecuter = commandExecuterDict.get(cmdRequestTypeName);
		return commandExecuter.execute(handler, cmdRequestModel);
	}

	/**
	 * 能否执行指定类型的命令
	 * 
	 * @param cmdRequestTypeName
	 * @return
	 */
	public Boolean canExecuteCommand(String cmdRequestTypeName) {
		return commandExecuterDict.containsKey(cmdRequestTypeName);
	}
}