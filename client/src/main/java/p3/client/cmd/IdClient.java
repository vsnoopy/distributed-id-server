package p3.client.cmd;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.LoggerFactory;
import p3.common.api.RmiApiChannel;
import p3.common.util.XColor;
import p3.common.util.XUtil;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Class representing the client side of the distributed Identity system.
 */
@Command(name = "IdClient", description = "Identity Client",
        subcommands = {IdClient.CreateCommand.class, IdClient.LookupCommand.class, IdClient.ReverseLookupCommand.class,
                IdClient.ModifyCommand.class, IdClient.DeleteCommand.class, IdClient.DeleteAllCommand.class,
                IdClient.GetCommand.class, IdClient.DebugStatsCommand.class, IdClient.TestCommand.class})

public class IdClient extends Thread implements Runnable {
    @Option(names = {"-s", "--server"}, required = true, description = "Server host")
    private String server = "localhost"; //zookeeper

    @Option(names = {"-n", "--numport"}, description = "Port number")
    private int numport = 2181; //zookeeper port

    private static final String DEFAULT_SERVER_NAME = "IdServer";
    private static final String DEFAULT_PASSWORD = "password";
    private static RmiApiChannel remote_class_obj = null;
    private static final String SHA_512 = "SHA-512";

    private ZooKeeper zk;
    private String prefix = "/IdServer";
    private String rmi_host;
    private int rmi_port;

