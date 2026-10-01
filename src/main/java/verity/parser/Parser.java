package verity.parser;

import verity.VerityException;
import verity.command.AddCommand;
import verity.command.Command;
import verity.command.DeleteCommand;
import verity.command.ExitCommand;
import verity.command.ListCommand;
import verity.command.MarkCommand;
import verity.command.UnmarkCommand;
import verity.storage.Storage;
import verity.task.Deadline;
import verity.task.Event;
import verity.task.Todo;

/**
 * Makes sense of the commands the user types, turning each line into a
 * {@link Command} that is ready to be carried out.
 *
 * <p>Every check on what the user typed is made here, so a command that gets
 * as far as being carried out is known to be well formed. The methods are
 * {@code static} because parsing needs no state of its own: the same text
 * always gives the same result.
 */
public class Parser {
    // The word the user types to choose a command.
    private static final String COMMAND_BYE = "bye";
    private static final String COMMAND_LIST = "list";
    private static final String COMMAND_MARK = "mark";
    private static final String COMMAND_UNMARK = "unmark";
    private static final String COMMAND_TODO = "todo";
    private static final String COMMAND_DEADLINE = "deadline";
    private static final String COMMAND_EVENT = "event";
    private static final String COMMAND_DELETE = "delete";

    /** Reminder of the commands on offer, added to messages that reject input. */
    private static final String COMMAND_HINT =
            "Try: todo, deadline, event, list, mark, unmark, delete, bye.";

    // A correct example of each command that takes arguments, shown alongside
    // the complaint when the user's attempt at that command could not be read.
    public static final String EXAMPLE_TODO = "Try: todo borrow book";
    private static final String EXAMPLE_DEADLINE = "Try: deadline return book /by Sunday";
    private static final String EXAMPLE_EVENT = "Try: event project meeting /from Mon 2pm /to 4pm";

    // The keyword that separates one argument of a command from the next. The
    // surrounding spaces are deliberately not part of the keyword: a command
    // that stops right after it, such as "deadline homework /by", can then be
    // reported as a missing due date rather than as a missing /by.
    private static final String KEYWORD_BY = "/by";
    private static final String KEYWORD_FROM = "/from";
    private static final String KEYWORD_TO = "/to";

    /**
     * Returns the command described by a line the user typed.
     *
     * @param input Whole line the user typed, without surrounding spaces.
     * @return The command, ready to be carried out.
     * @throws VerityException If the line is empty, the command word is not
     *                         known, or the command's arguments are not in
     *                         the form it needs.
     */
    public static Command parse(String input) throws VerityException {
        String commandWord = getCommandWord(input);
        String arguments = getArguments(input);

        switch (commandWord) {
        case COMMAND_BYE:
            requireNoArguments(arguments, COMMAND_BYE);
            return new ExitCommand();
        case COMMAND_LIST:
            requireNoArguments(arguments, COMMAND_LIST);
            return new ListCommand();
        case COMMAND_MARK:
            return new MarkCommand(parseTaskNumber(arguments, COMMAND_MARK));
        case COMMAND_UNMARK:
            return new UnmarkCommand(parseTaskNumber(arguments, COMMAND_UNMARK));
        case COMMAND_DELETE:
            return new DeleteCommand(parseTaskNumber(arguments, COMMAND_DELETE));
        case COMMAND_TODO:
            return new AddCommand(parseTodo(arguments));
        case COMMAND_DEADLINE:
            return new AddCommand(parseDeadline(arguments));
        case COMMAND_EVENT:
            return new AddCommand(parseEvent(arguments));
        default:
            throw new VerityException("Sorry, I don't know what that means.\n" + COMMAND_HINT);
        }
    }

    /**
     * Returns the word that chooses the command: everything up to the first
     * space of the input.
     *
     * <p>Splitting the command word from its arguments here, rather than
     * inside each command, means a command word typed on its own still reaches
     * its own command, which can then explain what it is missing.
     *
     * @param input Whole line the user typed, without surrounding spaces.
     * @return The command word.
     * @throws VerityException If the input is empty.
     */
    private static String getCommandWord(String input) throws VerityException {
        if (input.isEmpty()) {
            throw new VerityException("You pressed enter without typing a command.\n" + COMMAND_HINT);
        }
        int firstSpace = input.indexOf(' ');
        return (firstSpace < 0) ? input : input.substring(0, firstSpace);
    }

    /**
     * Returns the arguments of the command: everything after the first space
     * of the input, without surrounding spaces, or an empty string if there is
     * nothing after the command word.
     *
     * @param input Whole line the user typed, without surrounding spaces.
     */
    private static String getArguments(String input) {
        int firstSpace = input.indexOf(' ');
        return (firstSpace < 0) ? "" : input.substring(firstSpace + 1).trim();
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
     * Whether a task with that number exists is not checked here, since that
     * depends on the list rather than on what was typed.
     *
     * @param arguments Text the user typed after the command word.
     * @param commandWord Command the number belongs to, named in the message
     *                    shown if it cannot be read.
     * @return The task number the user typed.
     * @throws VerityException If no number was given, or what was given is not
     *                         a whole number.
     */
    private static int parseTaskNumber(String arguments, String commandWord) throws VerityException {
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
     * Creates the to-do described by the arguments of a
     * "todo &lt;description&gt;" command.
     *
     * @param arguments Description the user typed.
     * @return The to-do described.
     * @throws VerityException If no description was given, or it contains the
     *                         field separator.
     */
    private static Todo parseTodo(String arguments) throws VerityException {
        if (arguments.isEmpty()) {
            throw new VerityException("A todo needs a description saying what to do.\n"
                    + EXAMPLE_TODO);
        }
        rejectFieldSeparator(arguments, EXAMPLE_TODO);
        return new Todo(arguments);
    }

    /**
     * Creates the deadline described by the arguments of a
     * "deadline &lt;description&gt; /by &lt;when&gt;" command.
     *
     * @param arguments Description and due date/time the user typed.
     * @return The deadline described.
     * @throws VerityException If the description, the /by keyword or the due
     *                         date is missing, or the arguments contain the
     *                         field separator.
     */
    private static Deadline parseDeadline(String arguments) throws VerityException {
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
        return new Deadline(description, by);
    }

    /**
     * Creates the event described by the arguments of an
     * "event &lt;description&gt; /from &lt;start&gt; /to &lt;end&gt;" command.
     *
     * @param arguments Description, start and end date/time the user typed.
     * @return The event described.
     * @throws VerityException If the description, either keyword, the start or
     *                         the end is missing, the two keywords are the
     *                         wrong way round, or the arguments contain the
     *                         field separator.
     */
    private static Event parseEvent(String arguments) throws VerityException {
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
        return new Event(description, from, to);
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
}
