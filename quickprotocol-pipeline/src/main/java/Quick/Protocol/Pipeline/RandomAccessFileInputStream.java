package Quick.Protocol.Pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileInputStream extends InputStream {

	private RandomAccessFile file;

	public RandomAccessFileInputStream(RandomAccessFile file) {
		this.file = file;
	}

	@Override
	public int read() throws IOException {
		if (file.length() == 0)
			return -1;
		return file.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (file.length() == 0)
			return 0;
		return file.read(b, off, len);
	}
}
