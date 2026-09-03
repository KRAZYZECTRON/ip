/**
 * A task that must be done before a given date/time, for example
 * "return book (by: Sunday)".
 */
public class Deadline extends Task {
    /** When the task is due, kept as the text the user typed. */
    protected String by;

    /**
     * Creates a deadline with the given description and due date/time.
     *
     * @param description Text describing the task.
     * @param by When the task is due, as typed by the user.
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
