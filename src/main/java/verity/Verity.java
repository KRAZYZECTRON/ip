package verity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import verity.parser.Parser;
import verity.storage.Storage;
import verity.task.Task;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Entry point for the Verity chatbot: a command-line task list that supports
 * adding to-dos, deadlines and events, listing them, marking them done or not
 * done, and deleting them. The list is saved to disk whenever it changes, and
 * loaded again when Verity starts.
 */
public class Verity {
    /**
     * Where the task list is saved. The path is relative, so it is found in the
     * folder the program is started from on any computer, and it is built from
     * its parts by {@code Path.of} rather than written as "data/verity.txt", so
     * that the separator between them suits the operating system.
     */
    private static final Path DATA_FILE = Path.of("data", "verity.txt");

    /** Reads the user's commands and shows Verity's replies. */
    private final Ui ui;

    /** Reads and writes the task list on disk. */
    private final Storage storage;

    /** Tasks the user is keeping track of; every command works on this list. */
    private TaskList tasks;

    /**
     * Creates a Verity chatbot that keeps its tasks in the given file.
     *
     * @param filePath File the tasks are loaded from and saved to.
     */
    public Verity(Path filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
    }

    /**
     * Starts Verity and reads commands from standard input until "bye" is entered.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        new Verity(DATA_FILE).run();
    }

    /**
     * Greets the user, loads the saved tasks, and then carries out commands
     * from standard input until the user exits.
     */
    public void run() {
        ui.showWelcome();
        tasks = loadTasks();
        boolean isRunning = true;
        while (isRunning) {
            String input = ui.readCommand();
            ui.showLine();
            try {
                isRunning = executeCommand(input);
            } catch (VerityException e) {
                ui.showError(e.getMessage());
            }
            ui.showLine();
        }
    }

    /**
     * Loads the tasks saved by an earlier session, telling the user about
     * anything in the data file that could not be read.
     *
     * <p>A data file that cannot be read never stops Verity from starting: the
     * user gets whatever could be read, or an empty list, plus an explanation.
     *
     * @return The saved tasks, or an empty list if there are none or the data
     *         file cannot be read.
     */
    private TaskList loadTasks() {
        TaskList loadedTasks;
        try {
            loadedTasks = storage.load();
        } catch (IOException e) {
            ui.showLoadingError();
            return new TaskList();
        }

        List<String> skippedLines = storage.getSkippedLines();
        if (!skippedLines.isEmpty()) {
            ui.showSkippedLines(skippedLines);
        }
        return loadedTasks;
    }

    /**
     * Carries out one command and prints its reply.
     *
     * @param input Whole line the user typed.
     * @return True if Verity should keep reading commands, false if the user
     *         asked to exit.
     * @throws VerityException If the input is not a command that can be carried
     *                         out.
     */
    private boolean executeCommand(String input) throws VerityException {
        String commandWord = Parser.getCommandWord(input);
        String arguments = Parser.getArguments(input);

        if (commandWord.equals(Parser.COMMAND_BYE)) {
            Parser.requireNoArguments(arguments, Parser.COMMAND_BYE);
            ui.showGoodbye();
            return false;
        }

        if (commandWord.equals(Parser.COMMAND_LIST)) {
            Parser.requireNoArguments(arguments, Parser.COMMAND_LIST);
            ui.showTaskList(tasks);
        } else if (commandWord.equals(Parser.COMMAND_MARK)) {
            markTask(arguments);
        } else if (commandWord.equals(Parser.COMMAND_UNMARK)) {
            unmarkTask(arguments);
        } else if (commandWord.equals(Parser.COMMAND_TODO)) {
            addTask(Parser.parseTodo(arguments));
        } else if (commandWord.equals(Parser.COMMAND_DEADLINE)) {
            addTask(Parser.parseDeadline(arguments));
        } else if (commandWord.equals(Parser.COMMAND_EVENT)) {
            addTask(Parser.parseEvent(arguments));
        } else if (commandWord.equals(Parser.COMMAND_DELETE)) {
            deleteTask(arguments);
        } else {
            throw new VerityException("Sorry, I don't know what that means.\n" + Parser.COMMAND_HINT);
        }
        return true;
    }

