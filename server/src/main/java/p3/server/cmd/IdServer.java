package p3.server.cmd;

import ch.qos.logback.classic.LoggerContext;
import p3.common.api.RmiApiChannel;
import p3.common.util.XUtil;
import p3.server.storage.IdentityRecordHelpers;
import p3.server.storage.KafkaConfig;
import p3.server.storage.RedisDatabase;
import p3.server.storage.ServerKafkaConsumer;
import picocli.CommandLine;
import picocli.CommandLine.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.rmi.server.RemoteServer.getClientHost;

/**
 * Class representing the server side of the distributed Identity server.
 * Implements the RMI methods.
 */
@Command(name = "IdServer", description = "Identity Server")
public class IdServer implements RmiApiChannel, Watcher, AsyncCallback.Children2Callback
{
    @Option(names = {"-n", "--numport"}, required = true, description = "Port")
    private static int numport;

    @Option(names = {"-v", "--verbose"}, description = "Verbose mode")
    private static boolean verbose = false;
    
    private static final String DEFAULT_SERVER_NAME = "IdServer";
    private static final File file_path = new File(IdServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());

    private final RedisDatabase redis;
    private final CountDownLatch latch = new CountDownLatch(1);
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(IdServer.class);

    public static final Object console_critical_section = new Object();

    private static final String TOPIC = "idserver";
    private final KafkaProducer<String, String> producer;

    private ZooKeeper zk;
    private final String znode;
    private final String prefix = "/IdServer/";
    private boolean isLeader = false;


