import java.util.Scanner;

/**
 * Entry point for the YY chatbot: a command-line task list that supports
 * adding tasks, listing them, and marking them done or not done.
 */
public class YY {
    // Requirement caps storage at 100 tasks, so a fixed-size array is enough.
    private static final int MAX_TASKS = 100;

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
        String line = "____________________________________________________________";

        System.out.println(banner);
        System.out.println("Hello! I'm YY");
        System.out.println("What can I do for you?");
        System.out.println(line);

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        // Read commands until the user types "bye". "list" shows stored tasks;
        // "mark <index>"/"unmark <index>" toggle a task's done status; anything
        // else is stored as a new task and echoed back.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            System.out.println(line);
            if (input.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(line);
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
            } else {
                tasks[taskCount] = new Task(input);
                taskCount++;
                System.out.println(" added: " + input);
            }
            System.out.println(line);
        }
        scanner.close();
    }
}
