package p3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p3.client.cmd.IdClient;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for IdClient.
 */
public class ClientTest
{
    private IdClient client;

    /**
     * Setup for the client test.
     * @throws RemoteException
     */
    @BeforeEach
    public void setUp() throws RemoteException
    {
        client = new IdClient();
    }

    /**
     * Constructor for the client test.
     */
    @Test
    public void testConstructor()
    {
        assertNotEquals(client, null);
    }
}
