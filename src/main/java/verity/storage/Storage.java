package verity.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import verity.task.Deadline;
import verity.task.Event;
import verity.task.Task;
import verity.task.TaskList;
import verity.task.Todo;

/**
 * Keeps the task list in a text file on disk, so that it survives the program
 * being closed and is loaded again the next time it starts.
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

    // String.split reads its argument as a regular expression, in which "|"
    // means "or", so the separator has to be escaped to be matched literally.
    private static final String FIELD_SEPARATOR_REGEX = " \\| ";

    private static final String FLAG_DONE = "1";
    private static final String FLAG_NOT_DONE = "0";

    private final Path filePath;

    /** Lines the last {@link #load()} could not read, each with its line number. */
    private final List<String> skippedLines = new ArrayList<>();

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

    /**
     * Reads the tasks saved in the file.
     *
     * <p>If the file does not exist, as on the first run, an empty list is
     * returned. Blank lines are ignored. A line that is not a valid task, for
     * example because it was edited by hand, is skipped rather than stopping
     * the whole load; {@link #getSkippedLines()} then says which lines they were.
     *
     * @return The tasks read from the file.
     * @throws IOException If the file exists but cannot be read.
     */
    public TaskList load() throws IOException {
        skippedLines.clear();
        TaskList tasks = new TaskList();
        if (!Files.exists(filePath)) {
            return tasks;
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1;
            if (line.isBlank()) {
                continue;
            }
            if (tasks.isFull()) {
                skippedLines.add("line " + lineNumber + " (no room left in the list): " + line);
                continue;
            }
            try {
                tasks.add(parseTask(line));
            } catch (IllegalArgumentException e) {
                skippedLines.add("line " + lineNumber + ": " + line);
            }
        }
        return tasks;
    }

    /**
     * Returns the lines the last call to {@link #load()} skipped, each starting
     * with its line number, or an empty list if every line was read.
     */
    public List<String> getSkippedLines() {
        return List.copyOf(skippedLines);
    }

    /**
     * Recreates a task from one line of the file.
     *
     * @param line Line holding one saved task.
     * @return The task the line describes.
     * @throws IllegalArgumentException If the line is not a valid saved task: a
     *         field is empty, the type is unknown, the number of fields does not
     *         suit the type, or the done flag is neither "1" nor "0".
     */
    private static Task parseTask(String line) {
        // The limit of -1 keeps empty fields at the end of the line, so that
        // they are reported instead of silently dropped.
        String[] fields = line.split(FIELD_SEPARATOR_REGEX, -1);
        if (fields.length < 3) {
            throw new IllegalArgumentException("Too few fields: " + line);
        }
        for (String field : fields) {
            if (field.isBlank()) {
                throw new IllegalArgumentException("Empty field: " + line);
            }
        }

        String type = fields[0];
        String doneFlag = fields[1];
        String description = fields[2];

        // The type letters match the icons returned by each task's getTypeIcon().
        Task task;
        if (type.equals("T") && fields.length == 3) {
            task = new Todo(description);
        } else if (type.equals("D") && fields.length == 4) {
            task = new Deadline(description, fields[3]);
        } else if (type.equals("E") && fields.length == 5) {
            task = new Event(description, fields[3], fields[4]);
        } else {
            throw new IllegalArgumentException("Unknown type or wrong number of fields: " + line);
        }

        if (doneFlag.equals(FLAG_DONE)) {
            task.markAsDone();
        } else if (!doneFlag.equals(FLAG_NOT_DONE)) {
            throw new IllegalArgumentException("Done flag is not 1 or 0: " + line);
        }
        return task;
    }
}
