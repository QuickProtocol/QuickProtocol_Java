package Quick.Protocol.Utils;

public class Convert {

	public static byte ToByte(int data) {
		return (byte) data;
	}

	public static int ToInt32(byte b) {
		int ret = (int) b & 0xff;
		return ret;
	}
}
