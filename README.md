# Codepad — migration hexagonale (dépôt évaluateur)

*[English version](README_EN.md)*

> **Dépôt interne.** Il contient les solutions de référence et les tests d'évaluation : il doit rester **privé**. Un candidat ne reçoit jamais ce dépôt, seulement un pad monté avec `tools/build-pad.sh` (voir [NOTICE.md](NOTICE.md)).

Exercice d'entretien : migrer un service legacy vers une architecture hexagonale / DDD, à iso-fonctionnalité, en 60 minutes, puis en discuter 20 minutes avec le candidat.

## Déclinaisons

| Clé | Domaine | Service legacy | Niveau |
|---|---|---|---|
| `loan` | Crédit | `LoanSimulationService` | standard |
| `kyc` | Conformité | `KycComplianceService` | standard |
| `contract` | Crédit | `ContractClauseService` | standard |
| `payment` | Cash Management | `PaymentOrderValidationService` | standard |
| `sweeping` | Cash Management | `CashSweepingService` | standard |
| `funds` | Cash Management | `FundsAvailabilityService` | standard |
| `payment-senior` | Cash Management | `PaymentOrderProcessingService` | senior / tech lead |

## Organisation

| Chemin | Contenu | Livré au candidat |
|---|---|---|
| `src/main/java/.../legacy/` | Services legacy, référence de comportement (ne jamais modifier un service existant) | celui de la déclinaison |
| `src/main/java/.../application/port/in/` | Ports d'entrée | celui de la déclinaison |
| `src/main/java/.../application/port/out/*.java` | Ports de sortie génériques des déclinaisons standard | celui de la déclinaison (standard) |
| `src/main/java/.../infrastructure/InMemory*.java` | Adapters en mémoire de ces ports | celui de la déclinaison (standard) |
| `src/main/java/.../domain/<contexte>/` | Solution de référence : domaine | non |
| `src/main/java/.../application/service/` | Solution de référence : services applicatifs | non |
| `src/main/java/.../{application/port/out,infrastructure}/paymentprocessing/` | Solution senior : ports de sortie et adapters | non |
| `src/test/java/.../*BehaviorTest`, `*ParityTest`, `*CutOffTest` | Tests candidat | ceux de la déclinaison |
| `src/test/java/.../evaluation/` | Tests de l'évaluateur (pièges, bonus, persistance) | non |
| `src/test/java/.../utils/`, `HexagonalArchitectureTest` | Harness de test | oui |
| `pad/` | Énoncés (templates), `TRANSPARENCE.md`, `DECISIONS.md` | rendus par le script |
| `tools/build-pad.sh` | Montage et vérification d'un pad | non |
| `NOTICE.md`, `SOLUTION.md`, `CLAUDE.md` | Documentation interne | non |

## Commandes

```
mvn test                                                  # toutes les solutions + tous les tests (dont évaluation)
tools/build-pad.sh payment /chemin/pad --verify           # monte et vérifie un pad candidat
tools/build-pad.sh payment /tmp/check --with-solution --verify   # vérifie que la solution passe seule dans le pad
```

Java 21, JUnit 5.10.2, Maven.
