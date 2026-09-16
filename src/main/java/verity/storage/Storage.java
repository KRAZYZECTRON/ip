package verity.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import verity.task.TaskList;

/**
 * Keeps the task list in a text file on disk, so that it survives the program
 * being closed.
 *
 * <p>The file holds one task per line, with its fields separated by
 * {@value #FIELD_SEPARATOR}, for example:
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | June 6th
 * E | 0 | project meeting | Aug 6th 2pm | 4pm
 * </pre>
 * The fields are the type icon, "1" if the task is done or "0" if not, the
 * description, and then the date/time details of that task type. A plain text
 * format like this can be read and fixed by hand, which a binary format such as
 * Java object serialization could not.
 */
public class Storage {
    /**
     * Text written between two fields of a saved task. Since it cannot be told
     * apart from the same text inside a field, commands refuse any input
     * containing {@value #FORBIDDEN_CHARACTER}.
     */
    public static final String FIELD_SEPARATOR = " | ";

    /** Character that user input must not contain, as it marks field boundaries. */
    public static final String FORBIDDEN_CHARACTER = "|";

    private final Path filePath;

    /**
     * Creates a storage that keeps the tasks in the given file.
     *
     * @param filePath File to save the tasks to. A relative path is taken
     *                 relative to the folder the program was started from.
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Writes every task in the list to the file, replacing what it held. The
     * folder holding the file is created first if it does not exist yet, as
     * happens the first time the program is run on a computer.
     *
     * @param tasks Tasks to save.
     * @throws IOException If the folder or the file cannot be written.
     */
    public void save(TaskList tasks) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 1; i <= tasks.size(); i++) {
            lines.add(String.join(FIELD_SEPARATOR, tasks.get(i).getSaveFields()));
        }

        Path folder = filePath.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }
}
