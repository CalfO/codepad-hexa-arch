# Solution de référence et grille d'évaluation

Document interne, à ne jamais distribuer. Il explique les choix de la solution de référence pour les sept déclinaisons, liste les pièges de chaque legacy et propose une grille d'évaluation.

## Organisation de la solution

- **Domaine** : un package par contexte métier, `fr.cbtw.interview.domain.<contexte>` (`loan`, `kyc`, `contract`, `payment`, `sweeping`, `funds`, `paymentprocessing`). Aucun type n'est partagé entre contextes, donc chaque solution se copie seule dans un pad (`tools/build-pad.sh … --with-solution` le vérifie).
- **Use cases** : un service applicatif par déclinaison, `fr.cbtw.interview.application.service.<Service>ApplicationService`. Il implémente le port d'entrée, orchestre le domaine, appelle le port de sortie et traduit les refus du domaine en chaînes du contrat legacy. Le domaine ne connaît ni les ports d'entrée ni le format des réponses.
- **Ports de sortie** : génériques pour les déclinaisons standard (`LoanSimulationRepository<S>`). Ils compilent sans le domaine et n'imposent aucun nom. En senior, les ports sont propres à la solution (`application.port.out.paymentprocessing`) et ne sont pas livrés au candidat.

## Le câblage : les tests sont le composition root

Avant, le harness instanciait le use case par un constructeur sans argument. Le domaine devait donc faire `new InMemory…Repository()`, ce qui le faisait dépendre de l'infrastructure, contrairement à ce que demande l'énoncé. `HexagonalArchitectureTest` ne le voyait pas.

Désormais :
- `ImplementationLoader` construit le use case par son constructeur le plus riche dont il sait fournir tous les paramètres : `Clock` (horloge système, ou fixe via `wiring().provide(...)`), l'adapter de `infrastructure` pour chaque interface (récursivement), ou toute classe concrète du projet. Plus aucune solution n'a de constructeur sans argument.
- `HexagonalArchitectureTest` lit le **bytecode** des classes de `domain` et `application`. Toute référence à `infrastructure` ou à `legacy` est détectée, y compris un `new` dans un constructeur ou un appel dans un corps de méthode. Les types du domaine doivent avoir tous leurs champs `final`, statiques compris, ce qui attrape un candidat qui recopie la `static List` du legacy.
- Le use case peut vivre sous `domain` **ou** `application` (scan récursif des deux). Les sous-packages sont libres.

## Tests

| Fichier | Livré | Rôle |
|---|---|---|
| `<Service>BehaviorTest` | oui | Quelques cas lisibles, valeur exacte (`assertEquals`, plus de `contains`) |
| `<Service>ParityTest` | oui | Double run : environ 25 entrées jouées sur le legacy et sur le candidat, comparées au caractère près. Senior : 10 scénarios à état, legacy remis à zéro par réflexion |
| `PaymentOrderProcessingCutOffTest` | senior | Cut-offs et calendrier à horloge fixe, injection d'une `Clock` exigée |
| `HexagonalArchitectureTest` | oui | Dépendances du cœur et immuabilité du domaine |
| `evaluation/<Service>EvaluationTest` | non | Pièges, bornes, bonus `Clock`, persistance des seuls résultats acceptés |

Les jeux de parité livrés **évitent volontairement les pièges** listés plus bas : le candidat doit pouvoir les découvrir lui-même, ce qui alimente l'échange. Ils sont dans les tests d'évaluation. Les six tests de cas limites qui étaient livrés aux candidats (dont `zeroInterestRateProducesUndefinedMonthlyPayment`) y ont été déplacés.

`mvn test` sur ce dépôt : 277 tests, tous verts.

## Pièges et comportements legacy préservés

Règle de la solution : **parité stricte**. Chaque piège est reproduit et commenté dans le code. C'est le sujet d'échange principal : reproduire le bug ou le corriger ? Une correction est une évolution, à faire valider par le métier, idéalement après la migration.

| Déclinaison | Piège | Ce que fait le legacy |
|---|---|---|
| `loan` | Taux à 0 % | `0 / (1 - 1)` donne `NaN`. Toute comparaison avec `NaN` étant fausse, le ratio d'endettement n'est jamais rejeté : `APPROVED: monthly=NaN`. |
| `loan` | Revenu ≤ 0 | Revenu 0 : ratio infini, rejet. Revenu négatif : ratio négatif, **approbation**. |
| `kyc` | Horloge système | `LocalDate.now()` rend les cas d'expiration non déterministes. La solution injecte une `Clock` (bonus). |
| `kyc` | Ordre des contrôles | Document expiré **et** émis dans le futur : « expired » l'emporte. Revenu exactement 1000 : pas de malus. |
| `contract` | Seuils | 200 000 € exactement : pas de garantie hypothécaire, frais réduits accordés. 240 mois : pas d'assurance long terme. Codes sensibles à la casse. |
| `payment` | Décimales en `double` | `Math.round(19.99 * 100) != 19.99 * 100` : 19,99 €, 1,15 € ou 0,29 € sont refusés (« more than 2 decimals »). |
| `payment` | Dates | Date absente : « execution date in the past ». Instantané le week-end : pas de report. Cut-off strict (16:00:00 passe encore). `LocalDate.now()` et `LocalTime.now()` sont lus séparément, ce qui pose un problème autour de minuit ; la solution lit l'heure une seule fois. |
| `sweeping` | `-0.0` | Sous-compte à l'équilibre avec un minimum de 0 : `|0.0| < 0` est faux, donc branche « financement », et `SWEEP: FROM_MASTER=-0.0` est **enregistré**. |
| `sweeping` | Arrondis | Financement partiel : le solde du compte centralisateur est rendu non arrondi, alors que les montants pleins sont arrondis au centime. |
| `funds` | En-cours négatifs | Les débits en attente ne sont pas validés : une valeur négative augmente le disponible. Le statut du compte prime sur la validité du montant. |
| `funds` | Seuil de découvert | Usage du découvert à exactement 80 % : pas d'approbation. Artefacts flottants dans `remaining`. |

