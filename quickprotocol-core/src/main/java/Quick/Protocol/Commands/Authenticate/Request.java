package Quick.Protocol.Commands.Authenticate;

import com.github.quickprotocol.DisplayName;
import com.github.quickprotocol.IQpCommandRequest;

@DisplayName("认证")
public class Request implements IQpCommandRequest<Response> {
	/// 认证回答
	/// </summary>
	public String Answer;
}