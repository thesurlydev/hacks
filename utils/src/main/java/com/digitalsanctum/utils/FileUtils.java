package com.digitalsanctum.utils;

import java.io.File;
import java.nio.file.Path;

public class FileUtils {
  public static boolean setExecutable(Path path) {
    File script = path.toFile();
    return script.canExecute() || path.toFile().setExecutable(true, false);
  }
}
