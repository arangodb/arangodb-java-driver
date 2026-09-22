#!/usr/bin/env bash
# Helpers shared by the local RBAC launchers and the database launcher.
RBAC_REPO_ROOT=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
RBAC_STATE_DIR="$RBAC_REPO_ROOT/test-rbac/.run"
RBAC_PID_FILE="$RBAC_STATE_DIR/server.pid"
RBAC_LOG_FILE="$RBAC_STATE_DIR/server.log"
RBAC_PROCESS_MARKER="-Darangodb.rbac.test.state=$RBAC_STATE_DIR"

rbac_configure_host() {
    # In a development container, bind to its bridge address. On the Docker
    # host, bind to the default bridge gateway used by the database containers.
    local gateway
    gateway=$(docker network inspect bridge --format '{{range .IPAM.Config}}{{println .Gateway}}{{end}}' | head -n 1)
    [ -n "$gateway" ] || { echo "Docker bridge has no gateway" >&2; return 1; }
    RBAC_HOST=$(docker inspect --format '{{with index .NetworkSettings.Networks "bridge"}}{{.IPAddress}}{{end}}' "$(uname -n)" 2>/dev/null || true)
    RBAC_HOST=${RBAC_HOST:-$gateway}
    RBAC_URL="http://$RBAC_HOST:18080"
    export RBAC_HOST RBAC_URL
}

rbac_state_init() {
    mkdir -p "$RBAC_STATE_DIR"
    # Keep the lock outside Maven's target directory.
    exec 9>"$RBAC_STATE_DIR/lifecycle.lock"
    flock -x 9
}

rbac_start_ticks() {
    local stat
    local -a fields
    [ -r "/proc/$1/stat" ] || return 1
    stat=$(<"/proc/$1/stat")
    stat="${stat##*) }"
    read -r -a fields <<< "$stat"
    [ "${fields[0]:-}" != Z ] && [ "${fields[0]:-}" != X ] || return 1
    printf '%s\n' "${fields[19]}"
}

rbac_process_matches() {
    local ticks
    ticks=$(rbac_start_ticks "$1") || return 1
    [ "$ticks" = "$2" ] || return 1
    [ "$(stat -c %u "/proc/$1")" = "$(id -u)" ] || return 1
    tr '\0' '\n' < "/proc/$1/cmdline" | grep -Fx -- "$RBAC_PROCESS_MARKER" >/dev/null
}

rbac_read_pid() {
    read -r RBAC_PID RBAC_START_TICKS < "$RBAC_PID_FILE"
    [[ "$RBAC_PID" =~ ^[0-9]+$ && "$RBAC_START_TICKS" =~ ^[0-9]+$ ]] && (( RBAC_PID > 1 )) || {
        echo "Invalid RBAC PID file: $RBAC_PID_FILE" >&2
        return 1
    }
}

rbac_require_running() {
    local reported_pid
    rbac_read_pid || return 1
    rbac_process_matches "$RBAC_PID" "$RBAC_START_TICKS" || {
        echo "Recorded RBAC server is not running. Run docker/start_rbac.sh." >&2
        return 1
    }
    reported_pid=$(curl --noproxy '*' --silent --fail --max-time 3 "$RBAC_URL/health" |
        python3 -c 'import json,sys; r=json.load(sys.stdin); assert r["status"] == "ok" and r["service"] == "arangodb-java-driver/test-rbac"; print(r["pid"])' 2>/dev/null) || return 1
    [ "$reported_pid" = "$RBAC_PID" ] || {
        echo "A different service is listening at $RBAC_URL" >&2
        return 1
    }
}
