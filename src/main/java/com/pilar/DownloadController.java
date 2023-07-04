package com.pilar;

import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/download")
@AllArgsConstructor
@Slf4j
public class DownloadController {

  private final FileStorage storage;

  @GetMapping("/{uuid}")
  public ResponseEntity<Resource> download(HttpMethod httpMethod,
                                           @PathVariable UUID uuid,
                                           @RequestHeader(IF_NONE_MATCH) Optional<String> requestEtag,
                                           @RequestHeader(IF_MODIFIED_SINCE) Optional<Instant> ifModifiedSinceOpt) {
    return storage
      .findFile(uuid)
      .map(pointer -> new ExistingFile(httpMethod, pointer))
      .map(file -> file.handle(requestEtag, ifModifiedSinceOpt))
      .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
  }

}
