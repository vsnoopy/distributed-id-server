package p3.client.cmd;

import p3.common.api.RmiApiChannel;
import p3.common.util.XColor;
import p3.common.util.XUtil;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Class used for executing test cases for each Identity server command.
 */
public class IntTestCases
{
    private static final ArrayList<String> login_names =
        new ArrayList<>(Arrays.asList("alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa"));

    // TODO: WARNING: the fact that the array is static is a problem, but not serious, consider fixing
    private static final ArrayList<String> login_uuids = new ArrayList<>();

    private static final Object critical_section = new Object();

    // synchronizing the client threads allows the client side debug output
    // to be viewed more easily
    // the server will be effectively operating as single thread though
    private static final boolean synchronize_threads = false;


    /**
     * Simple examples of how to use of the IdServer/IdClient commands;
     */
    private static void SimpleExample()
    {
        final IdClient client = new IdClient();
        final RmiApiChannel rmi_api_channel;
        final String login_name = "login_name";
        final String new_login_name = "new_login_name";
        final String real_name = System.getProperty("user.name");
        final String password = "password";
        String uuid = null;
        String result = null;


        System.out.println("SimpleExample()");
        System.out.println();

        rmi_api_channel = client.LoadClient();

        try {
            uuid = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.CREATE, login_name, real_name, password);

            System.out.println("CREATE: result = " + uuid);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.DEBUG_STATS, null, null, null);

