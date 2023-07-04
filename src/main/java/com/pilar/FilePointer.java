package com.pilar;

import java.io.InputStream;
import java.time.Instant;

public interface FilePointer {

  InputStream open();

  boolean matchesEtag(String requestETag);

  String getEtag();

  boolean modifiedAfter(Instant isModifiedSince);

  Instant getLastModified();

  long getSize();
}
