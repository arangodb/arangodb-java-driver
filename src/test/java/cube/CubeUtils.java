/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package cube;

import org.arquillian.cube.docker.impl.client.config.Await;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.BindMode;
import org.arquillian.cube.docker.junit.rule.ContainerDslRule;

import java.nio.file.Paths;

/**
 * @author Michele Rastelli
 */
class CubeUtils {

    static int PORT = 8529;
    private static String DOCKER_IMAGE = "docker.io/arangodb/arangodb:3.5.1";
    private static String SSL_CERT_PATH = Paths.get("docker/server.pem").toAbsolutePath().toString();
    private static String PASSWORD = "test";

    static private Await arangoAwaitStrategy() {
        Await await = new Await();
        await.setStrategy("log");
        await.setMatch("ready for business");
        return await;
    }

    static ContainerDslRule arangodb() {
        return new ContainerDslRule(DOCKER_IMAGE)
                .withPortBinding(PORT)
                .withAwaitStrategy(CubeUtils.arangoAwaitStrategy())
                .withEnvironment("ARANGO_ROOT_PASSWORD", PASSWORD);
    }

    static ContainerDslRule arangodbWithChunkSize(int chunkSize) {
        return new ContainerDslRule(DOCKER_IMAGE)
                .withPortBinding(PORT)
                .withAwaitStrategy(CubeUtils.arangoAwaitStrategy())
                .withEnvironment("ARANGO_ROOT_PASSWORD", PASSWORD)
                .withCommand("arangod --log.level communication=trace --log.level requests=trace --log.foreground-tty --vst.maxsize " + chunkSize);
    }

    static ContainerDslRule arangodbSsl() {
        return new ContainerDslRule(DOCKER_IMAGE)
                .withPortBinding(PORT)
                .withAwaitStrategy(arangoAwaitStrategy())
                .withEnvironment("ARANGO_ROOT_PASSWORD", PASSWORD)
                .withVolume(SSL_CERT_PATH, "/server.pem", BindMode.READ_ONLY)
                .withCommand("arangod --ssl.keyfile /server.pem --server.endpoint ssl://0.0.0.0:8529");
    }

}
