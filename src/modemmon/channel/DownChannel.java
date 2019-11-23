package modemmon.channel;

import static modemmon.modem.Modem.MEGA;

public class DownChannel extends Channel {

    public static final int CHANNEL_WIDTH = 6_000_000;
//    public static final int MIN_FREQUENCY = 90 * MEGA;
//    public static final int MAX_FREQUENCY = 1002 * MEGA;

    public static final double POWER_SPREAD_MAX = 10;
    public static final double POWER_MIN = -15;
    public static final double POWER_MAX = Math.abs(POWER_MIN);
    public static final double SNR_MIN = 34;

    private final String modulation;
    private final double snr;
    private final long correctedErrors;
    private final long uncorrectedErrors;

    public DownChannel(int number, int id, int frequency, double power, String lockText,
            String modulation, double snr, long correctedErrors, long uncorrectedErrors) {
        super(number, id, frequency, power, lockText);
        this.modulation = modulation;
        this.snr = snr;
        this.correctedErrors = correctedErrors;
        this.uncorrectedErrors = uncorrectedErrors;
    }

    public final String getModulation() {
        return modulation;
    }

    public final double getSnr() {
        return snr;
    }

    public final long getCorrectedErrors() {
        return correctedErrors;
    }

    public final long getUncorrectedErrors() {
        return uncorrectedErrors;
    }

    @Override
    final public String toString() {
        String format = "%2d:%2d | %6s | %6s | %4d | %4.1f |% 5.1f | %,11d | %,11d%n";
        return String.format(format,
                getNumber(),
                getId(),
                getLockText(),
                getModulation(),
                getFrequency() / MEGA,
                getSnr(),
                getPower(),
                getCorrectedErrors(),
                getUncorrectedErrors()
        );
    }

    final public String toCSV() {
        String format = ",%9d,%4.1f,% 5.1f,%9d,%9d";
        return String.format(format,
                getFrequency(),
                getSnr(),
                getPower(),
                getCorrectedErrors(),
                getUncorrectedErrors()
        );
    }

}
