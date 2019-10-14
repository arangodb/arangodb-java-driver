
docker run -it --rm -e ARANGO_ROOT_PASSWORD=test -p 8529:8529 docker.io/arangodb/arangodb:3.5.1 arangod \
  --log.level communication=trace \
  --log.level requests=trace \
  --log.foreground-tty \
  --vst.maxsize 256
