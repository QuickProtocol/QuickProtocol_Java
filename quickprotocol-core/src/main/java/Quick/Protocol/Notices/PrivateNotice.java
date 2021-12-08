package Quick.Protocol.Notices;

import Quick.Protocol.Description;
import Quick.Protocol.DisplayName;

@DisplayName("私有通知")
@Description("用于传递私有协议通知。")
public class PrivateNotice {
	/// <summary>
	/// 动作
	/// </summary>
	public String Action;
	/// <summary>
	/// 内容
	/// </summary>
	public String Content;
}