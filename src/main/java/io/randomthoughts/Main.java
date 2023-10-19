package io.randomthoughts;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Pattern dateFinder = Pattern.compile("^.*(\\d{8})[\\-_]?(\\d{6}).*$");

    // Get the date created using ffprobe
    private static Date probeDateTaken(File file) {
        var output = runCommand("ffprobe.exe", "-v", "error", "-select_streams", "v:0", "-show_entries", "format_tags=creation_time", "-of", "default=noprint_wrappers=1:nokey=1", file.getAbsolutePath());

        if (!output.isEmpty()) {
            return DateUtil.fromProbedDate(output.get(0));
        }

        return null;
    }

    private static Date exifDateTaken(File file) {
        var output = runCommand("exiftool.exe", "-ExtractEmbedded", "-CreationDate", "-CreateDate", "-S", file.getAbsolutePath());

        var creationDate = parseExifDate("CreationDate", output);
        var createDate = parseExifDate("CreateDate", output);

        return Stream.of(creationDate, createDate).filter(Objects::nonNull).sorted().findFirst().orElse(null);
    }

    private static Optional<Date> getDateTaken(File file) {
        var date = probeDateTaken(file);

        if (date == null) {
            date = exifDateTaken(file);
        }

        return Optional.ofNullable(date);
    }

    private static Date parseExifDate(String field, List<String> output) {
        var fieldMarker = field.toLowerCase() + ": ";

        for (var line : output) {
            if (line.trim().toLowerCase().startsWith(fieldMarker)) {
                return DateUtil.fromExifDate(line.substring(fieldMarker.length()));
            }
        }

        return null;
    }

    public static void findPictures(File dir, FileNamerInterface renamer) {
        var files = dir.listFiles();

        assert files != null;

        for (var file : files) {
            if (file.isDirectory()) {
                findPictures(file, renamer);
            } else {
                renamer.rename(file);
            }
        }
    }

    // A function that renames a file according to the date taken
    private static void renameFile(File file) {
        var optionalNewFileName = getNewFileName(file);

        if (optionalNewFileName.isEmpty()) {
            return;
        }

        var newFileName = optionalNewFileName.get();

        var newFile = Paths.get(file.getParent(), newFileName.getFullName()).toFile();
        var renamed = file.renameTo(newFile);

        if (renamed) {
            System.out.println("Renamed " + file + " to " + newFile);
        } else {
            System.err.println("Failed to rename " + file + " to " + newFile);
        }
    }

    private static Optional<Date> predictDateFromFileName(FileName fileName) {
        var dateMatcher = dateFinder.matcher(fileName.getBaseName());

        if (dateMatcher.matches()) {
            var datePart = dateMatcher.group(1);
            var timePart = dateMatcher.group(2);

            try {
                return Optional.of(dateFormatter.parse(datePart + timePart));
            } catch (ParseException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    @SafeVarargs
    private static <T> Optional<T> coalesce(Optional<T>... args) {
        for (var value : args) {
            if (value != null && value.isPresent()) {
                return value;
            }
        }

        return Optional.empty();
    }

    public static Optional<FileName> getNewFileName(File file) {
        final var originalName = FileName.parse(file.getName());
        final var dateTaken = getDateTaken(file);

        return dateTaken.map(date -> originalName.setBaseName(DateUtil.toCanonicalTimestamp(date)));
    }

    private static List<String> runCommand(String... args) {
        var command = Arrays.asList(args);
        var processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        try {
            var process = processBuilder.start();
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.waitFor();

            var output = reader.lines().collect(Collectors.toUnmodifiableList());

            reader.close();

            return output;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    public static void main(String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please specify a directory path to scan. Exiting...");
        }

        var directory = new File(args[0]);

        if (directory.exists() && directory.isDirectory()) {
            findPictures(directory, Main::renameFile);
        } else {
            throw new IllegalArgumentException("Please specify an existing directory. Exiting...");
        }
    }
}
