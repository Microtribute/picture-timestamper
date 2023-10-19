package io.randomthoughts;

import java.util.Objects;

public class FileName {
    private final String baseName;
    private final String extension;

    public FileName(String baseName, String extension) {
        this.baseName = baseName;
        this.extension = extension;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getExtension() {
        return extension;
    }

    public String getFullName() {
        if (hasExtension()) {
            return baseName + '.' + extension;
        }

        return baseName;
    }

    public boolean hasExtension() {
        return extension != null;
    }

    @Override
    public String toString() {
        return this.getFullName();
    }

    public FileName setBaseName(String baseName) {
        return new FileName(baseName, this.extension);
    }

    public FileName setExtension(String extension) {
        return new FileName(this.baseName, extension);
    }

    public static FileName parse(String fileName) {
        Objects.requireNonNull(fileName);

        var dotPosition = fileName.lastIndexOf('.');

        if (dotPosition > -1) {
            var baseName = fileName.substring(0, dotPosition);
            var extension = fileName.substring(dotPosition + 1);

            return new FileName(baseName, extension);
        }

        return new FileName(fileName, null);
    }
}

