// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class defines a byte buffer that behaves as OutputStream. In other words it's an alternative
 * for the java ByteArrayOutputStream. In addition to that it defines some utility methods that
 * otherwise are not available in the ByteArrayOutputStream.
 */
public final class ByteBuffer extends OutputStream {
  /** The ThreadLocal that contains this Thread's ByteBuffer */
  private static ThreadLocal factory = new ByteBufferInitializer();

  /**
   * Returns a thread local instance of ByteBuffer. One needs be careful while using the returned
   * thread local instance. It may be possible, while you are using this thread local byte buffer
   * instance, in between you invoke some other function that again in turn gets this thread local
   * instance by invoking {@link #newInstance()} or {@link #newInstance(int)} method and thus
   * results in resetting this buffer and you lost your buffered bytes.
   *
   * @return a ThreadLocal ByteBuffer, reset to position 0
   */
  public static ByteBuffer newInstance() {
    ByteBuffer buffer = (ByteBuffer) factory.get();
    // Check if this buffer is already being used by this thread.
    // If so, then create a new one to avoid the conflict.
    if (!buffer.isAvailable()) {
      buffer = new ByteBuffer();
    }
    buffer.reset();
    // Mark it as not available until getByteString() or toByteString() is
    // invoked.
    buffer.setAvailable(false);
    return buffer;
  }

  /**
   * Returns a thread local instance of ByteBuffer. One needs be careful while using the returned
   * thread local instance. It may be possible, while you are using this thread local byte buffer
   * instance, in between you invoke some other function that again in turn gets this thread local
   * instance by invoking {@link #newInstance()} or {@link #newInstance(int)} method and thus
   * results in resetting this buffer and you lost your buffered bytes. This method ensures that
   * this buffer has the specified <code>capacity</code> already.
   *
   * @param capacity the required capacity that will be ensured in the returned thread local byte
   *     buffer instance.
   * @return a ThreadLocal ByteBuffer, reset to position 0, with at least a capacity of <code>
   *     capacity</code>.
   */
  public static ByteBuffer newInstance(int capacity) {
    ByteBuffer buffer = (ByteBuffer) factory.get();
    // Check if this buffer is already being used by this thread.
    // If so, then create a new one to avoid the conflict.
    if (!buffer.isAvailable()) {
      buffer = new ByteBuffer(capacity);
    } else if (buffer.capacity() < capacity) {
      buffer.setCapacity(capacity);
    }
    buffer.reset();

    // Mark it as not available until getByteString() or toByteString() is
    // invoked.
    buffer.setAvailable(false);
    return buffer;
  }

  /** The buffer where data is stored. */
  private byte buf[];

  /** The number of valid bytes in the buffer. */
  private int count;

  /** The size with which this buffer will get incremented when gets filled. */
  private int increment;

  /** Tells whether this buffer is available for reuse. */
  private boolean m_bAvailable = true;

  /**
   * Creates a new byte buffer. The buffer capacity is initially 32 bytes, though its size increases
   * if necessary.
   */
  public ByteBuffer() {
    this(32);
  }

  /**
   * Creates a new byte buffer, with a buffer capacity of the specified size, in bytes. The buffer
   * incrementer size will be set to (size/4 + 1).
   *
   * @param size the initial size.
   * @throws IllegalArgumentException if size is negative.
   */
  public ByteBuffer(int size) {
    this(size, size / 4 + 1);
  }

  /**
   * Creates a new byte buffer, with a buffer capacity of the specified size (in bytes) and having
   * the specified buffer size incrementer.
   *
   * @param size the initial size.
   * @param increment the size this buffer will get incremented when gets filled.
   * @throws IllegalArgumentException if size is negative.
   */
  public ByteBuffer(int size, int increment) {
    if (size < 0 || increment < 0) {
      throw new IllegalArgumentException("Negative initial size or increment:");
    }
    buf = new byte[size];
    this.increment = increment;
  }

