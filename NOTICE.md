# NOTICE — construire un pad candidat (une déclinaison sur trois)

Document interne, **à ne jamais copier dans un pad candidat** (voir la liste d'exclusion plus bas — il s'y ajoute lui-même).

Ce repo contient **trois déclinaisons indépendantes** du même exercice (`ContractClauseService`, `KycComplianceService`, `LoanSimulationService`), plus la solution de référence complète des trois. Un pad candidat ne doit contenir **qu'une seule déclinaison**, sans aucune trace de la solution ni des deux autres déclinaisons.

## 1. Choisir la déclinaison

| Déclinaison | Fichier legacy (le seul à garder) | Port d'entrée (le seul à garder) | Test de comportement (le seul à garder) |
|---|---|---|---|
| Contrat | `src/main/java/fr/cbtw/interview/legacy/ContractClauseService.java` | `src/main/java/fr/cbtw/interview/application/port/in/ContractClauseUseCase.java` | `src/test/java/fr/cbtw/interview/ContractClauseBehaviorTest.java` |
| KYC | `src/main/java/fr/cbtw/interview/legacy/KycComplianceService.java` | `src/main/java/fr/cbtw/interview/application/port/in/KycComplianceUseCase.java` | `src/test/java/fr/cbtw/interview/KycComplianceBehaviorTest.java` |
| Simulation de prêt | `src/main/java/fr/cbtw/interview/legacy/LoanSimulationService.java` | `src/main/java/fr/cbtw/interview/application/port/in/LoanSimulationUseCase.java` | `src/test/java/fr/cbtw/interview/LoanSimulationBehaviorTest.java` |

Chaque fichier legacy n'importe que son propre port d'entrée (vérifié : aucune dépendance croisée entre déclinaisons), donc copier une seule ligne de ce tableau suffit à obtenir un module qui compile seul.

## 2. Fichiers à copier dans le pad candidat

**Communs aux trois déclinaisons (toujours copiés) :**

```
pom.xml
.editorconfig
README.md
LICENSE
src/test/java/fr/cbtw/interview/HexagonalArchitectureTest.java
src/test/java/fr/cbtw/interview/utils/ClasspathScanner.java
src/test/java/fr/cbtw/interview/utils/ImplementationLoader.java
```

**Spécifiques à la déclinaison choisie (les 3 fichiers de la ligne retenue dans le tableau ci-dessus) :**

```
src/main/java/fr/cbtw/interview/legacy/<Service>.java
src/main/java/fr/cbtw/interview/application/port/in/<Service>UseCase.java
src/test/java/fr/cbtw/interview/<Service>BehaviorTest.java
```

Rien d'autre sous `src/main` ou `src/test` ne doit exister dans le pad : le candidat crée lui-même `domain/`, `application/port/out/` et `infrastructure/` pendant l'exercice.

## 3. Fichiers à supprimer / à ne jamais copier

- **La solution de référence entière**, quelle que soit la déclinaison choisie :
  ```
  src/main/java/fr/cbtw/interview/domain/
  src/main/java/fr/cbtw/interview/application/port/out/
  src/main/java/fr/cbtw/interview/infrastructure/
  ```
- **Les deux déclinaisons non retenues** (legacy, port `in`, test de comportement) — cf. les deux lignes non choisies du tableau.
- **Les documents internes à l'auteur** (rédigés pour préparer/évaluer l'exercice, pas pour le candidat) :
  ```
  CLAUDE.md
  SOLUTION.md
  NOTICE.md
  ```
- **Les artefacts de build et le dépôt git**, qui ne doivent pas voyager avec le pad (et qui, pour `.git/`, contiendraient tout l'historique donc la solution) :
  ```
  target/
  .git/
  ```

## 4. Vérification après montage du pad

Dans le pad ainsi constitué, `mvn test` doit **échouer** avant toute intervention du candidat :

- `ImplementationLoader.findImplementationOf(...)` lève une `AssertionError` (« Aucune classe sous le package 'fr.cbtw.interview.domain' ... n'implémente ... ») dans le test de comportement de la déclinaison, car `domain/` n'existe pas encore.
- `HexagonalArchitectureTest.atLeastOneValueObjectExistsInDomainModel()` échoue pour la même raison (aucune classe dans `domain.model`).

C'est l'état de départ attendu : ces échecs sont ce que le candidat doit faire passer au vert en 60 minutes en écrivant son propre `domain/`, `application/port/out/` et `infrastructure/` (organisés comme il le souhaite sous `fr.cbtw.interview.domain`, cf. README.md).
