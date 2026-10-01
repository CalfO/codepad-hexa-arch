# NOTICE — monter un pad candidat

Document interne. Ne jamais le copier dans un pad.

## 0. Avant tout : le dépôt doit être privé

Ce dépôt contient les solutions de référence (`domain/`, `application/service/`, `SOLUTION.md`) et les tests d'évaluation. S'il est public, un candidat qui cherche `fr.cbtw.interview` ou le nom du dépôt trouve le corrigé. Passez-le en privé ou dans l'organisation CBTW (`gh repo edit --visibility private`). Si le dépôt a déjà été public, partez du principe que les déclinaisons historiques (`loan`, `kyc`, `contract`) ont pu circuler.

## 1. Monter le pad

```
tools/build-pad.sh <déclinaison> <dossier-cible> --verify
```

Déclinaisons : `loan`, `kyc`, `contract`, `payment`, `sweeping`, `funds` (standard) et `payment-senior` (senior / tech lead). Pour CACIB, privilégier les déclinaisons Cash Management.

Le script :
1. copie les fichiers communs et ceux de la déclinaison (tableau ci-dessous), **rien d'autre** ;
2. rend l'énoncé (`README.md`, plus `README_EN.md` en standard) avec le vrai nom du service, du use case, du port et de l'adapter : le candidat n'a rien à remplacer ;
3. ajoute `TRANSPARENCE.md` (note d'usage de l'IA) et, en senior, `DECISIONS.md` ;
4. échoue s'il trouve dans le pad une trace d'une autre déclinaison, de la solution, des tests d'évaluation ou un placeholder non remplacé ;
5. avec `--verify`, lance Maven : le pad doit **compiler**, et seuls doivent échouer les tests qui attendent l'implémentation du candidat (`AssertionError` « Aucune classe sous [fr.cbtw.interview.domain, fr.cbtw.interview.application] … n'implémente … » et `domainContainsAtLeastOneBusinessType`).

Le pad ne contient ni `.git/` ni `target/`.

## 2. Contenu d'un pad

**Communs** : `pom.xml`, `.editorconfig`, `.gitignore`, `LICENSE`, `HexagonalArchitectureTest`, `utils/ClasspathScanner`, `utils/ImplementationLoader`.

**Par déclinaison** (chemins relatifs à `src/main/java/fr/cbtw/interview/` et `src/test/java/fr/cbtw/interview/`) :

| Clé | Legacy | Port d'entrée | Port de sortie + adapter | Tests |
|---|---|---|---|---|
| `loan` | `LoanSimulationService` | `LoanSimulationUseCase` | `LoanSimulationRepository`, `InMemoryLoanSimulationRepository` | `LoanSimulation{Behavior,Parity}Test` |
| `kyc` | `KycComplianceService` | `KycComplianceUseCase` | `ComplianceCheckRepository`, `InMemoryComplianceCheckRepository` | `KycCompliance{Behavior,Parity}Test` |
| `contract` | `ContractClauseService` | `ContractClauseUseCase` | `ContractRepository`, `InMemoryContractRepository` | `ContractClause{Behavior,Parity}Test` |
| `payment` | `PaymentOrderValidationService` | `PaymentOrderValidationUseCase` | `PaymentOrderRepository`, `InMemoryPaymentOrderRepository` | `PaymentOrderValidation{Behavior,Parity}Test` |
| `sweeping` | `CashSweepingService` | `CashSweepingUseCase` | `SweepInstructionRepository`, `InMemorySweepInstructionRepository` | `CashSweeping{Behavior,Parity}Test` |
| `funds` | `FundsAvailabilityService` | `FundsAvailabilityUseCase` | `FundsReservationRepository`, `InMemoryFundsReservationRepository` | `FundsAvailability{Behavior,Parity}Test` |
| `payment-senior` | `PaymentOrderProcessingService` | `PaymentOrderProcessingUseCase` | aucun (le candidat les conçoit) | `PaymentOrderProcessing{Behavior,Parity,CutOff}Test` |

Les ports de sortie fournis sont génériques (`LoanSimulationRepository<S>`) : ils compilent sans le domaine et n'imposent aucun nom de classe métier au candidat.

## 3. Jamais dans un pad

- `domain/`, `application/service/`, `application/port/out/paymentprocessing/`, `infrastructure/paymentprocessing/` : solutions de référence ;
- `src/test/java/fr/cbtw/interview/evaluation/` : tests de l'évaluateur ;
- les fichiers des autres déclinaisons ;
- `NOTICE.md`, `SOLUTION.md`, `CLAUDE.md`, `README.md` et `README_EN.md` (racine), `tools/`, `pad/` ;
- `.git/`, `target/`.

Le script s'en charge. Ce tableau sert à contrôler un pad monté à la main.

## 4. Après la session : tests de l'évaluateur

Pour mesurer ce que les tests fournis ne couvrent pas, copiez dans le pad rendu le fichier `src/test/java/fr/cbtw/interview/evaluation/<Service>EvaluationTest.java` de la déclinaison, puis lancez `mvn test`. Il contient :
- les cas limites et les pièges du legacy (taux à 0 %, décimales en `double`, `-0.0`…), comparés au legacy ;
- le bonus `Clock` (KYC, `payment`), ignoré via `Assumptions` si le candidat ne l'a pas fait ;
- la vérification que seuls les résultats acceptés sont persistés, ignorée si le candidat n'utilise pas l'adapter fourni.

Un test ignoré n'est pas un échec : c'est une information pour la grille (voir `SOLUTION.md`).

## 5. Vérifier la solution de référence dans un pad

```
tools/build-pad.sh <déclinaison> /tmp/check --with-solution --verify
```

Le script monte le pad avec la solution de la déclinaison et ses tests d'évaluation, et vérifie que tout est vert. **Ce dossier ne doit jamais être transmis.** Ce contrôle garantit que chaque solution est autonome, c'est-à-dire qu'elle ne dépend d'aucune autre déclinaison.