  /**
   * Returns the current capacity of this buffer.
   *
   * @return the current capacity of this buffer.
   */
  public int capacity() {
    return buf.length;
  }

  /**
   * Sets the capacity of this buffer to the specified <code>size</code>. This will also reset the
   * buffer incrementer to (size/4 + 1).
   *
   * @param size the new size or capacity of this buffer.
   */
  private void setCapacity(int size) {
    buf = new byte[size];
    increment = size / 4 + 1;
    count = 0;
  }

  /**
   * Writes the specified byte to this byte buffer.
   *
   * @param b the byte to be written.
   */
  public void write(int b) {
    int newcount = count + 1;
    if (newcount > buf.length) {
      byte newbuf[] = new byte[buf.length + increment];
      System.arraycopy(buf, 0, newbuf, 0, count);
      buf = newbuf;
    }
    buf[count] = (byte) b;
    count = newcount;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code>
   * to this byte buffer.
   *
   * @param b the data.
   */
  public void write(byte b[]) {
    write(b, 0, b.length);
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code>
   * to this byte buffer.
   *
   * @param b the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   */
  public void write(byte b[], int off, int len) {
    if ((off < 0)
        || (off > b.length)
        || (len < 0)
        || ((off + len) > b.length)
        || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException(
          "Actual Length = "
              + b.length
              + ", Specified offset = "
              + off
              + ", Specified length = "
              + len);
    } else if (len == 0) {
      return;
    }
    int newcount = count + len;
    if (newcount > buf.length) {
      byte newbuf[] = new byte[Math.max(buf.length + increment, newcount)];
      System.arraycopy(buf, 0, newbuf, 0, count);
      buf = newbuf;
    }
    System.arraycopy(b, off, buf, count, len);
    count = newcount;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code>
   * to this byte buffer.
   *
   * @param bytes the data to be written on this buffer.
   */
  public void write(DsByteString bytes) {
    if (bytes != null) {
      write(bytes.data(), (int) bytes.offset(), bytes.length());
    }
  }

  /**
   * Writes the complete contents of this byte buffer to the specified output stream argument, as if
   * by calling the output stream's write method using <code>out.write(buf, 0, count)</code>.
   *
   * @param out the output stream to which to write the data.
   * @throws IOException if an I/O error occurs.
   */
  public void writeTo(OutputStream out) throws IOException {
    out.write(buf, 0, count);
  }

  /**
   * Resets the <code>count</code> field of this byte array output stream to zero, so that all
   * currently accumulated output in the output stream is discarded. The output stream can be used
   * again, reusing the already allocated buffer space.
   */
  public void reset() {
    count = 0;
  }

  /**
   * Returns a newly constructed byte array of the size equal to the number of valid bytes available
   * in this byte buffer with the valid contents of this byte buffer copied to this newly
   * constructed byte array.
   *
   * @return the current contents of this output stream, as a byte array.
   */
  public byte[] toByteArray() {
    byte newbuf[] = new byte[count];
    System.arraycopy(buf, 0, newbuf, 0, count);

    // Mark it as available.
    m_bAvailable = true;

    return newbuf;
  }

  /**
   * Creates a newly allocated byte array. Its size is the current size of this byte buffer plus the
   * size of the specified byte array that needs to be appended. The valid contents of the buffer
   * have been copied and the specified byte array <code>bytes</code> have been appended.
   *
   * @param bytes the bytes that need to be appended at the end.
   * @return the current contents of this buffer plus the specified byte array <code>bytes</code>
   *     been appended, as a byte array.
   */
  public byte[] toByteArray(byte[] bytes) {
    int len = (bytes == null) ? 0 : bytes.length;
    byte newbuf[] = new byte[(len + count)];
    System.arraycopy(buf, 0, newbuf, 0, count);
    if (bytes != null) {
      System.arraycopy(bytes, 0, newbuf, count, len);
    }
    // Mark it as available.
    m_bAvailable = true;
    return newbuf;
  }

  /**
   * Returns the current size of the buffer.
   *
   * @return the value of the <code>count</code> field, which is the number of valid bytes in this
   *     output stream.
   */
  public int size() {
    return count;
  }

  /**
   * Returns the reference to the underlying byte array.
   *
   * @return the reference to the underlying byte array.
   */
  public byte[] data() {
    return buf;
  }

  /**
   * Returns the index of the specified <code>ch</code> character in this byte buffer from the
   * start.
   *
   * @param ch the char to find the index of.
   * @return the index of the specified <code>ch</code> character in this byte buffer from the
   *     start.
   */
  public int indexOf(char ch) {
    return indexOf((byte) ch);
  }

  /**
   * Returns the index of the specified <code>b</code> byte in this byte buffer from the start.
   *
   * @param b the byte to find the index of
   * @return the index of the specified <code>ch</code> byte in this byte buffer from the start.
   */
  public int indexOf(byte b) {
    for (int i = 0; i < count; i++) {
      if (buf[i] == b) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Copies the buffer's contents starting from the specified <code>off</code> offset up to the
   * specified number <code>count</code> of bytes into a new DsByteString and returns that
   * DsByteString.
   *
   * @param off the starting index in this byte buffer where from the bytes needs to be copied to
   *     the returned DsByteString
   * @param count the number of bytes that need to be copied to the returned DsByteString
   * @return the DsByteString starting at off, with count bytes in it
   */
  public DsByteString getByteString(int off, int count) {
    byte newbuf[] = new byte[count];
    System.arraycopy(buf, off, newbuf, 0, count);
    // Mark it as available.
    m_bAvailable = true;
    return new DsByteString(newbuf);
  }

  /**
   * Converts the buffer's contents into a DsByteString. It copies the bytes to the returned
   * DsByteString.
   *
   * @return the DsByteString that this buffer contains
   */
  public DsByteString getByteString() {
    // Mark it as available.
    m_bAvailable = true;
    return new DsByteString(toByteArray());
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into characters according to
   * the platform's default character encoding.
   *
   * @return a String translated from the buffer's contents.
   */
  public String toString() {
    // Mark it as available.
    m_bAvailable = true;
    return DsByteString.newString(buf, 0, count);
  }

  /**
   * Converts the buffer's contents into a DsByteString. The returned DsByteString will still be
   * pointing to the underlying byte array of this buffer. In other words, the underlying byte
   * buffer will be shared between the returned DsByteString and this byte buffer.
   *
   * @return a DsByteString translated from the buffer's contents.
   */
  public DsByteString toByteString() {
    // Mark it as available.
    m_bAvailable = true;
    return new DsByteString(buf, 0, count);
  }

  /**
   * Converts the buffer's contents starting from the specified <code>offset</code> offset up to the
   * specified number <code>count</code> of bytes into a new DsByteString and returns that
   * DsByteString. The returned DsByteString will still be pointing to the underlying byte array of
   * this buffer. In other words, the underlying byte buffer will be shared between the returned
   * DsByteString and this byte buffer.
   *
   * @param offset the starting index in this byte buffer.
   * @param count the number of bytes.
   * @return a DsByteString translated from offset for count bytes.
   */
  public DsByteString toByteString(int offset, int count) {
    // Mark it as available.
    m_bAvailable = true;
    return new DsByteString(buf, offset, count);
  }

  /**
   * Sets this buffer to be available or unavailable to reuse.
   *
   * @param available if <code>true</code>, then this buffer will be available for reuse.
   */
  private void setAvailable(boolean available) {
    m_bAvailable = available;
  }

  /**
   * Tells whether this buffer is available for reuse.
   *
   * @return <code>true</code> if this buffer is available for reuse, <code>false</code> otherwise.
   */
  private boolean isAvailable() {
    return m_bAvailable;
  }
}

/** ThreadLocal Byte Buffer Initializer. It provides for a thread local Byte Buffer instance. */
class ByteBufferInitializer extends ThreadLocal {
  protected Object initialValue() {
    return new ByteBuffer();
  }
}
