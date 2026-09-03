import java.util.Scanner;

/**
 * Entry point for the YY chatbot: a command-line task list that supports
 * adding to-dos, deadlines and events, listing them, and marking them
 * done or not done.
 */
public class YY {
    // Requirement caps storage at 100 tasks, so a fixed-size array is enough.
    private static final int MAX_TASKS = 100;
    private static final String LINE = "____________________________________________________________";

    /**
     * Starts YY and reads commands from standard input until "bye" is entered.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
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

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        // Read commands until the user types "bye". "list" shows stored tasks;
        // "mark <index>"/"unmark <index>" toggle a task's done status; "todo",
        // "deadline" and "event" each add a task of the matching type.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            System.out.println(LINE);
            if (input.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(LINE);
                break;
            } else if (input.equals("list")) {
                System.out.println(" Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + "." + tasks[i]);
                }
            } else if (input.startsWith("mark ")) {
                int index = Integer.parseInt(input.substring("mark ".length()).trim()) - 1;
                tasks[index].markAsDone();
                System.out.println(" Nice! I've marked this task as done:");
                System.out.println("   " + tasks[index]);
            } else if (input.startsWith("unmark ")) {
                int index = Integer.parseInt(input.substring("unmark ".length()).trim()) - 1;
                tasks[index].markAsNotDone();
                System.out.println(" OK, I've marked this task as not done yet:");
                System.out.println("   " + tasks[index]);
            } else if (input.startsWith("todo ")) {
                String description = input.substring("todo ".length()).trim();
                tasks[taskCount] = Task.createTodo(description);
                taskCount++;
                printAddedTask(tasks[taskCount - 1], taskCount);
            } else if (input.startsWith("deadline ")) {
                // Expected form: deadline <description> /by <when>
                String arguments = input.substring("deadline ".length());
                int byIndex = arguments.indexOf(" /by ");
                String description = arguments.substring(0, byIndex).trim();
                String by = arguments.substring(byIndex + " /by ".length()).trim();
                tasks[taskCount] = Task.createDeadline(description, by);
                taskCount++;
                printAddedTask(tasks[taskCount - 1], taskCount);
            } else if (input.startsWith("event ")) {
                // Expected form: event <description> /from <start> /to <end>
                String arguments = input.substring("event ".length());
                int fromIndex = arguments.indexOf(" /from ");
                int toIndex = arguments.indexOf(" /to ");
                String description = arguments.substring(0, fromIndex).trim();
                String from = arguments.substring(fromIndex + " /from ".length(), toIndex).trim();
                String to = arguments.substring(toIndex + " /to ".length()).trim();
                tasks[taskCount] = Task.createEvent(description, from, to);
                taskCount++;
                printAddedTask(tasks[taskCount - 1], taskCount);
            } else {
                System.out.println(" Sorry, I don't know what that means.");
                System.out.println(" Try: todo, deadline, event, list, mark, unmark, bye.");
            }
            System.out.println(LINE);
        }
        scanner.close();
    }

    /**
     * Prints the confirmation shown to the user after a task is added.
     *
     * @param task Task that was just added.
     * @param taskCount Number of tasks in the list after the addition.
     */
    private static void printAddedTask(Task task, int taskCount) {
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " tasks in the list.");
    }
}
