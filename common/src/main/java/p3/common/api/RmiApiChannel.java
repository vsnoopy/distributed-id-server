package p3.common.api;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Interface for RMI communication between the Identity Management Client
 * and the Identity Server.
 */
public interface RmiApiChannel extends Remote
{
    // message ID for the identity management commands.
    public enum MESSAGE_ID {DEFAULT, CREATE, LOOKUP, REVERSE_LOOKUP, MODIFY, DELETE, GET, DELETE_ALL, DEBUG_STATS};
    // message ID for the subcommands support by the GET command.
    public enum GET_TYPE {DEFAULT, USERS, UUIDS, ALL};

    // message id for the specific command being executed
    public static MESSAGE_ID message_id = MESSAGE_ID.DEFAULT;

    /**
     * Test method.
     * @return String containing the name of the command being executed.
     * @throws RemoteException if there is an error processing the command.
     */
    public String ExecuteCommand() throws RemoteException;

    /**
     * Method used to indicate what Identity command is to be processed by the
     * Identity Server and the command's particular to that command.
     * @param message_id Indicates the command to be executed.
     * @param param1 Command dependent parameter.
     * @param param2 Command dependent parameter.
     * @param param3 Command dependent parameter.
     * @return String containing the results of the command's execution.
     * @throws RemoteException if there is an error processing the command.
     */
    public String ExecuteCommand(MESSAGE_ID message_id, String param1, String param2, String param3) throws RemoteException;
}
