/**
 * A task in the task list: a description plus whether it has been done.
 *
 * <p>This class holds only what every task type has in common. Each concrete
 * type ({@link Todo}, {@link Deadline}, {@link Event}) supplies its own type
 * icon and adds its own date/time details. Declaring the class {@code abstract}
 * means a plain "Task" can never be created on its own, which is what we want:
 * every task in the list is one of the three types.
 */
public abstract class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a task with the given description. The task starts out not done.
     *
     * @param description Text describing the task.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /**
     * Returns the one-letter icon that identifies this task's type,
     * for example "T" for a to-do.
     */
    public abstract String getTypeIcon();

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
        return "[" + getTypeIcon() + "][" + getStatusIcon() + "] " + description;
    }
}
