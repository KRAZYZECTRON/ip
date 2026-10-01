package verity.command;

import verity.VerityException;
import verity.storage.Storage;
import verity.task.Task;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Deletes a task from the list. The tasks after it are renumbered to close
 * the gap.
 */
public class DeleteCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a command that deletes the task with the given number.
     *
     * @param taskNumber Number of the task to delete, counting from 1.
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws VerityException {
        getTask(tasks, taskNumber); // checks that the task exists
        Task task = tasks.remove(taskNumber);
        ui.showTaskDeleted(task, tasks.size());
        saveTasks(tasks, storage);
    }
}
