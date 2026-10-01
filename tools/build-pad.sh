#!/usr/bin/env bash
# Monte un pad candidat pour UNE déclinaison, sans solution ni trace des autres déclinaisons.
# Usage : tools/build-pad.sh <déclinaison> <dossier-cible> [--with-solution] [--verify]
#   --with-solution : ajoute la solution de référence et les tests d'évaluation (contrôle évaluateur, jamais pour un candidat)
#   --verify        : lance Maven dans le pad. Sans solution, le pad doit compiler et seuls les tests attendant
#                     une implémentation doivent échouer. Avec solution, tout doit passer.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MAIN=src/main/java/fr/cbtw/interview
TEST=src/test/java/fr/cbtw/interview
ALL_KEYS="loan kyc contract payment sweeping funds payment-senior"

CLOCK_BONUS="le legacy lit l'heure système, ce qui rend certains cas impossibles à tester de façon déterministe. Rendez votre cœur indépendant de l'horloge système ; le harness sait injecter un \`java.time.Clock\` dans votre constructeur."
EDGE_BONUS="ajoutez des tests de cas limites supplémentaires (bornes, valeurs particulières) et notez ce qu'ils révèlent du legacy."
CLOCK_BONUS_EN="the legacy reads the system clock, which makes some cases impossible to test deterministically. Make your core independent of the system clock; the harness can inject a \`java.time.Clock\` into your constructor."
EDGE_BONUS_EN="add more edge-case tests (boundaries, special values) and note what they reveal about the legacy."

declinaison() {
  # Définit : TITLE TITLE_EN LEGACY USE_CASE OUT_PORT ADAPTER LEVEL BONUS BONUS_EN CONTEXT SOLUTION_EXTRA TESTS
  OUT_PORT="" ADAPTER="" LEVEL=standard SOLUTION_EXTRA="" TESTS="BehaviorTest ParityTest"
  case "$1" in
    loan)     TITLE="Simulation de prêt"; TITLE_EN="Loan simulation"; LEGACY=LoanSimulationService
              USE_CASE=LoanSimulationUseCase; OUT_PORT=LoanSimulationRepository; CONTEXT=loan
              BONUS="$EDGE_BONUS"; BONUS_EN="$EDGE_BONUS_EN" ;;
    kyc)      TITLE="Conformité KYC"; TITLE_EN="KYC compliance"; LEGACY=KycComplianceService
              USE_CASE=KycComplianceUseCase; OUT_PORT=ComplianceCheckRepository; CONTEXT=kyc
              BONUS="$CLOCK_BONUS"; BONUS_EN="$CLOCK_BONUS_EN" ;;
    contract) TITLE="Clauses contractuelles"; TITLE_EN="Contract clauses"; LEGACY=ContractClauseService
              USE_CASE=ContractClauseUseCase; OUT_PORT=ContractRepository; CONTEXT=contract
              BONUS="$EDGE_BONUS"; BONUS_EN="$EDGE_BONUS_EN" ;;
    payment)  TITLE="Validation d'un ordre de paiement"; TITLE_EN="Payment order validation"
              LEGACY=PaymentOrderValidationService; USE_CASE=PaymentOrderValidationUseCase
              OUT_PORT=PaymentOrderRepository; CONTEXT=payment
              BONUS="$CLOCK_BONUS"; BONUS_EN="$CLOCK_BONUS_EN" ;;
    sweeping) TITLE="Règle de sweeping"; TITLE_EN="Cash sweeping rule"; LEGACY=CashSweepingService
              USE_CASE=CashSweepingUseCase; OUT_PORT=SweepInstructionRepository; CONTEXT=sweeping
              BONUS="$EDGE_BONUS"; BONUS_EN="$EDGE_BONUS_EN" ;;
    funds)    TITLE="Contrôle de provision"; TITLE_EN="Funds availability check"; LEGACY=FundsAvailabilityService
              USE_CASE=FundsAvailabilityUseCase; OUT_PORT=FundsReservationRepository; CONTEXT=funds
              BONUS="$EDGE_BONUS"; BONUS_EN="$EDGE_BONUS_EN" ;;
    payment-senior)
              TITLE="Traitement des ordres de paiement"; TITLE_EN=""; LEGACY=PaymentOrderProcessingService
              USE_CASE=PaymentOrderProcessingUseCase; CONTEXT=paymentprocessing; LEVEL=senior; BONUS=""; BONUS_EN=""
              TESTS="BehaviorTest ParityTest CutOffTest"
              SOLUTION_EXTRA="$MAIN/application/port/out/paymentprocessing $MAIN/infrastructure/paymentprocessing" ;;
    *) echo "Déclinaison inconnue : $1 (attendu : $ALL_KEYS)" >&2; exit 2 ;;
  esac
  [ -n "$OUT_PORT" ] && ADAPTER="InMemory$OUT_PORT"
  STEM="${USE_CASE%UseCase}"
  return 0
}

