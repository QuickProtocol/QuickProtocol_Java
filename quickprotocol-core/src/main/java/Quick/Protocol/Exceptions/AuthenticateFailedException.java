package Quick.Protocol.Exceptions;

public class AuthenticateFailedException extends RuntimeException {
	private static final long serialVersionUID = -2574460248762499208L;

	public AuthenticateFailedException(String message) {
		super(message);
	}
}
