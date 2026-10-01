# Codepad — hexagonal migration (evaluator repository)

> **Internal repository.** It contains the reference solutions and the evaluation tests: it must stay **private**. A candidate never receives this repository, only a pad built with `tools/build-pad.sh` (see [NOTICE.md](NOTICE.md), in French).

Interview exercise: migrate a legacy service to a hexagonal / DDD architecture at functional parity in 60 minutes, then discuss it with the candidate for 20 minutes.

## Variants

| Key | Domain | Legacy service | Level |
|---|---|---|---|
| `loan` | Lending | `LoanSimulationService` | standard |
| `kyc` | Compliance | `KycComplianceService` | standard |
| `contract` | Lending | `ContractClauseService` | standard |
| `payment` | Cash Management | `PaymentOrderValidationService` | standard |
| `sweeping` | Cash Management | `CashSweepingService` | standard |
| `funds` | Cash Management | `FundsAvailabilityService` | standard |
| `payment-senior` | Cash Management | `PaymentOrderProcessingService` | senior / tech lead |

## Layout

| Path | Content | Shipped to the candidate |
|---|---|---|
| `src/main/java/.../legacy/` | Legacy services, the behavioral reference (never modify an existing one) | the variant's one |
| `src/main/java/.../application/port/in/` | Input ports | the variant's one |
| `src/main/java/.../application/port/out/*.java` | Generic output ports of the standard variants | the variant's one (standard) |
| `src/main/java/.../infrastructure/InMemory*.java` | In-memory adapters for those ports | the variant's one (standard) |
| `src/main/java/.../domain/<context>/` | Reference solution: domain | no |
| `src/main/java/.../application/service/` | Reference solution: application services | no |
| `src/main/java/.../{application/port/out,infrastructure}/paymentprocessing/` | Senior solution: output ports and adapters | no |
| `src/test/java/.../*BehaviorTest`, `*ParityTest`, `*CutOffTest` | Candidate tests | the variant's ones |
| `src/test/java/.../evaluation/` | Evaluator tests (traps, bonus, persistence) | no |
| `src/test/java/.../utils/`, `HexagonalArchitectureTest` | Test harness | yes |
| `pad/` | Statements (templates, FR and EN for standard), `TRANSPARENCE.md`, `DECISIONS.md` | rendered by the script |
| `tools/build-pad.sh` | Builds and verifies a pad | no |
| `NOTICE.md`, `SOLUTION.md`, `CLAUDE.md` | Internal documentation | no |

## Commands

```
mvn test                                                  # all solutions + all tests (evaluation included)
tools/build-pad.sh payment /path/to/pad --verify          # builds and checks a candidate pad
tools/build-pad.sh payment /tmp/check --with-solution --verify   # checks the solution passes on its own in the pad
```

Java 21, JUnit 5.10.2, Maven.
