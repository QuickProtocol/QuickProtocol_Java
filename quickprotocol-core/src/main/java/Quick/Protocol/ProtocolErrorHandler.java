package Quick.Protocol;

import Quick.Protocol.Utils.ArraySegment;

public interface ProtocolErrorHandler {
	public void Invoke(ConnectionStreamInfo connectionStreamInfo, ArraySegment buffer);
}
