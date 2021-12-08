package Quick.Protocol.Commands.Authenticate;

import Quick.Protocol.DisplayName;
import Quick.Protocol.IQpCommandRequest;

@DisplayName("认证")
public class Request implements IQpCommandRequest<Response> {
	/// 认证回答
	/// </summary>
	public String Answer;
}