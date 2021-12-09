package Quick.Protocol.Utils;

public class ArraySegment {
	private byte[] array;
	private int count;
	private int offset;

	public ArraySegment(byte[] array) {
		this.array = array;
		this.offset = 0;
		this.count = array.length;
	}

	public ArraySegment(byte[] packageBuffer, int offset, int count) {
		this.array = packageBuffer;
		this.offset = offset;
		this.count = count;
	}

	public byte[] getArray() {
		return array;
	}

	public int getCount() {
		return count;
	}

	public int getOffset() {
		return offset;
	}
}
