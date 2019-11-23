package modemmon.modem;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import static modemmon.ModemMon.SECOND;
import modemmon.channel.DownChannel;
import modemmon.channel.UpChannel;
import modemmon.event.Event;
import modemmon.util.DurationFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Modem {

    public static final int MEGA = 1_000_000;
    public static final int KILO = 1_000;

    static final int DEFAULT_URL_TIMEOUT = 0 * SECOND; //TODO: Why does non-zero fail?

    private static final String URL_HOME = "http://192.168.100.1";
    static final String SB6183 = "SB6183";
    public static final String TEST = "TEST";

    private Modem.Model model = null;
    private String modelText;
    private String acquireDownstreamChannelStatus;
    private String acquireDownstreamChannelComment;
    private String connectivityStateStatus;
    private String connectivityStateComment;
    private String bootStateStatus;
    private String bootStateComment;
    private String configurationFileStatus;
    private String configurationFileComment;
    private String securityStatus;
    private String securityComment;
    private String docsisStatus;
    private String docsisComment;
    private String standardSpecificationCompliant;
    private String hardwareVersion;
    private String softwareVersion;
    private String macAddress;
    private String serialNumber;
    private LocalDateTime modemTime;
    private Duration upTime;

    private List<DownChannel> downChannels = new ArrayList<>();
    private final Map<LocalDateTime, List<DownChannel>> downChannelHistory = new LinkedHashMap<>();
    private List<UpChannel> upChannels = new ArrayList<>();
    private final Map<LocalDateTime, List<UpChannel>> upChannelHistory = new LinkedHashMap<>();
    private final NavigableSet<Event> events = new TreeSet<>();

    public final boolean isUpdated() {
        return parseStatus();
    }

    public static enum Model {
        SB6183, TEST;
    }

    private static Modem detectModem() {
        return detectModem(URL_HOME);
    }

    public static Modem detectModem(String url) {

        if (url == null) {
            return detectModem();
        } else {
            try {
                Document homePage = Jsoup.connect(url).timeout(DEFAULT_URL_TIMEOUT).get();
                return detectModem(homePage);
            } catch (IOException e) {
                System.out.println("Error reading modem's home page: ".concat(e.getMessage()));
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Modem detectModem(Document homePage) {
        String detectedModel = homePage.select("span#thisModelNumberIs").first().text().trim();
        switch (detectedModel) {
            default:
                return null;
            case SB6183:
                return new SB6183(homePage);
        }
    }

    public final Modem.Model getModel() {
        return model;
    }

    public final String getModelText() {
        return modelText;
    }

    final void setModel(Modem.Model model) {
        this.model = model;
        switch (model) {
            default:
                this.model = null;
                this.modelText = null;
                break;
            case SB6183:
                this.modelText = SB6183;
                break;
        }
    }

    final void setModelText(String modelText) {
        this.modelText = modelText;
        switch (modelText) {
            default:
                this.model = null;
                this.modelText = null;
                break;
            case SB6183:
                this.model = Model.SB6183;
                break;
        }
    }

    public final String getAcquireDownstreamChannelStatus() {
        return acquireDownstreamChannelStatus;
    }

    public final String getAcquireDownstreamChannelComment() {
        return acquireDownstreamChannelComment;
    }

    public final String getConnectivityStateStatus() {
        return connectivityStateStatus;
    }

    public final String getConnectivityStateComment() {
        return connectivityStateComment;
    }

    public final String getBootStateStatus() {
        return bootStateStatus;
    }

    public final String getBootStateComment() {
        return bootStateComment;
    }

    public final String getConfigurationFileStatus() {
        return configurationFileStatus;
    }

    public final String getConfigurationFileComment() {
        return configurationFileComment;
    }

    public final String getSecurityStatus() {
        return securityStatus;
    }

    public final String getSecurityComment() {
        return securityComment;
    }

    public final String getDocsisStatus() {
        return docsisStatus;
    }

    public final String getDocsisComment() {
        return docsisComment;
    }

    public final String getStandardSpecificationCompliant() {
        return standardSpecificationCompliant;
    }

    public final String getHardwareVersion() {
        return hardwareVersion;
    }

    public final String getSoftwareVersion() {
        return softwareVersion;
    }

    public final String getMacAddress() {
        return macAddress;
    }

    public final String getSerialNumber() {
        return serialNumber;
    }

    public final LocalDateTime getModemTime() {
        return modemTime;
    }

    public final Duration getUpTime() {
        return upTime;
    }

    public final List<DownChannel> getDownChannels() {
        return new ArrayList<>(downChannels);
    }

    public final List<UpChannel> getUpChannels() {
        return new ArrayList<>(upChannels);
    }

    public final NavigableSet<Event> getEvents() {
        return new TreeSet<>(events);
    }

    public final NavigableSet<Event> getEvents(LocalDateTime since) {
        if (since == null) {
            return getEvents();
        }
        NavigableSet<Event> recentEvents = new TreeSet<>();
        events.stream().filter(event -> (event.getTime().isAfter(since))).forEach(recentEvents::add);
        return recentEvents;
    }

    final void setEvents(List<Event> events) {
        events.stream().forEach(this.events::add);
    }

    final void setAcquireDownstreamChannelStatus(String acquireDownstreamChannelStatus) {
        this.acquireDownstreamChannelStatus = acquireDownstreamChannelStatus;
    }

    final void setAcquireDownstreamChannelComment(String acquireDownstreamChannelComment) {
        this.acquireDownstreamChannelComment = acquireDownstreamChannelComment;
    }

    final void setConnectivityStateStatus(String connectivityStateStatus) {
        this.connectivityStateStatus = connectivityStateStatus;
    }

    final void setConnectivityStateComment(String connectivityStateComment) {
        this.connectivityStateComment = connectivityStateComment;
    }

    final void setBootStateStatus(String bootStateStatus) {
        this.bootStateStatus = bootStateStatus;
    }

    final void setBootStateComment(String bootStateComment) {
        this.bootStateComment = bootStateComment;
    }

    final void setConfigurationFileStatus(String configurationFileStatus) {
        this.configurationFileStatus = configurationFileStatus;
    }

    final void setConfigurationFileComment(String configurationFileComment) {
        this.configurationFileComment = configurationFileComment;
    }

    final void setSecurityStatus(String securityStatus) {
        this.securityStatus = securityStatus;
    }

    final void setSecurityComment(String securityComment) {
        this.securityComment = securityComment;
    }

    final void setDocsisStatus(String docsisStatus) {
        this.docsisStatus = docsisStatus;
    }

    final void setDocsisComment(String docsisComment) {
        this.docsisComment = docsisComment;
    }

    final void setStandardSpecificationCompliant(String standardSpecificationCompliant) {
        this.standardSpecificationCompliant = standardSpecificationCompliant;
    }

    final void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    final void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    final void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    final void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    final void setModemTime(LocalDateTime modemTime) {
        this.modemTime = modemTime;
    }

    final void setUpTime(Duration upTime) {
        this.upTime = upTime;
    }

    final void setDownChannels(List<DownChannel> downChannels) {
        List<DownChannel> copy = new ArrayList<>(downChannels);
        Collections.sort(copy);
        downChannelHistory.put(modemTime, copy);
        this.downChannels = copy;
    }

    final void setUpChannels(List<UpChannel> upChannels) {
        List<UpChannel> copy = new ArrayList<>(upChannels);
        Collections.sort(copy);
        upChannelHistory.put(modemTime, copy);
        this.upChannels = copy;
    }

    public abstract int getMAX_DOWNSTREAM_BONDING_GROUP_SIZE();

    public abstract int getMAX_UPSTREAM_BONDING_GROUP_SIZE();

    public abstract int getMAX_DOWNSTREAM_FREQUENCY_SPREAD();

    abstract String parseAcquireDownstreamChannelStatus(Document document);

    abstract String parseAcquireDownstreamChannelComment(Document document);

    abstract String parseConnectivityStateStatus(Document document);

    abstract String parseConnectivityStateComment(Document document);

    abstract String parseBootStateStatus(Document document);

    abstract String parseBootStateComment(Document document);

    abstract String parseConfigurationFileStatus(Document document);

    abstract String parseConfigurationFileComment(Document document);

    abstract String parseSecurityStatus(Document document);

    abstract String parseSecurityComment(Document document);

    abstract String parseDocsisStatus(Document document);

    abstract String parseDocsisComment(Document document);

    abstract List<DownChannel> parseDownChannels(Document document);

    abstract List<UpChannel> parseUpChannels(Document document);

    abstract LocalDateTime parseModemTime(Document document);

    abstract Duration parseUpTime(Document document);

    abstract String parseStandardSpecificationCompliant(Document document);

    abstract String parseHardwareVersion(Document document);

    abstract String parseSoftwareVersion(Document document);

    abstract String parseMACaddress(Document document);

    abstract String parseSerialNumber(Document document);

    abstract List<Event> parseEvents(Document document);

    abstract boolean parseStatus(Document document);

    abstract boolean parseStatus();

    @Override
    public String toString() {
        String timeFormat = "EEEE, MMMM d, yyyy, HH:mm:ss";
        String timeText = getModemTime().format(DateTimeFormatter.ofPattern(timeFormat));
        String format = "Status as of %s:%n%n";
        StringBuilder result = new StringBuilder(String.format(format, timeText));

        format = "%-23s | %-18s | %-25s%n";
        result.append(String.format(format, "Downstream Channel", getAcquireDownstreamChannelStatus(), getAcquireDownstreamChannelComment()));
        result.append(String.format(format, "Connectivity State", getConnectivityStateStatus(), getConnectivityStateComment()));
        result.append(String.format(format, "Boot State", getBootStateStatus(), getBootStateComment()));
        result.append(String.format(format, "Configuration File", getConfigurationFileStatus(), getConfigurationFileComment()));
        result.append(String.format(format, "Security", getSecurityStatus(), getSecurityComment()));
        result.append(String.format(format, "DOCSIS Net Access", getDocsisStatus(), getDocsisComment()));

        format = "%-23s | %-46s%n";
        result.append(String.format(format, "Standard Specification", getStandardSpecificationCompliant()));
        result.append(String.format(format, "Software Version", getSoftwareVersion()));
        result.append(String.format(format, "Hardware Version", getHardwareVersion()));
        result.append(String.format(format, "Modem MAC Address", getMacAddress()));
        result.append(String.format(format, "Serial Number", getSerialNumber()));
        format = "%n%s %s uptime is %s.%n%n";
        result.append(String.format(format,
                getSoftwareVersion(),
                getStandardSpecificationCompliant(),
                DurationFormatter.toHours(upTime)
        ));
        return result.toString();
    }

    public String toCSV() {
        return getModemTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
