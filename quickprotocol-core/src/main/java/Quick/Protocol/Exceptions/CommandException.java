package Quick.Protocol.Exceptions;

public class CommandException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6708464326046860574L;
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
