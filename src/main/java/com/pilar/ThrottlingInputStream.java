package com.pilar;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.io.InputStream;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ThrottlingInputStream extends InputStream {
  private final InputStream target;
  private final RateLimiter maxBytesPerSecond;

  @Override
  public int read() throws IOException {
    maxBytesPerSecond.acquire(1);
    return target.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    maxBytesPerSecond.acquire(b.length);
    return target.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    maxBytesPerSecond.acquire(len);
    return target.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return target.skip(n);
  }

  @Override
  public int available() throws IOException {
    return target.available();
  }

  @Override
  public synchronized void mark(int readlimit) {
    target.mark(readlimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    target.reset();
  }

  @Override
  public boolean markSupported() {
    return target.markSupported();
  }

  @Override
  public void close() throws IOException {
    target.close();
  }
}
