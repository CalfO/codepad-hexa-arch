# Exercise — Migrating a business module to a hexagonal architecture

**Duration: 60 minutes**

## Context

You join a team in charge of evolving a legacy business application. The code below works in production, but it mixes several responsibilities (validation, business calculation, persistence) in a single class. Your mission is to evolve it toward a hexagonal architecture without changing its functional behavior.

## File to migrate

The code to evolve is here:

```text
src/main/java/fr/cbtw/interview/legacy/<ServiceName>.java
```

> Replace `<ServiceName>` with the real file name provided in this pad (visible in the left-side tree).

**Do not modify this file.** It serves as the behavioral reference: the tests in `src/test/java/fr/cbtw/interview/` run against it indirectly (via the use cases) and must continue to pass once your refactoring is complete (whether the final code calls the new architecture or not).

## What is a hexagonal architecture?

A hexagonal architecture, also called ports and adapters, aims to separate the business core from anything technical or external.

The idea is simple:

- the business domain contains the logic of the company (rules, calculations, decisions);
- the ports describe what the domain needs or exposes without depending on a specific technology;
- the adapters implement these ports with concrete details (database, in-memory storage, API, etc.).

Instead of having code where everything is mixed together, we isolate responsibilities:

- business code does not depend on implementation details;
- infrastructure can change without compromising the business logic;
- tests can focus on business behavior without depending on the database or framework.

In practice, we often aim for layers such as:

- `domain`: business objects and decision logic;
- `application.port.in`: use cases / system inputs;
- `application.port.out`: output / persistence interfaces;
- `infrastructure`: concrete implementations of these ports;
- `test`: verification of expected behavior.

## Expected file structure

The project follows the standard Maven layout, with the root package `fr.cbtw.interview`:

```text
src/
  main/
    java/
      fr/
        cbtw/
          interview/
            application/
              port/
                in/
                out/
            domain/
              model/
              service/
            infrastructure/
  test/
    java/
      fr/
        cbtw/
          interview/
```

The business logic should mainly live in `fr.cbtw.interview.domain`, while technical implementations should remain in `fr.cbtw.interview.infrastructure`.

In short:

- `domain` = what the company knows how to do;
- `application.port.in` = what the application can receive as a command or action;
- `application.port.out` = what the domain needs to call to produce output or save data;
- `infrastructure` = the concrete way to handle these outputs (memory, database, etc.).

## What you must produce

The project follows the standard Maven layout (`src/main/java`, `src/test/java`) with package root `fr.cbtw.interview`. The input ports, output ports, and basic infrastructure are already provided in the candidate pad to simplify the exercise and keep the focus on the business core:

1. **The domain** (package `fr.cbtw.interview.domain`)
   - The business concepts from the legacy file, modeled as Value Objects or Entities (no naked `String`/`double` used to represent a business concept).
   - The extracted business logic from the legacy file, implemented as a domain service that implements the corresponding use case (`fr.cbtw.interview.application.port.in.*`).
   - This package must not depend on any technical detail: no framework annotations, no concrete persistence implementation (nothing in `fr.cbtw.interview.infrastructure.*`).
   - You can organize subpackages under `domain` freely (for example `domain.model` / `domain.service`, or a split by subdomain such as `domain.contract`, `domain.kyc`...) — only the prefix `fr.cbtw.interview.domain` matters for the tests, which scan recursively through all subpackages.

2. **The output ports** (package `fr.cbtw.interview.application.port.out`)
   - The output interfaces are already passed to the candidate to keep the test focused on the migration of the business core.
   - These are the persistence or external communication obligations that the domain expects without depending on a concrete implementation.

3. **The infrastructure** (package `fr.cbtw.interview.infrastructure`)
   - Concrete adapters (for example in-memory ones) are also provided to the candidate to simplify testing.
   - The goal is mainly to validate the organization of the domain and the business logic without wasting time on repository or adapter implementation details.

4. **The tests** (`src/test/java/fr/cbtw/interview/`)
   - The tests already present must continue to pass.
   - Add at least one test covering a business edge case that was not already covered in the legacy code.

> Important: the exercise is intended to validate separation into a hexagonal architecture and the business logic. The details of output ports and infrastructure are not the central point of the test, which is why they are provided to the candidate.

## Instructions

- You are not required to migrate everything if time is short. A coherent architecture on a reduced scope is better than a partial and inconsistent refactor across the entire file.
- If you make trade-offs (unhandled portion, assumed simplification), note them in a comment at the relevant location — you are not asked to finish everything, but you should be able to explain what is missing and why.
- The naming of your classes and methods should reflect the business vocabulary of the domain, not generic technical terms.
- Your domain service must remain instantiable without arguments (a no-arg constructor that defaults to your in-memory adapter): this is what the test harness uses to find your implementation. Nothing prevents you from also adding an explicit constructor with output-port injection.

## Deliverable

Your goal is to produce a clean business-driven design where the legacy behavior is preserved, the business logic is isolated, and the architecture clearly respects the principles of ports and adapters.

The main evaluation criteria are:

- correct separation of responsibilities;
- business logic extracted from the legacy module;
- domain model expressed with meaningful types rather than raw primitives;
- no technical leakage into the business layer;
- preservation of existing behavior while adding one relevant edge-case test.
