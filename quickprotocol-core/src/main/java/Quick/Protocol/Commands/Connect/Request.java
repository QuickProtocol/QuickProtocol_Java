package Quick.Protocol.Commands.Connect;

import Quick.Protocol.DisplayName;
import Quick.Protocol.IQpCommandRequest;

/**
 * 连接请求命令
 */
@DisplayName("连接")
public class Request implements IQpCommandRequest<Response> {
	/**
	 * 指令集编号数组
	 */
	public String[] InstructionIds;
}