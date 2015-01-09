package com.dslplatform.client.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class JsonWriter extends Writer {

	public final char[] tmp = new char[48];
	private final char[] helper = new char[8192];
	private byte[] result;
	private int position;

	private static final Charset utf8 = Charset.forName("UTF-8");

	public JsonWriter() {
		this(512);
	}

	public JsonWriter(final int size) {
		this(new byte[size]);
	}

	public JsonWriter(final byte[] result) {
		this.result = result;
	}

	public static final byte OBJECT_START = '{';
	public static final byte OBJECT_END = '}';
	public static final byte ARRAY_START = '[';
	public static final byte ARRAY_END = ']';
	public static final byte COMMA = ',';
	public static final byte SEMI = ':';
	public static final byte QUOTE = '"';
	public static final byte ESCAPE = '\\';

	public final void writeNull() {
		final int s = position;
		position += 4;
		if (position >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2);
		}
		result[s] = 'n';
		result[s + 1] = 'u';
		result[s + 2] = 'l';
		result[s + 3] = 'l';
	}

	public final void writeByte(final byte c) {
		if (position == result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2);
		}
		result[position++] = c;
	}

	public final void writeString(final String str) {
		final int len = str.length();
		if (position + (len << 2) + (len << 1) + 2 >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + (len << 2) + (len << 1) + 2);
		}
		result[position] = QUOTE;
		int cur = position + 1;
		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (c == '"') {
				result[cur++] = ESCAPE;
				result[cur++] = QUOTE;
			} else if (c == '\\') {
				result[cur++] = ESCAPE;
				result[cur++] = ESCAPE;
			} else if (c < 32) {
				if (c == 8) {
					result[cur++] = ESCAPE;
					result[cur++] = 'b';
				} else if (c == 9) {
					result[cur++] = ESCAPE;
					result[cur++] = 't';
				} else if (c == 10) {
					result[cur++] = ESCAPE;
					result[cur++] = 'n';
				} else if (c == 12) {
					result[cur++] = ESCAPE;
					result[cur++] = 'f';
				} else if (c == 13) {
					result[cur++] = ESCAPE;
					result[cur++] = 'r';
				} else {
					result[cur] = ESCAPE;
					result[cur + 1] = 'u';
					result[cur + 2] = '0';
					result[cur + 3] = '0';
					switch (c) {
						case 0:
							result[cur + 4] = '0';
							result[cur + 5] = '0';
							break;
						case 1:
							result[cur + 4] = '0';
							result[cur + 5] = '1';
							break;
						case 2:
							result[cur + 4] = '0';
							result[cur + 5] = '2';
							break;
						case 3:
							result[cur + 4] = '0';
							result[cur + 5] = '3';
							break;
						case 4:
							result[cur + 4] = '0';
							result[cur + 5] = '4';
							break;
						case 5:
							result[cur + 4] = '0';
							result[cur + 5] = '5';
							break;
						case 6:
							result[cur + 4] = '0';
							result[cur + 5] = '6';
							break;
						case 7:
							result[cur + 4] = '0';
							result[cur + 5] = '7';
							break;
						case 11:
							result[cur + 4] = '0';
							result[cur + 5] = 'B';
							break;
						case 14:
							result[cur + 4] = '0';
							result[cur + 5] = 'E';
							break;
						case 15:
							result[cur + 4] = '0';
							result[cur + 5] = 'F';
							break;
						case 16:
							result[cur + 4] = '1';
							result[cur + 5] = '0';
							break;
						case 17:
							result[cur + 4] = '1';
							result[cur + 5] = '1';
							break;
						case 18:
							result[cur + 4] = '1';
							result[cur + 5] = '2';
							break;
						case 19:
							result[cur + 4] = '1';
							result[cur + 5] = '3';
							break;
						case 20:
							result[cur + 4] = '1';
							result[cur + 5] = '4';
							break;
						case 21:
							result[cur + 4] = '1';
							result[cur + 5] = '5';
							break;
						case 22:
							result[cur + 4] = '1';
							result[cur + 5] = '6';
							break;
						case 23:
							result[cur + 4] = '1';
							result[cur + 5] = '7';
							break;
						case 24:
							result[cur + 4] = '1';
							result[cur + 5] = '8';
							break;
						case 25:
							result[cur + 4] = '1';
							result[cur + 5] = '9';
							break;
						case 26:
							result[cur + 4] = '1';
							result[cur + 5] = 'A';
							break;
						case 27:
							result[cur + 4] = '1';
							result[cur + 5] = 'B';
							break;
						case 28:
							result[cur + 4] = '1';
							result[cur + 5] = 'C';
							break;
						case 29:
							result[cur + 4] = '1';
							result[cur + 5] = 'D';
							break;
						case 30:
							result[cur + 4] = '1';
							result[cur + 5] = 'E';
							break;
						default:
							result[cur + 4] = '1';
							result[cur + 5] = 'F';
							break;
					}
					cur += 6;
				}
			} else if (c < 0x007F) {
				result[cur++] = (byte) c;
			} else {
				final int cp = str.codePointAt(i);
				if (Character.isSupplementaryCodePoint(cp)) {
					i++;
				}
				if (cp == 0x007F) {
					result[cur++] = (byte) cp;
				} else if (cp <= 0x7FF) {
					result[cur++] = (byte) (0xC0 | ((cp >> 6) & 0x1F));
					result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFD)) {
					result[cur++] = (byte) (0xE0 | ((cp >> 12) & 0x0F));
					result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if (cp >= 0x10000 && cp <= 0x10FFFF) {
					result[cur++] = (byte) (0xF0 | ((cp >> 18) & 0x07));
					result[cur++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
					result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else {
					throw new RuntimeException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
				}
			}
		}
		result[cur] = QUOTE;
		position = cur + 1;
	}

	public final void writeBuffer(final int off, final int end) {
		if (position + 64 >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2);
		}
		int p = position;
		for (int i = off; i < end; i++) {
			result[p++] = (byte) tmp[i];
		}
		position = p;
	}

	public final void writeBuffer(final int len) {
		if (position + 64 >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2);
		}
		final int p = position;
		for (int i = 0; i < len; i++) {
			result[p + i] = (byte) tmp[i];
		}
		position += len;
	}

	public final void writeAscii(final String str) {
		final int len = str.length();
		if (position + len >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + len);
		}
		final int p = position;
		for (int i = 0; i < str.length(); i++) {
			result[p + i] = (byte) str.charAt(i);
		}
		position += len;
	}

	public final void writeBinary(final byte[] buf) {
		if (position + (buf.length << 1) + 2 >= result.length) {
			result = Arrays.copyOf(result, result.length + result.length / 2 + (buf.length << 1) + 2);
		}
		result[position++] = '"';
		position += Base64.encodeToBytes(buf, result, position);
		result[position++] = '"';
	}

	@Override
	public String toString() {
		return new String(result, 0, position, utf8);
	}

	public static class Bytes {
		public final byte[] content;
		public final int length;

		public Bytes(final byte[] content, final int length) {
			this.content = content;
			this.length = length;
		}
	}

	public final Bytes toBytes() {
		return new Bytes(result, position);
	}

	public final byte[] toByteArray() {
		return Arrays.copyOf(result, position);
	}

	public final void reset() {
		position = 0;
	}

	@Override
	public void write(int c) throws IOException {
		tmp[0] = (char) c;
		writeString(new String(tmp, 0, 1));
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		writeString(new String(cbuf, off, len));
	}

	@Override
	public void write(String str, int off, int len) {
		writeString(str.substring(off, off + len));
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		position = 0;
	}
}
