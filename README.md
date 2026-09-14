# Exercice — Migration d'un module métier vers une architecture hexagonale

**Durée : 60 minutes**

## Contexte

Vous rejoignez une équipe chargée de faire évoluer une application métier historique. Le code ci-dessous fonctionne en production, mais mélange plusieurs responsabilités (validation, calcul métier, persistance) dans une seule classe. Votre mission : le faire évoluer vers une architecture hexagonale, sans en changer le comportement fonctionnel.

## Fichier à migrer

Le code à faire évoluer se trouve ici :

```
src/main/java/fr/cbtw/interview/legacy/<NomDuService>.java
```

> Remplacer `<NomDuService>` par le nom réel du fichier fourni dans ce pad (visible dans l'arborescence à gauche).

**Ne modifiez pas ce fichier.** Il sert de référence de comportement : les tests fournis dans `src/test/java/fr/cbtw/interview/` tournent dessus (indirectement, via les use cases) et doivent continuer à passer une fois votre refactorisation terminée (que le code final appelle la nouvelle architecture ou non).

## Ce que vous devez produire

Le projet suit la disposition Maven standard (`src/main/java`, `src/test/java`), racine de package `fr.cbtw.interview`. Les ports d'entrée sont déjà fournis dans `fr.cbtw.interview.application.port.in` — à vous d'ajouter le reste :

1. **Le domaine** (package `fr.cbtw.interview.domain`)
   - Les concepts métier du fichier legacy, modélisés en Value Objects ou Entités (pas de `String`/`double` nus pour représenter une notion métier).
   - La logique métier extraite du fichier legacy, sous forme de service de domaine implémentant le use case correspondant (`fr.cbtw.interview.application.port.in.*`).
   - Ce package ne doit dépendre d'aucun détail technique : pas d'annotation de framework, pas d'implémentation concrète de persistance (rien de `fr.cbtw.interview.infrastructure.*`).
   - Vous organisez librement les sous-packages sous `domain` (par exemple `domain.model` / `domain.service`, ou un découpage par sous-domaine `domain.contract`, `domain.kyc`...) — seul le préfixe `fr.cbtw.interview.domain` compte pour les tests, qui scannent récursivement tous les sous-packages.

2. **Les ports de sortie** (package `fr.cbtw.interview.application.port.out`)
   - Une interface représentant le besoin de persistance, indépendante de son implémentation.

3. **L'adapter** (package `fr.cbtw.interview.infrastructure`)
   - Une implémentation concrète du port de sortie (par exemple en mémoire), qui vient se brancher sur le domaine sans que le domaine ait connaissance d'elle.

4. **Les tests** (`src/test/java/fr/cbtw/interview/`)
   - Les tests déjà présents doivent continuer à passer.
   - Ajoutez au moins un test couvrant un cas limite métier qui n'était pas testé dans le code legacy.

## Consignes

- Vous n'êtes pas obligé(e) de tout migrer si le temps manque. Une architecture cohérente sur un périmètre réduit vaut mieux qu'un refacto partiel et incohérent sur l'ensemble du fichier.
- Si vous faites des arbitrages (partie non traitée, simplification assumée), notez-les en commentaire à l'endroit concerné — on ne vous demande pas de tout finir, mais de savoir dire ce qui manque et pourquoi.
- Le nommage de vos classes/méthodes doit refléter le vocabulaire métier du domaine, pas des termes techniques génériques.
- Votre service de domaine doit rester instanciable sans argument (un constructeur sans paramètre, qui se branche par défaut sur votre adapter en mémoire) : c'est ce que le harness de test utilise pour retrouver votre implémentation. Rien ne vous empêche d'ajouter en plus un constructeur avec injection explicite du port de sortie.
