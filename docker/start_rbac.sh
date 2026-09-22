#!/usr/bin/env bash
# Build and start the disposable RBAC server on the host.
set -euo pipefail
LOCATION=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
source "$LOCATION/rbac_common.sh"
rbac_configure_host
rbac_state_init

if [ -f "$RBAC_PID_FILE" ]; then
    rbac_read_pid
    if rbac_process_matches "$RBAC_PID" "$RBAC_START_TICKS"; then
        echo "RBAC server is already running (PID $RBAC_PID)." >&2
        exit 1
    fi
    rm -f "$RBAC_PID_FILE"
fi

mvn -q -f "$RBAC_REPO_ROOT/test-rbac/pom.xml" -pl server -am -DskipTests package
jar="$RBAC_REPO_ROOT/test-rbac/server/target/rbac-test-server-1.0-SNAPSHOT-all.jar"
RBAC_BIND_ADDRESS="$RBAC_HOST" setsid nohup java "$RBAC_PROCESS_MARKER" -jar "$jar" \
    >"$RBAC_LOG_FILE" 2>&1 < /dev/null 9>&- &
RBAC_PID=$!
RBAC_START_TICKS=$(rbac_start_ticks "$RBAC_PID") || {
    echo "RBAC server exited immediately. See $RBAC_LOG_FILE" >&2
    exit 1
}
printf '%s %s\n' "$RBAC_PID" "$RBAC_START_TICKS" > "$RBAC_PID_FILE"

cleanup_on_error() {
    if [ "$?" -ne 0 ]; then
        if rbac_process_matches "$RBAC_PID" "$RBAC_START_TICKS"; then
            kill -TERM "$RBAC_PID" 2>/dev/null || true
        fi
        rm -f "$RBAC_PID_FILE"
        echo "RBAC startup failed. See $RBAC_LOG_FILE" >&2
    fi
}
trap cleanup_on_error EXIT

deadline=$((SECONDS + 30))
until rbac_require_running; do
    if (( SECONDS >= deadline )); then
        echo "RBAC server did not become ready." >&2
        exit 1
    fi
    sleep 0.2
done
echo "RBAC server ready at $RBAC_URL (PID $RBAC_PID)."
