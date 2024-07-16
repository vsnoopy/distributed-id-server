package p3;

import p3.server.cmd.IdServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for IdServer.
 */
public class ServerTest
{
    private IdServer server;

    /**
     * Setups up server unit test.
     * @throws RemoteException
     */
    @BeforeEach
    public void setUp() throws RemoteException
    {
        //server = new IdServer();
    }

    /**
     * Test constructor for server unit test.
     */
    @Test
    public void testConstructor()
    {
        //assertNotEquals(server, null);
    }
}
