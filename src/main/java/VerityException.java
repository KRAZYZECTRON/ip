/**
 * Signals that something the user typed cannot be carried out, and carries the
 * explanation to show them.
 *
 * <p>This is a <i>checked</i> exception (it extends {@code Exception} rather
 * than {@code RuntimeException}) on purpose: the compiler then forces every
 * command that can reject an input to declare it, and forces the command loop
 * to handle it. That makes it hard to forget one.
 *
 * <p>The message is written for the user to read, so it says what was wrong and
 * how to type the command correctly. A message may span several lines,
 * separated by {@code \n}; the caller decides how to lay them out.
 */
public class VerityException extends Exception {
    /**
     * Creates an exception carrying the explanation to show the user.
     *
     * @param message What was wrong with the input, in words the user can act
     *                on.
     */
    public VerityException(String message) {
        super(message);
    }
}
