package Quick.Protocol;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.StringUtils;

import Quick.Protocol.Utils.BitConverter;
import Quick.Protocol.Utils.CryptographyUtils;

public class App {

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException, DecoderException {
		// System.out.println("Schema:"+Base.getInstruction().NoticeInfos[0].NoticeTypeSchema);
		// System.out.println("SchemaSample:"+Base.getInstruction().NoticeInfos[0].NoticeTypeSchemaSample);
		// System.out.println(CryptographyUtils.ComputeMD5Hash("Hello"));

		/*
		 * byte[] tmpBuffer =
		 * CryptographyUtils.ComputeMD5Hash(StringUtils.getBytesUtf8("HelloQP")); byte[]
		 * passwordMd5Buffer = Arrays.copyOf(tmpBuffer, 8);
		 * 
		 * DESKeySpec dks = new DESKeySpec(passwordMd5Buffer); SecretKeyFactory
		 * keyFactory = SecretKeyFactory.getInstance("DES"); SecretKey secretKey =
		 * keyFactory.generateSecret(dks);
		 * 
		 * java.security.Security.addProvider(new
		 * org.bouncycastle.jce.provider.BouncyCastleProvider()); String
		 * CIPHER_ALGORITHM = "DES/ECB/PKCS7PADDING"; Cipher dec =
		 * Cipher.getInstance(CIPHER_ALGORITHM); dec.init(Cipher.DECRYPT_MODE,
		 * secretKey);
		 * 
		 * Cipher enc = Cipher.getInstance(CIPHER_ALGORITHM);
		 * enc.init(Cipher.ENCRYPT_MODE, secretKey);
		 * 
		 * byte[] content = new byte[] {1,2,3,4,5,6,7,8,9}; byte[] enBuffer =
		 * enc.doFinal(content);
		 * System.out.println(org.apache.commons.codec.binary.Hex.encodeHexString(
		 * enBuffer));
		 * System.out.println(org.apache.commons.codec.binary.Hex.encodeHexString(dec.
		 * doFinal(enBuffer))); byte[] dotnetEnBuffer =
		 * org.apache.commons.codec.binary.Hex.decodeHex(
		 * "44BC6A6E949BA5CF271F9EE9F11EFA58");
		 * System.out.println(org.apache.commons.codec.binary.Hex.encodeHexString(dec.
		 * doFinal(dotnetEnBuffer)));
		 */
		System.out.println(org.apache.commons.codec.binary.Hex.encodeHexString(new byte[] { -1, 0, (byte) 255 }));
		System.out.println("" + BitConverter.IsLittleEndian);
		System.out.println(BitConverter.class.getName());
		byte a = (byte) 255;
		int b = (int) a & 0xff;
		System.out.println("Byte 0xFF to int:" + b);
		System.out.println(String.format("%s %s", "12", 12));
		System.in.read();
	}

}
