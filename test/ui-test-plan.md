# Verity text-UI test plan

This file is the single source of truth for the text-UI (input/output) tests of
the Verity chatbot. The `test-ui` skill reads it, runs each test case against the
program, and compares the console output character for character.

## How the tests are run

For each test case the runner:

1. compiles `src/main/java/*.java` into a temporary folder,
2. starts the program and types the lines of the **Input** block on standard
   input, and
3. compares the whole console output with the **Expected output** block.

Run everything, or a single case, from the project root:

    .claude/skills/test-ui/run-ui-tests.sh
    .claude/skills/test-ui/run-ui-tests.sh --only TC-01

The first failing test case stops the session and prints the expected output,
the actual output, and the difference between them.

## Conventions used below

- Every test case has an **Aim**, an **Input** block and an **Expected output**
  block. The expected output covers the *entire* session, from the greeting to
  the farewell.
- A line containing `{{GREETING}}` or `{{FAREWELL}}` in an expected output is
  replaced by the matching block from "Common output blocks" below. This keeps
  the fixed parts of the session in one place.
- The horizontal rule the program prints is 60 underscore characters. Note that
  two of them appear back to back between commands: one closes the previous
  command's reply and one opens the next.

## Common output blocks

#### GREETING

```text
██╗   ██╗███████╗██████╗ ██╗████████╗██╗   ██╗
██║   ██║██╔════╝██╔══██╗██║╚══██╔══╝╚██╗ ██╔╝
██║   ██║█████╗  ██████╔╝██║   ██║    ╚████╔╝
╚██╗ ██╔╝██╔══╝  ██╔══██╗██║   ██║     ╚██╔╝
 ╚████╔╝ ███████╗██║  ██║██║   ██║      ██║
  ╚═══╝  ╚══════╝╚═╝  ╚═╝╚═╝   ╚═╝      ╚═╝

Hello! I'm Verity
What can I do for you?
____________________________________________________________
```

#### FAREWELL

```text
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## Test cases

### TC-01 Start up and exit

**Aim:** Verify that the program greets the user on start-up and says goodbye when the user types `bye`.

**Input:**

```text
bye
```

**Expected output:**

```text
{{GREETING}}
{{FAREWELL}}
```

### TC-02 Add a to-do

**Aim:** Verify that `todo` adds a task with no date/time, shows it with the `[T]` icon, and reports the new task count.

**Input:**

```text
todo borrow book
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [T][ ] borrow book
 Now you have 1 tasks in the list.
____________________________________________________________
{{FAREWELL}}
```

### TC-03 Add a deadline

**Aim:** Verify that `deadline <description> /by <when>` adds a task with the `[D]` icon and shows the due date after the description.

**Input:**

```text
deadline return book /by Sunday
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [D][ ] return book (by: Sunday)
 Now you have 1 tasks in the list.
____________________________________________________________
{{FAREWELL}}
```

### TC-04 Add an event

**Aim:** Verify that `event <description> /from <start> /to <end>` adds a task with the `[E]` icon and shows both the start and the end.

**Input:**

```text
event project meeting /from Mon 2pm /to 4pm
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [E][ ] project meeting (from: Mon 2pm to: 4pm)
 Now you have 1 tasks in the list.
____________________________________________________________
{{FAREWELL}}
```

### TC-05 Accept a date that is not a real date

**Aim:** Verify that dates/times are stored as free text, so a deadline such as `no idea :-p` is accepted and echoed back unchanged.

**Input:**

```text
deadline do homework /by no idea :-p
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [D][ ] do homework (by: no idea :-p)
 Now you have 1 tasks in the list.
____________________________________________________________
{{FAREWELL}}
```

### TC-06 List an empty task list

**Aim:** Verify that `list` on an empty list prints only the heading, with no task lines.

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Here are the tasks in your list:
____________________________________________________________
{{FAREWELL}}
```

### TC-07 List a mix of all three task types

**Aim:** Verify the worked example from the requirements: several tasks of different types, two of them done, are numbered from 1 and each shows the right type icon, done icon and date/time details.

**Input:**

```text
todo read book
mark 1
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo join sports club
mark 4
todo borrow book
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] read book
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [D][ ] return book (by: June 6th)
 Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
 Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] join sports club
 Now you have 4 tasks in the list.
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] join sports club
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] borrow book
 Now you have 5 tasks in the list.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][X] read book
 2.[D][ ] return book (by: June 6th)
 3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
 4.[T][X] join sports club
 5.[T][ ] borrow book
____________________________________________________________
{{FAREWELL}}
```

### TC-08 Mark a task as done

**Aim:** Verify that `mark <index>` sets the done icon of the task at that 1-based index and that the change is visible in a later `list`.

**Input:**

```text
todo read book
mark 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] read book
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][X] read book
____________________________________________________________
{{FAREWELL}}
```

### TC-09 Unmark a task