    /**
     * Constructor for IdServer. Inits ZooKeeper, Redis, and Kafka.
     */
    public IdServer() {
        super();

        try {
            //create ZooKeeper client
            this.zk = new ZooKeeper(System.getenv("ZOOKEEPER_SERVER"), 4000, this);

            //create parent znode if it does not exist
            if (zk.exists("/IdServer", false) == null) {
                zk.create("/IdServer", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            //create an ephemeral sequential znode
            this.znode = zk.create(prefix + "node-", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (IOException | InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
        //find leader
        zk.getChildren("/IdServer", false, (AsyncCallback.Children2Callback) this, null);

        //set data = host:port of this server
        setServerData(znode.substring(prefix.length()), System.getenv("ID_SERVER_HOST"), Integer.parseInt(System.getenv("ID_SERVER_PORT")));

        //set up redis
        try {
            this.redis = new RedisDatabase(System.getenv("REDIS_HOST"), Integer.parseInt(System.getenv("REDIS_PORT")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //create Kafka producer
        producer = KafkaConfig.createKafkaProducer(znode, System.getenv("KAFKA_SERVER"));

        //create Kafka consumer & listen for messages in a separate thread - this is responsible for updating the database
        Thread consumerThread = new Thread(new ServerKafkaConsumer(redis, znode, System.getenv("KAFKA_SERVER")));
        consumerThread.start();

    }

    /**
     * main method of the ID server class.
     * @param args Command line parameters.
     */
    public static void main(String[] args)
    {
        IdServer idServer = null;
        CommandLine cmd = null;
        try {
            idServer = new IdServer();

            cmd = new CommandLine(idServer);
            cmd.parseArgs(args);


            if (cmd.isUsageHelpRequested()) {
                cmd.usage(cmd.getOut());
                System.exit(cmd.getCommandSpec().exitCodeOnUsageHelp());
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(cmd.getOut());
                System.exit(cmd.getCommandSpec().exitCodeOnVersionHelp());
            }
            //set logging level
            if (!verbose) {
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                LOGGER.setLevel(ch.qos.logback.classic.Level.ERROR);
                loggerContext.getLogger("org.apache.zookeeper").setLevel(Level.ERROR); //works
                loggerContext.getLogger("org.apache.kafka").setLevel(Level.ERROR);
                loggerContext.getLogger(ServerKafkaConsumer.class).setLevel(Level.ERROR);
                loggerContext.getLogger(RedisDatabase.class).setLevel(Level.ERROR);
                loggerContext.getLogger(IdentityRecordHelpers.class).setLevel(Level.ERROR);
            } else { //set to INFO, you can change this to DEBUG if you want more logs
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                LOGGER.setLevel(ch.qos.logback.classic.Level.INFO);
                loggerContext.getLogger("org.apache.zookeeper").setLevel(Level.INFO); //works
                loggerContext.getLogger("org.apache.kafka").setLevel(Level.INFO);
                loggerContext.getLogger(ServerKafkaConsumer.class).setLevel(Level.INFO);
                loggerContext.getLogger(RedisDatabase.class).setLevel(Level.INFO);
                loggerContext.getLogger(IdentityRecordHelpers.class).setLevel(Level.INFO);
            }

            LOGGER.info("Starting IdServer ...");
            LOGGER.info("file_path = {}", file_path);

            idServer.LoadServer(); //init rmi and db connection
        } catch (ParameterException e) {
            LOGGER.error("Error: {}", e.getMessage());
            assert cmd != null;
            cmd.usage(cmd.getOut());
            System.exit(1);
        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            //the node we were watching was deleted. Check who is new leader
            zk.getChildren("/IdServer", false, this, null);
        }
    }


    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        if (children.isEmpty()) {
            LOGGER.error("Children list is empty");
            return;
        }

        //node with the smallest sequence number is the leader
        Collections.sort(children);
        int index = Collections.binarySearch(children, znode.substring(prefix.length()));
        if (index < 0) {
            LOGGER.error("Znode is not in the children list");
            //add a delay before retrying
            //this will loop until the znode is in the children list, if not it will loop forever, which should not happen
            //unless there is a problem with the ZooKeeper server
            try {
                Thread.sleep(1000); //sleep for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            zk.getChildren("/IdServer", false, this, null);
            return;
        }

        //check if this server is the leader, if not watch the leader
        //NOTE: in our implementation, we do not care who the leader is, and we can remove this,
        //but we are keeping it for demonstration purposes/future updates
        if (znode.equals(prefix + children.get(0))) {
            this.isLeader = true;
            LOGGER.info("I am the leader: {}", children.get(0));
        } else {
            this.isLeader = false;
            if (index > 0) {
                LOGGER.info("Watching leader: {}", children.get(0));
                String watchNode = children.get(0);
                zk.exists(prefix + watchNode, this, null, true);
            }
        }
    }

    /**
     * Set the server data.
     * @param serverName Server name.
     * @param host Host name or IP address.
     * @param port Port number.
     */
    private void setServerData(String serverName, String host, int port) {
        String hostPort = host + ":" + port;
        byte[] data = hostPort.getBytes(StandardCharsets.UTF_8);

        try {
            LOGGER.info("Setting data for {} to {}", serverName, hostPort);
            zk.setData(prefix + serverName, data, -1);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("Error setting server data: {}", e.getMessage());
            throw new RuntimeException("Failed to set server data in ZooKeeper", e);
        }
    }

    /**
     * Loads the RMI identity server.
     * Prior to loading a secure SSL communication channel is created.
     */
    private void LoadServer()
    {
        final RMIClientSocketFactory rmi_ssl_client;
        final RMIServerSocketFactory rmi_ssl_server;
        RmiApiChannel remote = null;
        Registry registry = null;

        LOGGER.info("Server Loading ...");
        LOGGER.info("Setting System Properties...");
        System.setProperty("javax.net.ssl.keyStore", System.getenv("RMI_KEYSTORE"));
        System.setProperty("javax.net.ssl.keyStorePassword", System.getenv("RMI_KEYSTORE_PASSWORD"));
        System.setProperty("java.security.policy", System.getenv("RMI_POLICY"));

        rmi_ssl_client = new SslRMIClientSocketFactory();
        rmi_ssl_server = new SslRMIServerSocketFactory();

        LOGGER.info("remote_server_obj = {}", this);

        // Setup registry
        try {
            registry = LocateRegistry.createRegistry(numport);
            LOGGER.info("registry = {}", registry);

            remote = (RmiApiChannel)UnicastRemoteObject.exportObject(this, 0, rmi_ssl_client, rmi_ssl_server);
            LOGGER.info("remote = {}", remote);

            registry.rebind(DEFAULT_SERVER_NAME, remote);
            LOGGER.info("Registry bound to server");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        XUtil.PrintError("Server Loaded...");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered, stopping server...");
            latch.countDown();
            redis.close();
        }));

        // JVM main thread will exit unless we keep it alive
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RMI remote interface method exposed to the client.
     * @return Name of the specified command.
     */
    @Override
    public String ExecuteCommand()
    {
        LOGGER.info("this.message_id = {}", this.message_id);
        return message_id.name();
    }

    /**
     * RMI remote interface method exposed to the client.
     * Means by which client executes an ID server command.
     * @param message_id Indicates the command to be executed.
     * @param param1 Command dependent parameter.
     * @param param2 Command dependent parameter.
     * @param param3 Command dependent parameter.
     * @return Result of the executed command.
     */
    @Override
    public String ExecuteCommand(MESSAGE_ID message_id, String param1, String param2, String param3)
    {
        final String client_ip;
        final String result;

        try {
            client_ip = getClientHost();
        } catch (ServerNotActiveException e) {
            LOGGER.error("Error getting client host: {}", e.getMessage());
            return "Error processing request";
        }

        try {
            Processing(message_id);
            switch (message_id) {
                case CREATE:
                    result = Commands.Create(redis, producer, param1, param2, param3, client_ip);
                    break;
                case LOOKUP:
                    result = Commands.Lookup(redis, param1);
                    break;
                case REVERSE_LOOKUP:
                    result = Commands.ReverseLookup(redis, param1);
                    break;
                case MODIFY:
                    result = Commands.Modify(redis, producer, param1, param2, param3);
                    break;
                case DELETE:
                    result = Commands.Delete(redis, producer, param1, param2);
                    break;
                case DELETE_ALL:
                    result = Commands.DeleteAll(redis, producer);
                    break;
                case GET:
                    result = Commands.Get(redis, param1);
                    break;
                case DEBUG_STATS:
                    result = Commands.DebugStats(redis);
                    break;
                default:
                    result = "unknown command";
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("Error processing request: {}", e.getMessage());
            return "Error processing request";
        }

        try {
            Thread.yield();
            Thread.sleep(0);
        } catch (InterruptedException e) {
            LOGGER.error("Error sleeping thread: {}", e.getMessage());
            return "Error processing request";
        }

        return result;
    }

    /**
     * Debug for display the thread that is executing the specified command.
     * @param message_id Command being executed by the current thread.
     */
    private void Processing(MESSAGE_ID message_id)
    {
        LOGGER.info("Processing [tid={}]: {}", Thread.currentThread().getId(), message_id);
    }

}



