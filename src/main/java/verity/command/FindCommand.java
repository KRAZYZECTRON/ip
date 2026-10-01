package verity.command;

import java.util.List;

import verity.storage.Storage;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Shows the tasks whose description contains a keyword.
 */
public class FindCommand extends Command {
    private final String keyword;

    /**
     * Creates a command that finds the tasks containing the given keyword.
     *
     * @param keyword Text to look for in the task descriptions.
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        List<Integer> taskNumbers = tasks.find(keyword);
        ui.showMatchingTasks(tasks, taskNumbers, keyword);
    }
}
