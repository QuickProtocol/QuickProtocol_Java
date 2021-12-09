package Quick.Protocol.Utils;

import java.nio.ByteBuffer;

public class ByteUtils {

	public static int B2I_BE(byte[] buffer, int startIndex) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		for (int i = 0; i < 4; i++) {
			// 如果是小端字节序，则交换
			if (BitConverter.IsLittleEndian)
				byteBuffer.put(buffer[startIndex + (4 - i)]);
			else
				byteBuffer.put(buffer[startIndex + i]);
		}
		byteBuffer.position(0);
		int ret = byteBuffer.getInt();
		return ret;
	}
}
