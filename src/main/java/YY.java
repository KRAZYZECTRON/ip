import java.util.Scanner;

/**
 * Entry point for the YY chatbot: a command-line task list that supports
 * adding to-dos, deadlines and events, listing them, and marking them
 * done or not done.
 */
public class YY {
    private static final String LINE = "____________________________________________________________";

    /**
     * Starts YY and reads commands from standard input until "bye" is entered.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
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
     * Carries out one command and prints its reply.
     *
     * @param input Whole line the user typed.
     * @param tasks List of tasks the command works on.
     * @return True if YY should keep reading commands, false if the user asked
     *         to exit.
     */
    private static boolean executeCommand(String input, TaskList tasks) {
        if (input.equals("bye")) {
            System.out.println(" Bye. Hope to see you again soon!");
            return false;
        }

        if (input.equals("list")) {
            printTasks(tasks);
        } else if (input.startsWith("mark ")) {
            markTask(input, tasks);
        } else if (input.startsWith("unmark ")) {
            unmarkTask(input, tasks);
        } else if (input.startsWith("todo ")) {
            addTodo(input, tasks);
        } else if (input.startsWith("deadline ")) {
            addDeadline(input, tasks);
        } else if (input.startsWith("event ")) {
            addEvent(input, tasks);
        } else {
            printUnknownCommand();
        }
        return true;
    }

    /**
     * Prints the banner and welcome message shown when YY starts.
     */
    private static void printGreeting() {
        String banner = "\\ \\      / /  \\ \\      / /\n"
                + " \\ \\    / /    \\ \\    / / \n"
                + "  \\ \\  / /      \\ \\  / /  \n"
                + "   \\ \\/ /        \\ \\/ /   \n"
                + "    |  |          |  |    \n"
                + "    |  |          |  |    \n"
                + "    |  |          |  |    \n";

        System.out.println(banner);
        System.out.println("Hello! I'm YY");
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
     * @param input Whole line the user typed.
     * @param tasks List holding the task to mark.
     */
    private static void markTask(String input, TaskList tasks) {
        int taskNumber = Integer.parseInt(input.substring("mark ".length()).trim());
        Task task = tasks.get(taskNumber);
        task.markAsDone();
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
    }

    /**
     * Marks the task named by an "unmark &lt;task number&gt;" command as not done.
     *
     * @param input Whole line the user typed.
     * @param tasks List holding the task to unmark.
     */
    private static void unmarkTask(String input, TaskList tasks) {
        int taskNumber = Integer.parseInt(input.substring("unmark ".length()).trim());
        Task task = tasks.get(taskNumber);
        task.markAsNotDone();
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
    }

    /**
     * Adds the to-do described by a "todo &lt;description&gt;" command.
     *
     * @param input Whole line the user typed.
     * @param tasks List to add the task to.
     */
    private static void addTodo(String input, TaskList tasks) {
        String description = input.substring("todo ".length()).trim();
        addTask(new Todo(description), tasks);
    }

    /**
     * Adds the deadline described by a
     * "deadline &lt;description&gt; /by &lt;when&gt;" command.
     *
     * @param input Whole line the user typed.
     * @param tasks List to add the task to.
     */
    private static void addDeadline(String input, TaskList tasks) {
        String arguments = input.substring("deadline ".length());
        int byIndex = arguments.indexOf(" /by ");
        String description = arguments.substring(0, byIndex).trim();
        String by = arguments.substring(byIndex + " /by ".length()).trim();
        addTask(new Deadline(description, by), tasks);
    }

    /**
     * Adds the event described by an
     * "event &lt;description&gt; /from &lt;start&gt; /to &lt;end&gt;" command.
     *
     * @param input Whole line the user typed.
     * @param tasks List to add the task to.
     */
    private static void addEvent(String input, TaskList tasks) {
        String arguments = input.substring("event ".length());
        int fromIndex = arguments.indexOf(" /from ");
        int toIndex = arguments.indexOf(" /to ");
        String description = arguments.substring(0, fromIndex).trim();
        String from = arguments.substring(fromIndex + " /from ".length(), toIndex).trim();
        String to = arguments.substring(toIndex + " /to ".length()).trim();
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
