package com.pilar;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("!test")
public class FileSystemStorage implements FileStorage {
  @Override
  public Optional<FilePointer> findFile(UUID uuid) {
    log.debug("Downloading {}", uuid);
    final URL resource = getClass().getResource("/logback.xml");
    final File file = new File(resource.getFile());
    final FileSystemPointer pointer = new FileSystemPointer(file);
    return Optional.of(pointer);
  }
}
