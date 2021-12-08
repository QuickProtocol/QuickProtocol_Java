package Quick.Protocol.Exceptions;

public class InstructionNotSupportException extends RuntimeException {
	private static final long serialVersionUID = -6638925536198172413L;

	public InstructionNotSupportException(String message) {
		super(message);
	}
}