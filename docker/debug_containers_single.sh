container_name="arangodb"

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

debug_container $container_name
