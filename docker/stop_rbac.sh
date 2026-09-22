#!/usr/bin/env bash
# Stops ONLY the recorded host JVM. Never stops/removes Docker containers/volumes.
set -euo pipefail
LOCATION=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
source "$LOCATION/rbac_common.sh"
rbac_state_init
[ -f "$RBAC_PID_FILE" ] || exit 0
rbac_read_pid
if ! rbac_start_ticks "$RBAC_PID" >/dev/null; then
    rm -f "$RBAC_PID_FILE"
    exit 0
fi
if ! rbac_process_matches "$RBAC_PID" "$RBAC_START_TICKS"; then
    echo "Refusing to stop PID $RBAC_PID: owner, start time, or test-server marker does not match $RBAC_PID_FILE." >&2
    exit 1
fi
kill -TERM "$RBAC_PID"
deadline=$((SECONDS + 15))
while rbac_process_matches "$RBAC_PID" "$RBAC_START_TICKS"; do
    if (( SECONDS >= deadline )); then
        kill -KILL "$RBAC_PID" 2>/dev/null || true
        break
    fi
    sleep 0.2
done
rm -f "$RBAC_PID_FILE"
echo "Stopped host RBAC server PID $RBAC_PID. Log retained at $RBAC_LOG_FILE"
