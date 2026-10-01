package verity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import verity.storage.Storage;
import verity.task.Deadline;
import verity.task.Event;
import verity.task.Task;
import verity.task.TaskList;
import verity.task.Todo;
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

    // The word the user types to choose a command.
    private static final String COMMAND_BYE = "bye";
    private static final String COMMAND_LIST = "list";
    private static final String COMMAND_MARK = "mark";
    private static final String COMMAND_UNMARK = "unmark";
    private static final String COMMAND_TODO = "todo";
    private static final String COMMAND_DEADLINE = "deadline";
    private static final String COMMAND_EVENT = "event";
    private static final String COMMAND_DELETE = "delete";

    // The keyword that separates one argument of a command from the next. The
    // surrounding spaces are deliberately not part of the keyword: a command
    // that stops right after it, such as "deadline homework /by", can then be
    // reported as a missing due date rather than as a missing /by.
    private static final String KEYWORD_BY = "/by";
    private static final String KEYWORD_FROM = "/from";
    private static final String KEYWORD_TO = "/to";

    /** Reminder of the commands on offer, added to messages that reject input. */
    private static final String COMMAND_HINT =
            "Try: todo, deadline, event, list, mark, unmark, delete, bye.";

    // A correct example of each command that takes arguments, shown alongside
    // the complaint when the user's attempt at that command could not be read.
    private static final String EXAMPLE_TODO = "Try: todo borrow book";
    private static final String EXAMPLE_DEADLINE = "Try: deadline return book /by Sunday";
    private static final String EXAMPLE_EVENT = "Try: event project meeting /from Mon 2pm /to 4pm";

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
        if (input.isEmpty()) {
            throw new VerityException("You pressed enter without typing a command.\n" + COMMAND_HINT);
        }

        // Everything up to the first space chooses the command; whatever
        // follows is that command's arguments. Splitting here rather than
        // inside each command means a command word typed on its own reaches
        // its own method, which can then explain what it is missing.
        int firstSpace = input.indexOf(' ');
        String commandWord = (firstSpace < 0) ? input : input.substring(0, firstSpace);
        String arguments = (firstSpace < 0) ? "" : input.substring(firstSpace + 1).trim();

        if (commandWord.equals(COMMAND_BYE)) {
            requireNoArguments(arguments, COMMAND_BYE);
            ui.showGoodbye();
            return false;
        }

        if (commandWord.equals(COMMAND_LIST)) {
            requireNoArguments(arguments, COMMAND_LIST);
            ui.showTaskList(tasks);
        } else if (commandWord.equals(COMMAND_MARK)) {
            markTask(arguments);
        } else if (commandWord.equals(COMMAND_UNMARK)) {
            unmarkTask(arguments);
        } else if (commandWord.equals(COMMAND_TODO)) {
            addTodo(arguments);
        } else if (commandWord.equals(COMMAND_DEADLINE)) {
            addDeadline(arguments);
        } else if (commandWord.equals(COMMAND_EVENT)) {
            addEvent(arguments);
        } else if (commandWord.equals(COMMAND_DELETE)) {
            deleteTask(arguments);
        } else {
            throw new VerityException("Sorry, I don't know what that means.\n" + COMMAND_HINT);
        }
        return true;
    }

    /**
     * Checks that a command which takes no arguments was given none.
     *
     * @param arguments Text the user typed after the command word.
     * @param commandWord Command that was typed, named in the message shown.
     * @throws VerityException If anything was typed after the command word.
     */
    private static void requireNoArguments(String arguments, String commandWord) throws VerityException {
        if (!arguments.isEmpty()) {
            throw new VerityException("The " + commandWord + " command takes nothing after it,"
                    + " but you added \"" + arguments + "\".\n"
                    + "Try: " + commandWord);
        }
    }

    /**
     * Reads the task number a "mark", "unmark" or "delete" command was given.
     *
     * @param arguments Text the user typed after the command word.
     * @param commandWord Command the number belongs to, named in the message
     *                    shown if it cannot be read.
     * @return The task number the user typed.
     * @throws VerityException If no number was given, or what was given is not
     *                         a whole number.
     */
    private static int readTaskNumber(String arguments, String commandWord) throws VerityException {
        if (arguments.isEmpty()) {
            throw new VerityException("Tell me which task to " + commandWord + ", by its number.\n"
                    + "Try: " + commandWord + " 1");
        }
        try {
            return Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            throw new VerityException("\"" + arguments + "\" is not a task number.\n"
                    + "Try: " + commandWord + " 1");
        }
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
        int taskNumber = readTaskNumber(arguments, commandWord);
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
                    + EXAMPLE_TODO;
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
        Task task = tasks.get(findTaskNumber(arguments, COMMAND_MARK));
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
        Task task = tasks.get(findTaskNumber(arguments, COMMAND_UNMARK));
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
        Task task = tasks.remove(findTaskNumber(arguments, COMMAND_DELETE));
        ui.showTaskDeleted(task, tasks.size());
        saveTasks();
    }

    /**
     * Adds the to-do described by a "todo &lt;description&gt;" command.
     *
     * @param arguments Description the user typed.
     * @throws VerityException If no description was given, the description
     *                         contains the field separator, or the task could
     *                         not be saved.
     */
    private void addTodo(String arguments) throws VerityException {
        if (arguments.isEmpty()) {
            throw new VerityException("A todo needs a description saying what to do.\n"
                    + EXAMPLE_TODO);
        }
        rejectFieldSeparator(arguments, EXAMPLE_TODO);
        addTask(new Todo(arguments));
    }

    /**
     * Adds the deadline described by a
     * "deadline &lt;description&gt; /by &lt;when&gt;" command.
     *
     * @param arguments Description and due date/time the user typed.
     * @throws VerityException If the description, the /by keyword or the due
     *                         date is missing, the arguments contain the field
     *                         separator, or the task could not be saved.
     */
    private void addDeadline(String arguments) throws VerityException {
        if (arguments.isEmpty()) {
            throw new VerityException("A deadline needs a description and a due date.\n"
                    + EXAMPLE_DEADLINE);
        }
        rejectFieldSeparator(arguments, EXAMPLE_DEADLINE);

        int byIndex = arguments.indexOf(KEYWORD_BY);
        if (byIndex < 0) {
            throw new VerityException("I can't tell when \"" + arguments + "\" is due."
                    + " Mark the due date with /by.\n"
                    + EXAMPLE_DEADLINE);
        }

        String description = arguments.substring(0, byIndex).trim();
        String by = arguments.substring(byIndex + KEYWORD_BY.length()).trim();
        if (description.isEmpty()) {
            throw new VerityException("A deadline needs a description before the /by.\n"
                    + EXAMPLE_DEADLINE);
        }
        if (by.isEmpty()) {
            throw new VerityException("A deadline needs a due date after the /by.\n"
                    + EXAMPLE_DEADLINE);
        }
        addTask(new Deadline(description, by));
    }

    /**
     * Adds the event described by an
     * "event &lt;description&gt; /from &lt;start&gt; /to &lt;end&gt;" command.
     *
     * @param arguments Description, start and end date/time the user typed.
     * @throws VerityException If the description, either keyword, the start or
     *                         the end is missing, the two keywords are the
     *                         wrong way round, the arguments contain the field
     *                         separator, or the task could not be saved.
     */
    private void addEvent(String arguments) throws VerityException {
        if (arguments.isEmpty()) {
            throw new VerityException("An event needs a description, a start and an end.\n"
                    + EXAMPLE_EVENT);
        }
        rejectFieldSeparator(arguments, EXAMPLE_EVENT);

        int fromIndex = arguments.indexOf(KEYWORD_FROM);
        if (fromIndex < 0) {
            throw new VerityException("I can't tell when \"" + arguments + "\" starts."
                    + " Mark the start with /from.\n"
                    + EXAMPLE_EVENT);
        }

        // The end is looked for only after the start, so that "/to" typed
        // before "/from" is reported instead of being read the wrong way round.
        int toIndex = arguments.indexOf(KEYWORD_TO, fromIndex);
        if (toIndex < 0) {
            throw new VerityException("An event needs an end, marked with /to after the /from.\n"
                    + EXAMPLE_EVENT);
        }

        String description = arguments.substring(0, fromIndex).trim();
        String from = arguments.substring(fromIndex + KEYWORD_FROM.length(), toIndex).trim();
        String to = arguments.substring(toIndex + KEYWORD_TO.length()).trim();
        if (description.isEmpty()) {
            throw new VerityException("An event needs a description before the /from.\n"
                    + EXAMPLE_EVENT);
        }
        if (from.isEmpty()) {
            throw new VerityException("An event needs a start after the /from.\n"
                    + EXAMPLE_EVENT);
        }
        if (to.isEmpty()) {
            throw new VerityException("An event needs an end after the /to.\n"
                    + EXAMPLE_EVENT);
        }
        addTask(new Event(description, from, to));
    }

    /**
     * Adds a task to the list and prints the confirmation shown to the user.
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
     * Checks that the arguments of a command that adds a task do not contain
     * the character the data file uses to separate fields. A description
     * holding it would be split in the wrong places when the file is loaded.
     *
     * @param arguments Text the user typed after the command word.
     * @param example Correct example of the command, shown if it is refused.
     * @throws VerityException If the arguments contain the character.
     */
    private static void rejectFieldSeparator(String arguments, String example) throws VerityException {
        if (arguments.contains(Storage.FORBIDDEN_CHARACTER)) {
            throw new VerityException("Sorry, I can't store the " + Storage.FORBIDDEN_CHARACTER
                    + " character, because I use it to separate the parts of a saved task.\n"
                    + example);
        }
    }

    /**
     * Saves the whole task list to disk, called after every change to it.
     *
     * <p>The reply confirming the change is printed before this is called, so
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
