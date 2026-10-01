# Exercice — {{TITLE}} : migration vers une architecture hexagonale

**Durée : 60 minutes**

## Contexte

Vous rejoignez une équipe qui fait évoluer une application historique. La classe ci-dessous tourne en production mais mélange validation, règles métier et persistance. Votre mission : la faire évoluer vers une architecture hexagonale (ports & adapters) **sans changer son comportement fonctionnel**.

```
src/main/java/fr/cbtw/interview/legacy/{{LEGACY_CLASS}}.java
```

**Ne modifiez pas ce fichier** : c'est la référence de comportement.

## Ce qui est fourni

| Élément | Emplacement |
|---|---|
| Port d'entrée (le use case à implémenter) | `application/port/in/{{USE_CASE}}.java` |
| Port de sortie (persistance) | `application/port/out/{{OUT_PORT}}.java` |
| Adapter en mémoire de ce port | `infrastructure/{{ADAPTER}}.java` |
| Tests | `src/test/java/fr/cbtw/interview/` |

Le port de sortie est **générique** : c'est à vous de choisir le type métier qu'il enregistre (`{{OUT_PORT}}<VotreType>`).

Les tests fournis :
- `*BehaviorTest` : quelques cas lisibles, valeur attendue exacte ;
- `*ParityTest` : **double run** — chaque entrée est jouée sur le legacy et sur votre implémentation, et les réponses doivent être identiques au caractère près ;
- `HexagonalArchitectureTest` : le cœur (`domain`, `application`) ne doit référencer ni `infrastructure` ni `legacy`, et les types du domaine doivent être immuables.

Au départ, `mvn test` échoue : aucune implémentation du use case n'existe encore.

## Ce que vous devez produire

1. **Le domaine**, sous `fr.cbtw.interview.domain` : les concepts métier du legacy en Value Objects / Entités (pas de `String` ou `double` nus pour porter une notion métier), et les règles métier. Aucun détail technique.
2. **L'implémentation du use case** `{{USE_CASE}}`, sous `fr.cbtw.interview.domain` **ou** `fr.cbtw.interview.application` (service applicatif qui orchestre le domaine). Les deux placements sont acceptés : justifiez le vôtre.
3. **Au moins un test** couvrant un cas limite métier que les tests fournis ne couvrent pas.

Les sous-packages sont libres : les tests cherchent récursivement sous `domain` et `application`.

### Câblage : les tests jouent le rôle de composition root

Votre implémentation reçoit ses dépendances **par constructeur**. Le harness de test (`utils/ImplementationLoader`) la construit et injecte :
- l'adapter de `infrastructure` qui implémente chaque port demandé en paramètre ;
- un `java.time.Clock` si votre constructeur en demande un.

Le cœur ne doit donc jamais faire `new {{ADAPTER}}()` lui-même.

## Consignes

- Une architecture cohérente sur un périmètre réduit vaut mieux qu'un refacto partiel et incohérent sur l'ensemble du fichier.
- Notez vos arbitrages (partie non traitée, simplification assumée) en commentaire, à l'endroit concerné. On ne vous demande pas de tout finir, mais de savoir dire ce qui manque et pourquoi.
- Le nommage doit refléter le vocabulaire métier, pas des termes techniques génériques.
- Si un comportement du legacy vous semble être un bug, notez-le et expliquez ce que vous en avez fait.

**Pour aller plus loin (facultatif)** : {{BONUS}}

## Ce qui est évalué

| Critère | Ce qu'on regarde |
|---|---|
| Iso-fonctionnalité | Les tests de parité passent. Les écarts sont repérés et assumés. |
| Modélisation du domaine | Value Objects, invariants, nommage métier, place des règles. |
| Architecture | Sens des dépendances, rôle des ports, câblage depuis l'extérieur. |
| Tests | Pertinence du ou des cas limites ajoutés. |
| Arbitrages | Clarté des notes : ce qui est fait, ce qui ne l'est pas, pourquoi. |

La séance se termine par environ 20 minutes d'échange sur votre code.

## Usage de l'IA

Les assistants IA sont autorisés. Déclarez-les dans `TRANSPARENCE.md` : quel outil, pour quoi faire, ce que vous avez vérifié ou corrigé vous-même. Ce que vous rendez doit être du code que vous savez expliquer et défendre.
