package Quick.Protocol.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class MemoryOutputStream extends OutputStream {

	private int position;
	private byte[] buffer;
	private int offset;
	private int length;

	public int getPosition() {
		return position;
	}

	public MemoryOutputStream(byte[] buffer, int offset, int length) {
		this.buffer = buffer;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public void write(int b) throws IOException {
		if (position + 1 > length)
			throw new IOException("数组越界");
		buffer[offset + position] = (byte) b;
		position++;
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (position + len > length)
			throw new IOException("数组越界");
		System.arraycopy(b, off, buffer, offset + position, len);
		position += len;
	}
}
