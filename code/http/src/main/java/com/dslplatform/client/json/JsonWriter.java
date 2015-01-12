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
		final int start = this.position;
		this.position += 4;
		if (this.position >= this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2);
		}
		final byte[] _result = this.result;
		_result[start] = 'n';
		_result[start + 1] = 'u';
		_result[start + 2] = 'l';
		_result[start + 3] = 'l';
	}

	public final void writeByte(final byte c) {
		if (this.position == this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2);
		}
		this.result[this.position++] = c;
	}

	public final void writeString(final String str) {
		final int stringLen = str.length();
		final int _position = this.position;
		if (_position + (stringLen << 2) + (stringLen << 1) + 2 >= this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2 + (stringLen << 2) + (stringLen << 1) + 2);
		}
		final byte[] _result = this.result;
		_result[_position] = QUOTE;
		int cur = _position + 1;
		for (int i = 0; i < stringLen; i++) {
			final char c = str.charAt(i);
			if (c == QUOTE) {
					_result[cur++] = ESCAPE;
					_result[cur++] = QUOTE;
				} else if (c == ESCAPE) {
					_result[cur++] = ESCAPE;
					_result[cur++] = ESCAPE;
				} else if (c < 32) {
				if (c == '\b') {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'b';
				} else if (c == '\t') {
					_result[cur++] = ESCAPE;
					_result[cur++] = 't';
				} else if (c == '\n') {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'n';
				} else if (c == '\f') {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'f';
				} else if (c == '\r') {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'r';
				} else {
					_result[cur] = ESCAPE;
					_result[cur + 1] = 'u';
					_result[cur + 2] = '0';
					_result[cur + 3] = '0';
					switch (c) {
						case 0:
							_result[cur + 4] = '0';
							_result[cur + 5] = '0';
							break;
						case 1:
							_result[cur + 4] = '0';
							_result[cur + 5] = '1';
							break;
						case 2:
							_result[cur + 4] = '0';
							_result[cur + 5] = '2';
							break;
						case 3:
							_result[cur + 4] = '0';
							_result[cur + 5] = '3';
							break;
						case 4:
							_result[cur + 4] = '0';
							_result[cur + 5] = '4';
							break;
						case 5:
							_result[cur + 4] = '0';
							_result[cur + 5] = '5';
							break;
						case 6:
							_result[cur + 4] = '0';
							_result[cur + 5] = '6';
							break;
						case 7:
							_result[cur + 4] = '0';
							_result[cur + 5] = '7';
							break;
						case 11:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'B';
							break;
						case 14:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'E';
							break;
						case 15:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'F';
							break;
						case 16:
							_result[cur + 4] = '1';
							_result[cur + 5] = '0';
							break;
						case 17:
							_result[cur + 4] = '1';
							_result[cur + 5] = '1';
							break;
						case 18:
							_result[cur + 4] = '1';
							_result[cur + 5] = '2';
							break;
						case 19:
							_result[cur + 4] = '1';
							_result[cur + 5] = '3';
							break;
						case 20:
							_result[cur + 4] = '1';
							_result[cur + 5] = '4';
							break;
						case 21:
							_result[cur + 4] = '1';
							_result[cur + 5] = '5';
							break;
						case 22:
							_result[cur + 4] = '1';
							_result[cur + 5] = '6';
							break;
						case 23:
							_result[cur + 4] = '1';
							_result[cur + 5] = '7';
							break;
						case 24:
							_result[cur + 4] = '1';
							_result[cur + 5] = '8';
							break;
						case 25:
							_result[cur + 4] = '1';
							_result[cur + 5] = '9';
							break;
						case 26:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'A';
							break;
						case 27:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'B';
							break;
						case 28:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'C';
							break;
						case 29:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'D';
							break;
						case 30:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'E';
							break;
						default:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'F';
							break;
					}
					cur += 6;
				}
			} else if (c < 0x007F) {
				_result[cur++] = (byte) c;
			} else {
				final int cp = str.codePointAt(i);
				if (Character.isSupplementaryCodePoint(cp)) {
					i++;
				}
				if (cp == 0x007F) {
					_result[cur++] = (byte) cp;
				} else if (cp <= 0x7FF) {
					_result[cur++] = (byte) (0xC0 | ((cp >> 6) & 0x1F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFD)) {
					_result[cur++] = (byte) (0xE0 | ((cp >> 12) & 0x0F));
					_result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if (cp >= 0x10000 && cp <= 0x10FFFF) {
					_result[cur++] = (byte) (0xF0 | ((cp >> 18) & 0x07));
					_result[cur++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
					_result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else {
					throw new RuntimeException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
				}
			}
		}
		_result[cur] = QUOTE;
		this.position = cur + 1;
	}

	public final void writeBuffer(final int off, final int end) {
		int _position = this.position;
		if (_position + 64 >= this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2);
		}
		final byte[] _result = this.result;
		for (int i = off; i < end; i++) {
			_result[_position++] = (byte) this.tmp[i];
		}

		this.position = _position;
	}

	public final void writeBuffer(final int len) {
		final int _position = this.position;

		if (_position + 64 >= this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2);
		}
		final byte[] _result = this.result;
		for (int i = 0; i < len; i++) {
			_result[_position + i] = (byte) this.tmp[i];
		}

		this.position += len;
	}

	public final void writeAscii(final String str) {
		final int stringLen = str.length();
		final int _position = this.position;

		if (_position + stringLen >= this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2 + stringLen);
		}
		final byte[] _result = this.result;
		for (int i = 0; i < str.length(); i++) {
			_result[_position + i] = (byte) str.charAt(i);
		}

		this.position += stringLen;
	}

	public final void writeBinary(final byte[] buf) {
		int _position = this.position;

		if (_position + (buf.length << 1) + 2 >= this.result.length) {
			this.result = Arrays.copyOf(this.result, this.result.length + this.result.length / 2 + (buf.length << 1) + 2);
		}
		final byte[] _result = this.result;
		_result[_position++] = '"';
		_position += Base64.encodeToBytes(buf, _result, _position);
		_result[_position++] = '"';

		this.position = _position;
	}

	@Override
	public String toString() {
		return new String(this.result, 0, this.position, utf8);
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
		return new Bytes(this.result, this.position);
	}

	public final byte[] toByteArray() {
		return Arrays.copyOf(this.result, this.position);
	}

	public final void reset() {
		this.position = 0;
	}

	@Override
	public void write(final int c) throws IOException {
		this.tmp[0] = (char) c;
		writeString(new String(this.tmp, 0, 1));
	}

	@Override
	public void write(final char[] cbuf, final int off, final int len) {
		writeString(new String(cbuf, off, len));
	}

	@Override
	public void write(final String str, final int off, final int len) {
		writeString(str.substring(off, off + len));
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		this.position = 0;
	}
}
