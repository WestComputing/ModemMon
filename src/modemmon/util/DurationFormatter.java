package modemmon.util;

import java.time.Duration;

public class DurationFormatter {

    public static String toMinutes(Duration duration) {

        StringBuilder result = new StringBuilder();

        if (duration.toMinutes() > 0) {

            long days = duration.toHours() / 24;
            long hours = duration.minusDays(days).toHours();
            long minutes = duration.minusDays(days).minusHours(hours).toMinutes();

            if (days > 1) {
                result.append(days).append(" days");
            } else if (days == 1) {
                result.append(days).append(" day");
            }

            if (hours > 0) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                if (hours > 1) {
                    result.append(hours).append(" hours");
                } else if (hours == 1) {
                    result.append(hours).append(" hour");
                }
            }

            if (minutes > 0) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                if (minutes > 1) {
                    result.append(minutes).append(" minutes");
                } else if (minutes == 1) {
                    result.append(minutes).append(" minute");
                }
            }

        } else {

            result.append("less than a minute");

        }
        return result.toString();
    }

    public static String toHours(Duration duration) {

        StringBuilder result = new StringBuilder();

        long days = duration.toHours() / 24;
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();

        if (duration.toHours() > 0) {

            if (days > 1) {
                result.append(days).append(" days");
            } else if (days == 1) {
                result.append(days).append(" day");
            }

            if (hours > 0) {
                if (result.length() > 0) {
                    result.append(", ");
                } else if (minutes > 40) {
                    hours += 1;
                    result.append("almost ");
                } else if (minutes > 10) {
                    result.append("over ");
                }
                if (hours > 1) {
                    result.append(hours).append(" hours");
                } else if (hours == 1) {
                    result.append(hours).append(" hour");
                }
            }

        } else {

            if (minutes > 40) {
                result.append("almost");
            } else if (minutes > 20) {
                result.append("about half");
            } else {
                result.append("less than half");
            }
            result.append(" an hour");

        }
        return result.toString();
    }

}
