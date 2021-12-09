package Quick.Protocol.Commands.Authenticate;

import Quick.Protocol.IQpCommandRequest;
import Quick.Protocol.Annotations.DisplayName;

@DisplayName("认证")
public class Request implements IQpCommandRequest<Response> {
	/// 认证回答
	/// </summary>
	public String Answer;
}