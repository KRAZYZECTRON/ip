---
name: seedu-java-coding-standard
description: Apply the SE-EDU intermediate Java coding standard (https://se-education.org/guides/conventions/java/intermediate.html) whenever writing, editing, or reviewing Java code in this project. Use for naming, layout/formatting, statements, and comment/Javadoc conventions.
---

# SE-EDU Java Coding Standard

This project mandates the [SE-EDU intermediate Java coding standard](https://se-education.org/guides/conventions/java/intermediate.html) for all Java code. Apply these rules whenever writing new Java code, editing existing Java code, or reviewing a diff.

## Naming

- Package names: all lower case (e.g. `todobuddy.ui`).
- Class/enum names: nouns in `PascalCase` (e.g. `Line`, `AudioSystem`).
- Variable names: `camelCase` (e.g. `line`, `audioSystem`).
- Constant names: `ALL_UPPERCASE_WITH_UNDERSCORES` (e.g. `MAX_ITERATIONS`).
- Method names: verbs in `camelCase` (e.g. `getName()`, `computeTotalWidth()`). Test methods: `featureUnderTest_testScenario_expectedBehavior()`, omitting the second/third parts as needed.
- Abbreviations/acronyms inside a name are not all-uppercase: `exportHtmlSource()`, not `exportHTMLSource()`.
- All names are in English.
- Scope drives name length: short scratch names (`i, j, k, m, n` for ints; `c, d` for chars) are fine for small/temporary scope; wide-scope variables get longer, descriptive names.
- Boolean variables/methods read like booleans: prefix with `is`, `has`, `was`, etc. (`isSet`, `hasData`, `boolean hasLicense()`, `void setFound(boolean isFound)`).
- Collections use plural names (`Collection<Point> points`, `int[] values`).
- Nested-loop iterators: `i`, then `j`, `k` for inner loops.
- Related constants share a common prefix (`COLOR_RED`, `COLOR_GREEN`, `COLOR_BLUE`).
- Every class goes in a package (not the default/unnamed package).

## Layout

- Indent with 4 spaces, never tabs.
- Line length: soft limit 110 chars, hard limit 120 chars.
- Wrapped lines: indent 8 spaces (double the normal indent).
- Break long lines after commas, or before an operator (including `.`, `&&`, `||`); keep a method/constructor name attached to its opening `(`; prefer breaking at a higher syntactic level. A wrapped ternary puts `?` and `:` at the start of their own lines.
- K&R ("Egyptian") brace style — opening brace stays on the same line:
  ```java
  while (!done) {
      doSomething();
  }
  ```
- `if`/`for`/`while`/`do-while`/`try-catch` all keep the opening brace on the same line as the keyword; every loop body and every conditional body is wrapped in braces even for a single statement, and the conditional/loop keyword is never on the same line as the body statement.
- Switch: traditional form needs an explicit `// Fallthrough` comment on any `case` that intentionally has no `break`; the arrow (`case X -> ...`) and switch-expression forms are also fine.
- Whitespace: spaces around binary operators (`a = (b + c) * d;`), a space after reserved words before `(` (`while (true) {`), a space after commas and after `;` in a `for` header, spaces around `:`/`?` in ternaries.
- One blank line between logical units within a block (often introduced by a comment).

## Statements

- Import individual classes explicitly; never use wildcard imports (`import java.util.*;` is not allowed).
- Import order: static imports, then `java`, `javax`, `org`, `com`, `javafx`, `junit` groups.
- Array specifiers attach to the type, not the variable: `int[] a = new int[20];`, not `int a[] = new int[20];`.
- Declare variables in the smallest possible scope, initialized at the point of declaration.
- Class fields are never `public` unless the class is a pure data class with no behavior (constants are the exception — `public static final` is fine).

## Comments

- All comments in English, American spelling.
- Every public class and public method gets a descriptive header comment (Javadoc). Getters/setters, overriding methods already covered by the parent's Javadoc, and test classes/methods are exempt.
- Javadoc form:
  ```java
  /**
   * Returns lateral location of the specified position.
   * If the position is unset, NaN is returned.
   *
   * @param x X coordinate of position.
   * @param y Y coordinate of position.
   * @return Lateral location.
   * @throws IllegalArgumentException If zone is <= 0.
   */
  ```
  - First sentence is a short summary.
  - Method summaries are third-person descriptive ("Returns ...", "Sends ...", "Adds ..."), not imperative ("Return ...").
  - Blank `*` line between the description and the `@param`/`@return`/`@throws` block.
  - `@param` is either present for every parameter or omitted entirely when all are self-explanatory; `@return` can be dropped when the return value is obvious or the method is void.
  - Overriding methods may use `@inheritDoc` instead of repeating the parent doc.
  - Simple field/constant comments may be a single `/** ... */` line.
- Comments are indented to match the code around them; short trailing comments are fine (`process('ABC'); // process dummy String first`).

## How to apply this

When writing or editing `.java` files in this project: follow every rule above. When reviewing a diff or being asked to check code, flag violations of this standard specifically (naming, brace/indent style, missing Javadoc on public classes/methods, wildcard imports, public fields on non-data classes, etc.) in addition to normal correctness review.
