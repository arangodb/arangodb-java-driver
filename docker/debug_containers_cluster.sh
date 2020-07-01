debug_container() {
  echo "=== === === ==="
  echo "=== $1"

  running=$(docker inspect -f '{{.State.Running}}' "$1")

  if [ "$running" = false ]; then
    echo "=== $1 IS NOT RUNNING!"
  fi

  echo "=== === === ==="

  docker logs "$1"
}

for c in agent1 \
  agent2 \
  agent3 \
  dbserver1 \
  dbserver2 \
  coordinator1 \
  coordinator2; do
  debug_container $c
done