    /**
     * main method for the IdClient class.
     * @param args Command line parameters.
     */
    public static void main(String[] args)
    {
        final IdClient id_client = new IdClient();
        CommandLine cmd = new CommandLine(id_client); //picoCLI

        try {
            cmd.parseArgs(args);

            //suppress ZooKeeper logs - comment out if you want to see ZooKeeper logs
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getLogger("org.apache.zookeeper").setLevel(Level.ERROR);


            id_client.initializeZooKeeper();

            //get server list from zookeeper and attempt to connect to one - randomly
            List<Map.Entry<String, String>> servers = new ArrayList<>(id_client.getServerList().entrySet());
            Random random = new Random();
            while (true) {
                int serverIndex = random.nextInt(servers.size()); //random selection
                Map.Entry<String, String> server = servers.get(serverIndex);

                try {
                    id_client.rmi_host = server.getValue().split(":")[0];
                    id_client.rmi_port = Integer.parseInt(server.getValue().split(":")[1]);
                    remote_class_obj = id_client.LoadClient();
                    System.out.println("Connected to server " + server.getKey() + " at " + server.getValue());
                    break;
                } catch (Exception e) {
                    System.out.println("Failed to connect to server " + server + ", trying next one...");
                }
            }

            //check if cmd is help or version
            if (cmd.isUsageHelpRequested()) {
                cmd.usage(cmd.getOut());
                System.exit(cmd.getCommandSpec().exitCodeOnUsageHelp());
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(cmd.getOut());
                System.exit(cmd.getCommandSpec().exitCodeOnVersionHelp());
            }

            cmd.execute(args); //execute the command
        } catch (ParameterException e) {
            System.out.println("Error: " + e.getMessage());
            cmd.usage(cmd.getOut());
            System.exit(1);
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Initialize ZooKeeper.
     */
    private void initializeZooKeeper() {
        try {
            this.zk = new ZooKeeper(this.server+":"+this.numport, 4000, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to ZooKeeper", e);
        }
    }

    /**
     * Gets a list of ID servers.
     * @return Map of ID servers to their host:port.
     */
    private Map<String, String> getServerList() {
        Map<String, String> serverList = new HashMap<>();
        try {
            List<String> servers = zk.getChildren(prefix, false);
            for (String server : servers) {
                byte[] data = zk.getData(prefix + "/" + server, false, null);
                String hostPort = new String(data, StandardCharsets.UTF_8);
                serverList.put(server, hostPort);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException("Failed to retrieve server list from ZooKeeper", e);
        }
        return serverList;
    }

    /**
     * Runnable method that executes the client and runs the test cases.
     * This method is called when the client is run as a thread.
     */
    public void run()
    {
        final IdClient id_client = new IdClient();
        final RmiApiChannel remote_class_obj = id_client.LoadClient();
        IntTestCases.Tests(remote_class_obj);
    }

    /**
     * Class that represents the CLI CREATE command.
     */
    @Command(name = "--create", description = "Creates a new login name")
    static class CreateCommand implements Runnable {

        @Parameters(index = "0", arity = "1", description = "Login name to create")
        private String loginName;

        @Parameters(index = "1", arity = "0..1", description = "Real name of the user", defaultValue = "${sys:user.name}")
        private String realName = System.getProperty("user.name");

        @Option(names = {"-p", "--password"}, description = "Password for the account")
        private char[] password;

        /**
         * Runnable methods that executes the CLI CREATE command.
         */
        public void run() {
            System.out.println("Creating login name: " + loginName + " for user: " + realName);

            String hash_pass_word = Sha512Hash(password);
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.CREATE, loginName, realName, hash_pass_word);
        }
    }

    /**
     * Class that represents the CLI LOOKUP command.
     */
    @Command(name = "--lookup", description = "Look up login name")
    static class LookupCommand implements Runnable {
        @Parameters(index = "0", arity = "1", description = "Login name to look up")
        private String loginName;

        /**
         * Runnable methods that executes the CLI LOOKUP command.
         */
        public void run() {
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.LOOKUP, loginName, null, null);
        }
    }

    /**
     * Class that represents the CLI REVERSE-LOOKUP command.
     */
    @Command(name = "--reverse-lookup", description = "Look up UUID")
    static class ReverseLookupCommand implements Runnable {
        @Parameters(index = "0", arity = "1", description = "UUID to look up")
        private String uuid;

        /**
         * Runnable methods that executes the CLI REVERSE-LOOKUP command.
         */
        public void run() {
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.REVERSE_LOOKUP, uuid, null, null);
        }
    }

    /**
     * Class that represents the CLI MODIFY command.
     */
    @Command(name = "--modify", description = "Change login name")
    static class ModifyCommand implements Runnable {
        @Parameters(index = "0", arity = "1", description = "Login name to change")
        private String loginName;

        @Parameters(index = "1", arity = "1", description = "New login name")
        private String newLoginName;

        @Option(names = {"-p", "--password"}, description = "Password for the account")
        private char[] password;

        /**
         * Runnable methods that executes the CLI MODIFY command.
         */
        public void run() {
            final String hash_pass_word = Sha512Hash(password);
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.MODIFY, loginName, newLoginName, hash_pass_word);
        }
    }

    /**
     * Class that represents the CLI DELETE command.
     */
    @Command(name = "--delete", description = "Delete login name")
    static class DeleteCommand implements Runnable {
        @Parameters(index = "0", arity = "1", description = "Login name to delete")
        private String loginName;

        @Option(names = {"-p", "--password"}, description = "Password for the account")
        private char[] password;

