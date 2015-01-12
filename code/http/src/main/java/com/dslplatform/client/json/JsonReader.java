package com.dslplatform.client.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.dslplatform.patterns.ServiceLocator;

public final class JsonReader {
	private final byte[] buffer;
	private final int length;
	private final ServiceLocator locator;
	private static final int TMP_SIZE = 48;
	private final char[] tmp = new char[TMP_SIZE];

	private int tokenStart;
	private int currentIndex = 0;
	private byte last = ' ';

	public JsonReader(final byte[] buffer, final ServiceLocator locator) {
		this.buffer = buffer;
		this.length = buffer.length;
		this.locator = locator;
	}

	public JsonReader(final byte[] buffer, final int length, final ServiceLocator locator) throws IOException {
		this.buffer = buffer;
		this.length = length;
		this.locator = locator;
		if (length > buffer.length) {
			throw new IOException("length can't be longer than buffer.length");
		}
	}

	public final byte read() throws IOException {
		if (this.currentIndex >= this.length) {
			throw new IOException("end of stream");
		}
		return this.last = this.buffer[this.currentIndex++];
	}

	public final byte last() {
		return this.last;
	}

	public final String readShortValue() throws IOException {
		int _currentIndex = this.currentIndex;
		final int _length = this.length;
		char _lastChar = (char) this.last;
		final byte[] _buffer = this.buffer;
		final char[] _tmp = this.tmp;

		_tmp[0] = _lastChar;
		int i=1;
		for (; _lastChar != ',' && _lastChar != '}' && _lastChar != ']' && _lastChar != '"' && i < TMP_SIZE && _currentIndex < _length;i++, _currentIndex++) {
			_tmp[i] = _lastChar = (char) _buffer[_currentIndex];
		}

		this.last = (byte) _lastChar;
		this.currentIndex = _currentIndex;
		return new String(this.tmp, 0, i - 1);
	}

	public final int getTokenStart() {
		return this.tokenStart;
	}

	public final int getCurrentIndex() {
		return this.currentIndex;
	}

	public final char[] readNumber() {
		final char[] _tmp = this.tmp;
		int _currentIndex=this.currentIndex;
		final int _length = this.length;
		final byte[] _buffer = this.buffer;
		char _lastChar = (char) this.last;

		this.tokenStart = _currentIndex - 1;
		_tmp[0] = _lastChar;
		for (int i = 1; _lastChar != ',' && _lastChar != '}' && _lastChar != ']' && _lastChar != '"' && i < TMP_SIZE && _currentIndex < _length; i++, _currentIndex++) {
			_tmp[i] = _lastChar = (char) _buffer[_currentIndex];
		}

		this.last = (byte) _lastChar;
		this.currentIndex = _currentIndex;

		return this.tmp;
	}

	public final String readSimpleString() throws IOException {
		if (this.last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) this.last);
		final byte[] _buffer = this.buffer;
		final char[] _tmp = this.tmp;
		final int _length = this.length;
		final int start = this.currentIndex;
		int i = start;
		for (; i < _length && _buffer[i] != '"'; i++) {
			_tmp[i - start] = (char) _buffer[i];
		}
		this.currentIndex = i + 1;
		this.last = '"';

