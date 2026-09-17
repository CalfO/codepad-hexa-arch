# Solution de référence

Ce document explique les choix de conception de la migration hexagonale fournie dans `src/main/java/fr/cbtw/interview/domain`, `src/main/java/fr/cbtw/interview/application/port/out` et `src/main/java/fr/cbtw/interview/infrastructure`, pour les trois services legacy (`ContractClauseService`, `KycComplianceService`, `LoanSimulationService`). Il sert de corrigé/baseline pour évaluer les candidats — pas de référence à distribuer telle quelle.

## Packages nichés sous `fr.cbtw.interview`

`src/test/java/fr/cbtw/interview/utils/ClasspathScanner.java` scanne désormais un package **et tous ses sous-packages, récursivement**. `ImplementationLoader.findImplementationOf(port)` s'en sert pour chercher une implémentation sous le package par défaut `fr.cbtw.interview.domain` (plus besoin qu'elle vive exactement dans `domain.service`), et `HexagonalArchitectureTest` scanne `domain`/`domain.model` de la même façon. Une première version de cette solution avait dû placer `domain.model`/`domain.service` en top-level (sans préfixe) car le harness pointait vers des chemins non préfixés — incohérent avec le reste du projet (les ports d'entrée fournis vivent sous `fr.cbtw.interview.application.port.in.*`). Le harness a depuis été corrigé pour chercher sous `fr.cbtw.interview.domain` (et sous-packages), donc `domain` vit maintenant sous `fr.cbtw.interview`, comme le reste du code.

Le scan étant maintenant récursif, `HexagonalArchitectureTest.domainMustNotDependOnInfrastructure()` trouve bien toutes les classes de `domain.model`/`domain.service` (et de tout sous-package qu'un candidat ajouterait) et vérifie effectivement l'absence de dépendance vers `infrastructure.*` — ce n'est plus un test qui passe vide/trivialement comme avant ce changement de harness.

Dans cette variante simplifiée du test, les ports de sortie et les adaptateurs d'infrastructure sont fournis au candidat. L'objectif est de réduire le bruit technique et de garder le sujet centré sur le métier : le candidat écrit principalement le domaine et ses tests, tandis que les interfaces et implémentations concrètes de sortie sont déjà disponibles. Cette approche garde une vraie architecture hexagonale, mais déplace le point de difficulté vers la logique métier et la modélisation du domaine.

Cette solution garde `domain.model` et `domain.service` comme packages plats, partagés entre les trois contextes métier, avec des noms de classes préfixés par contexte (`Contract*`, `Kyc*`, `LoanSimulation*`) pour éviter toute collision — un choix qui reste valide et simple pour un exercice à trois services. Le scan étant maintenant récursif, rien n'empêche un sous-découpage par bounded context (`domain.model.contract`, `domain.model.kyc`, etc., ou `domain.contract.model`/`domain.contract.service`) ; ce n'est plus une contrainte du harness mais un choix d'organisation laissé au candidat (cf. README).

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
