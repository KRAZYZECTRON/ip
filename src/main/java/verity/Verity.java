package verity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import verity.command.Command;
import verity.parser.Parser;
import verity.storage.Storage;
import verity.task.TaskList;
import verity.ui.Ui;

/**
 * Entry point for the Verity chatbot: a command-line task list that supports
 * adding to-dos, deadlines and events, listing them, finding them by keyword,
 * marking them done or not done, and deleting them. The list is saved to disk whenever it changes, and
 * loaded again when Verity starts.
 */
public class Verity {
    /**
     * Where the task list is saved. The path is relative, so it is found in the
     * folder the program is started from on any computer, and it is built from
     * its parts by {@code Path.of} rather than written as "data/verity.txt", so
     * that the separator between them suits the operating system.
     */
    private static final Path DATA_FILE = Path.of("data", "verity.txt");

    /** Reads the user's commands and shows Verity's replies. */
    private final Ui ui;

    /** Reads and writes the task list on disk. */
    private final Storage storage;

    /** Tasks the user is keeping track of; every command works on this list. */
    private TaskList tasks;

    /**
     * Creates a Verity chatbot that keeps its tasks in the given file.
     *
     * @param filePath File the tasks are loaded from and saved to.
     */
    public Verity(Path filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
    }

    /**
     * Starts Verity and reads commands from standard input until "bye" is entered.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        new Verity(DATA_FILE).run();
    }

    /**
     * Greets the user, loads the saved tasks, and then carries out commands
     * from standard input until the user exits.
     */
    public void run() {
        ui.showWelcome();
        tasks = loadTasks();
        boolean isExit = false;
        while (!isExit) {
            String input = ui.readCommand();
            ui.showLine();
            try {
                Command command = Parser.parse(input);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (VerityException e) {
                ui.showError(e.getMessage());
            } finally {
                ui.showLine();
            }
        }
    }

    /**
     * Loads the tasks saved by an earlier session, telling the user about
     * anything in the data file that could not be read.
     *
     * <p>A data file that cannot be read never stops Verity from starting: the
     * user gets whatever could be read, or an empty list, plus an explanation.
     *
     * @return The saved tasks, or an empty list if there are none or the data
     *         file cannot be read.
     */
    private TaskList loadTasks() {
        TaskList loadedTasks;
        try {
            loadedTasks = storage.load();
        } catch (IOException e) {
            ui.showLoadingError();
            return new TaskList();
        }

        List<String> skippedLines = storage.getSkippedLines();
        if (!skippedLines.isEmpty()) {
            ui.showSkippedLines(skippedLines);
        }
        return loadedTasks;
    }
}
