package io.github.sinri.keel.integration.mysql;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.mysqlclient.SslMode;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KeelMySQLConfigurationTest {

    @Test
    void getConnectOptionsShouldApplySslModeAndPemTrustCert() throws Exception {
        KeelMySQLConfiguration configuration = createConfiguration(
                "mysql.secure.host", "mysql.example.test",
                "mysql.secure.sslMode", "verify-identity",
                "mysql.secure.sslCert", "/etc/mysql/ca.pem",
                "mysql.secure.sslHostnameVerificationAlgorithm", "HTTPS"
        );

        var connectOptions = configuration.getConnectOptions();
        var sslOptions = connectOptions.getSslOptions();
        assertNotNull(sslOptions);

        assertEquals(SslMode.VERIFY_IDENTITY, connectOptions.getSslMode());
        assertEquals("HTTPS", sslOptions.getHostnameVerificationAlgorithm());

        PemTrustOptions trustOptions = assertInstanceOf(PemTrustOptions.class, sslOptions.getTrustOptions());
        assertEquals("/etc/mysql/ca.pem", trustOptions.getCertPaths().get(0));
    }

    @Test
    void getConnectOptionsShouldMapSslBooleanToRequiredMode() throws Exception {
        KeelMySQLConfiguration configuration = createConfiguration(
                "mysql.secure.ssl", "true"
        );

        assertEquals(SslMode.REQUIRED, configuration.getConnectOptions().getSslMode());
    }

    @Test
    void getSslOptionsShouldApplyPemClientCertificate() throws Exception {
        KeelMySQLConfiguration configuration = createConfiguration(
                "mysql.secure.sslCert", "/etc/mysql/client-cert.pem",
                "mysql.secure.sslKey", "/etc/mysql/client-key.pem"
        );

        var sslOptions = configuration.getSslOptions();
        assertNotNull(sslOptions);

        PemKeyCertOptions keyCertOptions = assertInstanceOf(PemKeyCertOptions.class, sslOptions.getKeyCertOptions());
        assertEquals("/etc/mysql/client-cert.pem", keyCertOptions.getCertPaths().get(0));
        assertEquals("/etc/mysql/client-key.pem", keyCertOptions.getKeyPaths().get(0));
    }

    @Test
    void getSslOptionsShouldApplyJksTrustStore() throws Exception {
        KeelMySQLConfiguration configuration = createConfiguration(
                "mysql.secure.sslJksTrustStorePath", "/etc/mysql/truststore.jks",
                "mysql.secure.sslJksTrustStorePassword", "secret"
        );

        var sslOptions = configuration.getSslOptions();
        assertNotNull(sslOptions);

        JksOptions trustOptions = assertInstanceOf(JksOptions.class, sslOptions.getTrustOptions());
        assertEquals("/etc/mysql/truststore.jks", trustOptions.getPath());
        assertEquals("secret", trustOptions.getPassword());
    }

    @Test
    void getSslOptionsShouldApplyPfxTrustStore() throws Exception {
        KeelMySQLConfiguration configuration = createConfiguration(
                "mysql.secure.sslPfxTrustStorePath", "/etc/mysql/truststore.p12",
                "mysql.secure.sslPfxTrustStorePassword", "secret"
        );

        var sslOptions = configuration.getSslOptions();
        assertNotNull(sslOptions);

        PfxOptions trustOptions = assertInstanceOf(PfxOptions.class, sslOptions.getTrustOptions());
        assertEquals("/etc/mysql/truststore.p12", trustOptions.getPath());
        assertEquals("secret", trustOptions.getPassword());
    }

    @Test
    void getPoolOptionsShouldApplyIdleTimeoutInSeconds() throws Exception {
        KeelMySQLConfiguration configuration = createConfiguration(
                "mysql.secure.poolIdleTimeout", "120"
        );

        var poolOptions = configuration.getPoolOptions();

        assertEquals(120, poolOptions.getIdleTimeout());
        assertEquals(TimeUnit.SECONDS, poolOptions.getIdleTimeoutUnit());
    }

    private KeelMySQLConfiguration createConfiguration(String... keyAndValuePairs) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mysql.secure.username", "test_user");
        properties.setProperty("mysql.secure.password", "test_password");
        for (int i = 0; i < keyAndValuePairs.length; i += 2) {
            properties.setProperty(keyAndValuePairs[i], keyAndValuePairs[i + 1]);
        }

        ConfigElement root = new ConfigElement("");
        root.loadData(properties);
        return new KeelMySQLConfiguration(root.extract("mysql", "secure"));
    }
}