            System.out.println("DEBUG_STATS: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.LOOKUP, login_name, null, null);

            System.out.println("LOOKUP: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.REVERSE_LOOKUP, uuid, null, null);

            System.out.println("REVERSE_LOOKUP: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.MODIFY, login_name, new_login_name, password);

            System.out.println("MODIFY: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.GET, RmiApiChannel.GET_TYPE.USERS.name(), null, null);

            System.out.println("GET-USERS: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.GET, RmiApiChannel.GET_TYPE.UUIDS.name(), null, null);

            System.out.println("GET-UUIDS: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.GET, RmiApiChannel.GET_TYPE.ALL.name(), null, null);

            System.out.println("GET-ALL: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.DELETE, new_login_name, password, null);

            System.out.println("DELETE: result = " + result);

            result = rmi_api_channel.ExecuteCommand(RmiApiChannel.MESSAGE_ID.DEBUG_STATS, null, null, null);

            System.out.println("DEBUG_STATS: result = " + result);

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Extensive test cases.
     *  Can be either single thread or multi-threaded.
     *  When multi-threaded, multiple clients are simulated.
     */
    protected static void ExecuteTests()
    {
        final boolean multi_thread_test = true;

        IdClient client0 = new IdClient();
        IdClient client1 = new IdClient();
        IdClient client2 = new IdClient();
        IdClient client3 = new IdClient();
        IdClient client4 = new IdClient();
        IdClient client5 = new IdClient();
        IdClient client6 = new IdClient();
        IdClient client7 = new IdClient();
        IdClient client8 = new IdClient();
        IdClient client9 = new IdClient();

        client0.start();
        if (multi_thread_test) {
            client1.start();
            client2.start();
            client3.start();
            client4.start();
            client5.start();
            client6.start();
            client7.start();
            client8.start();
            client9.start();
        }

        try {
            client1.join();
            if (!multi_thread_test) {
                client1.join();
                client2.join();
                client3.join();
                client4.join();
                client5.join();
                client6.join();
                client7.join();
                client8.join();
                client9.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for testing each Identity server command.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    static void Tests(RmiApiChannel remote_class_obj)
    {
        Thread.yield();
        DebugReport(remote_class_obj);

        // CREATE a group for identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                CreateGroup(remote_class_obj);
            }
        } else {
            CreateGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // LOOKUP the group of identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                LookUpGroup(remote_class_obj);
            }
        } else {
            LookUpGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // REVERSE LOOKUP the group of identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                ReverseLookUpGroup(remote_class_obj);
            }
        } else {
            ReverseLookUpGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // MODIFY the group of identities for this thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                ModifyGroup(remote_class_obj);
            }
        } else {
            ModifyGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // UN-MODIFY the group of identities for this thread
        // special test case to reverse the MODIFY above
        if (synchronize_threads) {
            synchronized (critical_section) {
                ReverseModifyGroupSpecial(remote_class_obj);
            }
        } else {
            ReverseModifyGroupSpecial(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // DELETE a group for identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                DeleteGroup(remote_class_obj);
            }
        } else {
            DeleteGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        login_uuids.clear();

        // CREATE a group for identities for this client thread again
        if (synchronize_threads) {
            synchronized (critical_section) {
                CreateGroup(remote_class_obj);
            }
        } else {
            CreateGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // GET-USER for a group for identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                GetUserGroup(remote_class_obj);
            }
        } else {
            GetUserGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // GET-UUID for a group for identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                GetUUIDGroup(remote_class_obj);
            }
        } else {
            GetUUIDGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // GET-ALL for a group for identities for this client thread
        if (synchronize_threads) {
            synchronized (critical_section) {
                GetAllGroup(remote_class_obj);
            }
        } else {
            GetAllGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        // DELETE a group for identities for this client thread again
        if (synchronize_threads) {
            synchronized (critical_section) {
                DeleteGroup(remote_class_obj);
            }
        } else {
            DeleteGroup(remote_class_obj);
        }

        Thread.yield();
        DebugReport(remote_class_obj);

        login_uuids.clear();

        System.out.println("login_uuids.size() = " + login_uuids.size());
    }

    /**
     * Test for the CREATE command.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void CreateGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        String uuid = null;
        String unique_login_name = null;
        String real_name = null;
        String password = null;
        String hash_password = null;
        int counter = 0;

        message_id = RmiApiChannel.MESSAGE_ID.CREATE;
        XUtil.DisplayTestHeader(message_id.name());

        for (String login_name : login_names) {
            unique_login_name = GenerateUniqueLoginName(login_name);
            real_name = GenerateRealNameFromLoginName(unique_login_name);
            password = GeneratePasswordFromLoginName(unique_login_name);
            hash_password = IdClient.Sha512Hash(password.toCharArray());
            uuid = SendToServer(remote_class_obj, message_id, unique_login_name, real_name, hash_password);
            PrintResultThread(uuid);
            // save the  UUIDs for the reverse lookup tests
            login_uuids.add(uuid);
            counter++;
        }

        XUtil.DisplayTestFooter("Counter = " + counter);
    }

    /**
     * Test for the LOOKUP command.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void LookUpGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        String unique_login_name = null;
        String result = null;
        int counter = 0;

        message_id = RmiApiChannel.MESSAGE_ID.LOOKUP;
        XUtil.DisplayTestHeader(message_id.name());

        for (String login_name : login_names) {
            unique_login_name = GenerateUniqueLoginName(login_name);
            result = SendToServer(remote_class_obj, message_id, unique_login_name, null, null);
            PrintResultThread(result);
            counter++;
        }
        XUtil.DisplayTestFooter("Counter = " + counter);
    }

    /**
     * Test for the REVERSE-LOOKUP command.
     * @param remote_class_obj RMI remote object reference.
     */
    private static void ReverseLookUpGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        String result = null;
        int counter = 0;

        message_id = RmiApiChannel.MESSAGE_ID.REVERSE_LOOKUP;
        XUtil.DisplayTestHeader(message_id.name());

        for (String user_uuid : login_uuids) {
            result = SendToServer(remote_class_obj, message_id, user_uuid, null, null);
            PrintResultThread(result);
            counter++;
        }

        XUtil.DisplayTestFooter("Counter = " + counter);
    }

    /**
     * Test for the MODIFY command.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void ModifyGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        String unique_login_name = null;
        String new_login_name = null;
        String password = null;
        String hash_password = null;
        String result = null;
        int counter = 0;

        message_id = RmiApiChannel.MESSAGE_ID.MODIFY;
        XUtil.DisplayTestHeader(message_id.name());

        for (String login_name : login_names) {
            unique_login_name = GenerateUniqueLoginName(login_name);
            new_login_name = GenerateNewNameFormCurrentName(unique_login_name);
            password = GeneratePasswordFromLoginName(unique_login_name);
            hash_password = IdClient.Sha512Hash(password.toCharArray());
            result = SendToServer(remote_class_obj, message_id, unique_login_name, new_login_name, hash_password);
            PrintResultThread(result);
            counter++;
        }

        XUtil.DisplayTestFooter("Counter = " + counter);
    }

    /**
     * Test for the reverse MODIFY "command".
     * This is not a real Identity server command.
     * Purpose is to reverse the effect of the MODIFY command test.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void ReverseModifyGroupSpecial(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        String unique_login_name = null;
        String current_login_name = null;
        String new_login_name = null;
        String password = null;
        String hash_password = null;
        String result = null;
        int counter = 0;

        message_id = RmiApiChannel.MESSAGE_ID.MODIFY;
        XUtil.DisplayTestHeader(message_id.name());

        for (String login_name : login_names) {
            unique_login_name = GenerateUniqueLoginName(login_name);
            new_login_name = unique_login_name;
            current_login_name = GenerateNewNameFormCurrentName(unique_login_name);
            password = GeneratePasswordFromLoginName(unique_login_name);
            hash_password = IdClient.Sha512Hash(password.toCharArray());
            result = SendToServer(remote_class_obj, message_id, current_login_name, new_login_name, hash_password);
            PrintResultThread(result);
            counter++;
        }

        XUtil.DisplayTestFooter("Counter = " + counter);
    }

    /**
     * Test for the DELETE command.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void DeleteGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        String unique_login_name = null;
        String password = null;
        String hash_password = null;
        String result = null;
        int counter = 0;

        message_id = RmiApiChannel.MESSAGE_ID.DELETE;
        XUtil.DisplayTestHeader(message_id.name());

        for (String login_name : login_names) {
            unique_login_name = GenerateUniqueLoginName(login_name);
            password = GeneratePasswordFromLoginName(unique_login_name);
            hash_password = IdClient.Sha512Hash(password.toCharArray());
            result = SendToServer(remote_class_obj, message_id, unique_login_name, hash_password, null);
            PrintResultThread(result);
            counter++;
        }
        XUtil.DisplayTestFooter("Counter = " + counter);
    }

    /**
     * Test for the GET command with the USERS option.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void GetUserGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        final RmiApiChannel.GET_TYPE get_type;
        String result = null;

        message_id = RmiApiChannel.MESSAGE_ID.GET;
        get_type = RmiApiChannel.GET_TYPE.USERS;
        XUtil.DisplayTestHeader(message_id.name() + " : " + get_type.name());

        result = SendToServer(remote_class_obj, message_id, get_type.name(), null, null);
        PrintResultThread(result, true);
    }

    /**
     * Test for the GET command with the UUIDS option.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void GetUUIDGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        final RmiApiChannel.GET_TYPE get_type;
        String result = null;

        message_id = RmiApiChannel.MESSAGE_ID.GET;
        get_type = RmiApiChannel.GET_TYPE.UUIDS;
        XUtil.DisplayTestHeader(message_id.name() + " : " + get_type.name());

        result = SendToServer(remote_class_obj, message_id, get_type.name(), null, null);
        PrintResultThread(result, true);
    }

    /**
     * Test for the GET command with the ALL option.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void GetAllGroup(RmiApiChannel remote_class_obj)
    {
        final RmiApiChannel.MESSAGE_ID message_id;
        final RmiApiChannel.GET_TYPE get_type;
        String result = null;

        message_id = RmiApiChannel.MESSAGE_ID.GET;
        get_type = RmiApiChannel.GET_TYPE.ALL;
        XUtil.DisplayTestHeader(message_id.name() + " : " + get_type.name());

        result = SendToServer(remote_class_obj, message_id, get_type.name(), null, null);
        PrintResultThread(result, true);
    }

    /**
     * Test for the DEBUG-STATS command.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     */
    private static void DebugReport(RmiApiChannel remote_class_obj)
    {
        final String result;

        result = SendToServer(remote_class_obj, RmiApiChannel.MESSAGE_ID.DEBUG_STATS, null, null, null);
        XUtil.PrintThread(XColor.CYAN + "RmiApiChannel.MESSAGE_ID.DEBUG_STATS -> " + result + XColor.RESET);
    }

    /**
     * Send the RMI object to the server for execution and
     * receive the results.
     * @param remote_class_obj RMI object used to communicate with Identity server.
     * @param message_id Command id.
     * @param param1 Parameter for the command.
     * @param param2 Parameter for the command.
     * @param param3 Parameter for the command.
     * @return
     */
    private static String SendToServer(RmiApiChannel remote_class_obj, RmiApiChannel.MESSAGE_ID message_id, String param1, String param2, String param3)
    {
        final String result;

        try {
            result = remote_class_obj.ExecuteCommand(message_id, param1, param2, param3);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Generates a unique login name for testing.
     * @param login_name
     * @return Unique login name.
     */
    private static String GenerateUniqueLoginName(String login_name)
    {
        long thread_id = Thread.currentThread().getId();

        return login_name + thread_id;
    }

    /**
     * Generates a real login name for testing.
     * @param login_name
     * @return Real logn name.
     */
    private static String GenerateRealNameFromLoginName(String login_name)
    {
        return login_name.toUpperCase();
    }

    /**
     * Generates a password based on login name for testing.
     * @param login_name
     * @return Password.
     */
    private static String GeneratePasswordFromLoginName(String login_name)
    {
        return login_name + "-pw";
    }

    /**
     * Generate a new login name for testing.
     * The new login name is based on the login name and
     * is used for testing the modify command.
     * @param current_login_name
     * @return New login name.
     */
    private static String GenerateNewNameFormCurrentName(String current_login_name)
    {
        return current_login_name + "-new";
    }

    /**
     * Print results.
     * @param result Result.
     */
    private static void PrintResultThread(String result)
    {
        PrintResultThread(result, false);
    }

    /**
     * Print results.
     * @param result Result.
     * @param new_line Indicate is a new line should be used.
     */
    private static void PrintResultThread(String result, boolean new_line)
    {
        if (new_line) {
            XUtil.PrintThread(XColor.BLACK + "Result ->\n" + XColor.RESET + result);
        } else {
            XUtil.PrintThread(XColor.BLACK + "Result ->" + XColor.RESET + " " + result);
        }
    }
}
