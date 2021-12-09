package Quick.Protocol.Exceptions;

public class CommandException extends RuntimeException {
	/// <summary>
	/// 错误码
	/// </summary>
	public byte Code;

	public CommandException(byte code, String message) {
		super(message);
		if (code == 0)
			throw new RuntimeException("Code in CommandException must bigger than 0.");
		Code = code;
	}

	public CommandException(byte code, String message, Exception innerException) {
		super(message, innerException);
		Code = code;
	}
}
