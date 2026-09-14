# Exercice — Migration d'un module métier vers une architecture hexagonale

**Durée : 60 minutes**

## Contexte

Vous rejoignez une équipe chargée de faire évoluer une application métier historique. Le code ci-dessous fonctionne en production, mais mélange plusieurs responsabilités (validation, calcul métier, persistance) dans une seule classe. Votre mission : le faire évoluer vers une architecture hexagonale, sans en changer le comportement fonctionnel.

## Fichier à migrer

Le code à faire évoluer se trouve ici :

```
src/legacy/<NomDuService>.java
```

> Remplacer `<NomDuService>` par le nom réel du fichier fourni dans ce pad (visible dans l'arborescence à gauche).

**Ne modifiez pas ce fichier.** Il sert de référence de comportement : les tests fournis dans `src/test/` tournent dessus et doivent continuer à passer une fois votre refactorisation terminée (que le code final appelle la nouvelle architecture ou non).

## Ce que vous devez produire

1. **Le domaine** (dossier `src/domain/`)
   - `model/` : les concepts métier du fichier legacy, modélisés en Value Objects ou Entités (pas de `String`/`double` nus pour représenter une notion métier).
   - `service/` : la logique métier extraite du fichier legacy, sous forme de service de domaine.
   - Ce dossier ne doit dépendre d'aucun détail technique : pas d'annotation de framework, pas d'implémentation concrète de persistance.

2. **Les ports** (dossier `src/application/port/`)
   - `in/` : une interface représentant l'action métier telle qu'elle est demandée de l'extérieur (le use case).
   - `out/` : une interface représentant le besoin de persistance, indépendante de son implémentation.

3. **L'adapter** (dossier `src/infrastructure/`)
   - Une implémentation concrète du port de sortie (par exemple en mémoire), qui vient se brancher sur le domaine sans que le domaine ait connaissance d'elle.

4. **Les tests** (dossier `src/test/`)
   - Les tests déjà présents doivent continuer à passer.
   - Ajoutez au moins un test couvrant un cas limite métier qui n'était pas testé dans le code legacy.

## Consignes

- Vous n'êtes pas obligé(e) de tout migrer si le temps manque. Une architecture cohérente sur un périmètre réduit vaut mieux qu'un refacto partiel et incohérent sur l'ensemble du fichier.
- Si vous faites des arbitrages (partie non traitée, simplification assumée), notez-les en commentaire à l'endroit concerné — on ne vous demande pas de tout finir, mais de savoir dire ce qui manque et pourquoi.
- Le nommage de vos classes/méthodes doit refléter le vocabulaire métier du domaine, pas des termes techniques génériques.

## Si vous terminez en avance

Une partie optionnelle est disponible (frontend Angular). Elle n'est pas obligatoire et n'impacte pas l'évaluation du cœur de l'exercice — voyez-la comme un bonus si le temps le permet.