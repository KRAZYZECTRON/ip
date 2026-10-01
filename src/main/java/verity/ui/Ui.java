package verity.ui;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import verity.parser.Parser;
import verity.task.Task;
import verity.task.TaskList;

/**
 * Handles every interaction with the user: reading the commands they type and
 * printing Verity's replies.
 *
 * <p>Keeping all the wording and layout of the replies in this one class means
 * the rest of the program says <i>what</i> happened ("this task was added"),
 * while this class decides <i>how</i> that is shown. Changing the look of the
 * chatbot, or replacing the console with a GUI later, then touches only here.
 */
public class Ui {
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

    // Replies are indented by one space, and a task quoted inside a reply by
    // three, so that the task stands out from the sentence introducing it.
    private static final String INDENT_MESSAGE = " ";
    private static final String INDENT_TASK = "   ";

    private final Scanner in = new Scanner(System.in);

    /**
     * Creates a user interface that reads from standard input and writes to
     * standard output.
     */
    public Ui() {
        useUtf8Output();
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
     * Returns the next line the user types, with surrounding spaces removed.
     * Spaces around a command are the user's slip, not part of the command.
     *
     * <p>If the input has ended, for example because the user pressed Ctrl+D
     * (Ctrl+Z then Enter on Windows), there is no line left to read. The exit
     * command is then returned, so that Verity says goodbye and stops normally
     * instead of crashing.
     */
    public String readCommand() {
        if (!in.hasNextLine()) {
            return Parser.COMMAND_BYE;
        }
        return in.nextLine().trim();
    }

    /**
     * Prints the horizontal line that separates one reply from the next.
     */
    public void showLine() {
        System.out.println(LINE);
    }

    /**
     * Prints the banner and welcome message shown when Verity starts.
     */
    public void showWelcome() {
        System.out.println(BANNER);
        System.out.println("Hello! I'm Verity");
        System.out.println("What can I do for you?");
        showLine();
    }

    /**
     * Prints the farewell shown when the user exits.
     */
    public void showGoodbye() {
        showMessage("Bye. Hope to see you again soon!");
    }

    /**
     * Prints an explanation of why a command was rejected. The explanation may
     * span several lines, separated by {@code \n}; each is indented like any
     * other reply.
     *
     * @param message Explanation to show.
     */
    public void showError(String message) {
        for (String line : message.split("\n")) {
            showMessage(line);
        }
    }

    /**
     * Tells the user that the saved tasks could not be read at all, so Verity
     * is starting with an empty list.
     */
    public void showLoadingError() {
        showMessage("I couldn't read your saved tasks, so I'm starting with an empty list.");
        showMessage("They will be replaced the next time your list changes.");
        showLine();
    }

    /**
     * Tells the user which lines of the data file were skipped because they
     * could not be read.
     *
     * @param skippedLines Lines that were skipped, each with its line number.
     */
    public void showSkippedLines(List<String> skippedLines) {
        showMessage("Some lines of your saved tasks are not in a form I can read, so I skipped them:");
        for (String skippedLine : skippedLines) {
            System.out.println(INDENT_TASK + skippedLine);
        }
        showMessage("They will be left out the next time your list is saved.");
        showLine();
    }

    /**
     * Prints every task in the list, numbered from 1.
     *
     * @param tasks Tasks to print.
     */
    public void showTaskList(TaskList tasks) {
        showMessage("Here are the tasks in your list:");
        for (int i = 1; i <= tasks.size(); i++) {
            showMessage(i + "." + tasks.get(i));
        }
    }

    /**
     * Prints the tasks that matched a search, each with its number in the
     * full list, or says that nothing matched.
     *
     * @param tasks Full task list the search was made in.
     * @param taskNumbers Numbers of the matching tasks, counting from 1.
     * @param keyword Text that was searched for, named if nothing matched.
     */
    public void showMatchingTasks(TaskList tasks, List<Integer> taskNumbers, String keyword) {
        if (taskNumbers.isEmpty()) {
            showMessage("No task in your list has \"" + keyword + "\" in its description.");
            return;
        }
        showMessage("Here are the matching tasks in your list:");
        for (int taskNumber : taskNumbers) {
            showMessage(taskNumber + "." + tasks.get(taskNumber));
        }
    }

    /**
     * Confirms that a task was added to the list.
     *
     * @param task Task that was added.
     * @param taskCount Number of tasks in the list after adding it.
     */
    public void showTaskAdded(Task task, int taskCount) {
        showMessage("Got it. I've added this task:");
        showTask(task);
        showTaskCount(taskCount);
    }

    /**
     * Confirms that a task was removed from the list.
     *
     * @param task Task that was removed.
     * @param taskCount Number of tasks left in the list.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        showMessage("Noted. I've removed this task:");
        showTask(task);
        showTaskCount(taskCount);
    }

    /**
     * Confirms that a task was marked as done.
     *
     * @param task Task that was marked.
     */
    public void showTaskMarked(Task task) {
        showMessage("Nice! I've marked this task as done:");
        showTask(task);
    }

    /**
     * Confirms that a task was marked as not done.
     *
     * @param task Task that was unmarked.
     */
    public void showTaskUnmarked(Task task) {
        showMessage("OK, I've marked this task as not done yet:");
        showTask(task);
    }

    private void showMessage(String message) {
        System.out.println(INDENT_MESSAGE + message);
    }

    private void showTask(Task task) {
        System.out.println(INDENT_TASK + task);
    }

    private void showTaskCount(int taskCount) {
        showMessage("Now you have " + taskCount + " tasks in the list.");
    }
}
