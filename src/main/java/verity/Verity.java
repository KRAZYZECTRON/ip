package verity;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import verity.storage.Storage;
import verity.task.Deadline;
import verity.task.Event;
import verity.task.Task;
import verity.task.TaskList;
import verity.task.Todo;

/**
 * Entry point for the Verity chatbot: a command-line task list that supports
 * adding to-dos, deadlines and events, listing them, marking them done or not
 * done, and deleting them. The list is saved to disk whenever it changes, and
 * loaded again when Verity starts.
 */
public class Verity {
    private static final String LINE = "____________________________________________________________";

    /**
     * Where the task list is saved. The path is relative, so it is found in the
     * folder the program is started from on any computer, and it is built from
     * its parts by {@code Path.of} rather than written as "data/verity.txt", so
     * that the separator between them suits the operating system.
     */
    private static final Storage STORAGE = new Storage(Path.of("data", "verity.txt"));

    /** Block-letter banner spelling out the chatbot's name. */
    private static final String BANNER = """
            ██╗   ██╗███████╗██████╗ ██╗████████╗██╗   ██╗
            ██║   ██║██╔════╝██╔══██╗██║╚══██╔══╝╚██╗ ██╔╝
            ██║   ██║█████╗  ██████╔╝██║   ██║    ╚████╔╝
            ╚██╗ ██╔╝██╔══╝  ██╔══██╗██║   ██║     ╚██╔╝
             ╚████╔╝ ███████╗██║  ██║██║   ██║      ██║
              ╚═══╝  ╚══════╝╚═╝  ╚═╝╚═╝   ╚═╝      ╚═╝
            """;

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

    /**
     * Starts Verity and reads commands from standard input until "bye" is entered.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        useUtf8Output();
        printGreeting();

        TaskList tasks = loadTasks();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {
            String input = scanner.nextLine();
            System.out.println(LINE);
            try {
                // Spaces around the command are the user's slip, not a command
                // of their own, so they are dropped before anything is read.
                isRunning = executeCommand(input.trim(), tasks);
            } catch (VerityException e) {
                printError(e);
            }
            System.out.println(LINE);
        }
        scanner.close();
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
    private static TaskList loadTasks() {
        TaskList tasks;
        try {
            tasks = STORAGE.load();
        } catch (IOException e) {
            System.out.println(" I couldn't read your saved tasks, so I'm starting with an empty list.");
            System.out.println(" They will be replaced the next time your list changes.");
            System.out.println(LINE);
            return new TaskList();
        }

        List<String> skippedLines = STORAGE.getSkippedLines();
        if (!skippedLines.isEmpty()) {
            System.out.println(" Some lines of your saved tasks are not in a form I can read,"
                    + " so I skipped them:");
            for (String skippedLine : skippedLines) {
                System.out.println("   " + skippedLine);
            }
            System.out.println(" They will be left out the next time your list is saved.");
            System.out.println(LINE);
        }
        return tasks;
    }

    /**
     * Prints the explanation carried by a rejected command, laid out like every
     * other reply: one line at a time, each indented by a single space.
     *
     * @param error Exception describing what was wrong with the input.
     */
    private static void printError(VerityException error) {
        for (String line : error.getMessage().split("\n")) {
            System.out.println(" " + line);
        }
    }

    /**
     * Makes {@code System.out} write its text as UTF-8.
     *
     * <p>The banner is drawn with box-drawing characters, which are not part of
     * the character set a Windows console uses by default; without this, the
     * console prints each of them as a question mark. Replacing the standard
     * output stream with one that is told to use UTF-8 keeps the banner
     * readable whichever console the program is started from.
     */
    private static void useUtf8Output() {
        FileOutputStream standardOutput = new FileOutputStream(FileDescriptor.out);
        System.setOut(new PrintStream(standardOutput, true, StandardCharsets.UTF_8));
    }

    /**
     * Carries out one command and prints its reply.
     *
     * @param input Whole line the user typed.
     * @param tasks List of tasks the command works on.
     * @return True if Verity should keep reading commands, false if the user
     *         asked to exit.
     * @throws VerityException If the input is not a command that can be carried
     *                         out.
     */
    private static boolean executeCommand(String input, TaskList tasks) throws VerityException {
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
            System.out.println(" Bye. Hope to see you again soon!");
            return false;
        }

