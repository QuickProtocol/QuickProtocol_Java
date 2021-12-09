package Quick.Protocol.Commands.PrivateCommand;

import Quick.Protocol.IQpCommandRequest;
import Quick.Protocol.Annotations.DisplayName;

/**
 * 私有命令请求
 */
@DisplayName("私有命令")
public class Request implements IQpCommandRequest<Response> {
	/**
	 * 动作
	 */
	public String Action;
	/**
	 * 内容
	 */
	public String Content;
}
