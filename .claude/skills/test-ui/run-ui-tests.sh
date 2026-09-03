#!/usr/bin/env bash
#
# Runs the text-UI test cases for the YY chatbot.
#
# Each test case feeds a list of commands to the program on standard input and
# compares the whole console output against the expected output. Test cases are
# read from a Markdown test plan (test/ui-test-plan.md by default).
#
# Usage:
#   run-ui-tests.sh [--plan FILE] [--only TC-ID]
#   run-ui-tests.sh --input FILE --expected FILE   # ad-hoc, outside the plan
#
# Exits with status 0 if every test case passes, 1 on the first failure.

set -u

PLAN="test/ui-test-plan.md"
ONLY=""
AD_HOC_INPUT=""
AD_HOC_EXPECTED=""

usage() {
    sed -n '3,14p' "$0" | sed 's/^# \{0,1\}//'
}

while [ $# -gt 0 ]; do
    case "$1" in
        --plan) PLAN="$2"; shift 2 ;;
        --only) ONLY="$2"; shift 2 ;;
        --input) AD_HOC_INPUT="$2"; shift 2 ;;
        --expected) AD_HOC_EXPECTED="$2"; shift 2 ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown option: $1" >&2; usage; exit 2 ;;
    esac
done

if [ ! -d src/main/java ]; then
    echo "ERROR: run this script from the project root (src/main/java not found)." >&2
    exit 2
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------
echo "Compiling src/main/java ..."
if ! javac -d "$WORK/classes" src/main/java/*.java; then
    echo ""
    echo "TEST SESSION TERMINATED: compilation failed." >&2
    exit 1
fi
echo "Compilation OK (java $(java -version 2>&1 | head -1 | sed 's/.*version //; s/"//g'))"
echo ""

# Runs the program with the given input file and writes the console output
# (with Windows carriage returns stripped) to the given output file.
run_program() {
    java -cp "$WORK/classes" YY < "$1" 2>&1 | tr -d '\r' > "$2"
}

# Prints the input and the actual output of one test case, so the reader can
# see the whole console session.
show_session() {
    local input_file="$1" output_file="$2"
    echo "--- console input ---"
    cat "$input_file"
    echo "--- console output ---"
    cat "$output_file"
    echo "--- end of session ---"
}

# Reports a mismatch in full and stops the whole test session.
fail() {
    local label="$1" expected_file="$2" actual_file="$3" input_file="$4"
    echo ""
    echo "FAILED: $label"
    echo ""
    echo "===== console input ====="
    cat "$input_file"
    echo "===== expected output ====="
    cat "$expected_file"
    echo "===== actual output ====="
    cat "$actual_file"
    echo "===== difference (- expected, + actual) ====="
    diff -u "$expected_file" "$actual_file" | tail -n +3
    echo ""
    echo "TEST SESSION TERMINATED at $label." >&2
    exit 1
}

# ---------------------------------------------------------------------------
# Ad-hoc mode: a single input/expected pair given on the command line
# ---------------------------------------------------------------------------
if [ -n "$AD_HOC_INPUT" ] || [ -n "$AD_HOC_EXPECTED" ]; then
    if [ -z "$AD_HOC_INPUT" ] || [ -z "$AD_HOC_EXPECTED" ]; then
        echo "ERROR: --input and --expected must be used together." >&2
        exit 2
    fi
    tr -d '\r' < "$AD_HOC_EXPECTED" > "$WORK/adhoc.exp"
    run_program "$AD_HOC_INPUT" "$WORK/adhoc.out"
    if ! diff -q "$WORK/adhoc.exp" "$WORK/adhoc.out" > /dev/null; then
        fail "ad-hoc test" "$WORK/adhoc.exp" "$WORK/adhoc.out" "$AD_HOC_INPUT"
    fi
    echo "PASSED: ad-hoc test"
    show_session "$AD_HOC_INPUT" "$WORK/adhoc.out"
    exit 0
fi

# ---------------------------------------------------------------------------
# Plan mode: read the test cases out of the Markdown test plan
# ---------------------------------------------------------------------------
if [ ! -f "$PLAN" ]; then
    echo "ERROR: test plan not found: $PLAN" >&2
    exit 2
fi

mkdir -p "$WORK/cases"

# The plan is Markdown with a fixed shape:
#   "#### NAME"                + fenced block -> a reusable block, referenced
#                                                from an expected output as a
#                                                line containing {{NAME}}
#   "### TC-xx <title>"                       -> starts a test case
#   "**Aim:** ..."                            -> the aim of that test case
#   "**Input:**"               + fenced block -> the commands to type
#   "**Expected output:**"     + fenced block -> the console output expected
awk -v dir="$WORK/cases" '
    {
        line = $0
        sub(/\r$/, "", line)

        if (capturing) {
            if (line ~ /^```[ \t]*$/) {
                capturing = 0
                mode = ""
                next
            }
            if (mode == "in") {
                inputs[n] = inputs[n] line "\n"
            } else if (mode == "exp") {
                expected[n] = expected[n] line "\n"
            } else if (mode == "blk") {
                blocks[blockName] = blocks[blockName] line "\n"
            }
            next
        }

        if (line ~ /^#### /) {
            blockName = substr(line, 6)
            mode = "blk"
            awaiting = 1
            next
        }
        if (line ~ /^### /) {
            n++
            names[n] = substr(line, 5)
            mode = ""
            awaiting = 0
            next
        }
        if (line ~ /^\*\*Aim:\*\*/) {
            aims[n] = substr(line, 10)
            next
        }
        if (line ~ /^\*\*Input:\*\*/) {
            mode = "in"
            awaiting = 1
            next
        }
        if (line ~ /^\*\*Expected output:\*\*/) {
            mode = "exp"
            awaiting = 1
            next
        }
        if (awaiting && line ~ /^```/) {
            capturing = 1
            awaiting = 0
            next
        }
    }
    END {
        for (i = 1; i <= n; i++) {
            printf "%s", inputs[i] > (dir "/" i ".in")

            # Expand {{BLOCK}} placeholders in the expected output.
            count = split(expected[i], parts, "\n")
            text = ""
            for (j = 1; j < count; j++) {
                if (parts[j] ~ /^\{\{[A-Z_]+\}\}$/) {
                    key = substr(parts[j], 3, length(parts[j]) - 4)
                    text = text blocks[key]
                } else {
                    text = text parts[j] "\n"
                }
            }
            printf "%s", text > (dir "/" i ".exp")

            printf "%s\n", names[i] > (dir "/" i ".name")
            printf "%s\n", aims[i] > (dir "/" i ".aim")
        }
        printf "%d\n", n > (dir "/count")
    }
' "$PLAN"

CASE_COUNT="$(cat "$WORK/cases/count" 2>/dev/null || echo 0)"
if [ "$CASE_COUNT" -eq 0 ]; then
    echo "ERROR: no test cases found in $PLAN" >&2
    exit 2
fi

echo "Test plan: $PLAN ($CASE_COUNT test cases)"
echo ""

PASSED=0
SKIPPED=0
i=1
while [ "$i" -le "$CASE_COUNT" ]; do
    NAME="$(cat "$WORK/cases/$i.name")"
    AIM="$(cat "$WORK/cases/$i.aim")"

    if [ -n "$ONLY" ] && [ "${NAME#"$ONLY"}" = "$NAME" ]; then
        SKIPPED=$((SKIPPED + 1))
        i=$((i + 1))
        continue
    fi

    run_program "$WORK/cases/$i.in" "$WORK/cases/$i.out"
    if ! diff -q "$WORK/cases/$i.exp" "$WORK/cases/$i.out" > /dev/null; then
        fail "$NAME" "$WORK/cases/$i.exp" "$WORK/cases/$i.out" "$WORK/cases/$i.in"
    fi

    echo "PASSED: $NAME"
    echo "  Aim: $AIM"
    show_session "$WORK/cases/$i.in" "$WORK/cases/$i.out"
    echo ""
    PASSED=$((PASSED + 1))
    i=$((i + 1))
done

if [ "$PASSED" -eq 0 ]; then
    echo "ERROR: no test case matched --only $ONLY" >&2
    exit 2
fi

echo "============================================================"
echo "ALL TESTS PASSED: $PASSED of $CASE_COUNT test cases run, $SKIPPED skipped."
echo "============================================================"