		return new String(this.tmp, 0, i - start);
	}

	public final char[] readSimpleQuote() throws IOException {
		if (this.last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) this.last);
		final byte[] _buffer = this.buffer;
		final char[] _tmp = this.tmp;
		final int _length = this.length;
		final int start = this.tokenStart = this.currentIndex;
		int i = this.currentIndex;
		for (; i < _length && _buffer[i] != '"'; i++) {
			_tmp[i - start] = (char) _buffer[i];
		}
		this.currentIndex = i + 1;
		this.last = '"';
		return this.tmp;
	}

	public final String readString() throws IOException {

		final int startIndex = this.currentIndex;
		// At this point, buffer cannot be empty or null, it is safe to read first character
		if (this.last != '"') {
			throw new IOException("JSON string must start with a double quote! Instead found: " + byteDetails(this.buffer[this.currentIndex - 1]));
		}

		byte bb = 0;
		{
			final byte[] _buffer = this.buffer;
			final char[] _tmp = this.tmp;
			final int _length = this.length;
			int _currentIndex = this.currentIndex;
			for (int pos = 0; pos < TMP_SIZE; pos++) {
				if (_currentIndex >= _length) {
					this.currentIndex = _currentIndex;
					throw new IOException("JSON string was not closed with a double quote!");
				}
				bb = _buffer[_currentIndex++];
				if (bb == '"') {
					this.last = '"';
					this.currentIndex = _currentIndex;
					return new String(_tmp, 0, pos);
				}
				// If we encounter a backslash, which is a beginning of an escape sequence
				// or a high bit was set - indicating an UTF-8 encoded multibyte character,
				// there is no chance that we can decode the string without instantiating
				// a temporary buffer, so quit this loop
				if ((bb ^ '\\') < 1) break;
				_tmp[pos] = (char) bb;
			}
			this.currentIndex = _currentIndex;
		}

		// If the buffer contains an ASCII string (no high bit set) without any escape codes "\n", "\t", etc...,
		// there is no need to instantiate any temporary buffers, we just decode the original buffer directly
		// via ISO-8859-1 encoding since it is the fastest encoding which is guaranteed to retain all ASCII characters
		{
			final byte[] _buffer = this.buffer;
			final int _length = this.length;
			int _currentIndex = this.currentIndex;
			while (true) {
				if (_currentIndex >= _length) {
					this.currentIndex = _currentIndex;
					throw new IOException("JSON string was not closed with a double quote!");
				}
				// If we encounter a backslash, which is a beginning of an escape sequence
				// or a high bit was set - indicating an UTF-8 encoded multibyte character,
				// there is no chance that we can decode the string without instantiating
				// a temporary buffer, so quit this loop
				if ((bb ^ '\\') < 1) break;
				bb = _buffer[_currentIndex++];
				if (bb == '"') {
					this.last = '"';
					this.currentIndex = _currentIndex;
					return new String(_buffer, startIndex, _currentIndex - startIndex - 1, "ISO-8859-1");
				}
			}
		}

		// temporary buffer, will resize if need be
		int soFar = --this.currentIndex - startIndex;
		char[] chars = new char[soFar + 256];

		// copy all the ASCII characters so far
		{
			final byte[] _buffer = this.buffer;
			for (int i = soFar - 1; i >= 0; i--) {
				chars[i] = (char) _buffer[startIndex + i];
			}
		}

		{
			final byte[] _buffer = this.buffer;
			final int _length = this.length;
			int _currentIndex = this.currentIndex;
			while (_currentIndex < _length) {
				int bc = _buffer[_currentIndex++];
				if (bc == '"') {
					this.last = '"';
					this.currentIndex = _currentIndex;
					return new String(chars, 0, soFar);
				}

				// if we're running out of space, double the buffer capacity
				if (soFar >= chars.length - 3) {
					final char[] newChars = new char[chars.length << 1];
					System.arraycopy(chars, 0, newChars, 0, soFar);
					chars = newChars;
				}

				if (bc == '\\') {
					bc = _buffer[_currentIndex++];

					switch (bc) {
						case 'b':
							bc = '\b';
							break;
						case 't':
							bc = '\t';
							break;
						case 'n':
							bc = '\n';
							break;
						case 'f':
							bc = '\f';
							break;
						case 'r':
							bc = '\r';
							break;
						case '"':
						case '/':
						case '\\':
							break;
						case 'u':
							bc =
									(hexToInt(_buffer[_currentIndex++]) << 12) +
											(hexToInt(_buffer[_currentIndex++]) << 8) +
											(hexToInt(_buffer[_currentIndex++]) << 4) +
											hexToInt(_buffer[_currentIndex++]);
							break;

						default:
							this.currentIndex = _currentIndex;
							throw new IOException("Could not parse String, got invalid escape combination '\\" + bc + "'");
					}
				} else if ((bc & 0x80) != 0) {
					final int u2 = _buffer[_currentIndex++];
					if ((bc & 0xE0) == 0xC0) {
						bc = ((bc & 0x1F) << 6) + (u2 & 0x3F);
					} else {
						final int u3 = _buffer[_currentIndex++];
						if ((bc & 0xF0) == 0xE0) {
							bc = ((bc & 0x0F) << 12) + ((u2 & 0x3F) << 6) + (u3 & 0x3F);
						} else {
							final int u4 = _buffer[_currentIndex++];
							if ((bc & 0xF8) == 0xF0) {
								bc = ((bc & 0x07) << 18) + ((u2 & 0x3F) << 12) + ((u3 & 0x3F) << 6) + (u4 & 0x3F);
							} else {
								this.currentIndex = _currentIndex;
								// there are legal 5 & 6 byte combinations, but none are _valid_
								throw new IOException();
							}

							if (bc >= 0x10000) {
								// check if valid unicode
								if (bc >= 0x110000) {
									this.currentIndex = _currentIndex;
									throw new IOException();
								}

								// split surrogates
								final int sup = bc - 0x10000;
								chars[soFar++] = (char) ((sup >>> 10) + 0xd800);
								chars[soFar++] = (char) ((sup & 0x3ff) + 0xdc00);
							}
						}
					}
				}
				chars[soFar++] = (char) bc;
			}
			this.currentIndex = _currentIndex;
		}
		throw new IOException("JSON string was not closed with a double quote!");
	}

	private static int hexToInt(final byte value) throws IOException {
		if (value >= '0' && value <= '9') return value - 0x30;
		if (value >= 'A' && value <= 'F') return value - 0x37;
		if (value >= 'a' && value <= 'f') return value - 0x57;
		throw new IOException("Could not parse unicode escape, expected a hexadecimal digit, got '" + value + "'");
	}

	private String byteDetails(final byte c) {
		return "'" + ((char) c) + "'" + "(" + c + ")";
	}

	private boolean wasWhiteSpace() {
		if (this.last == '"' || this.last == ',') {
			return false;
		}

		final byte[] _buffer = this.buffer;
		final int _length = this.length;
		int _currentIndex = this.currentIndex;
		switch (this.last) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 32:
			case -96:
				this.currentIndex = _currentIndex;
				return true;
			case -31:
				if (_currentIndex + 1 < _length && _buffer[_currentIndex] == -102 && _buffer[_currentIndex + 1] == -128) {
					_currentIndex += 2;
					this.last = ' ';
					this.currentIndex = _currentIndex;
					return true;
				}
				this.currentIndex = _currentIndex;
				return false;
			case -30:
				if (_currentIndex + 1 < _length) {
					final byte b1 = _buffer[_currentIndex];
					final byte b2 = _buffer[_currentIndex + 1];
					if (b1 == -127 && b2 == -97) {
						_currentIndex += 2;
						this.last = ' ';
						this.currentIndex = _currentIndex;
						return true;
					}
					if (b1 != -128) {
						this.currentIndex = _currentIndex;
						return false;
					}
					switch (b2) {
						case -128:
						case -127:
						case -126:
						case -125:
						case -124:
						case -123:
						case -122:
						case -121:
						case -120:
						case -119:
						case -118:
						case -88:
						case -87:
						case -81:
							_currentIndex += 2;
							this.last = ' ';
							this.currentIndex = _currentIndex;
							return true;
						default:
							this.currentIndex = _currentIndex;
							return false;
					}
				} else {
					this.currentIndex = _currentIndex;
					return false;
				}
			case -29:
				if (_currentIndex + 1 < _length && _buffer[_currentIndex] == -128 && _buffer[_currentIndex + 1] == -128) {
					_currentIndex += 2;
					this.last = ' ';
					this.currentIndex = _currentIndex;
					return true;
				}
				this.currentIndex = _currentIndex;
				return false;
			default:
				this.currentIndex = _currentIndex;
				return false;
		}
	}

	public final byte getNextToken() throws IOException {
		read();
		while (wasWhiteSpace())
			read();
		return this.last;
	}

	public final byte moveToNextToken() throws IOException {
		while (wasWhiteSpace())
			read();
		return this.last;
	}

	public final long positionInStream() {
		return this.currentIndex;
	}

	public final int fillName() throws IOException {
		if (this.last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) this.last);
		this.tokenStart = this.currentIndex;
		byte c = read();
		long hash = 0x811c9dc5;
		for (; c != '"'; c = read()) {
			hash ^= 0xFF & c;
			hash *= 0x1000193;
		}
		if (read() != ':')
			throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) this.last);
		return (int) hash;
	}

	public final int calcHash() throws IOException {
		if (this.last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) this.last);
		this.tokenStart = this.currentIndex;
		byte c = read();
		long hash = 0x811c9dc5;
		do {
			hash ^= 0xFF & c;
			hash *= 0x1000193;
		} while ((c = read()) != '"');
		return (int) hash;
	}

	public final boolean wasLastName(final String name) {
		if (name.length() != this.currentIndex - this.tokenStart) {
			return false;
		}
		final byte[] _buffer = this.buffer;
		final int _tokenStart = this.tokenStart;
		final int nameLength = name.length();
		for (int i = 0; i < nameLength; i++) {
			if (name.charAt(i) != _buffer[_tokenStart + i]) {
				return false;
			}
		}
		return true;
	}

	public final String getLastName() throws IOException {
		return new String(this.buffer, this.tokenStart, this.currentIndex - this.tokenStart - 1, "ISO-8859-1");
	}

	private byte skipString() throws IOException {
		byte c = read();
		byte prev = c;
		while (c != '"' || prev == '\\') {
			prev = c;
			c = read();
		}
		return getNextToken();
	}

	public final byte skip() throws IOException {
		if (this.last == '"') return skipString();
		else if (this.last == '{') {
			byte nextToken = getNextToken();
			if (nextToken == '}') return getNextToken();
			if (nextToken == '"') nextToken = skipString();
			else
				throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
			if (nextToken != ':')
				throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
			getNextToken();
			nextToken = skip();
			while (nextToken == ',') {
				nextToken = getNextToken();
				if (nextToken == '"') nextToken = skipString();
				else
					throw new IOException("Expecting '\"' at position " + positionInStream() + ". Found " + (char) nextToken);
				if (nextToken != ':')
					throw new IOException("Expecting ':' at position " + positionInStream() + ". Found " + (char) nextToken);
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != '}')
				throw new IOException("Expecting '}' at position " + positionInStream() + ". Found " + (char) nextToken);
			return getNextToken();
		} else if (this.last == '[') {
			getNextToken();
			byte nextToken = skip();
			while (nextToken == ',') {
				getNextToken();
				nextToken = skip();
			}
			if (nextToken != ']')
				throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) nextToken);
			return getNextToken();
		} else {
			while (this.last != ',' && this.last != '}' && this.last != ']')
				read();
			return this.last;
		}
	}

	public final String readNext() throws IOException {
		final int start = this.currentIndex - 1;
		skip();
		return new String(this.buffer, start, this.currentIndex - start - 1, "UTF-8");
	}

	public final byte[] readBase64() throws IOException {
		if (this.last != '"')
			throw new IOException("Expecting '\"' at position " + positionInStream() + " at base64 start. Found " + (char) this.last);
		final int start = this.currentIndex;
		this.currentIndex = Base64.findEnd(this.buffer, start);
		this.last = this.buffer[this.currentIndex++];
		if (this.last != '"') {
			throw new IOException("Expecting '\"' at position " + positionInStream() + " at base64 end. Found " + (char) this.last);
		}
		return Base64.decodeFast(this.buffer, start, this.currentIndex - 1);
	}

	public static interface ReadObject<T> {
		T read(final JsonReader reader) throws IOException;
	}

	public static interface ReadJsonObject<T extends JsonObject> {
		T deserialize(final JsonReader reader, final ServiceLocator locator) throws IOException;
	}

	public final boolean wasNull() throws IOException {
		if (this.last == 'n') {
			final byte[] _buffer = this.buffer;
			final int _currentIndex = this.currentIndex;
			if (_currentIndex + 2 < this.length
					&& _buffer[_currentIndex] == 'u'
					&& _buffer[_currentIndex + 1] == 'l'
					&& _buffer[_currentIndex + 2] == 'l') {
				this.currentIndex += 3;
				return true;
			}
			throw new IOException("Invalid null value found at: " + this.currentIndex);
		}
		return false;
	}

	public final boolean wasTrue() throws IOException {
		if (this.last == 't') {
			final byte[] _buffer = this.buffer;
			final int _currentIndex = this.currentIndex;
			if (_currentIndex + 2 < this.length
					&& _buffer[_currentIndex] == 'r'
					&& _buffer[_currentIndex + 1] == 'u'
					&& _buffer[_currentIndex + 2] == 'e') {
				this.currentIndex += 3;
				return true;
			}
			throw new IOException("Invalid boolean value found at: " + this.currentIndex);
		}
		return false;
	}

	public final boolean wasFalse() throws IOException {
		if (this.last == 'f') {
			final byte[] _buffer = this.buffer;
			final int _currentIndex = this.currentIndex;
			if (_currentIndex + 3 < this.length
					&& _buffer[_currentIndex] == 'a'
					&& _buffer[_currentIndex + 1] == 'l'
					&& _buffer[_currentIndex + 2] == 's'
					&& _buffer[_currentIndex + 3] == 'e') {
				this.currentIndex += 4;
				return true;
			}
			throw new IOException("Invalid boolean value found at: " + this.currentIndex);
		}
		return false;
	}

	public final <T> ArrayList<T> deserializeCollectionWithGet(final ReadObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>();
		deserializeCollectionWithGet(readObject, res);
		return res;
	}

	public final <T> ArrayList<T> deserializeCollectionWithMove(final ReadObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>();
		deserializeCollectionWithMove(readObject, res);
		return res;
	}

	public final <T> void deserializeCollectionWithGet(final ReadObject<T> readObject, final Collection<T> res) throws IOException {
		res.add(readObject.read(this));
		while (getNextToken() == ',') {
			getNextToken();
			res.add(readObject.read(this));
		}
		if (this.last != ']') {
			if (this.currentIndex >= this.length) throw new IOException("Unexpected end of json in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) this.last);
		}
	}

	public final <T> void deserializeCollectionWithMove(final ReadObject<T> readObject, final Collection<T> res) throws IOException {
		res.add(readObject.read(this));
		while (moveToNextToken() == ',') {
			getNextToken();
			res.add(readObject.read(this));
		}
		if (this.last != ']') {
			if (this.currentIndex >= this.length) throw new IOException("Unexpected end of json in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) this.last);
		}
	}

	public final <T> ArrayList<T> deserializeNullableCollectionWithGet(final ReadObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>();
		deserializeNullableCollectionWithGet(readObject, res);
		return res;
	}

	public final <T> ArrayList<T> deserializeNullableCollectionWithMove(final ReadObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>();
		deserializeNullableCollectionWithMove(readObject, res);
		return res;
	}

	public final <T> void deserializeNullableCollectionWithGet(final ReadObject<T> readObject, final Collection<T> res) throws IOException {
		if (wasNull()) {
			res.add(null);
		} else {
			res.add(readObject.read(this));
		}
		while (getNextToken() == ',') {
			getNextToken();
			if (wasNull()) {
				res.add(null);
			} else {
				res.add(readObject.read(this));
			}
		}
		if (this.last != ']') {
			if (this.currentIndex >= this.length) throw new IOException("Unexpected end of json in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) this.last);
		}
	}

	public final <T> void deserializeNullableCollectionWithMove(final ReadObject<T> readObject, final Collection<T> res) throws IOException {
		if (wasNull()) {
			res.add(null);
			getNextToken();
		} else {
			res.add(readObject.read(this));
			moveToNextToken();
		}
		while (this.last == ',') {
			getNextToken();
			if (wasNull()) {
				res.add(null);
				getNextToken();
			} else {
				res.add(readObject.read(this));
				moveToNextToken();
			}
		}
		if (this.last != ']') {
			if (this.currentIndex >= this.length) throw new IOException("Unexpected end of json in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) this.last);
		}
	}

	public final <T extends JsonObject> ArrayList<T> deserializeCollection(final ReadJsonObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>();
		deserializeCollection(readObject, res);
		return res;
	}

	public final <T extends JsonObject> void deserializeCollection(final ReadJsonObject<T> readObject, final Collection<T> res) throws IOException {
		if (this.last == '{') {
			res.add(readObject.deserialize(this, this.locator));
		} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) this.last);
		while (getNextToken() == ',') {
			if (getNextToken() == '{') {
				res.add(readObject.deserialize(this, this.locator));
			} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) this.last);
		}
		if (this.last != ']') {
			if (this.currentIndex >= this.length) throw new IOException("Unexpected end of json in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) this.last);
		}
	}

	public final <T extends JsonObject> ArrayList<T> deserializeNullableCollection(final ReadJsonObject<T> readObject) throws IOException {
		final ArrayList<T> res = new ArrayList<T>();
		deserializeNullableCollection(readObject, res);
		return res;
	}

	public final <T extends JsonObject> void deserializeNullableCollection(final ReadJsonObject<T> readObject, final Collection<T> res) throws IOException {
		if (this.last == '{') {
			res.add(readObject.deserialize(this, this.locator));
		} else if (wasNull()) {
			res.add(null);
		} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) this.last);
		while (getNextToken() == ',') {
			if (getNextToken() == '{') {
				res.add(readObject.deserialize(this, this.locator));
			} else if (wasNull()) {
				res.add(null);
			} else throw new IOException("Expecting '{' at position " + positionInStream() + ". Found " + (char) this.last);
		}
		if (this.last != ']') {
			if (this.currentIndex >= this.length) throw new IOException("Unexpected end of json in collection.");
			else throw new IOException("Expecting ']' at position " + positionInStream() + ". Found " + (char) this.last);
		}
	}
}
