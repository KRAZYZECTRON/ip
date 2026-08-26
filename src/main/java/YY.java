import java.util.Scanner;

public class YY {
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

        // Read commands until the user types "bye", echoing everything else back.
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            System.out.println(line);
            if (input.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(line);
                break;
            }
            System.out.println(" " + input);
            System.out.println(line);
        }
        scanner.close();
    }
}