**Aim:** Verify that `unmark <index>` clears the done icon of a task that was marked done, keeping its type and date details intact.

**Input:**

```text
deadline return book /by Sunday
mark 1
unmark 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [D][ ] return book (by: Sunday)
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [D][X] return book (by: Sunday)
____________________________________________________________
____________________________________________________________
 OK, I've marked this task as not done yet:
   [D][ ] return book (by: Sunday)
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[D][ ] return book (by: Sunday)
____________________________________________________________
{{FAREWELL}}
```

### TC-10 Reject an unrecognised command

**Aim:** Verify that input that is not one of the known commands is reported to the user instead of being stored as a task.

**Input:**

```text
blah
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Sorry, I don't know what that means.
 Try: todo, deadline, event, list, mark, unmark, bye.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
____________________________________________________________
{{FAREWELL}}
```

### TC-11 Reject a blank line

**Aim:** Verify that pressing enter without typing anything is reported as a mistake with its own message, rather than being treated as an unknown command, and that it leaves the task list untouched.

**Input:**

```text

todo read book

list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 You pressed enter without typing a command.
 Try: todo, deadline, event, list, mark, unmark, bye.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 You pressed enter without typing a command.
 Try: todo, deadline, event, list, mark, unmark, bye.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] read book
____________________________________________________________
{{FAREWELL}}
```

### TC-12 Ignore spaces around a command

**Aim:** Verify that leading and trailing spaces around a command line are ignored, so a stray space does not turn a valid command into an unknown one.

**Input:**

```text
   todo read book   
  list  
   bye   
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] read book
____________________________________________________________
{{FAREWELL}}
```

### TC-13 Reject a todo with no description

**Aim:** Verify that `todo` without a description is refused with a message naming what is missing, and that a valid `todo` between two refused ones is still the only task in the list.

**Input:**

```text
todo
todo read book
todo   
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 A todo needs a description saying what to do.
 Try: todo borrow book
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 A todo needs a description saying what to do.
 Try: todo borrow book
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] read book
____________________________________________________________
{{FAREWELL}}
```

### TC-14 Reject an incomplete deadline

**Aim:** Verify that each way of getting `deadline` wrong — nothing after the command word, no `/by`, no description before the `/by`, no date after the `/by` — is refused with its own message, and that none of the four is stored.

**Input:**

```text
deadline
deadline return book
deadline /by Sunday
deadline return book /by
deadline return book /by Sunday
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 A deadline needs a description and a due date.
 Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
 I can't tell when "return book" is due. Mark the due date with /by.
 Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
 A deadline needs a description before the /by.
 Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
 A deadline needs a due date after the /by.
 Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [D][ ] return book (by: Sunday)
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[D][ ] return book (by: Sunday)
____________________________________________________________
{{FAREWELL}}
```

### TC-15 Reject an incomplete event

**Aim:** Verify that every way of getting `event` wrong is refused with its own message, including `/to` typed before `/from`, and that only the one correct event ends up in the list.

**Input:**

```text
event
event project meeting
event project meeting /to 4pm /from Mon 2pm
event /from Mon 2pm /to 4pm
event project meeting /from /to 4pm
event project meeting /from Mon 2pm /to
event project meeting /from Mon 2pm /to 4pm
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 An event needs a description, a start and an end.
 Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
 I can't tell when "project meeting" starts. Mark the start with /from.
 Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
 An event needs an end, marked with /to after the /from.
 Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
 An event needs a description before the /from.
 Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
 An event needs a start after the /from.
 Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
 An event needs an end after the /to.
 Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [E][ ] project meeting (from: Mon 2pm to: 4pm)
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[E][ ] project meeting (from: Mon 2pm to: 4pm)
____________________________________________________________
{{FAREWELL}}
```

### TC-16 Reject a mark or unmark with no readable task number

**Aim:** Verify that `mark` and `unmark` without a number, or with something that is not a number, are refused with a message naming the command, and that a valid `mark` afterwards still works.

**Input:**

```text
todo read book
mark
mark abc
unmark
mark 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Tell me which task to mark, by its number.
 Try: mark 1
____________________________________________________________
____________________________________________________________
 "abc" is not a task number.
 Try: mark 1
____________________________________________________________
____________________________________________________________
 Tell me which task to unmark, by its number.
 Try: unmark 1
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] read book
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][X] read book
____________________________________________________________
{{FAREWELL}}
```

### TC-17 Reject extra words after a command that takes none

**Aim:** Verify that `list` and `bye` complain about anything typed after them instead of silently ignoring it, and that `bye` with extra words does not end the session.

**Input:**

```text
list now
bye now
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 The list command takes nothing after it, but you added "now".
 Try: list
____________________________________________________________
____________________________________________________________
 The bye command takes nothing after it, but you added "now".
 Try: bye
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
____________________________________________________________
{{FAREWELL}}
```
