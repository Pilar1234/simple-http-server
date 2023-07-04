package com.pilar;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public class FileSystemPointer implements FilePointer {
  private final File targetFile;
  private final HashCode tag;

  public FileSystemPointer(File targetFile) {
    try {
      this.targetFile = targetFile;
      this.tag = Files.asByteSource(targetFile).hash(Hashing.sha512());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public InputStream open() {
    try {
      return new BufferedInputStream(new FileInputStream(targetFile));
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public boolean matchesEtag(String requestETag) {
    return getEtag().equals(requestETag);
  }

  @Override
  public String getEtag() {
    return "\"" + tag + "\"";
  }

  @Override
  public boolean modifiedAfter(Instant clientTime) {
    return !clientTime.isBefore(getLastModified());
  }

  @Override
  public Instant getLastModified() {
    return Instant.ofEpochMilli(targetFile.lastModified());
  }
}