## Grille d'évaluation (standard)

| Critère | Attendu | Signal fort | Signal faible |
|---|---|---|---|
| Iso-fonctionnalité | Les tests de parité passent | Pièges repérés et arbitrés par écrit | « Correction » silencieuse d'un piège, `BigDecimal` qui casse la parité sans le dire |
| Modélisation | Value Objects avec invariants, enums métier | Règles dans le domaine, refus typés, pas de `String`/`double` nus | Service anémique qui recopie le legacy, chaînes de résultat dans le domaine |
| Architecture | Dépendances vers le cœur, constructeur avec port | Explique le rôle du port générique et du harness | `new InMemory…` dans le cœur, délégation au legacy (détectés par les tests) |
| Placement du use case | `domain` ou `application`, les deux sont acceptés | Sait pourquoi : un service applicatif qui implémente `port.in` évite que le domaine dépende de la couche application | Ne se pose pas la question |
| Tests | Au moins un cas limite pertinent | Trouve un piège non couvert (NaN, `-0.0`, 19,99) | Duplique un cas déjà couvert |
| Bonus | `Clock` injectée (`kyc`, `payment`) | Test déterministe du cut-off ou de l'expiration | — |
| IA | `TRANSPARENCE.md` renseigné | Sait dire ce qu'il a vérifié ou refusé | Code qu'il ne sait pas expliquer |

## Déclinaison senior / tech lead : `payment-senior`

Le legacy fait environ 120 lignes et porte neuf responsabilités. Le candidat conçoit lui-même ses ports.

**Solution de référence** :
- ports `PaymentOrderJournal` (`contains`, `committedAmountInEur`, `record`) et `TreasuryNotifier` (`approvalRequired`), plus `Clock` ;
- adapters `InMemoryPaymentOrderJournal`, qui somme dans l'ordre d'insertion pour garder le même arrondi flottant, et `ConsoleTreasuryNotifier`, qui écrit le même message que le legacy ;
- domaine : `PaymentReference` (clé d'idempotence), `Iban`, `Money`/`Currency` (taux codés en dur), `SepaZone`, `PaymentScheme` (sélection, cut-off, frais), `BankingCalendar`, `DailyLimit`, l'agrégat `PaymentOrder`, puis `ScheduledPayment` et `ProcessedPaymentOrder`.

**Pièges spécifiques** : décimales en `double` (comme `payment`) ; un ordre refusé n'est pas enregistré, donc sa référence peut être resoumise ; un ordre `PENDING_APPROVAL` compte dans le plafond et bloque sa référence ; le filtre `!REJECTED` du legacy est du code mort, puisqu'un ordre refusé n'est jamais enregistré ; plafond atteint exactement : accepté ; EUR vers le Royaume-Uni ou la Suisse en SWIFT (liste SEPA incomplète) ; calendrier limité à Noël et au 1er janvier ; date absente = « dès que possible » (et non « dans le passé » comme en standard) ; cut-off inclusif (`!isBefore`), alors que la version standard est stricte.

**Réponses attendues dans `DECISIONS.md`** :
1. **Mise en production** : strangler fig derrière le port d'entrée. Double run en production en *shadow* : le legacy fait foi, le nouveau code calcule sans effet de bord et les écarts sont journalisés. Bascule sur un taux d'écart nul pendant une période représentative (fins de mois, jours fériés), avec un feature flag par lot de clients ou par schéma, et retour arrière immédiat par le flag. Les tests de parité en sont la version miniature.
2. **Concurrence** : non. « Lire la somme engagée, puis enregistrer » n'est pas atomique, donc deux soumissions simultanées passent toutes deux sous le plafond. Même chose pour le contrôle de doublon (vérification puis insertion). Correctifs possibles : contrainte d'unicité sur la référence, verrou ou sérialisation par débiteur et date (`SELECT … FOR UPDATE`, version optimiste sur un agrégat « exposition journalière »), ou un traitement séquentiel par débiteur via une file. C'est une responsabilité de l'adapter et de la transaction, pas du domaine.
3. **Écarts** : décimales en `double`, liste SEPA, calendrier TARGET2 incomplet, taux de change en dur, double lecture de l'horloge, `System.out` comme canal d'alerte. Ce qui est attendu : reproduire d'abord, isoler chaque écart derrière un concept nommé, puis corriger un par un avec validation du métier et un test qui documente le changement.

**Grille senior, en plus de la grille standard** : identification des ports sans aide, avec des noms métier et pas un `Repository` générique ; maîtrise du temps ; état isolé par instance ; qualité de la stratégie de bascule ; concurrence repérée spontanément ; capacité à prioriser en 60 minutes (chemin principal propre plutôt que tout couvrir à moitié).
