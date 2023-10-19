package io.randomthoughts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class DateUtil {
    private static final TimeZone localTimeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
    private static final DateFormat fileNameDateFormatter = new SimpleDateFormat("yyyyMMddHHmmss") {{
        setTimeZone(localTimeZone);
    }};

    // The date formats output by `exiftool` comes with or without the timezone offset.
    // 2023:10:19 17:38:58+08:00
    // 2023:10:19 17:38:58
    private static final DateFormat exifDateFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ssXXX");
    private static final Pattern exifDatePattern = Pattern.compile("^(\\d{4}:\\d{2}:\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})([+-]\\d{2}:\\d{2}|)$");
    private static final DateFormat probedDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'") {{
        setTimeZone(localTimeZone);
    }};

    public static Date fromExifDate(final String exifDate) {
        // MP4 files store the media creation date in UTC
        return TryCatch.attempt(() -> {
            var normalized = exifDate;
            var test = exifDatePattern.matcher(normalized);

            if (!test.matches()) {
                throw new IllegalArgumentException("Unable to parse date: " + normalized);
            } else if (test.group(2).isBlank()) {
                normalized += "+08:00";
            }

            return exifDateFormatter.parse(normalized);
        }, null);
    }

    public static Date fromProbedDate(final String dateString) {
        var date = TryCatch.attempt(() -> probedDateFormatter.parse(dateString));

        if (date != null) {
            return fromUtc(date);
        }

        return null;
    }

    public static String toCanonicalTimestamp(final Date date) {
        return fileNameDateFormatter.format(date);
    }

    public static Date fromCanonicalTimestamp(final String dateString) {
        return TryCatch.attempt(() -> fileNameDateFormatter.parse(dateString));
    }

    public static Date fromUtc(Date date){
        return new Date(date.getTime() + Calendar.getInstance().getTimeZone().getOffset(new Date().getTime()));
    }

    public static Date toUtc(Date date){
        return new Date(date.getTime() - Calendar.getInstance().getTimeZone().getOffset(date.getTime()));
    }
}
