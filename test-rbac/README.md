# Curl RBAC demo

On Linux with Docker, Java 17+, Maven, Python 3, and curl, run from the repository root:

```bash
./test-rbac/curl-rbac-demo.sh
```

The script builds and starts a local test RBAC server and a single-server
ArangoDB Enterprise 3.12.11 deployment. It creates a disposable `rbac-demo`
user, signs in, verifies access, then changes that user's policy and verifies
that the same JWT is denied. The server always allows every operation for
`root`; its policy cannot be changed. Unknown users are denied. The server is
for this disposable demo, not production use.

The script leaves the server and Docker containers running. To clean up manually,
run `./docker/stop_rbac.sh`, inspect `docker ps -a` for the `adb` deployment
containers and `arangodb-data`, then stop and remove those containers with
`docker stop` and `docker rm`. Remove them before running the demo again.