    /**
     * Returns the number of an existing task that a "mark", "unmark" or
     * "delete" command names.
     *
     * @param arguments Text the user typed after the command word.
     * @param commandWord Command the number belongs to, named in the message
     *                    shown if the number cannot be read.
     * @return The task number the user typed, known to name a task in the list.
     * @throws VerityException If the number is missing, is not a number, or
     *                         does not name a task in the list.
     */
    private int findTaskNumber(String arguments, String commandWord) throws VerityException {
        int taskNumber = Parser.parseTaskNumber(arguments, commandWord);
        if (!tasks.contains(taskNumber)) {
            throw new VerityException(describeMissingTask(taskNumber));
        }
        return taskNumber;
    }

    /**
     * Returns the explanation shown when a task number does not name a task,
     * telling the user which numbers they can use instead.
     *
     * @param taskNumber Number the user asked for.
     */
    private String describeMissingTask(int taskNumber) {
        if (tasks.size() == 0) {
            return "There is no task " + taskNumber + ", because your list is empty.\n"
                    + Parser.EXAMPLE_TODO;
        }
        String range = (tasks.size() == 1)
                ? "You have 1 task, numbered 1."
                : "You have " + tasks.size() + " tasks, numbered 1 to " + tasks.size() + ".";
        return "There is no task " + taskNumber + ". " + range + "\n"
                + "Try: list";
    }

    /**
     * Marks the task named by a "mark &lt;task number&gt;" command as done.
     *
     * @param arguments Task number the user typed.
     * @throws VerityException If the task number is missing, unreadable, or
     *                         does not name a task in the list, or the
     *                         change could not be saved.
     */
    private void markTask(String arguments) throws VerityException {
        Task task = tasks.get(findTaskNumber(arguments, Parser.COMMAND_MARK));
        task.markAsDone();
        ui.showTaskMarked(task);
        saveTasks();
    }

    /**
     * Marks the task named by an "unmark &lt;task number&gt;" command as not done.
     *
     * @param arguments Task number the user typed.
     * @throws VerityException If the task number is missing, unreadable, or
     *                         does not name a task in the list, or the
     *                         change could not be saved.
     */
    private void unmarkTask(String arguments) throws VerityException {
        Task task = tasks.get(findTaskNumber(arguments, Parser.COMMAND_UNMARK));
        task.markAsNotDone();
        ui.showTaskUnmarked(task);
        saveTasks();
    }

    /**
     * Deletes the task named by a "delete &lt;task number&gt;" command. The
     * tasks after it are renumbered to close the gap.
     *
     * @param arguments Task number the user typed.
     * @throws VerityException If the task number is missing, unreadable, or
     *                         does not name a task in the list, or the
     *                         change could not be saved.
     */
    private void deleteTask(String arguments) throws VerityException {
        Task task = tasks.remove(findTaskNumber(arguments, Parser.COMMAND_DELETE));
        ui.showTaskDeleted(task, tasks.size());
        saveTasks();
    }

    /**
     * Adds a task to the list and shows the confirmation to the user.
     *
     * @param task Task to add.
     * @throws VerityException If the task was added but the list could not be
     *                         saved.
     */
    private void addTask(Task task) throws VerityException {
        tasks.add(task);
        ui.showTaskAdded(task, tasks.size());
        saveTasks();
    }

    /**
     * Saves the whole task list to disk, called after every change to it.
     *
     * <p>The reply confirming the change is shown before this is called, so
     * if saving fails the user sees both that the change was made and that it
     * will not outlast the session.
     *
     * @throws VerityException If the list could not be written to disk.
     */
    private void saveTasks() throws VerityException {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            throw new VerityException("I couldn't save your tasks to disk, so this change"
                    + " will be lost when you exit.");
        }
    }
}
