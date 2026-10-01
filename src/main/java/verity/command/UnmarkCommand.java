package verity.command;

import verity.VerityException;
import verity.storage.Storage;
import verity.task.Task;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Marks a task as not done.
 */
public class UnmarkCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a command that marks the task with the given number as not done.
     *
     * @param taskNumber Number of the task to unmark, counting from 1.
     */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws VerityException {
        Task task = getTask(tasks, taskNumber);
        task.markAsNotDone();
        ui.showTaskUnmarked(task);
        saveTasks(tasks, storage);
    }
}
