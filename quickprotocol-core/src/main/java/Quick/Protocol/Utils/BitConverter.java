package Quick.Protocol.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class BitConverter {
	/**
	 * 是否是小端字节序
	 */
	public static Boolean IsLittleEndian;

	static {
		IsLittleEndian = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);
	}

	public static byte[] GetBytes(int data) {
		return ByteBuffer.allocate(4).putInt(data).array();
	}

	public static String ToString(byte[] array, int offset, int count) {
		return new String(org.apache.commons.codec.binary.Hex.encodeHex(array, offset, count, false));
	}

	public static byte[] GetBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
}
