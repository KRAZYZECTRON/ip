import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Entry point for the Verity chatbot: a command-line task list that supports
 * adding to-dos, deadlines and events, listing them, and marking them
 * done or not done.
 */
public class Verity {
    private static final String LINE = "____________________________________________________________";

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

    // The keyword that separates one argument of a command from the next.
    private static final String KEYWORD_BY = " /by ";
    private static final String KEYWORD_FROM = " /from ";
    private static final String KEYWORD_TO = " /to ";

    /**
     * Starts Verity and reads commands from standard input until "bye" is entered.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        useUtf8Output();
        printGreeting();

        TaskList tasks = new TaskList();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {
            String input = scanner.nextLine();
            System.out.println(LINE);
            isRunning = executeCommand(input, tasks);
            System.out.println(LINE);
        }
        scanner.close();
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
     */
    private static boolean executeCommand(String input, TaskList tasks) {
        if (input.equals(COMMAND_BYE)) {
            System.out.println(" Bye. Hope to see you again soon!");
            return false;
        }

        if (input.equals(COMMAND_LIST)) {
            printTasks(tasks);
        } else if (hasCommand(input, COMMAND_MARK)) {
            markTask(getArguments(input, COMMAND_MARK), tasks);
        } else if (hasCommand(input, COMMAND_UNMARK)) {
            unmarkTask(getArguments(input, COMMAND_UNMARK), tasks);
        } else if (hasCommand(input, COMMAND_TODO)) {
            addTodo(getArguments(input, COMMAND_TODO), tasks);
        } else if (hasCommand(input, COMMAND_DEADLINE)) {
            addDeadline(getArguments(input, COMMAND_DEADLINE), tasks);
        } else if (hasCommand(input, COMMAND_EVENT)) {
            addEvent(getArguments(input, COMMAND_EVENT), tasks);
        } else {
            printUnknownCommand();
        }
        return true;
    }

    /**
     * Returns true if the input is the given command word followed by arguments.
     *
     * @param input Whole line the user typed.
     * @param commandWord Command word to look for, such as "todo".
     */
    private static boolean hasCommand(String input, String commandWord) {
        return input.startsWith(commandWord + " ");
    }

    /**
     * Returns the part of the input that follows the command word.
     *
     * @param input Whole line the user typed.
     * @param commandWord Command word the input starts with.
     */
    private static String getArguments(String input, String commandWord) {
        return input.substring(commandWord.length()).trim();
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
     */
    private static void markTask(String arguments, TaskList tasks) {
        int taskNumber = Integer.parseInt(arguments);
        Task task = tasks.get(taskNumber);
        task.markAsDone();
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
    }

    /**
     * Marks the task named by an "unmark &lt;task number&gt;" command as not done.
     *
     * @param arguments Task number the user typed.
     * @param tasks List holding the task to unmark.
     */
    private static void unmarkTask(String arguments, TaskList tasks) {
        int taskNumber = Integer.parseInt(arguments);
        Task task = tasks.get(taskNumber);
        task.markAsNotDone();
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
    }

    /**
     * Adds the to-do described by a "todo &lt;description&gt;" command.
     *
     * @param arguments Description the user typed.
     * @param tasks List to add the task to.
     */
    private static void addTodo(String arguments, TaskList tasks) {
        addTask(new Todo(arguments), tasks);
    }

    /**
     * Adds the deadline described by a
     * "deadline &lt;description&gt; /by &lt;when&gt;" command.
     *
     * @param arguments Description and due date/time the user typed.
     * @param tasks List to add the task to.
     */
    private static void addDeadline(String arguments, TaskList tasks) {
        int byIndex = arguments.indexOf(KEYWORD_BY);
        String description = arguments.substring(0, byIndex).trim();
        String by = arguments.substring(byIndex + KEYWORD_BY.length()).trim();
        addTask(new Deadline(description, by), tasks);
    }

    /**
     * Adds the event described by an
     * "event &lt;description&gt; /from &lt;start&gt; /to &lt;end&gt;" command.
     *
     * @param arguments Description, start and end date/time the user typed.
     * @param tasks List to add the task to.
     */
    private static void addEvent(String arguments, TaskList tasks) {
        int fromIndex = arguments.indexOf(KEYWORD_FROM);
        int toIndex = arguments.indexOf(KEYWORD_TO);
        String description = arguments.substring(0, fromIndex).trim();
        String from = arguments.substring(fromIndex + KEYWORD_FROM.length(), toIndex).trim();
        String to = arguments.substring(toIndex + KEYWORD_TO.length()).trim();
        addTask(new Event(description, from, to), tasks);
    }

    /**
     * Adds a task to the list and prints the confirmation shown to the user.
     *
     * @param task Task to add.
     * @param tasks List to add the task to.
     */
    private static void addTask(Task task, TaskList tasks) {
        tasks.add(task);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Tells the user that their input was not one of the known commands.
     */
    private static void printUnknownCommand() {
        System.out.println(" Sorry, I don't know what that means.");
        System.out.println(" Try: todo, deadline, event, list, mark, unmark, bye.");
    }
}
