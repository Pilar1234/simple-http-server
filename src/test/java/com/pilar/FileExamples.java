package com.pilar;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class FileExamples {

  public static final UUID TXT_FILE_UUID = UUID.randomUUID();
  public static final FilePointer TXT_FILE = txtFile();
  public static final UUID NOT_FOUND_UUID = UUID.randomUUID();

  private static FileSystemPointer txtFile() {
    final URL resource = FileExamples.class.getResource("/test_file.txt");
    final File file = new File(resource.getFile());
    return new FileSystemPointer(file);
  }
}
