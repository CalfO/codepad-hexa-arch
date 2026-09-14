# Solution de référence

Ce document explique les choix de conception de la migration hexagonale fournie dans `src/main/java/domain`, `src/main/java/application/port/out` et `src/main/java/infrastructure`, pour les trois services legacy (`ContractClauseService`, `KycComplianceService`, `LoanSimulationService`). Il sert de corrigé/baseline pour évaluer les candidats — pas de référence à distribuer telle quelle.

## Pourquoi les packages sont "à plat" et sans préfixe `fr.cbtw.interview`

`src/test/java/fr/cbtw/interview/utils/ClasspathScanner.java` et `ImplementationLoader.java` (fournis, à ne pas modifier) scannent des packages **littéraux et non récursifs** :

- `ImplementationLoader.findImplementationOf(port)` cherche une implémentation dans le package `domain.service`.
- `HexagonalArchitectureTest` scanne `domain` et `domain.model`.

Ces chemins ne sont **pas** préfixés par `fr.cbtw.interview`, contrairement aux ports d'entrée déjà fournis (`fr.cbtw.interview.application.port.in.*`, à ne pas déplacer). Toute implémentation candidate doit donc vivre dans les packages top-level `domain.model` / `domain.service` pour être trouvée par réflexion — sans quoi les tests échouent silencieusement avec une `AssertionError` explicite ("Aucune classe ... n'implémente ..."). C'est un piège volontaire de l'exercice : il faut lire les utilitaires de test avant d'implémenter.

Par cohérence avec cette contrainte (et avec les chemins du README, eux aussi non préfixés : `src/domain/`, `src/application/port/`, `src/infrastructure/`), `application.port.out` et `infrastructure` sont également top-level dans cette solution. Dans un vrai projet à plusieurs bounded contexts, on sous-nommerait plutôt `domain.model.contract`, `domain.model.kyc`, etc. — ici, la contrainte du harness l'en empêche (scan non récursif), d'où le choix assumé d'un seul package `domain.model` partagé, avec des noms de classes préfixés par contexte (`Contract*`, `Kyc*`, `LoanSimulation*`) pour éviter toute collision.

## Pourquoi chaque service de domaine a un constructeur sans argument

`ImplementationLoader` instancie la classe candidate via `getDeclaredConstructor()` (sans argument) puis `newInstance()`. Une injection de dépendance "propre" (constructeur avec le port de sortie en paramètre) ne serait donc jamais instanciable par le harness de test.

Chaque service de domaine (`ContractClauseService`, `KycComplianceService`, `LoanSimulationService`) expose donc deux constructeurs :
- un constructeur avec injection explicite de l'adapter de sortie (à utiliser dans un vrai composition root) ;
- un constructeur sans argument qui se branche par défaut sur l'implémentation en mémoire, pour rester compatible avec le harness réflexif.

C'est un compromis documenté, pas une préférence de conception : en production, seul le premier constructeur devrait être utilisé.

## Comportements legacy préservés à l'identique

- **`LoanSimulationService`, taux à 0 %** : `monthlyRate = 0` rend le calcul d'amortissement `0 / (1 - 1)`, soit `NaN`. Toute comparaison avec `NaN` (`>`, `<`, `>=`) étant fausse en Java, le contrôle de taux d'endettement ne se déclenche jamais et le legacy renvoie `"APPROVED: monthly=NaN"`. Ce comportement (probable bug) est reproduit à l'identique dans `LoanSimulationRequest.simulate()` — la consigne de l'exercice est la parité fonctionnelle, pas la correction de bugs. Un test verrouille explicitement ce cas (`zeroInterestRateProducesUndefinedMonthlyPayment`).
- **État mutable partagé** : les listes statiques (`savedContracts`, `savedChecks`, `savedSimulations`) du legacy sont remplacées par un état d'instance dans les adapters `InMemory*Repository` — c'est exactement le couplage caché que l'architecture hexagonale est censée extraire vers un port de sortie, donc corrigé ici (contrairement au point ci-dessus, ce n'est pas un comportement fonctionnel observable par l'appelant).

## Nouveaux tests ajoutés (cas métier non couverts avant)

Deux tests par service, ajoutés aux fichiers existants sans toucher aux tests déjà présents :

- **Contrat** : profil de risque inconnu (`unknownRiskProfileIsRejected`) ; clause primo-accédant (`firstTimeBuyerWithinThresholdGetsReducedFeesClause`).
- **KYC** : type de document manquant (`missingDocumentTypeIsRejected`) ; frontière exacte du score de risque à 20 (`zeroDeclaredIncomeTriggersApprovedWithMonitoring`).
- **Simulation de prêt** : taux d'endettement excessif (`rejectsApplicantWithExcessiveDebtRatio`), jamais exercé par les deux tests fournis ; taux à 0 % (`zeroInterestRateProducesUndefinedMonthlyPayment`), cf. ci-dessus.

## Comment exécuter

```
mvn test
```

18 tests passent : les 12 tests fournis (inchangés) + les 6 nouveaux ci-dessus.
