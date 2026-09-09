package verity.task;

/**
 * A task with no date/time attached to it, for example "borrow book".
 */
public class Todo extends Task {
    /**
     * Creates a to-do with the given description.
     *
     * @param description Text describing the task.
     */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String getTypeIcon() {
        return "T";
    }
}
