package verity.task;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Returns the pieces of information needed to recreate this task when it
     * is loaded from disk, in the order they are saved: the type icon, "1" if
     * done or "0" if not, the description, and then any date/time details the
     * task type adds.
     *
     * <p>Subclasses with extra details override this method, call it through
     * {@code super}, and append their own fields to the list it returns.
     */
    public List<String> getSaveFields() {
        List<String> fields = new ArrayList<>();
        fields.add(getTypeIcon());
        fields.add(isDone ? "1" : "0");
        fields.add(description);
        return fields;
    }

    @Override
    public String toString() {
        return "[" + getTypeIcon() + "][" + getStatusIcon() + "] " + description;
    }
}
