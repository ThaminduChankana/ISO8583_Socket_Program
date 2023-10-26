package logic;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomLogFormatter extends SimpleFormatter {
    private static final String format = "INFO : %1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp - %2$s %n";

    @Override
    public synchronized String format(LogRecord record) {
        return String.format(format, new Date(record.getMillis()), record.getMessage());
    }
}