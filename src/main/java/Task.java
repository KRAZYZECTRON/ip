/**
 * A single task in the task list: a description, whether it is done, and the
 * date/time details that its type needs.
 *
 * <p>All three task types (to-do, deadline, event) are represented by this one
 * class; the fields that do not apply to a given type are left unused.
 */
public class Task {
    /** Type icon of a task that has no date/time attached. */
    public static final char TYPE_TODO = 'T';
    /** Type icon of a task that must be done before one date/time. */
    public static final char TYPE_DEADLINE = 'D';
    /** Type icon of a task that spans a start and an end date/time. */
    public static final char TYPE_EVENT = 'E';

    protected String description;
    protected boolean isDone;
    protected char type;
    /** When a deadline is due; unused by the other types. */
    protected String by;
    /** When an event starts; unused by the other types. */
    protected String from;
    /** When an event ends; unused by the other types. */
    protected String to;

    /**
     * Creates a task of the given type. The task starts out not done.
     * Use the {@code create...} factory methods instead of calling this directly,
     * as they make clear which arguments each type actually needs.
     *
     * @param type Type icon of the task.
     * @param description Text describing the task.
     * @param by When a deadline is due, or null.
     * @param from When an event starts, or null.
     * @param to When an event ends, or null.
     */
    private Task(char type, String description, String by, String from, String to) {
        this.type = type;
        this.description = description;
        this.isDone = false;
        this.by = by;
        this.from = from;
        this.to = to;
    }

    /**
     * Returns a to-do: a task with no date/time attached.
     *
     * @param description Text describing the task.
     */
    public static Task createTodo(String description) {
        return new Task(TYPE_TODO, description, null, null, null);
    }

    /**
     * Returns a deadline: a task that must be done before a date/time.
     *
     * @param description Text describing the task.
     * @param by When the task is due, as typed by the user.
     */
    public static Task createDeadline(String description, String by) {
        return new Task(TYPE_DEADLINE, description, by, null, null);
    }

    /**
     * Returns an event: a task that runs from one date/time to another.
     *
     * @param description Text describing the task.
     * @param from When the event starts, as typed by the user.
     * @param to When the event ends, as typed by the user.
     */
    public static Task createEvent(String description, String from, String to) {
        return new Task(TYPE_EVENT, description, null, from, to);
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    @Override
    public String toString() {
        String details = "";
        if (type == TYPE_DEADLINE) {
            details = " (by: " + by + ")";
        } else if (type == TYPE_EVENT) {
            details = " (from: " + from + " to: " + to + ")";
        }
        return "[" + type + "][" + getStatusIcon() + "] " + description + details;
    }
}
