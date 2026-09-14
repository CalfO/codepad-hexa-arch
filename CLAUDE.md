# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

The author's working copy of a 60-minute hexagonal-architecture / DDD interview exercise (see README.md, in French) for three legacy Java services. This repo holds **both** the candidate-facing exercise materials (legacy code, ports, tests) **and** a reference solution (`SOLUTION.md` + the `domain`/`application`/`infrastructure` packages) used to evaluate candidates. When asked to change the exercise itself, keep the reference solution in sync; when asked to work on the reference solution, don't accidentally leak it into files a candidate pad would ship with (e.g. `src/legacy/`, the pre-provided ports, or the test harness under `utils/`).

## Build & test

```
mvn test          # compile + run all tests
mvn clean test     # force a clean rebuild
```

Java 21, JUnit 5.10.2, Maven Surefire. Standard Maven layout: `src/main/java`, `src/test/java`.

## Hard constraints

- **Never modify files under `src/main/java/fr/cbtw/interview/legacy/`.** They are the behavioral reference — tests run against them (indirectly, via the use-case ports) and must keep passing regardless of the refactor.
- **Never modify `src/test/java/fr/cbtw/interview/utils/ClasspathScanner.java` or `ImplementationLoader.java`.** These are the exercise's test harness, not exercise scaffolding to complete.

## The reflection-based test harness — read before adding an implementation

`ImplementationLoader.findImplementationOf(port)` finds a candidate's use-case implementation by scanning the classpath, **non-recursively**, for a class assignable to `port` inside a **literal, non-prefixed** package:

- default search package: `domain.service`
- `HexagonalArchitectureTest` similarly scans `domain` and `domain.model` (also non-prefixed, non-recursive)

This means any use-case implementation **must** live in the top-level packages `domain.model` / `domain.service` — not nested under `fr.cbtw.interview.*` like the pre-provided ports (`fr.cbtw.interview.application.port.in.*`). Getting this wrong makes `ImplementationLoader` throw an `AssertionError` saying no implementation was found, which is by design — it's meant to force careful reading of the harness before implementing.

The loader also instantiates the found class via a **no-arg constructor** (`getDeclaredConstructor()` with no args). A use-case implementation that only takes its output port via constructor injection won't be instantiable by the harness — it needs a no-arg constructor that self-wires a default adapter (see `SOLUTION.md` for how the reference solution handles this trade-off).

## Reference solution

`SOLUTION.md` documents the design decisions behind the hexagonal migration under `src/main/java/domain/`, `src/main/java/application/port/out/`, and `src/main/java/infrastructure/` — package layout rationale, the no-arg-constructor trade-off, and legacy quirks preserved on purpose (e.g. a 0%-interest-rate loan simulation produces `NaN` and is still reported `APPROVED`, matching the legacy bug rather than fixing it). Six new edge-case tests (two per service) were added to the existing behavior test files, not new files, so the original tests stay untouched.

## Actual vs. README paths

The README describes the target layout as `src/domain/`, `src/application/port/`, `src/infrastructure/`, `src/test/` — those map to what's actually on disk (Maven's `src/main/java/...` and `src/test/java/...`, with the packages named literally `domain`, `application.port`, `infrastructure` as required by the test harness above).
