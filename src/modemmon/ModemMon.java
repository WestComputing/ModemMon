package modemmon;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NavigableSet;
import modemmon.channel.DownChannel;
import modemmon.channel.UpChannel;
import modemmon.event.Event;
import modemmon.modem.Modem;
import static modemmon.modem.Modem.KILO;
import static modemmon.modem.Modem.MEGA;
import modemmon.util.DurationFormatter;
import modemmon.util.NumberFormatter;

public class ModemMon {

    private static final String LOGDIR = System.getProperty("user.home").concat("/ModemMon/");
    private static final String LOGDATE = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-'W'ww"));
    private static final String LOGFILENAME = LOGDIR.concat("ModemMon-").concat(LOGDATE);
    private static final String LOGTEXT = LOGFILENAME.concat(".txt");
    private static final String LOGCSV = LOGFILENAME.concat(".csv");

    public static final int SECOND = 1_000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;

    private static final int INTERVAL_DEFAULT = (1 * HOUR) + (0 * MINUTE);
    private static int interval = INTERVAL_DEFAULT;
    private static final int RECOVERY_DEFAULT = (10 * MINUTE) + (0 * SECOND);
    private static int recovery = RECOVERY_DEFAULT;

    public static void main(String[] args) {

        String urlArg = null;
        if (args.length > 0) {
            urlArg = "http://".concat(args[0]);
        }

        Modem modem = null;
        while (modem == null) {
            System.out.println("Looking for modem...");
            modem = Modem.detectModem(urlArg);
        }
        System.out.println("Found ".concat(modem.getModelText()));
        Log logText = new Log(LOGTEXT);
        Log logCSV = new Log(LOGCSV);

        LocalDateTime noLogTime = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime lastLogTime = noLogTime;
        LocalDateTime lastOptimalTime = lastLogTime;

        List<DownChannel> downChannels = modem.getDownChannels();
        List<DownChannel> lastDownChannels;

        while (true) {

            System.out.print(modem);
            logText.write(modem.toString());
            logCSV.write(modem.toCSV());

            String format;
            String result;

            boolean optimal = true;
            lastDownChannels = downChannels;
            downChannels = modem.getDownChannels();
            int priorChannelFrequency = downChannels.get(0).getFrequency() - DownChannel.CHANNEL_WIDTH;

            long aggregateCorrectedErrors = 0;
            long aggregateUncorrectedErrors = 0;
            long aggregateCorrectedDelta = 0;
            long aggregateUncorrectedDelta = 0;
            double lowestPower = Double.POSITIVE_INFINITY;
            double highestPower = Double.NEGATIVE_INFINITY;
            int lowestFrequency = Integer.MAX_VALUE;
            int highestFrequency = Integer.MIN_VALUE;

            for (DownChannel downChannel : downChannels) {

                if (!(downChannel.getLockText().equals("Locked"))) {
                    optimal = false;
                }

                String sequenceStatus = "  ";
                int channelFrequency = downChannel.getFrequency();
                if (channelFrequency - DownChannel.CHANNEL_WIDTH != priorChannelFrequency) {
                    optimal = false;
                    sequenceStatus = "BG";
                }
                priorChannelFrequency = channelFrequency;
                lowestFrequency = Math.min(lowestFrequency, channelFrequency);
                highestFrequency = Math.max(highestFrequency, channelFrequency);

                String snrStatus = "   ";
                if (downChannel.getSnr() < DownChannel.SNR_MIN) {
                    optimal = false;
                    snrStatus = "SNR";
                }

                String powerStatus = "  ";
                double power = downChannel.getPower();
                if (power < DownChannel.POWER_MIN) {
                    optimal = false;
                    powerStatus = "LO";
                }
                if (power > DownChannel.POWER_MAX) {
                    optimal = false;
                    powerStatus = "HI";
                }
                lowestPower = Math.min(power, lowestPower);
                highestPower = Math.max(power, highestPower);

                aggregateCorrectedErrors += downChannel.getCorrectedErrors();
                aggregateUncorrectedErrors += downChannel.getUncorrectedErrors();

                long correctedDelta = 0;
                long uncorrectedDelta = 0;
                int c = lastDownChannels.indexOf(downChannel);
                if (c >= 0) {
                    DownChannel lastDownChannel = lastDownChannels.get(c);
                    correctedDelta = downChannel.getCorrectedErrors() - lastDownChannel.getCorrectedErrors();
                    uncorrectedDelta = downChannel.getUncorrectedErrors() - lastDownChannel.getUncorrectedErrors();
                    aggregateCorrectedDelta += correctedDelta;
                    aggregateUncorrectedDelta += uncorrectedDelta;
                }

                format = "%2s %3s %2s";
                result = String.format(format,
                        powerStatus,
                        snrStatus,
                        sequenceStatus
                );
                String comment = result;

                format = "%2d:%2d | %6s | %6s | %4d | %4.1f |% 5.1f | %4s| %4s|%5s|%5s| %s%n";
                result = String.format(format,
                        downChannel.getNumber(),
                        downChannel.getId(),
                        downChannel.getLockText(),
                        downChannel.getModulation(),
                        downChannel.getFrequency() / MEGA,
                        downChannel.getSnr(),
                        downChannel.getPower(),
                        NumberFormatter.format(downChannel.getCorrectedErrors()),
                        NumberFormatter.format(downChannel.getUncorrectedErrors()),
                        (correctedDelta > 0) ? "+".concat(NumberFormatter.format(correctedDelta)) : "",
                        (uncorrectedDelta > 0) ? "+".concat(NumberFormatter.format(uncorrectedDelta)) : "",
                        comment
                );
                System.out.print(result);
                logText.write(result);
                logCSV.write(downChannel.toCSV());
            }

            if (downChannels.size() < modem.getMAX_DOWNSTREAM_BONDING_GROUP_SIZE()) {
                optimal = false;
            }

            if (aggregateCorrectedDelta > 0 || aggregateUncorrectedDelta > 0) {
                optimal = false;
            }

            double powerSpread;
            powerSpread = Math.abs(highestPower - lowestPower);
            if (powerSpread > DownChannel.POWER_SPREAD_MAX) {
                optimal = false;
            }
            format = "%39s %4.1f | %4s| %4s|%5s|%5s|%n";
            result = String.format(format,
                    "Downstream Bonding Group power spread:",
                    powerSpread,
                    NumberFormatter.format(aggregateCorrectedErrors),
                    NumberFormatter.format(aggregateUncorrectedErrors),
                    (aggregateCorrectedDelta > 0) ? "+".concat(NumberFormatter.format(aggregateCorrectedDelta)) : "",
                    (aggregateUncorrectedDelta > 0) ? "+".concat(NumberFormatter.format(aggregateUncorrectedDelta)) : ""
            );
            System.out.print(result);
            logText.write(result);

            double frequencySpread;
            frequencySpread = highestFrequency - lowestFrequency;
            if (frequencySpread > modem.getMAX_DOWNSTREAM_FREQUENCY_SPREAD()) {
//                optimal = false;
                format = "%39s%3dMHz|%23s|%n";
                result = String.format(format,
                        "Downstream frequency spread:",
                        (int) frequencySpread / MEGA,
                        ""
                );
                System.out.print(result);
                logText.write(result);
            }

            System.out.println();
            logText.write("\r\n");

            List<UpChannel> upChannels = modem.getUpChannels();
            int priorChannelID = upChannels.get(0).getId() - 1;

            for (UpChannel upChannel : upChannels) {

                if (!(upChannel.getLockText().equals("Locked"))) {
                    optimal = false;
                }

                String sequenceStatus = "";
                int channelID = upChannel.getId();
                if (channelID - 1 != priorChannelID) {
                    optimal = false;
                    sequenceStatus = "UBG";
                }
                priorChannelID = channelID;

                String powerStatus = "";
                double power = upChannel.getPower();
                if (power < UpChannel.POWER_MIN) {
                    optimal = false;
                    powerStatus = "LO";
                }
                if (power > UpChannel.POWER_MAX) {
                    optimal = false;
                    powerStatus = "HI";
                }

                format = "%3s %2s";
                result = String.format(format,
                        sequenceStatus,
                        powerStatus
                );
                String comment = result.trim();

                format = " %1d:%1d  | %6s | %6s | %4.1f | %4d |% 5.1f |%23s| %s%n";
                result = String.format(format,
                        upChannel.getNumber(),
                        channelID,
                        upChannel.getLockText(),
                        upChannel.getType(),
                        (float) upChannel.getFrequency() / MEGA,
                        upChannel.getSymbolRate() / KILO,
                        power,
                        "",
                        comment
                );
                System.out.print(result);
                logText.write(result);
                logCSV.write(upChannel.toCSV());
            }

            if (upChannels.size() < modem.getMAX_UPSTREAM_BONDING_GROUP_SIZE()) {
                optimal = false;
            }

            NavigableSet<Event> recentEvents;
            recentEvents = modem.getEvents(lastLogTime);

            if (recentEvents.size() > 0) {

                optimal = false;
                if (lastLogTime.isEqual(noLogTime)) {
                    format = "%n%s%n%n";
                    result = String.format(format, "All stored modem events:");
                } else {
                    format = "%nEvents since %s:%n%n";
                    result = String.format(format,
                            lastLogTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy, HH:mm:ss")));
                }
                System.out.print(result);
                logText.write(result);

                for (Event event : recentEvents) {
                    System.out.print(event);
                    logText.write(event.toString());
                }
                lastLogTime = recentEvents.pollLast().getTime().plusNanos(1);

            } else {

                format = "%nNo new events logged since %s.%n";
                result = String.format(format,
                        lastLogTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy, HH:mm:ss")));
                System.out.print(result);
                logText.write(result);

            }

            if (optimal) {

                interval = Math.min(interval += recovery, INTERVAL_DEFAULT);
                recovery = Math.min(recovery += RECOVERY_DEFAULT, INTERVAL_DEFAULT / 3);

                Duration lastOptimalUptime = Duration.between(lastOptimalTime, modem.getModemTime());
                if (lastOptimalUptime.toMinutes() > 0) {

                    format = "Since %s, optimal uptime is %s.";
                    result = String.format(format,
                            lastOptimalTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy, HH:mm:ss")),
                            DurationFormatter.toMinutes(lastOptimalUptime)
                    );
                    System.out.print(result);
                    logText.write(result);
                }

            } else {

                recovery = RECOVERY_DEFAULT;
                interval = recovery;
                lastOptimalTime = modem.getModemTime();

            }

            System.out.printf("%n%n%n");
            logText.write("\r\n\r\n");
            logCSV.write("\r\n");

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                System.out.println("Interrupted during sleep");
            }

            while (!modem.isUpdated()) {
                try {
                    Thread.sleep(10 * SECOND);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted during sleep");
                }
            }
        }
    }

}
