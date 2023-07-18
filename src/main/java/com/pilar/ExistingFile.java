package com.pilar;

import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.OK;

import com.google.common.util.concurrent.RateLimiter;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@AllArgsConstructor
public class ExistingFile {
  private final HttpMethod method;
  private final FilePointer filePointer;

  public ResponseEntity<Resource> handle(Optional<String> requestEtagOpt, Optional<Instant> ifModifiedSinceOpt) {
    if (requestEtagOpt.isPresent()) {
      final String requestEtag = requestEtagOpt.get();
      if (filePointer.matchesEtag(requestEtag)) {
        return notModified(filePointer);
      }
    }
    if (ifModifiedSinceOpt.isPresent()) {
      final Instant isModifiedSince = ifModifiedSinceOpt.get();
      if (filePointer.modifiedAfter(isModifiedSince)) {
        return notModified(filePointer);
      }
    }
    return serveDownload(filePointer);
  }

  private ResponseEntity<Resource> notModified(FilePointer filePointer) {
    log.trace("Cached on client side {}, returning 304", filePointer);
    return response(filePointer, NOT_MODIFIED, null);
  }

  private ResponseEntity<Resource> serveDownload(FilePointer filePointer) {
    log.debug("Serving '{}'", filePointer);
    final InputStreamResource resource = resourceToReturn(filePointer);
    return response(filePointer, OK, resource);
  }

  private InputStreamResource resourceToReturn(FilePointer filePointer) {
    if (method == HttpMethod.GET) {
      return buildResource(filePointer);
    } else {
      return null;
    }
  }

  private InputStreamResource buildResource(FilePointer filePointer) {
    final InputStream inputStream = filePointer.open();
    final RateLimiter throttler = RateLimiter.create(64d * FileUtils.ONE_KB);
    final ThrottlingInputStream throttlingInputStream = new ThrottlingInputStream(inputStream, throttler);
    return new InputStreamResource(throttlingInputStream);
  }

  private ResponseEntity<Resource> response(FilePointer filePointer, HttpStatus status, Resource body) {
    return ResponseEntity
      .status(status)
      .eTag(filePointer.getEtag())
      .contentLength(filePointer.getSize())
      .lastModified(filePointer.getLastModified().toEpochMilli())
      .body(body);
  }
}