        if (commandWord.equals(COMMAND_LIST)) {
            requireNoArguments(arguments, COMMAND_LIST);
            printTasks(tasks);
        } else if (commandWord.equals(COMMAND_MARK)) {
            markTask(arguments, tasks);
        } else if (commandWord.equals(COMMAND_UNMARK)) {
            unmarkTask(arguments, tasks);
        } else if (commandWord.equals(COMMAND_TODO)) {
            addTodo(arguments, tasks);
        } else if (commandWord.equals(COMMAND_DEADLINE)) {
            addDeadline(arguments, tasks);
        } else if (commandWord.equals(COMMAND_EVENT)) {
            addEvent(arguments, tasks);
        } else if (commandWord.equals(COMMAND_DELETE)) {
            deleteTask(arguments, tasks);
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
     * @param tasks List the number must name a task in.
     * @return The task number the user typed, known to name a task in the list.
     * @throws VerityException If the number is missing, is not a number, or
     *                         does not name a task in the list.
     */
    private static int findTaskNumber(String arguments, String commandWord, TaskList tasks)
            throws VerityException {
        int taskNumber = readTaskNumber(arguments, commandWord);
        if (!tasks.contains(taskNumber)) {
            throw new VerityException(describeMissingTask(taskNumber, tasks));
        }
        return taskNumber;
    }

    /**
     * Returns the explanation shown when a task number does not name a task,
     * telling the user which numbers they can use instead.
     *
     * @param taskNumber Number the user asked for.
     * @param tasks List the number was looked up in.
     */
    private static String describeMissingTask(int taskNumber, TaskList tasks) {
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
     * Prints the banner and welcome message shown when Verity starts.
     */
    private static void printGreeting() {
        System.out.println(BANNER);
        System.out.println("Hello! I'm Verity");
        System.out.println("What can I do for you?");
        System.out.println(LINE);
    }

    /**
     * Prints every task in the list, numbered from 1.
     *
     * @param tasks List of tasks to print.
     */
    private static void printTasks(TaskList tasks) {
        System.out.println(" Here are the tasks in your list:");
        for (int i = 1; i <= tasks.size(); i++) {
            System.out.println(" " + i + "." + tasks.get(i));
        }
    }

    /**
     * Marks the task named by a "mark &lt;task number&gt;" command as done.
     *
     * @param arguments Task number the user typed.
     * @param tasks List holding the task to mark.
     * @throws VerityException If the task number is missing, unreadable, or
     *                         does not name a task in the list, or the
     *                         change could not be saved.
     */
    private static void markTask(String arguments, TaskList tasks) throws VerityException {
        Task task = tasks.get(findTaskNumber(arguments, COMMAND_MARK, tasks));
        task.markAsDone();
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
        saveTasks(tasks);
    }

    /**
     * Marks the task named by an "unmark &lt;task number&gt;" command as not done.
     *
     * @param arguments Task number the user typed.
     * @param tasks List holding the task to unmark.
     * @throws VerityException If the task number is missing, unreadable, or
     *                         does not name a task in the list, or the
     *                         change could not be saved.
     */
    private static void unmarkTask(String arguments, TaskList tasks) throws VerityException {
        Task task = tasks.get(findTaskNumber(arguments, COMMAND_UNMARK, tasks));
        task.markAsNotDone();
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
        saveTasks(tasks);
    }

    /**
     * Deletes the task named by a "delete &lt;task number&gt;" command. The
     * tasks after it are renumbered to close the gap.
     *
     * @param arguments Task number the user typed.
     * @param tasks List holding the task to delete.
     * @throws VerityException If the task number is missing, unreadable, or
     *                         does not name a task in the list, or the
     *                         change could not be saved.
     */
    private static void deleteTask(String arguments, TaskList tasks) throws VerityException {
        Task task = tasks.remove(findTaskNumber(arguments, COMMAND_DELETE, tasks));
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
        saveTasks(tasks);
    }

    /**
     * Adds the to-do described by a "todo &lt;description&gt;" command.
     *
     * @param arguments Description the user typed.
     * @param tasks List to add the task to.
     * @throws VerityException If no description was given, the description
     *                         contains the field separator, or the task could
     *                         not be saved.
     */
    private static void addTodo(String arguments, TaskList tasks) throws VerityException {
        if (arguments.isEmpty()) {
            throw new VerityException("A todo needs a description saying what to do.\n"
                    + EXAMPLE_TODO);
        }
        rejectFieldSeparator(arguments, EXAMPLE_TODO);
        addTask(new Todo(arguments), tasks);
    }

    /**
     * Adds the deadline described by a
     * "deadline &lt;description&gt; /by &lt;when&gt;" command.
     *
     * @param arguments Description and due date/time the user typed.
     * @param tasks List to add the task to.
     * @throws VerityException If the description, the /by keyword or the due
     *                         date is missing, the arguments contain the field
     *                         separator, or the task could not be saved.
     */
    private static void addDeadline(String arguments, TaskList tasks) throws VerityException {
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
        addTask(new Deadline(description, by), tasks);
    }

    /**
     * Adds the event described by an
     * "event &lt;description&gt; /from &lt;start&gt; /to &lt;end&gt;" command.
     *
     * @param arguments Description, start and end date/time the user typed.
     * @param tasks List to add the task to.
     * @throws VerityException If the description, either keyword, the start or
     *                         the end is missing, the two keywords are the
     *                         wrong way round, the arguments contain the field
     *                         separator, or the task could not be saved.
     */
    private static void addEvent(String arguments, TaskList tasks) throws VerityException {
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
        addTask(new Event(description, from, to), tasks);
    }

    /**
     * Adds a task to the list and prints the confirmation shown to the user.
     *
     * @param task Task to add.
     * @param tasks List to add the task to.
     * @throws VerityException If the task was added but the list could not be
     *                         saved.
     */
    private static void addTask(Task task, TaskList tasks) throws VerityException {
        tasks.add(task);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
        saveTasks(tasks);
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
     * @param tasks Tasks to save.
     * @throws VerityException If the list could not be written to disk.
     */
    private static void saveTasks(TaskList tasks) throws VerityException {
        try {
            STORAGE.save(tasks);
        } catch (IOException e) {
            throw new VerityException("I couldn't save your tasks to disk, so this change"
                    + " will be lost when you exit.");
        }
    }
}
