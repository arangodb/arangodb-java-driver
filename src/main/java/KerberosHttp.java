import com.arangodb.ArangoDB;
import com.arangodb.Protocol;

/**
 * @author Michele Rastelli
 */
public class KerberosHttp {

    public static void main(String[] args) throws InterruptedException {
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
//        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login_keytab.conf");
//        System.setProperty("java.security.auth.login.config", "/home/michele/arango/arangodb-java-driver/src/test/resources/login_cache.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("sun.security.jgss.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("sun.security.jgss.native", "true");

        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("bruecklinux.arangodb.biz", 8899)
                .useProtocol(Protocol.HTTP_JSON)
                .build();

        while (true) {
            System.out.println(arangoDB.db().getVersion().getVersion());
            Thread.sleep(5000);
        }
    }

}
