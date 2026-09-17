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

## Qu'est-ce qu'une architecture hexagonale ?

Une architecture hexagonale, aussi appelée ports et adaptateurs, vise à séparer le cœur du métier de tout ce qui est technique ou externe.

L'idée est simple :

- le domaine métier contient la logique de votre entreprise (règles, calculs, décisions);
- les ports décrivent ce dont le domaine a besoin ou ce qu'il expose sans dépendre d'une technologie précise;
- les adaptateurs implémentent ces ports avec des détails concrets (base de données, mémoire, API, etc.).

Au lieu d'avoir un code où tout est mélangé, on isole les responsabilités :

- le code métier ne dépend pas des détails d'implémentation ;
- l'infrastructure peut changer sans remettre en cause la logique métier ;
- les tests peuvent se concentrer sur le comportement métier sans dépendre de la base de données ou du framework.

En pratique, on cherche souvent à avoir des couches comme :

- `domain` : objets métier et logique de décision ;
- `application.port.in` : les cas d'utilisation / entrées du système ;
- `application.port.out` : les interfaces de sortie / persistance ;
- `infrastructure` : implémentations concrètes de ces ports ;
- `test` : vérification du comportement attendu.

## Structure de fichiers attendue

Le projet suit la structure Maven standard, avec le package racine `fr.cbtw.interview` :

```text
src/
  main/
    java/
      fr/
        cbtw/
          interview/
            application/
              port/
                in/
                out/
            domain/
              model/
              service/
            infrastructure/
  test/
    java/
      fr/
        cbtw/
          interview/
```

La logique métier doit surtout vivre dans `fr.cbtw.interview.domain`, tandis que les implémentations techniques doivent rester dans `fr.cbtw.interview.infrastructure`.

En résumé :

- `domain` = ce que l'entreprise sait faire ;
- `application.port.in` = ce que l'application peut recevoir comme commande / action ;
- `application.port.out` = ce que le domaine a besoin d'appeler pour sortir ou sauvegarder des données ;
- `infrastructure` = la façon concrète de faire ces sorties (mémoire, base de données, etc.).

## Ce que vous devez produire

Le projet suit la disposition Maven standard (`src/main/java`, `src/test/java`), racine de package `fr.cbtw.interview`. Les ports d'entrée, les ports de sortie et l'infrastructure de base sont déjà fournis dans le pad candidat pour simplifier le sujet et permettre de se concentrer sur le cœur du métier :

1. **Le domaine** (package `fr.cbtw.interview.domain`)
   - Les concepts métier du fichier legacy, modélisés en Value Objects ou Entités (pas de `String`/`double` nus pour représenter une notion métier).
   - La logique métier extraite du fichier legacy, sous forme de service de domaine implémentant le use case correspondant (`fr.cbtw.interview.application.port.in.*`).
   - Ce package ne doit dépendre d'aucun détail technique : pas d'annotation de framework, pas d'implémentation concrète de persistance (rien de `fr.cbtw.interview.infrastructure.*`).
   - Vous organisez librement les sous-packages sous `domain` (par exemple `domain.model` / `domain.service`, ou un découpage par sous-domaine `domain.contract`, `domain.kyc`...) — seul le préfixe `fr.cbtw.interview.domain` compte pour les tests, qui scannent récursivement tous les sous-packages.

2. **Les ports de sortie** (package `fr.cbtw.interview.application.port.out`)
   - Les interfaces de sortie sont déjà transmises au candidat, afin de garder le test centré sur la migration du cœur métier.
   - Il s'agit des obligations de persistance ou de communication externe que le domaine attend sans dépendre d'une implémentation concrète.

3. **L'infrastructure** (package `fr.cbtw.interview.infrastructure`)
   - Les adaptateurs concrets (par exemple en mémoire) sont également fournis au candidat pour simplifier le test.
   - L'objectif est de vérifier surtout l'organisation du domaine et la logique métier, sans faire perdre du temps sur l'implémentation technique des repositories ou des adaptateurs.

4. **Les tests** (`src/test/java/fr/cbtw/interview/`)
   - Les tests déjà présents doivent continuer à passer.
   - Ajoutez au moins un test couvrant un cas limite métier qui n'était pas testé dans le code legacy.

> Important : l'exercice vise à valider le découpage en architecture hexagonale et la logique métier. Les détails de ports de sortie et d'infrastructure ne sont pas le point central du test, c'est pourquoi ils sont transmis au candidat.

## Consignes

- Vous n'êtes pas obligé(e) de tout migrer si le temps manque. Une architecture cohérente sur un périmètre réduit vaut mieux qu'un refacto partiel et incohérent sur l'ensemble du fichier.
- Si vous faites des arbitrages (partie non traitée, simplification assumée), notez-les en commentaire à l'endroit concerné — on ne vous demande pas de tout finir, mais de savoir dire ce qui manque et pourquoi.
- Le nommage de vos classes/méthodes doit refléter le vocabulaire métier du domaine, pas des termes techniques génériques.
- Votre service de domaine doit rester instanciable sans argument (un constructeur sans paramètre, qui se branche par défaut sur votre adapter en mémoire) : c'est ce que le harness de test utilise pour retrouver votre implémentation. Rien ne vous empêche d'ajouter en plus un constructeur avec injection explicite du port de sortie.
