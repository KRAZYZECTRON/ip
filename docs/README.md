# Verity User Guide

**Verity** is a chatbot that keeps track of your tasks from the command line.
You type short commands to add to-dos, deadlines and events, then list, find,
mark and delete them. Verity saves your list automatically, so it is still
there the next time you start it.

```
Hello! I'm Verity
What can I do for you?
____________________________________________________________
todo read book
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
```

- [Quick start](#quick-start)
- [Features](#features)
  - [Adding a to-do: `todo`](#adding-a-to-do-todo)
  - [Adding a deadline: `deadline`](#adding-a-deadline-deadline)
  - [Adding an event: `event`](#adding-an-event-event)
  - [Listing all tasks: `list`](#listing-all-tasks-list)
  - [Finding tasks: `find`](#finding-tasks-find)
  - [Marking a task as done: `mark`](#marking-a-task-as-done-mark)
  - [Marking a task as not done: `unmark`](#marking-a-task-as-not-done-unmark)
  - [Deleting a task: `delete`](#deleting-a-task-delete)
  - [Exiting: `bye`](#exiting-bye)
  - [Saving your tasks](#saving-your-tasks)
- [FAQ](#faq)
- [Command summary](#command-summary)

## Quick start

1. Make sure you have **Java 25** installed. You can check by running
   `java -version` in a terminal.
2. Download the latest `verity.jar` from the
   [releases page](https://github.com/KRAZYZECTRON/ip/releases).
3. Copy the file into an empty folder. Verity will keep your tasks in a
   `data` folder created next to it.
4. Open a terminal in that folder and run:

   ```
   java -jar verity.jar
   ```

5. Type a command and press Enter. Try `todo read book`, then `list`, then
   `bye` to exit.

## Features

**About the command format:**

- Words in `UPPER_CASE` are values you supply. In `todo DESCRIPTION`,
  `DESCRIPTION` could be `read book`.
- Command words are lower case: `list` works, `LIST` does not.
- `INDEX` is the number shown next to a task by `list` or `find`, starting
  at 1.
- Each reply shows a task as `[type][done] description`. The type is `T` for a
  to-do, `D` for a deadline and `E` for an event. The second box holds an `X`
  once the task is done.
- Task text cannot contain the `|` character, because Verity uses it in its
  data file.

### Adding a to-do: `todo`

Adds a task with no date attached.

Format: `todo DESCRIPTION`

Example: `todo borrow book`

```
 Got it. I've added this task:
   [T][ ] borrow book
 Now you have 1 tasks in the list.
```

### Adding a deadline: `deadline`

Adds a task that must be done by a certain date.

Format: `deadline DESCRIPTION /by DATE`

- `DATE` must be written as `yyyy-mm-dd`, for example `2019-12-02` for
  2 December 2019. Verity shows it back as `Dec 02 2019`.
- Other formats such as `2/12/2019`, and dates that do not exist such as
  `2019-02-30`, are refused, so a date is never misread.

Example: `deadline return book /by 2019-12-02`

```
 Got it. I've added this task:
   [D][ ] return book (by: Dec 02 2019)
 Now you have 2 tasks in the list.
```

### Adding an event: `event`

Adds a task that starts and ends at certain times.

Format: `event DESCRIPTION /from START /to END`

- `START` and `END` can be any text, such as `Mon 2pm` or `4pm`.
- `/from` must come before `/to`.

Example: `event project meeting /from Mon 2pm /to 4pm`

```
 Got it. I've added this task:
   [E][ ] project meeting (from: Mon 2pm to: 4pm)
 Now you have 3 tasks in the list.
```

### Listing all tasks: `list`

Shows every task in your list, with its number.

Format: `list`

```
 Here are the tasks in your list:
 1.[T][ ] borrow book
 2.[D][ ] return book (by: Dec 02 2019)
 3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
```

### Finding tasks: `find`

Shows the tasks whose description contains a keyword.

Format: `find KEYWORD`

- Upper and lower case are ignored: `find book` also finds `Book`.
- The keyword can be several words, such as `find return book`.
- Only descriptions are searched, not dates or times.
- Each task keeps its number from the full list, so you can use it straight
  away with `mark`, `unmark` or `delete`.

Example: `find book`

```
 Here are the matching tasks in your list:
 1.[T][ ] borrow book
 2.[D][ ] return book (by: Dec 02 2019)
```

### Marking a task as done: `mark`

Format: `mark INDEX`

Example: `mark 1`

```
 Nice! I've marked this task as done:
   [T][X] borrow book
```

### Marking a task as not done: `unmark`

Format: `unmark INDEX`

Example: `unmark 1`

```
 OK, I've marked this task as not done yet:
   [T][ ] borrow book
```

### Deleting a task: `delete`

Removes a task from the list. The tasks after it move up, so their numbers
each drop by one.

Format: `delete INDEX`

Example: `delete 3`

```
 Noted. I've removed this task:
   [E][ ] project meeting (from: Mon 2pm to: 4pm)
 Now you have 2 tasks in the list.
```

### Exiting: `bye`

Format: `bye`

```
 Bye. Hope to see you again soon!
```

### Saving your tasks

Verity saves your list to `data/verity.txt` after every change, so there is
no save command. The next time you start Verity from the same folder, your
tasks are loaded again.

Advanced users can edit `data/verity.txt` by hand. Each line is one task, for
example `D | 0 | return book | 2019-12-02`. If a line cannot be read, Verity
tells you which one at start-up and skips it. It is then dropped the next time
your list changes, so fix the line first if you want to keep that task.

## FAQ

**Q: How do I move my tasks to another computer?**<br>
A: Install Verity on the other computer and copy your `data/verity.txt` into
the `data` folder next to `verity.jar` there.

**Q: Verity says it doesn't know what my command means.**<br>
A: Check the spelling and that the command word is in lower case. The reply
lists every command Verity knows.

## Command summary

| Action   | Format, examples                                                                       |
|----------|----------------------------------------------------------------------------------------|
| To-do    | `todo DESCRIPTION`<br>e.g. `todo borrow book`                                          |
| Deadline | `deadline DESCRIPTION /by yyyy-mm-dd`<br>e.g. `deadline return book /by 2019-12-02`    |
| Event    | `event DESCRIPTION /from START /to END`<br>e.g. `event meeting /from Mon 2pm /to 4pm`  |
| List     | `list`                                                                                 |
| Find     | `find KEYWORD`<br>e.g. `find book`                                                     |
| Mark     | `mark INDEX`<br>e.g. `mark 1`                                                          |
| Unmark   | `unmark INDEX`<br>e.g. `unmark 1`                                                      |
| Delete   | `delete INDEX`<br>e.g. `delete 3`                                                      |
| Exit     | `bye`                                                                                  |
