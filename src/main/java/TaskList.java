/**
 * The list of tasks the user is keeping track of.
 *
 * <p>Hides how the tasks are stored, so the rest of the program can work in
 * terms of "add a task" and "give me task 3" instead of array slots and a
 * counter. Task numbers used here are the ones the user sees, starting at 1.
 */
public class TaskList {
    // Requirement caps storage at 100 tasks, so a fixed-size array is enough.
    private static final int MAX_TASKS = 100;

    private final Task[] tasks = new Task[MAX_TASKS];
    private int taskCount = 0;

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        tasks[taskCount] = task;
        taskCount++;
    }

    /**
     * Returns the task at the given position.
     *
     * @param taskNumber Position of the task as shown by the "list" command,
     *                   counting from 1.
     */
    public Task get(int taskNumber) {
        return tasks[taskNumber - 1];
    }

    /**
     * Returns how many tasks the list holds.
     */
    public int size() {
        return taskCount;
    }
}
