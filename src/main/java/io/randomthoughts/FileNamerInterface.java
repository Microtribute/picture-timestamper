package io.randomthoughts;

import java.io.File;

@FunctionalInterface
public interface FileNamerInterface {
    void rename(File file);
}
