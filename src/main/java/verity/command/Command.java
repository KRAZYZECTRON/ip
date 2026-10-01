package verity.command;

import java.io.IOException;

import verity.VerityException;
import verity.parser.Parser;
import verity.storage.Storage;
import verity.task.Task;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * A command the user typed, already checked and ready to be carried out.
 *
 * <p>Each kind of command is a subclass that supplies its own
 * {@link #execute(TaskList, Ui, Storage)}. The main loop can then run any
 * command the same way, without knowing which kind it is, and adding a new
 * command means adding a new subclass rather than editing a long chain of
 * if-else statements. This is <i>polymorphism</i>.
 */
public abstract class Command {
    /**
     * Carries out the command and shows its reply.
     *
     * @param tasks Task list the command works on.
     * @param ui User interface to show the reply on.
     * @param storage Storage to save the task list to if the command changes it.
     * @throws VerityException If the command cannot be carried out, or the
     *                         change it made could not be saved.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws VerityException;

    /**
     * Returns true if Verity should stop after this command. Only the command
     * that exits overrides this.
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Returns the task with the given number, checking first that the list
     * has one.
     *
     * @param tasks List to look in.
     * @param taskNumber Number of the task, counting from 1.
     * @return The task with that number.
     * @throws VerityException If no task in the list has that number. The
     *                         message tells the user which numbers they can use.
     */
    protected static Task getTask(TaskList tasks, int taskNumber) throws VerityException {
        if (tasks.contains(taskNumber)) {
            return tasks.get(taskNumber);
        }
        if (tasks.size() == 0) {
            throw new VerityException("There is no task " + taskNumber + ", because your list is empty.\n"
                    + Parser.EXAMPLE_TODO);
        }
        String range = (tasks.size() == 1)
                ? "You have 1 task, numbered 1."
                : "You have " + tasks.size() + " tasks, numbered 1 to " + tasks.size() + ".";
        throw new VerityException("There is no task " + taskNumber + ". " + range + "\n"
                + "Try: list");
    }

    /**
     * Saves the whole task list to disk, called after every change to it.
     *
     * <p>The reply confirming the change is shown before this is called, so
     * if saving fails the user sees both that the change was made and that it
     * will not outlast the session.
     *
     * @param tasks Tasks to save.
     * @param storage Storage to save them to.
     * @throws VerityException If the list could not be written to disk.
     */
    protected static void saveTasks(TaskList tasks, Storage storage) throws VerityException {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            throw new VerityException("I couldn't save your tasks to disk, so this change"
                    + " will be lost when you exit.");
        }
    }
}
