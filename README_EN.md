# Exercise — Cash sweeping rule: migrating to a hexagonal architecture

**Duration: 60 minutes**

## Context

You join a team evolving a legacy application. The class below runs in production but mixes validation, business rules and persistence. Your mission: evolve it toward a hexagonal architecture (ports & adapters) **without changing its functional behavior**.

```
src/main/java/fr/cbtw/interview/legacy/CashSweepingService.java
```

**Do not modify this file**: it is the behavioral reference.

## What is provided

| Item | Location |
|---|---|
| Input port (the use case to implement) | `application/port/in/CashSweepingUseCase.java` |
| Output port (persistence) | `application/port/out/SweepInstructionRepository.java` |
| In-memory adapter for that port | `infrastructure/InMemorySweepInstructionRepository.java` |
| Tests | `src/test/java/fr/cbtw/interview/` |

The output port is **generic**: you choose the business type it stores (`SweepInstructionRepository<YourType>`).

The provided tests:
- `*BehaviorTest`: a few readable cases with exact expected values;
- `*ParityTest`: a **double run** — each input is played against the legacy and against your implementation, and the answers must be identical, character for character;
- `HexagonalArchitectureTest`: the core (`domain`, `application`) must not reference `infrastructure` or `legacy`, and domain types must be immutable.

At the start, `mvn test` fails: no implementation of the use case exists yet.

## What you must produce

1. **The domain**, under `fr.cbtw.interview.domain`: the legacy's business concepts as Value Objects / Entities (no bare `String` or `double` carrying a business notion), and the business rules. No technical details.
2. **The use case implementation** of `CashSweepingUseCase`, under `fr.cbtw.interview.domain` **or** `fr.cbtw.interview.application` (an application service orchestrating the domain). Both placements are accepted: justify yours.
3. **At least one test** covering a business edge case the provided tests do not cover.

Sub-packages are free: tests search recursively under `domain` and `application`.

### Wiring: the tests act as the composition root

Your implementation receives its dependencies **through its constructor**. The test harness (`utils/ImplementationLoader`) builds it and injects:
- the `infrastructure` adapter implementing each port requested as a parameter;
- a `java.time.Clock` if your constructor asks for one.

The core must therefore never call `new InMemorySweepInstructionRepository()` itself.

## Guidelines

- A coherent architecture on a reduced scope beats a partial, inconsistent refactoring of the whole file.
- Write down your trade-offs (untreated part, assumed simplification) as comments where they apply. You are not asked to finish everything, but to be able to say what is missing and why.
- Naming must reflect the business vocabulary, not generic technical terms.
- If a legacy behavior looks like a bug to you, note it and explain what you did about it.

**Going further (optional)**: add more edge-case tests (boundaries, special values) and note what they reveal about the legacy.

## What is evaluated

| Criterion | What we look at |
|---|---|
| Functional parity | Parity tests pass. Gaps are spotted and owned. |
| Domain modelling | Value Objects, invariants, business naming, where rules live. |
| Architecture | Dependency direction, role of the ports, wiring from the outside. |
| Tests | Relevance of the edge case(s) you added. |
| Trade-offs | Clarity of your notes: what is done, what is not, why. |

The session ends with about 20 minutes of discussion about your code.

## Use of AI

AI assistants are allowed. Declare them in `TRANSPARENCE.md`: which tool, what for, what you checked or fixed yourself. What you hand in must be code you can explain and defend.
