package verity.command;

import verity.VerityException;
import verity.storage.Storage;
import verity.task.Task;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Adds a task to the list. The same command serves "todo", "deadline" and
 * "event", since the parser has already created the right kind of task.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Creates a command that adds the given task.
     *
     * @param task Task to add.
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws VerityException {
        tasks.add(task);
        ui.showTaskAdded(task, tasks.size());
        saveTasks(tasks, storage);
    }
}
