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
        // Parallel to tasks: isDone[i] tracks whether tasks[i] is marked done.
        boolean[] isDone = new boolean[MAX_TASKS];
        int taskCount = 0;

        // Read commands until the user types "bye". "list" shows stored tasks;
        // "mark <index>" marks a task done; anything else is stored as a new
        // task and echoed back.
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
                    String statusIcon = isDone[i] ? "X" : " ";
                    System.out.println(" " + (i + 1) + ".[" + statusIcon + "] " + tasks[i]);
                }
            } else if (input.startsWith("mark ")) {
                int index = Integer.parseInt(input.substring("mark ".length()).trim()) - 1;
                isDone[index] = true;
                System.out.println(" Nice! I've marked this task as done:");
                System.out.println("   [X] " + tasks[index]);
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
