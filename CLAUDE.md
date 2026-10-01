# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

The author's evaluator repo for a 60-minute hexagonal-architecture / DDD interview exercise (legacy Java service → ports & adapters, at functional parity). It holds **both** the candidate-facing material (legacy services, ports, candidate tests, statement templates) **and** the reference solutions + evaluator-only tests. Candidates never get this repo: `tools/build-pad.sh <key> <dir> --verify` assembles a pad with one declension only (see `NOTICE.md`). Docs are in French; `README.md` / `README_EN.md` (root) are an internal overview (keep them in sync), the candidate statements are templates in `pad/`.

Seven declensions: `loan`, `kyc`, `contract` (retail credit), `payment`, `sweeping`, `funds` (cash management), and `payment-senior` (senior / tech-lead, no output ports provided). Their file manifest lives in `tools/build-pad.sh` (`declinaison()`): when adding or renaming a declension, update that function, the `ALL_KEYS` list, `NOTICE.md` and `SOLUTION.md` together.

## Build & test

```
mvn test                                              # everything, reference solutions + evaluation tests
tools/build-pad.sh <key> <dir> --verify               # candidate pad must compile and fail only on "no implementation"
tools/build-pad.sh <key> <dir> --with-solution --verify   # that declension's solution alone must be green in its pad
```

Java 21, JUnit 5.10.2 (`junit-jupiter`, includes params), Maven Surefire.

## Hard constraints

- **Never modify existing files under `src/main/java/fr/cbtw/interview/legacy/`.** They are the behavioral reference; parity tests compare candidate output to them character for character. Adding a new legacy service for a new declension is fine.
- Candidate-facing files must not reference solution code: ports in `application/port/out/*.java` are generic (`XxxRepository<T>`) precisely so a pad compiles without `domain/`. The build script greps every pad for leaks (other declensions, `domain.`, `application.service`, `evaluation`, leftover `{{placeholders}}`).
- Candidate parity tests deliberately avoid the legacy traps (0 % rate → NaN, `double` decimal check rejecting 19.99, sweeping `-0.0`, …). Traps go in `src/test/java/fr/cbtw/interview/evaluation/` only.
- Each reference solution must be self-contained: `domain/<context>/` + `application/service/<Service>ApplicationService.java` (+ senior `…/paymentprocessing/` ports and adapters), no type shared across contexts.

## The test harness

`src/test/java/fr/cbtw/interview/utils/` (`ClasspathScanner`, `ImplementationLoader`) and `HexagonalArchitectureTest` ship with every pad. A candidate pad shouldn't touch them; this repo's author redesigns them on request.

- `ClasspathScanner` scans a package **recursively**.
- `ImplementationLoader` looks for the single implementation of an input port under `fr.cbtw.interview.domain` **and** `fr.cbtw.interview.application`, then builds it through its richest constructor whose parameters it can resolve: `java.time.Clock` (system clock, or a fixed one via `wiring().provide(Clock.class, …)`), the unique concrete adapter under `fr.cbtw.interview.infrastructure` for any interface (built recursively), or any concrete project class. The test is the composition root: no use case needs a no-arg constructor, and none should `new` an adapter. `wiring().hasInjected(Clock.class)` / `instanceOf(Adapter.class)` let evaluation tests check the Clock bonus and what got persisted.
- `HexagonalArchitectureTest` reads class-file bytes of `domain` + `application` classes and fails on any reference to `fr/cbtw/interview/infrastructure/` or `fr/cbtw/interview/legacy/`; it also requires at least one class under `domain` and every non-synthetic field of a `domain` class (static included) to be `final`.

## Reference solution conventions

Domain objects throw a context-specific `XxxRejectedException(XxxRejection)`; the application service maps the enum to the legacy's exact output string. Legacy quirks are reproduced on purpose and commented in place, with the rationale in `SOLUTION.md` (which also holds the evaluation grid and the expected senior `DECISIONS.md` answers).
