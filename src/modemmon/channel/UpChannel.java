package modemmon.channel;

import static modemmon.modem.Modem.KILO;
import static modemmon.modem.Modem.MEGA;

public class UpChannel extends Channel {

//    public static final int MAX_CHANNEL_WIDTH = 6_400_000;
//    public static final int MIN_FREQUENCY = 5 * MEGA;
//    public static final int MAX_FREQUENCY = 42 * MEGA;
    public static final double POWER_MIN = 28;
    public static final double POWER_MAX = 50;

    private final String type;
    private final int symbolRate;

    public UpChannel(int number, int id, int frequency, double power, String lockText, String type, int symbolRate) {
        super(number, id, frequency, power, lockText);
        this.type = type;
        this.symbolRate = symbolRate;
    }

    public final String getType() {
        return type;
    }

    public final int getSymbolRate() {
        return symbolRate;
    }

    @Override
    public final String toString() {
        String format = " %1d:%1d  | %6s | %6s | %4.1f | %4d |% 5.1f%n";
        return String.format(format,
                getNumber(),
                getId(),
                getLockText(),
                getType(),
                getFrequency() / MEGA,
                getSymbolRate() / KILO,
                getPower()
        );
    }

    public final String toCSV() {
        String format = ",%9d,% 5.1f";
        return String.format(format, getFrequency(), getPower());
    }

}
