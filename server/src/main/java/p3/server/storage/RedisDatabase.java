package p3.server.storage;


import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

/**
 * Class representing the Redis database.
 */
public class RedisDatabase {
    private final JedisPooled pool; // pool of Jedis connections
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RedisDatabase.class);

    /**
     * Constructor.
     * @param host Redis host name.
     * @param port Redis port number.
     */
    public RedisDatabase(String host, int port) {
        try {
            HostAndPort redisHost = new HostAndPort(host, port);
            SSLSocketFactory sslFactory = createSslSocketFactory(
                    System.getenv("REDIS_TRUSTSTORE"),
                    System.getenv("REDIS_TRUSTSTORE_PASSWORD"),
                    System.getenv("REDIS_KEYSTORE"),
                    System.getenv("REDIS_KEYSTORE_PASSWORD")
            );
            JedisClientConfig config = DefaultJedisClientConfig.builder()
                    .ssl(true)
                    .sslSocketFactory(sslFactory)
                    .user("default")
                    .password(System.getenv("REDIS_PASSWORD"))
                    .build();
            this.pool = new JedisPooled(redisHost, config);
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Error initializing RedisDatabase", e);
            throw new RuntimeException("Error initializing RedisDatabase", e);
        }
    }

    /**
     * Saves a record to the database.
     * @param login_name Login name
     * @param recordJson Record JSON
     */
    public void saveRecord(String login_name, String recordJson) {
        pool.hset("name_to_record", login_name, recordJson);
    }

    /**
     * Gets a record from the database.
     * @param login_name Login name
     * @return Record JSON
     */
    public String getRecord(String login_name) {
        return pool.hget("name_to_record", login_name);
    }

    /**
     * Saves a UUID to the database.
     * @param uuid UUID
     * @param login_name Login name
     */
    public void saveUUID(String uuid, String login_name) {
        pool.hset("uuid_to_name", uuid, login_name);
    }

    /**
     * Gets a login name from the database.
     * @param uuid UUID
     * @return Login name
     */
    public String getLoginName(String uuid) {
        return pool.hget("uuid_to_name", uuid);
    }

    /**
     * Deletes a record from the database.
     * @param login_name Login name
     */
    public void deleteRecord(String login_name) {
        pool.hdel("name_to_record", login_name);
    }

    /**
     * Deletes a UUID from the database.
     * @param uuid UUID
     */
    public void deleteUUID(String uuid) {
        pool.hdel("uuid_to_name", uuid);
    }

    /**
     * Gets all records from the database.
     * @return Map of login names to record JSONs
     */
    public Map<String, String> getAllRecords() {
        return pool.hgetAll("name_to_record");
    }

    /**
     * Gets all UUIDs from the database.
     * @return Map of UUIDs to login names
     */
    public Map<String, String> getAllUUIDs() {
        return pool.hgetAll("uuid_to_name");
    }

    /**
     * Gets the number of records in the database.
     * @return Number of records
     */
    public long getRecordCount() {
        return pool.hlen("name_to_record");
    }

    /**
     * Gets the number of UUIDs in the database.
     * @return Number of UUIDs
     */
    public long getUUIDCount() {
        return pool.hlen("uuid_to_name");
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        pool.close();
    }

    /**
     * Creates an SSL socket factory.
     * @param caCertPath CA certificate path
     * @param caCertPassword CA certificate password
     * @param userCertPath User certificate path
     * @param userCertPassword User certificate password
     * @return SSL socket factory
     */
    private static SSLSocketFactory createSslSocketFactory(
            String caCertPath, String caCertPassword, String userCertPath, String userCertPassword)
            throws IOException, GeneralSecurityException {

        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(Files.newInputStream(Paths.get(userCertPath)), userCertPassword.toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(Files.newInputStream(Paths.get(caCertPath)), caCertPassword.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("PKIX");
        keyManagerFactory.init(keyStore, userCertPassword.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }
}