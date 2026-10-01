package verity.command;

import verity.VerityException;
import verity.storage.Storage;
import verity.task.Task;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Marks a task as done.
 */
public class MarkCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a command that marks the task with the given number as done.
     *
     * @param taskNumber Number of the task to mark, counting from 1.
     */
    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws VerityException {
        Task task = getTask(tasks, taskNumber);
        task.markAsDone();
        ui.showTaskMarked(task);
        saveTasks(tasks, storage);
    }
}
