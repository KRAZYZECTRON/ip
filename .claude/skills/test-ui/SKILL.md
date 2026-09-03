---
name: test-ui
description: Run the text-UI (console input/output) tests for the YY chatbot. Use after every change to the Java code in this project, and whenever asked to test, verify, or check the program's console behaviour, or to add a new UI test case.
---

# Text-UI testing for YY

This project is tested by feeding lists of commands to the program on standard
input and comparing the resulting console output, character for character,
against the expected output.

All test cases live in `test/ui-test-plan.md`. That file is the single source of
truth: each test case records its **aim**, its **input** (the commands typed) and
its **expected output** (the whole console session). The runner script
`.claude/skills/test-ui/run-ui-tests.sh` parses that file, so a test case is
added by editing the Markdown, not the script.

## How to run the tests

From the project root:

```bash
.claude/skills/test-ui/run-ui-tests.sh
```

Other forms:

```bash
.claude/skills/test-ui/run-ui-tests.sh --only TC-03            # one test case
.claude/skills/test-ui/run-ui-tests.sh --plan path/to/plan.md  # another plan
.claude/skills/test-ui/run-ui-tests.sh --input in.txt --expected out.txt
```

The last form is for an ad-hoc check of a list of commands that is not (yet) in
the test plan.

The script compiles `src/main/java/*.java` with `javac` into a temporary folder,
so it needs Java 25 on the `PATH`; it does not touch `out/` or any build folder.

## Rules to follow when using this skill

1. **Run the full suite after every change to the Java code.** A change that is
   "obviously safe" still changes the console output often enough to be worth
   the few seconds.
2. **Update `test/ui-test-plan.md` first when behaviour changes.** If a change
   adds a command or changes the wording of a message, add or edit the test
   case *before* running, so the run proves the new behaviour rather than
   recording it. Never edit an expected output merely to match whatever the
   program happens to print.
3. **Stop at the first failure.** The script exits immediately on a failing test
   case and prints the console input, the expected output, the actual output and
   the difference. Report those to the user, and fix the cause before running
   anything else.
4. **Always show the session record.** After the run, show the user the console
   input and output of the test session (the script prints them for each test
   case) so the behaviour can be checked by eye as well.

## Adding a test case

Append a section to the "Test cases" part of `test/ui-test-plan.md` in exactly
this shape (the runner relies on the headings and the fenced blocks):

    ### TC-11 Short title

    **Aim:** One sentence saying what this test case establishes.

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

Notes:

- Every input must end with `bye`, otherwise the program waits forever for
  input.
- `{{GREETING}}` and `{{FAREWELL}}` stand for the fixed start-up and shut-down
  blocks defined under "Common output blocks" in the test plan; write them on a
  line of their own.
- Leading spaces in the expected output matter: the program indents its replies
  by one space and indents a task by three.
