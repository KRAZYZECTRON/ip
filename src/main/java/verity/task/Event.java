package verity.task;

/**
 * A task that runs from one date/time to another, for example
 * "project meeting (from: Mon 2pm to: 4pm)".
 */
public class Event extends Task {
    /** When the event starts, kept as the text the user typed. */
    protected String from;
    /** When the event ends, kept as the text the user typed. */
    protected String to;

    /**
     * Creates an event with the given description and start/end date/time.
     *
     * @param description Text describing the task.
     * @param from When the event starts, as typed by the user.
     * @param to When the event ends, as typed by the user.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String getTypeIcon() {
        return "E";
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
