package verity.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * A task that must be done by a given date, for example
 * "return book (by: Dec 02 2019)".
 */
public class Deadline extends Task {
    /**
     * How the due date is shown to the user, for example "Oct 15 2019". The
     * locale is fixed to English so that the month names do not change with
     * the language settings of the computer the program runs on.
     */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /**
     * When the task is due. Keeping a {@link LocalDate} rather than the text
     * the user typed means the date is known to be real, and can be shown in
     * any format or compared with other dates.
     */
    protected LocalDate by;

    /**
     * Creates a deadline with the given description and due date.
     *
     * @param description Text describing the task.
     * @param by Date the task is due.
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    /**
     * {@inheritDoc}
     *
     * <p>The due date is saved in the yyyy-mm-dd form, which
     * {@link LocalDate#parse(CharSequence)} reads back.
     */
    @Override
    public List<String> getSaveFields() {
        List<String> fields = super.getSaveFields();
        fields.add(by.toString());
        return fields;
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by.format(DISPLAY_FORMAT) + ")";
    }
}
