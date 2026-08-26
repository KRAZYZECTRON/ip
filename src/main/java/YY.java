import java.util.Scanner;

public class YY {
    // Requirement caps storage at 100 tasks, so a fixed-size array is enough.
    private static final int MAX_TASKS = 100;

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

        String[] tasks = new String[MAX_TASKS];
        int taskCount = 0;

        // Read commands until the user types "bye". "list" shows stored tasks;
        // anything else is stored as a new task and echoed back.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            System.out.println(line);
            if (input.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(line);
                break;
            } else if (input.equals("list")) {
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks[i]);
                }
            } else {
                tasks[taskCount] = input;
                taskCount++;
                System.out.println(" added: " + input);
            }
            System.out.println(line);
        }
        scanner.close();
    }
}
