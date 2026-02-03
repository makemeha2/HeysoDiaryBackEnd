package heyso.HeysoDiaryBackEnd.utils;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String nowKorea() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(FORMATTER);
    }
}
