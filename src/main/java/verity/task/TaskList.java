package verity.task;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of tasks the user is keeping track of.
 *
 * <p>Hides how the tasks are stored, so the rest of the program can work in
 * terms of "add a task" and "give me task 3" instead of list indexes that start
 * at 0. Task numbers used here are the ones the user sees, starting at 1.
 *
 * <p>The tasks are kept in an {@link ArrayList}, which grows by itself as tasks
 * are added, so there is no fixed limit on how many the list can hold.
 */
public class TaskList {
    private final ArrayList<Task> tasks = new ArrayList<>();

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task at the given position.
     *
     * @param taskNumber Position of the task as shown by the "list" command,
     *                   counting from 1.
     */
    public Task get(int taskNumber) {
        return tasks.get(taskNumber - 1);
    }

    /**
     * Removes the task at the given position and returns it. The tasks after
     * it move up by one, so their numbers each drop by one.
     *
     * @param taskNumber Position of the task as shown by the "list" command,
     *                   counting from 1.
     * @return The task that was removed.
     */
    public Task remove(int taskNumber) {
        return tasks.remove(taskNumber - 1);
    }

    /**
     * Returns true if the given task number names a task that is in the list.
     *
     * @param taskNumber Position of the task as shown by the "list" command,
     *                   counting from 1.
     */
    public boolean contains(int taskNumber) {
        return taskNumber >= 1 && taskNumber <= tasks.size();
    }

    /**
     * Returns the numbers of the tasks whose description contains the given
     * keyword, in list order. Numbers are returned rather than the tasks
     * themselves so that each match can be shown with the number the user
     * needs for mark, unmark or delete.
     *
     * @param keyword Text to look for, ignoring upper/lower case.
     * @return Task numbers of the matches, counting from 1; empty if none match.
     */
    public List<Integer> find(String keyword) {
        List<Integer> taskNumbers = new ArrayList<>();
        for (int i = 1; i <= tasks.size(); i++) {
            if (get(i).hasKeyword(keyword)) {
                taskNumbers.add(i);
            }
        }
        return taskNumbers;
    }

    /**
     * Returns how many tasks the list holds.
     */
    public int size() {
        return tasks.size();
    }
}
