#!/usr/bin/env bash
# End-to-end check of the running API from the terminal.
# Usage: ./scripts/smoke-test.sh [base-url]   (default http://localhost:8080)
# Requires curl and jq. Set ADMIN_EMAIL / ADMIN_PASSWORD if they differ from .env.example.
set -uo pipefail

BASE="${1:-${BASE_URL:-http://localhost:8080}}"
API="$BASE/api/v1"
MAILPIT="${MAILPIT_URL:-http://localhost:8025}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@mutuals.app}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin!2026pass}"
PASSWORD='Str0ng!Pass'
SUFFIX="$(date +%s)"

GREEN=$'\e[32m'; RED=$'\e[31m'; BOLD=$'\e[1m'; RESET=$'\e[0m'
PASSED=0; FAILED=0; STATUS=""; BODY=""
BODY_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE"' EXIT

command -v jq >/dev/null || { echo "jq is required (brew install jq)"; exit 1; }

request() { # method path [token] [json]
  local args=(-s -o "$BODY_FILE" -w '%{http_code}' -X "$1" "$API$2")
  [ -n "${3:-}" ] && args+=(-H "Authorization: Bearer $3")
  [ -n "${4:-}" ] && args+=(-H 'Content-Type: application/json' -d "$4")
  STATUS="$(curl "${args[@]}")"
  BODY="$(cat "$BODY_FILE")"
}

check() { # description expected-status
  if [ "$STATUS" = "$2" ]; then
    PASSED=$((PASSED + 1)); printf '  %s✔%s %-55s %s\n' "$GREEN" "$RESET" "$1" "$STATUS"
  else
    FAILED=$((FAILED + 1)); printf '  %s✘%s %-55s got %s, expected %s\n' "$RED" "$RESET" "$1" "$STATUS" "$2"
    [ -n "$BODY" ] && printf '      %s\n' "$(echo "$BODY" | head -c 300)"
  fi
}

field() { echo "$BODY" | jq -r "$1"; }

section() { printf '\n%s%s%s\n' "$BOLD" "$1" "$RESET"; }

register() { # username -> prints access token
  request POST /auth/register "" "{\"email\":\"$1@mail.com\",\"username\":\"$1\",\"displayName\":\"$1\",\"password\":\"$PASSWORD\"}"
}

section "Health  ($BASE)"
STATUS="$(curl -s -o "$BODY_FILE" -w '%{http_code}' "$BASE/actuator/health")"; BODY="$(cat "$BODY_FILE")"
check "GET /actuator/health" 200
if [ "$STATUS" != 200 ]; then echo "${RED}The API is not running at $BASE${RESET}"; exit 1; fi

section "Registration and login"
ANA="ana$SUFFIX"; JHAN="jhan$SUFFIX"
register "$ANA";  check "Register Ana" 201; ANA_TOKEN="$(field .accessToken)"; ANA_REFRESH="$(field .refreshToken)"; ANA_ID="$(field .user.id)"
register "$JHAN"; check "Register Jhan" 201; JHAN_TOKEN="$(field .accessToken)"; JHAN_ID="$(field .user.id)"
register "$ANA";  check "Duplicate email is rejected" 409
request POST /auth/register "" '{"email":"not-an-email","username":"A","displayName":"","password":"123"}'
check "Invalid data is rejected" 400
request POST /auth/login "" "{\"email\":\"$ANA@mail.com\",\"password\":\"$PASSWORD\"}"
check "Login with valid credentials" 200
request POST /auth/login "" "{\"email\":\"$ANA@mail.com\",\"password\":\"Wrong!Pass1\"}"
check "Login with wrong password" 401
request POST /auth/login "" '{bad json'
check "Malformed JSON" 400

section "Security and roles"
request GET /users/me;                  check "GET /users/me without token" 401
request GET /users/me "$ANA_TOKEN";     check "GET /users/me with token" 200
request GET /users/me "invalid.token";  check "GET /users/me with invalid token" 401
request GET /admin/metrics "$ANA_TOKEN"; check "USER cannot access /admin/metrics" 403
request POST /auth/login "" "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}"
check "Admin login" 200; ADMIN_TOKEN="$(field .accessToken)"
request GET /admin/metrics "$ADMIN_TOKEN"; check "ADMIN can access /admin/metrics" 200

