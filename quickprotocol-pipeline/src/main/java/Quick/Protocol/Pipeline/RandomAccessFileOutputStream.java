package Quick.Protocol.Pipeline;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileOutputStream extends OutputStream {

	private RandomAccessFile file;

	public RandomAccessFileOutputStream(RandomAccessFile file) {
		this.file = file;
	}

	@Override
	public void write(int b) throws IOException {
		file.write(b);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		file.write(b, off, len);
	}
}
