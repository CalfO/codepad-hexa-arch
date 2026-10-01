# Exercice senior / tech lead — Traitement des ordres de paiement

**Durée : 75 minutes**, soit environ 60 minutes de code et 15 minutes pour `DECISIONS.md`.

## Contexte

Le module de traitement des ordres de paiement d'une plateforme de Cash Management tourne en production depuis des années. Il tient en une classe qui fait tout : validation, idempotence, choix du schéma (SEPA, SEPA Instant, TARGET2, SWIFT), cut-offs, calendrier bancaire, frais, plafond journalier par débiteur, persistance et alerte de la trésorerie.

```
src/main/java/fr/cbtw/interview/legacy/PaymentOrderProcessingService.java
```

**Ne modifiez pas ce fichier** : c'est la référence de comportement.

Votre mission : le migrer vers une architecture hexagonale **à iso-fonctionnalité**, et préparer sa mise en production.

## Ce qui est fourni

- Le port d'entrée `application/port/in/PaymentOrderProcessingUseCase.java`.
- Les tests (`src/test/java/fr/cbtw/interview/`) :
  - `PaymentOrderProcessingBehaviorTest` : quelques cas lisibles ;
  - `PaymentOrderProcessingParityTest` : **double run par scénario**. Une séquence de soumissions est rejouée sur le legacy et sur votre implémentation, et chaque réponse doit être identique au caractère près ;
  - `PaymentOrderProcessingCutOffTest` : cut-offs et calendrier vérifiés avec une **horloge fixe** ;
  - `HexagonalArchitectureTest` : le cœur (`domain`, `application`) ne référence ni `infrastructure` ni `legacy`, et les types du domaine sont immuables.

**Rien d'autre n'est fourni.** Vous concevez vous-même les ports de sortie et leurs adapters.

## Ce que vous devez produire

1. **Le domaine** sous `fr.cbtw.interview.domain` : concepts et règles métier, sans détail technique.
2. **L'implémentation du use case** sous `fr.cbtw.interview.domain` ou `fr.cbtw.interview.application`.
3. **Les ports de sortie** qu'exige le legacy (à vous de les identifier) sous `fr.cbtw.interview.application`, et **leurs adapters** sous `fr.cbtw.interview.infrastructure`.
4. **`DECISIONS.md`** (voir plus bas).

### Câblage : les tests jouent le rôle de composition root

Le harness (`utils/ImplementationLoader`) construit votre implémentation par son constructeur et injecte :
- pour chaque paramètre de type interface (vos ports), l'unique classe concrète de `fr.cbtw.interview.infrastructure` qui l'implémente, elle-même construite selon les mêmes règles ;
- un `java.time.Clock` si un constructeur en demande un.

**Le test de cut-off exige que l'heure courante soit injectée** sous forme de `Clock`.

## Consignes

- Priorisez. Une migration cohérente du chemin principal, avec des arbitrages notés, vaut mieux que tout couvrir à moitié.
- Notez vos arbitrages en commentaire, à l'endroit concerné.
- Le nommage reflète le vocabulaire métier du paiement.
- Si un comportement du legacy vous semble être un bug, notez-le et expliquez ce que vous en avez fait.

## `DECISIONS.md` : la partie tech lead

Une page au maximum, en bullet points. Répondez au moins à ces trois questions :

1. **Mise en production** : comment remplaceriez-vous le legacy en production sans risque pour les paiements ? Indiquez l'étape par étape, le critère de bascule et le retour arrière.
2. **Concurrence** : deux soumissions simultanées pour le même débiteur sont-elles correctement plafonnées ? Si non, où et comment le corriger ?
3. **Écarts** : quels comportements du legacy vous semblent faux ? Pour chacun, l'avez-vous reproduit ou corrigé, et comment faire valider cet arbitrage par le métier ?

## Ce qui est évalué

| Critère | Ce qu'on regarde |
|---|---|
| Iso-fonctionnalité | Les tests de parité et de cut-off passent. Les écarts sont repérés et assumés. |
| Conception des ports | Ports de sortie identifiés et nommés selon le métier, au bon niveau d'abstraction. |
| Modélisation du domaine | Value Objects, invariants, place des règles (schéma, calendrier, frais, plafond). |
| Testabilité | Maîtrise du temps, état isolé, tests ajoutés. |
| Vision tech lead | Qualité de `DECISIONS.md` : stratégie de bascule, concurrence, arbitrages avec le métier. |

La séance se termine par environ 20 minutes d'échange sur votre code et vos décisions.

## Usage de l'IA

Les assistants IA sont autorisés. Déclarez-les dans `TRANSPARENCE.md` : quel outil, pour quoi faire, ce que vous avez vérifié ou corrigé vous-même. Ce que vous rendez doit être du code et des décisions que vous savez expliquer et défendre.