section "Mutuals, streaks and interactions"
request POST "/users/$JHAN_ID/follow" "$ANA_TOKEN"; check "Ana follows Jhan" 201
request POST "/users/$ANA_ID/follow" "$JHAN_TOKEN"; check "Jhan follows Ana (mutual created)" 201
[ "$(field .mutual)" = "true" ] || echo "      ${RED}expected mutual=true${RESET}"
request GET /mutuals "$ANA_TOKEN"; check "Ana lists her mutuals" 200
request POST /streak-invitations "$ANA_TOKEN" "{\"friendId\":$JHAN_ID}"
check "Ana invites Jhan to a streak" 201; INVITATION_ID="$(field .id)"
request PATCH "/streak-invitations/$INVITATION_ID" "$JHAN_TOKEN" '{"accept":true}'
check "Jhan accepts the invitation" 200
request POST /interactions "$ANA_TOKEN" "{\"friendId\":$JHAN_ID,\"note\":\"Coffee after class\"}"
check "Ana registers an interaction" 201; INTERACTION_ID="$(field .id)"; STREAK_ID="$(field .streakId)"
request GET /interactions/pending "$JHAN_TOKEN"; check "Jhan sees the pending interaction" 200
request POST "/interactions/$INTERACTION_ID/confirm" "$ANA_TOKEN"; check "Ana cannot confirm her own interaction" 403
request POST "/interactions/$INTERACTION_ID/confirm" "$JHAN_TOKEN"; check "Jhan confirms the interaction" 200
request GET "/streaks/$STREAK_ID" "$ANA_TOKEN"; check "Streak detail" 200
echo "      streak length: $(field .currentLength)"
request GET /streaks/999999 "$ANA_TOKEN"; check "Non-existent streak" 404
request GET /interactions/timeline "$ANA_TOKEN"; check "Interaction timeline" 200

section "Wallet, notifications and feed"
request GET /wallet "$ANA_TOKEN"; check "Wallet" 200
request POST /shields/purchase "$ANA_TOKEN" '{"quantity":5}'; check "Buying without enough gems" 409
request POST /shields/purchase "$ANA_TOKEN" '{"quantity":100}'; check "Buying more than 5 shields is rejected" 400
sleep 1
request GET /notifications "$JHAN_TOKEN"; check "Jhan's notifications" 200
request GET /notifications/unread-count "$JHAN_TOKEN"; check "Unread count" 200
request GET /activities "$ANA_TOKEN"; check "Activity feed" 200
request GET /achievements "$ANA_TOKEN"; check "Achievements" 200

section "Tokens"
request POST /auth/refresh "" "{\"refreshToken\":\"$ANA_REFRESH\"}"; check "Refresh token" 200
request POST /auth/refresh "" "{\"refreshToken\":\"$ANA_REFRESH\"}"; check "Reusing a refresh token is rejected" 401
request POST /auth/password-reset "" "{\"email\":\"$ANA@mail.com\"}"; check "Password reset request" 202

section "Email (Mailpit)"
sleep 2
if MAILS="$(curl -fs "$MAILPIT/api/v1/search?query=to:$ANA@mail.com")"; then
  COUNT="$(echo "$MAILS" | jq '.messages | length')"
  STATUS=$([ "$COUNT" -ge 2 ] && echo 2 || echo "$COUNT"); BODY=""
  check "Welcome and reset emails delivered ($COUNT)" 2
  echo "$MAILS" | jq -r '.messages[].Subject' | sed 's/^/      - /'
else
  echo "  Mailpit is not reachable at $MAILPIT (skipped)"
fi

printf '\n%sResult: %s passed, %s failed%s\n' "$BOLD" "$PASSED" "$FAILED" "$RESET"
[ "$FAILED" -eq 0 ]
