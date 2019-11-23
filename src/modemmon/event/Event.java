package modemmon.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event implements Comparable<Event> {

    private final LocalDateTime time;
    private final boolean clockSet;
    private final String priorityName;
    private final int priorityRank;
    private final String fullDescription;
    private final String briefDescription;

    public Event(LocalDateTime time, boolean clockSet,
            String priorityName, int priorityRank,
            String fullDescription, String briefDescription) {
        this.time = time;
        this.clockSet = clockSet;
        this.priorityName = priorityName;
        this.priorityRank = priorityRank;
        this.fullDescription = fullDescription;
        this.briefDescription = briefDescription;
    }

    public final LocalDateTime getTime() {
        return time;
    }

    public final boolean isClockSet() {
        return clockSet;
    }

    public final String getPriorityName() {
        return priorityName;
    }

    public final int getPriorityRank() {
        return priorityRank;
    }

    public final String getFullDescription() {
        return fullDescription;
    }

    public final String getBriefDescription() {
        return briefDescription;
    }

    @Override
    public final boolean equals(Object object) {
        if (object instanceof Event) {
            Event otherEvent = (Event) object;
            if (time.equals(otherEvent.time) && (fullDescription.equals(otherEvent.fullDescription))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return (time.getYear() * 1_000) + time.getDayOfYear();
    }

    @Override
    public final int compareTo(Event event) {
        return time.compareTo(event.time);
    }

    @Override
    public final String toString() {
        String format = "%s| %1d %-8s | %s%n";
        String isClockSet = (isClockSet()) ? " " : "*";
        String timeText = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).concat(isClockSet);
        return String.format(format, timeText, priorityRank, priorityName, briefDescription);
    }

}
