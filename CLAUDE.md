# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

The author's working copy of a 60-minute hexagonal-architecture / DDD interview exercise (see README.md, in French) for three legacy Java services. This repo holds **both** the candidate-facing exercise materials (legacy code, ports, tests) **and** a reference solution (`SOLUTION.md` + the `fr.cbtw.interview.{domain,application.port.out,infrastructure}` packages) used to evaluate candidates. When asked to change the exercise itself, keep the reference solution in sync; when asked to work on the reference solution, don't accidentally leak it into files a candidate pad would ship with (e.g. `src/legacy/`, the pre-provided ports).

## Build & test

```
mvn test          # compile + run all tests
mvn clean test     # force a clean rebuild
```

Java 21, JUnit 5.10.2, Maven Surefire. Standard Maven layout: `src/main/java`, `src/test/java`.

## Hard constraint

**Never modify files under `src/main/java/fr/cbtw/interview/legacy/`.** They are the behavioral reference — tests run against them (indirectly, via the use-case ports) and must keep passing regardless of the refactor.

`src/test/java/fr/cbtw/interview/utils/ClasspathScanner.java` and `ImplementationLoader.java` are the exercise's test harness — not scaffolding for a candidate to complete, so a candidate pad shouldn't touch them. This repo's author, however, actively redesigns them (e.g. the package layout below was fixed by adapting the harness itself, not by contorting the reference solution around a bad harness default) — so edit them here when asked to, just don't do it incidentally while working on the exercise content.

## The reflection-based test harness — read before adding an implementation

`ImplementationLoader.findImplementationOf(port)` finds a candidate's use-case implementation by scanning the classpath, **non-recursively**, for a class assignable to `port` inside a literal package:

- default search package: `fr.cbtw.interview.domain.service`
- `HexagonalArchitectureTest` similarly scans `fr.cbtw.interview.domain` and `fr.cbtw.interview.domain.model`

Any use-case implementation **must** live in `fr.cbtw.interview.domain.model` / `fr.cbtw.interview.domain.service` to be found — consistent now with the pre-provided ports (`fr.cbtw.interview.application.port.in.*`). Getting this wrong makes `ImplementationLoader` throw an `AssertionError` saying no implementation was found, which is by design — it's meant to force careful reading of the harness before implementing.

Because the scan is non-recursive, `HexagonalArchitectureTest.domainMustNotDependOnInfrastructure()` (scanning bare `fr.cbtw.interview.domain`) never finds any class — everything lives one level deeper, in `domain.model`/`domain.service` — so that check passes vacuously. Known limitation of the scanner, not something either package move introduced.

The loader also instantiates the found class via a **no-arg constructor** (`getDeclaredConstructor()` with no args). A use-case implementation that only takes its output port via constructor injection won't be instantiable by the harness — it needs a no-arg constructor that self-wires a default adapter (see `SOLUTION.md` for how the reference solution handles this trade-off).

## Reference solution

`SOLUTION.md` documents the design decisions behind the hexagonal migration under `fr.cbtw.interview.domain`, `fr.cbtw.interview.application.port.out`, and `fr.cbtw.interview.infrastructure` — package layout rationale, the no-arg-constructor trade-off, and legacy quirks preserved on purpose (e.g. a 0%-interest-rate loan simulation produces `NaN` and is still reported `APPROVED`, matching the legacy bug rather than fixing it). Six new edge-case tests (two per service) were added to the existing behavior test files, not new files, so the original tests stay untouched.

## Actual vs. README paths

The README describes the target layout as `src/domain/`, `src/application/port/`, `src/infrastructure/`, `src/test/`. On disk these map to Maven's `src/main/java/fr/cbtw/interview/{domain,application/port,infrastructure}` and `src/test/java/fr/cbtw/interview/...` — packages nested under the project's `fr.cbtw.interview` root, matching the pre-provided ports and the legacy package.
