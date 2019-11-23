package modemmon.channel;

abstract class Channel implements Comparable<Channel> {

    private int number;
    private int id;
    private int frequency;
    private double power;
    private String lockText;

    Channel(int number, int id, int frequency, double power, String lockText) {
        this.number = number;
        this.id = id;
        this.frequency = frequency;
        this.power = power;
        this.lockText = lockText;
    }

    public final int getNumber() {
        return number;
    }

    public final int getId() {
        return id;
    }

    public final int getFrequency() {
        return frequency;
    }

    public final double getPower() {
        return power;
    }

    public final String getLockText() {
        return lockText;
    }

    protected final void setNumber(int number) {
        this.number = number;
    }

    protected final void setId(int id) {
        this.id = id;
    }

    protected final void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    protected final void setPower(double power) {
        this.power = power;
    }

    protected final void setLockText(String lockText) {
        this.lockText = lockText;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Channel) {
            Channel c = (Channel) o;
            return frequency == c.frequency;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return frequency;
    }

    @Override
    public int compareTo(Channel c) {
        return frequency - c.frequency;
    }

}
