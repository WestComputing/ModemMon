package modemmon.modem;

import java.io.IOException;
import static java.lang.Double.NaN;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import modemmon.channel.DownChannel;
import modemmon.channel.UpChannel;
import modemmon.event.Event;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SB6183 extends Modem {

    private static final String URL_CONNECT = "http://192.168.100.1/RgConnect.asp";
    private static final String URL_INFO = "http://192.168.100.1/RgSwInfo.asp";
    private static final String URL_EVENTLOG = "http://192.168.100.1/RgEventLog.asp";

    private static final int MAX_DOWNSTREAM_BONDING_GROUP_SIZE = 16;
    private static final int MAX_UPSTREAM_BONDING_GROUP_SIZE = 4;
    private static final int MAX_DOWNSTREAM_FREQUENCY_SPREAD = 96 * MEGA;

    private SB6183() {
        setModel(Modem.Model.SB6183);
        setModelText(Modem.SB6183);
    }

    SB6183(Document homePage) {
        this();
        if (!parseStatus(homePage)) {
            System.exit(6183);
        }
    }

    @Override
    public int getMAX_DOWNSTREAM_BONDING_GROUP_SIZE() {
        return MAX_DOWNSTREAM_BONDING_GROUP_SIZE;
    }

    @Override
    public int getMAX_UPSTREAM_BONDING_GROUP_SIZE() {
        return MAX_UPSTREAM_BONDING_GROUP_SIZE;
    }

    @Override
    public int getMAX_DOWNSTREAM_FREQUENCY_SPREAD() {
        return MAX_DOWNSTREAM_FREQUENCY_SPREAD;
    }

    @Override
    public final boolean parseStatus() {
        try {
            Document document = Jsoup.connect(URL_CONNECT).timeout(DEFAULT_URL_TIMEOUT).get();
            parseStatus(document);
        } catch (IOException e) {
            System.out.println("Error reading: ".concat(e.getMessage()));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public final boolean parseStatus(Document document) {
        try {
            setAcquireDownstreamChannelStatus(parseAcquireDownstreamChannelStatus(document));
            setAcquireDownstreamChannelComment(parseAcquireDownstreamChannelComment(document));
            setConnectivityStateStatus(parseConnectivityStateStatus(document));
            setConnectivityStateComment(parseConnectivityStateComment(document));
            setBootStateStatus(parseBootStateStatus(document));
            setBootStateComment(parseBootStateComment(document));
            setConfigurationFileStatus(parseConfigurationFileStatus(document));
            setConfigurationFileComment(parseConfigurationFileComment(document));
            setSecurityStatus(parseSecurityStatus(document));
            setSecurityComment(parseSecurityComment(document));
            setDocsisStatus(parseDocsisStatus(document));
            setDocsisComment(parseDocsisComment(document));
            setModemTime(parseModemTime(document));
            setDownChannels(parseDownChannels(document));
            setUpChannels(parseUpChannels(document));

            document = Jsoup.connect(URL_INFO).timeout(DEFAULT_URL_TIMEOUT).get();
            setStandardSpecificationCompliant(parseStandardSpecificationCompliant(document));
            setHardwareVersion(parseHardwareVersion(document));
            setSoftwareVersion(parseSoftwareVersion(document));
            setMacAddress(parseMACaddress(document));
            setSerialNumber(parseSerialNumber(document));
            setUpTime(parseUpTime(document));

            document = Jsoup.connect(URL_EVENTLOG).timeout(DEFAULT_URL_TIMEOUT).get();
            setEvents(parseEvents(document));

        } catch (Exception e) {
            System.out.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    String parseAcquireDownstreamChannelStatus(Document document) {
        String key = "td:contains(Acquire Downstream Channel) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseAcquireDownstreamChannelComment(Document document) {
        String key = "td:contains(Acquire Downstream Channel) ~ td:eq(2)";
        return document.select(key).text().trim();
    }

    @Override
    String parseConnectivityStateStatus(Document document) {
        String key = "td:contains(Connectivity State) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseConnectivityStateComment(Document document) {
        String key = "td:contains(Connectivity State) ~ td:eq(2)";
        return document.select(key).text().trim();
    }

    @Override
    String parseBootStateStatus(Document document) {
        String key = "td:contains(Boot State) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseBootStateComment(Document document) {
        String key = "td:contains(Boot State) ~ td:eq(2)";
        return document.select(key).text().trim();
    }

    @Override
    String parseConfigurationFileStatus(Document document) {
        String key = "td:contains(Configuration File) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseConfigurationFileComment(Document document) {
        String key = "td:contains(Configuration File) ~ td:eq(2)";
        return document.select(key).text().trim();
    }

    @Override
    String parseSecurityStatus(Document document) {
        String key = "td:contains(Security) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseSecurityComment(Document document) {
        String key = "td:contains(Security) ~ td:eq(2)";
        return document.select(key).text().trim();
    }

    @Override
    String parseDocsisStatus(Document document) {
        String key = "td:contains(DOCSIS Network Access Enabled) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseDocsisComment(Document document) {
        String key = "td:contains(DOCSIS Network Access Enabled) ~ td:eq(2)";
        return document.select(key).text().trim();
    }

    @Override
    List<DownChannel> parseDownChannels(Document document) {
        String key = "Downstream Bonded Channels";
        Elements channelRow = document.select("tr:contains(" + key + ") ~ tr:gt(1) > td");

        List<DownChannel> channels = new ArrayList<>();

        for (int channelCol = 0; channelCol < channelRow.size();) {

            int number = -1;
            String lockText = "";
            String modulation = "";
            int id = -1;
            int frequency = 0;
            double power = NaN;
            double snr = NaN;
            int correctedErrors = -1;
            int uncorrectedErrors = -1;

            try {
                number = Integer.parseInt(channelRow.get(channelCol++).text().trim());
                lockText = channelRow.get(channelCol++).text().trim();
                modulation = channelRow.get(channelCol++).text().trim();
                id = Integer.parseInt(channelRow.get(channelCol++).text().trim());

                String frequencyText = channelRow.get(channelCol++).text();
                frequencyText = frequencyText.replace("Hz", "").trim();
                frequency = Integer.parseInt(frequencyText);

                String powerText = channelRow.get(channelCol++).text();
                powerText = powerText.replace("dBmV", "").trim();
                power = Double.parseDouble(powerText);

                String snrText = channelRow.get(channelCol++).text();
                snrText = snrText.replace("dB", "").trim();
                snr = Double.parseDouble(snrText);

                correctedErrors = Integer.parseInt(channelRow.get(channelCol++).text().trim());
                uncorrectedErrors = Integer.parseInt(channelRow.get(channelCol++).text().trim());

            } catch (Exception e) {
                System.out.println("Downchannel parse error: ".concat(e.getMessage()));
            }

            DownChannel downChannel = new DownChannel(number, id, frequency, power, lockText,
                    modulation, snr, correctedErrors, uncorrectedErrors);
            channels.add(downChannel);
        }
        return channels;
    }

    @Override
    List<UpChannel> parseUpChannels(Document document) {
        String key = "Upstream Bonded Channels";
        Elements channelRow = document.select("tr:contains(" + key + ") ~ tr:gt(1) > td");

        List<UpChannel> channels = new ArrayList<>();

        for (int channelCol = 0; channelCol < channelRow.size();) {

            int number = -1;
            String lockText = "";
            String type = "";
            int id = -1;
            int symbolRate = -1;
            int frequency = 0;
            double power = NaN;

            try {
                number = Integer.parseInt(channelRow.get(channelCol++).text().trim());
                lockText = channelRow.get(channelCol++).text().trim();
                type = channelRow.get(channelCol++).text().trim();
                id = Integer.parseInt(channelRow.get(channelCol++).text().trim());

                String symbolRateText = channelRow.get(channelCol++).text().trim();
                symbolRateText = symbolRateText.replace("Ksym/sec", "").trim();
                symbolRate = Integer.parseInt(symbolRateText) * 1_000;

                String frequencyText = channelRow.get(channelCol++).text();
                frequencyText = frequencyText.replace("Hz", "").trim();
                frequency = Integer.parseInt(frequencyText);

                String powerText = channelRow.get(channelCol++).text();
                powerText = powerText.replace("dBmV", "").trim();
                power = Double.parseDouble(powerText);

            } catch (Exception e) {
                System.out.println("Upchannel parse error::".concat(e.getMessage()));
            }

            UpChannel upChannel = new UpChannel(number, id, frequency, power, lockText, type, symbolRate);
            channels.add(upChannel);
        }
        return channels;
    }

    @Override
    LocalDateTime parseModemTime(Document document) {
        String key = "Current System Time: ";
        String timeText = document.select("p:contains(" + key + ")").text();
        timeText = timeText.replace(key, "");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");
        return LocalDateTime.parse(timeText, format);
    }

    @Override
    Duration parseUpTime(Document document) {
        String key = "Up Time";
        String upTimeText = document.select("td:contains(" + key + ") ~ td:eq(1)").text().trim();
        upTimeText = "P".concat(upTimeText
                .replace(" days ", "DT")
                .replace("h:", "H")
                .replace("m:", "M")
                .replace("s", "S"));
        return Duration.parse(upTimeText);
    }

    @Override
    String parseStandardSpecificationCompliant(Document document) {
        String key = "td:contains(Standard Specification Compliant) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseHardwareVersion(Document document) {
        String key = "td:contains(Hardware Version) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseSoftwareVersion(Document document) {
        String key = "td:contains(Software Version) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseMACaddress(Document document) {
        String key = "td:contains(Cable Modem MAC Address) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    String parseSerialNumber(Document document) {
        String key = "td:contains(Serial Number) ~ td:eq(1)";
        return document.select(key).text().trim();
    }

    @Override
    List<Event> parseEvents(Document document) {

        LocalDateTime timePriorEvent = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0, 0);
        Elements modemEvents = document.select("td");

        List<Event> events = new LinkedList<>();

        // Skip first and last elements, three elements span each line entry
        final int modemEventsFirstIndex = 1;
        final int modemEventsLastIndex = modemEvents.size() - 1;
        final int modemEventsSpanSize = 3;

        for (int modemEventsIndex = modemEventsLastIndex - modemEventsSpanSize;
                modemEventsIndex > modemEventsFirstIndex;
                modemEventsIndex -= modemEventsSpanSize) {

            LocalDateTime time = timePriorEvent;
            boolean clockSet = false;
            String priorityText = "";
            int priorityRank = -1;
            String descriptionFull = "";
            String descriptionBrief = "";

            try {
                String timeText = modemEvents.get(modemEventsIndex).text().trim();
                if (timeText.contains("Not")) {
                    time = timePriorEvent.plusNanos(1);
                } else {
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");
                    time = LocalDateTime.parse(timeText, format);
                    clockSet = true;
                }
                timePriorEvent = time;

                String priorityTextText = modemEvents.get(modemEventsIndex + 1).text().trim();
                String[] priorityTextArray = priorityTextText.split("\\s");
                priorityText = priorityTextArray[0];
                String priorityRankText = priorityTextArray[1].replaceAll("[()]", "").trim();
                priorityRank = Integer.parseInt(priorityRankText);

                descriptionFull = modemEvents.get(modemEventsIndex + 2).text().trim();
                String delimiter = ";";
                int delimiterPosition = descriptionFull.contains(delimiter)
                        ? descriptionFull.indexOf(delimiter)
                        : descriptionFull.length();
                descriptionBrief = descriptionFull.substring(0, delimiterPosition).trim();
                if (descriptionBrief.contains("T1")) {
                    descriptionBrief = "T1 timeout";
                }
                if (descriptionBrief.contains("T2")) {
                    descriptionBrief = "T2 timeout";
                }
                if (descriptionBrief.contains("T3")) {
                    descriptionBrief = "T3 timeout";
                }
                if (descriptionBrief.contains("T4")) {
                    descriptionBrief = "T4 timeout";
                }
                descriptionBrief = descriptionBrief.replace("SYNC Timing Synchronization failure - ", "");

            } catch (Exception e) {
                System.out.println("Event parse error: ".concat(e.getMessage()));
                //System.exit(0);
            }

            Event event = new Event(time, clockSet, priorityText, priorityRank, descriptionFull, descriptionBrief);
            events.add(event);
        }

        return events;
    }

}
