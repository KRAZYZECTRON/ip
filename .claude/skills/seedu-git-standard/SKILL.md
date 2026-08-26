---
name: seedu-git-standard
description: Apply the SE-EDU Git conventions (https://se-education.org/guides/conventions/git.html) whenever writing or proposing a commit message, or naming a branch, in this project.
---

# SE-EDU Git Standard

This project mandates the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html) for all commits going forward. Apply these rules whenever writing, proposing, or reviewing a commit message, or naming a branch.

## Commit message: subject line

- Soft limit 50 characters, hard limit 72.
- Imperative mood: "Add", "Fix", "Refactor" — not "Added", "Fixes", "Refactoring".
- Capitalize the first letter.
- No trailing period.
- Optionally prefix with a `<scope>:`/`<category>:` when it aids scanning (e.g. `Person class:`, `bug fix:`, `chore:`).

## Commit message: body

Include a body for any non-trivial commit; a purely mechanical or self-evident change can skip it.

Structure:
- Blank line between subject and body.
- Wrap body text at 72 characters.
- Blank line between paragraphs; bullet points where they help.

Content — explain **what** and **why**, not **how** (the diff already shows how):
1. State the current situation (present tense, and avoid words like "currently"/"originally" — just describe it as it is).
2. Explain why a change is needed.
3. Describe what is being done, in imperative mood (e.g. "Let's ..." is a natural way to introduce the change).
4. Justify the approach chosen, if it isn't obvious.
5. Note any other information a reviewer needs to judge the change without reading the diff.

Don't duplicate what's already said in code comments.

If the body is getting long or is trying to justify several unrelated things at once, that's a sign the commit should be split into finer-grained pieces instead — keep each commit to one logical, self-contained change.

## Branch names

- Kebab-case, meaningful keywords (e.g. `refactor-ui-tests`).
- Issue-related: `issueNumber-keywords-from-title` (e.g. `1234-ui-freeze-error`).

## How to apply this

When asked to write, propose, or create a commit message in this project, follow the subject/body rules above exactly. When asked to create commits, keep each one scoped to a single logical change rather than bundling unrelated changes together.
