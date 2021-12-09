package Quick.Protocol;

public class ProtocolException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2047934307573162984L;
	public ArraySegment ReadBuffer;

	public ProtocolException(ArraySegment readBuffer, String message) {
		super(message);
		ReadBuffer = readBuffer;
	}
}