[ $# -ge 2 ] || { sed -n '2,6p' "$0" >&2; exit 2; }
KEY="$1"; OUT="$2"; shift 2
WITH_SOLUTION=false; VERIFY=false
for opt in "$@"; do
  case "$opt" in
    --with-solution) WITH_SOLUTION=true ;;
    --verify) VERIFY=true ;;
    *) echo "Option inconnue : $opt" >&2; exit 2 ;;
  esac
done
declinaison "$KEY"

if [ -e "$OUT" ] && [ -n "$(ls -A "$OUT" 2>/dev/null)" ]; then
  echo "Le dossier cible $OUT existe et n'est pas vide." >&2; exit 1
fi
mkdir -p "$OUT"
OUT="$(cd "$OUT" && pwd)"

copy() { mkdir -p "$OUT/$(dirname "$1")"; cp -R "$ROOT/$1" "$OUT/$1"; }

# Communs
for f in pom.xml .editorconfig .gitignore LICENSE \
         "$TEST/HexagonalArchitectureTest.java" "$TEST/utils/ClasspathScanner.java" "$TEST/utils/ImplementationLoader.java"; do
  copy "$f"
done

# Propres à la déclinaison
copy "$MAIN/legacy/$LEGACY.java"
copy "$MAIN/application/port/in/$USE_CASE.java"
if [ -n "$OUT_PORT" ]; then
  copy "$MAIN/application/port/out/$OUT_PORT.java"
  copy "$MAIN/infrastructure/$ADAPTER.java"
fi
for t in $TESTS; do copy "$TEST/$STEM$t.java"; done

# Énoncé
render() {
  sed -e "s|{{TITLE}}|$2|g" -e "s|{{LEGACY_CLASS}}|$LEGACY|g" -e "s|{{USE_CASE}}|$USE_CASE|g" \
      -e "s|{{OUT_PORT}}|$OUT_PORT|g" -e "s|{{ADAPTER}}|$ADAPTER|g" -e "s|{{BONUS}}|$3|g" "$1"
}
if [ "$LEVEL" = senior ]; then
  cp "$ROOT/pad/README.senior.md" "$OUT/README.md"
  cp "$ROOT/pad/DECISIONS.md" "$OUT/DECISIONS.md"
else
  render "$ROOT/pad/README.standard.md" "$TITLE" "$BONUS" > "$OUT/README.md"
  render "$ROOT/pad/README.standard.en.md" "$TITLE_EN" "$BONUS_EN" > "$OUT/README_EN.md"
fi
cp "$ROOT/pad/TRANSPARENCE.md" "$OUT/TRANSPARENCE.md"

# Contrôle de fuite : aucune trace des autres déclinaisons, de la solution ou des tests d'évaluation
if [ "$WITH_SOLUTION" = false ]; then
  leaks=""
  for other in $ALL_KEYS; do
    [ "$other" = "$KEY" ] && continue
    ( declinaison "$other"
      grep -rlE "\b($LEGACY|$USE_CASE|${OUT_PORT:-__none__})\b" "$OUT" ) && leaks="$leaks $other" || true
  done
  grep -rlE "SOLUTION|evaluation|fr\.cbtw\.interview\.domain\.|application\.service" "$OUT/src" && leaks="$leaks solution" || true
  grep -rl "{{" "$OUT" && leaks="$leaks placeholder" || true
  if [ -n "$leaks" ]; then echo "FUITE détectée :$leaks" >&2; exit 1; fi
fi

# Solution de référence (contrôle évaluateur uniquement)
if [ "$WITH_SOLUTION" = true ]; then
  copy "$MAIN/domain/$CONTEXT"
  copy "$MAIN/application/service/${STEM}ApplicationService.java"
  for extra in $SOLUTION_EXTRA; do copy "$extra"; done
  copy "$TEST/evaluation/${STEM}EvaluationTest.java"
fi

echo "Pad « $TITLE » ($LEVEL) monté dans $OUT$([ "$WITH_SOLUTION" = true ] && echo ' — AVEC solution, ne pas transmettre')"

if [ "$VERIFY" = true ]; then
  cd "$OUT"
  if [ "$WITH_SOLUTION" = true ]; then
    mvn -q test >"$OUT/verify.log" 2>&1 || { echo "ÉCHEC : la solution ne passe pas dans le pad (voir $OUT/verify.log)" >&2; exit 1; }
    echo "OK : solution de référence verte dans le pad."
  else
    mvn -q test-compile >"$OUT/verify.log" 2>&1 || { echo "ÉCHEC : le pad ne compile pas (voir $OUT/verify.log)" >&2; exit 1; }
    mvn test >>"$OUT/verify.log" 2>&1 && { echo "ÉCHEC : les tests passent sans implémentation" >&2; exit 1; }
    unexpected=$(grep -E "^\[ERROR\]   [A-Za-z]+\." "$OUT/verify.log" \
      | grep -vE "Aucune classe sous|domainContainsAtLeastOneBusinessType" || true)
    [ -z "$unexpected" ] || { echo "ÉCHEC : échecs inattendus :" >&2; echo "$unexpected" >&2; exit 1; }
    echo "OK : le pad compile ; seuls échouent les tests qui attendent l'implémentation du candidat."
  fi
  rm -rf "$OUT/target" "$OUT/verify.log"
fi
