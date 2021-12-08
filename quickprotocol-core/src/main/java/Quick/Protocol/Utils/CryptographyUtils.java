package Quick.Protocol.Utils;

import org.apache.commons.codec.digest.DigestUtils;

public class CryptographyUtils {

	public static String ComputeMD5Hash(String data) {
		byte[] buffer = DigestUtils.md5(data);
		return org.apache.commons.codec.binary.Hex.encodeHexString(buffer, true);
	}

	public static byte[] ComputeMD5Hash(byte[] data) {
		return DigestUtils.md5(data);
	}
}
