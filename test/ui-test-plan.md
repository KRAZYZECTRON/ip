# Verity text-UI test plan

This file is the single source of truth for the text-UI (input/output) tests of
the Verity chatbot. The `test-ui` skill reads it, runs each test case against the
program, and compares the console output character for character.

## How the tests are run

For each test case the runner:

1. compiles every `.java` file under `src/main/java` into a temporary folder,
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

### TC-18 Reject a task number that names no task

**Aim:** Verify that `mark` and `unmark` refuse a number that is outside the list — on an empty list, below 1, and above the last task — that the message says which numbers are usable, and that the tasks that do exist are left untouched.

**Input:**

```text
mark 1
todo read book
todo return book
mark 0
mark 3
unmark -1
mark 2
list
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 There is no task 1, because your list is empty.
 Try: todo borrow book
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] return book
 Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
 There is no task 0. You have 2 tasks, numbered 1 to 2.
 Try: list
____________________________________________________________
____________________________________________________________
 There is no task 3. You have 2 tasks, numbered 1 to 2.
 Try: list
____________________________________________________________
____________________________________________________________
 There is no task -1. You have 2 tasks, numbered 1 to 2.
 Try: list
____________________________________________________________
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] return book
____________________________________________________________
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] read book
 2.[T][X] return book
____________________________________________________________
{{FAREWELL}}
```

### TC-19 Report a single existing task in the singular

**Aim:** Verify that the message for an out-of-range task number reads naturally when exactly one task exists, rather than offering a range of "1 to 1".

**Input:**

```text
todo read book
mark 2
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
 There is no task 2. You have 1 task, numbered 1.
 Try: list
____________________________________________________________
{{FAREWELL}}
```

### TC-20 Refuse to add past the 100-task limit

**Aim:** Verify that the 101st task is refused with an explanation instead of overflowing the fixed-size array the task list is built on. The expected output is long because every accepted task is confirmed; it is kept last so it does not get in the way of reading the other cases.

**Input:**

```text
todo task 1
todo task 2
todo task 3
todo task 4
todo task 5
todo task 6
todo task 7
todo task 8
todo task 9
todo task 10
todo task 11
todo task 12
todo task 13
todo task 14
todo task 15
todo task 16
todo task 17
todo task 18
todo task 19
todo task 20
todo task 21
todo task 22
todo task 23
todo task 24
todo task 25
todo task 26
todo task 27
todo task 28
todo task 29
todo task 30
todo task 31
todo task 32
todo task 33
todo task 34
todo task 35
todo task 36
todo task 37
todo task 38
todo task 39
todo task 40
todo task 41
todo task 42
todo task 43
todo task 44
todo task 45
todo task 46
todo task 47
todo task 48
todo task 49
todo task 50
todo task 51
todo task 52
todo task 53
todo task 54
todo task 55
todo task 56
todo task 57
todo task 58
todo task 59
todo task 60
todo task 61
todo task 62
todo task 63
todo task 64
todo task 65
todo task 66
todo task 67
todo task 68
todo task 69
todo task 70
todo task 71
todo task 72
todo task 73
todo task 74
todo task 75
todo task 76
todo task 77
todo task 78
todo task 79
todo task 80
todo task 81
todo task 82
todo task 83
todo task 84
todo task 85
todo task 86
todo task 87
todo task 88
todo task 89
todo task 90
todo task 91
todo task 92
todo task 93
todo task 94
todo task 95
todo task 96
todo task 97
todo task 98
todo task 99
todo task 100
todo task 101
bye
```

**Expected output:**

```text
{{GREETING}}
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 1
 Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 2
 Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 3
 Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 4
 Now you have 4 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 5
 Now you have 5 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 6
 Now you have 6 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 7
 Now you have 7 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 8
 Now you have 8 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 9
 Now you have 9 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 10
 Now you have 10 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 11
 Now you have 11 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 12
 Now you have 12 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 13
 Now you have 13 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 14
 Now you have 14 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 15
 Now you have 15 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 16
 Now you have 16 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 17
 Now you have 17 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 18
 Now you have 18 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 19
 Now you have 19 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 20
 Now you have 20 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 21
 Now you have 21 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 22
 Now you have 22 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 23
 Now you have 23 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 24
 Now you have 24 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 25
 Now you have 25 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 26
 Now you have 26 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 27
 Now you have 27 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 28
 Now you have 28 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 29
 Now you have 29 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 30
 Now you have 30 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 31
 Now you have 31 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 32
 Now you have 32 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 33
 Now you have 33 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 34
 Now you have 34 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 35
 Now you have 35 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 36
 Now you have 36 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 37
 Now you have 37 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 38
 Now you have 38 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 39
 Now you have 39 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 40
 Now you have 40 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 41
 Now you have 41 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 42
 Now you have 42 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 43
 Now you have 43 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 44
 Now you have 44 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 45
 Now you have 45 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 46
 Now you have 46 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 47
 Now you have 47 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 48
 Now you have 48 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 49
 Now you have 49 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 50
 Now you have 50 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 51
 Now you have 51 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 52
 Now you have 52 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 53
 Now you have 53 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 54
 Now you have 54 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 55
 Now you have 55 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 56
 Now you have 56 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 57
 Now you have 57 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 58
 Now you have 58 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 59
 Now you have 59 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 60
 Now you have 60 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 61
 Now you have 61 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 62
 Now you have 62 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 63
 Now you have 63 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 64
 Now you have 64 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 65
 Now you have 65 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 66
 Now you have 66 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 67
 Now you have 67 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 68
 Now you have 68 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 69
 Now you have 69 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 70
 Now you have 70 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 71
 Now you have 71 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 72
 Now you have 72 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 73
 Now you have 73 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 74
 Now you have 74 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 75
 Now you have 75 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 76
 Now you have 76 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 77
 Now you have 77 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 78
 Now you have 78 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 79
 Now you have 79 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 80
 Now you have 80 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 81
 Now you have 81 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 82
 Now you have 82 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 83
 Now you have 83 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 84
 Now you have 84 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 85
 Now you have 85 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 86
 Now you have 86 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 87
 Now you have 87 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 88
 Now you have 88 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 89
 Now you have 89 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 90
 Now you have 90 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 91
 Now you have 91 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 92
 Now you have 92 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 93
 Now you have 93 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 94
 Now you have 94 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 95
 Now you have 95 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 96
 Now you have 96 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 97
 Now you have 97 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 98
 Now you have 98 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 99
 Now you have 99 tasks in the list.
____________________________________________________________
____________________________________________________________
 Got it. I've added this task:
   [T][ ] task 100
 Now you have 100 tasks in the list.
____________________________________________________________
____________________________________________________________
 Your list is full at 100 tasks, so I can't add another one.
 There is no way to remove a task yet, so that is as many as I can hold.
____________________________________________________________
{{FAREWELL}}
```