        /**
         * Runnable methods that executes the CLI DELETE command.
         */
        public void run() {
            final String hash_pass_word = Sha512Hash(password);
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.DELETE, loginName, hash_pass_word, null);
        }
    }

    /**
     * Class that represents the CLI DELETE-ALL command.
     */
    @Command(name = "--delete-all", description = "Delete all accounts")
    static class DeleteAllCommand implements Runnable {

        /**
         * Runnable methods that execute the CLI DELETE-ALL command.
         */
        public void run() {
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.DELETE_ALL, null, null, null);
        }
    }

    /**
     * Class that represents the CLI DEBUG-STATS command.
     */
    @Command(name = "--debug-stats", description = "Report debug statistics")
    static class DebugStatsCommand implements Runnable {

        /**
         * Runnable methods that execute that executes the CLI DEBUG-STATS command.
         */
        public void run() {
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.DEBUG_STATS, null, null, null);
        }
    }

    /**
     * Class that represents the CLI TEST command.
     */
    @Command(name = "--test", description = "Execute test case")
    static class TestCommand implements Runnable {

        /**
         * Runnable methods that executes the CLI TEST command.
         */
        public void run() {
            IntTestCases.ExecuteTests();
        }
    }

    /**
     * Class that represents the CLI GET command and its options (USERS, UUIDS, ALL).
     */
    @Command(name = "--get", description = "Get list of users, UUIDs, or all accounts")
    static class GetCommand implements Runnable {
        @Parameters(index = "0", arity = "1", description = "users|uuids|all")
        private String type;

        /**
         * Runnable methods that execute the CLI GET command.
         */
        public void run() {
            if (!Arrays.asList("users", "uuids", "all").contains(type.toLowerCase())) {
                throw new ParameterException(new CommandLine(this), "Invalid type: " + type);
            }

            type = type.toUpperCase();
            ExecuteCommand(remote_class_obj, RmiApiChannel.MESSAGE_ID.GET, type, null, null);
        }
    }

    /**
     * Execute the RMI command.
     * @param remote_class_obj RMI object.
     * @param message_id Command being executed.
     * @param param1 First parameter of command being executed.
     * @param param2 Second parameter of command being executed.
     * @param param3 Third parameter of command being executed.
     */
    private static void ExecuteCommand(
        RmiApiChannel remote_class_obj,
        RmiApiChannel.MESSAGE_ID message_id, String param1, String param2, String param3)
    {
        final String result;

        try {
            result = remote_class_obj.ExecuteCommand(message_id, param1, param2, param3);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        DisplayResult(result);
    }

    /**
     * Loads the client.
     * @return RMI reference object used to communicate with Identity server.
     */
    protected RmiApiChannel LoadClient()
    {
        final Registry registry;
        final RmiApiChannel remote_class_obj;

        System.setProperty("javax.net.ssl.trustStore", "./client/tls/Client_Truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("java.security.policy", "./client/ssl_security.policy");

        try {
            registry = LocateRegistry.getRegistry(rmi_host, rmi_port);
            remote_class_obj = (RmiApiChannel)registry.lookup(DEFAULT_SERVER_NAME);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }

        return remote_class_obj;
    }

    /**
     * Display the result of the command.
     * @param result Result of the command.
     */
    private static void DisplayResult(String result)
    {
        System.out.println(XColor.BLUE_UNDERLINED + "Result:" + XColor.RESET);
        if (!XUtil.ErrorMessageReturn(result)) {
            if (!result.isEmpty()) {
                System.out.println(result);
            } else {
                System.out.println(XColor.YELLOW + "<empty>" + XColor.RESET);
            }
        }
    }

    /**
     * Performs an SHA512 hash on a password.
     * @param password Password to be hashed.
     * @return The base64 encoding of the hashed password.
     */
    protected static String Sha512Hash(char[] password)
    {
        final MessageDigest message_digest;
        final String encoded_hash_str;
        final byte[] sha512_hash;
        final byte[] encoded_hash;

        if (password == null) {
            // if a password not provided, user default
            password = DEFAULT_PASSWORD.toCharArray();
        }

        try {
            message_digest = MessageDigest.getInstance(SHA_512);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: SHA-512 algorithm not found.");
            System.exit(1);
            return null; // Unreachable code, but required to satisfy the compiler
        }

        message_digest.reset();
        sha512_hash = message_digest.digest(new String(password).getBytes(StandardCharsets.UTF_8));

        encoded_hash = Base64.getEncoder().encode(sha512_hash);
        encoded_hash_str = new String(encoded_hash);

        // Clear the password
        Arrays.fill(password, '0');

        return encoded_hash_str;
    }
}

