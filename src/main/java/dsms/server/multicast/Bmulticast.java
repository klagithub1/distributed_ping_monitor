package dsms.server.multicast;

import dsms.logger.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

/**
 * Created by Dick on 2016-08-09.
 */
public class Bmulticast {

    private static final int MAGIC_PORT_1 = 41000;
    private static final int MAGIC_PORT_2 = 42000;
    private static final String addressName = "127.0.0.1";
    private DatagramSocket socket;
    private int serverId;

    public Bmulticast(int serverId) {

        this.serverId = serverId;
        try {
            socket = new DatagramSocket(MAGIC_PORT_1 + serverId);
        } catch (SocketException e) {
            Logger.log("Bmulticast ctor() - " + e.getMessage(), "RM.txt");
        }
        Logger.log(String.format("Bmulticast ctor() - serverId: %d, socket: %s\n", serverId, socket), "BM.txt");
    }

    public void send(int[] group, String piggybackMessage) {
        for (int id = 0; id < group.length; id++) {
            if (group[id] == 1) {
                byte[] buffer = piggybackMessage.getBytes();
                DatagramPacket packet;
                try {
                    packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(addressName), MAGIC_PORT_2 + id + 1);
                    socket.send(packet);
                    Logger.log(String.format("Bmulticast send() - i:%d, leaderID: %s, sequencenumber: %s\n", id, packet, socket), "BM.txt");
                } catch (UnknownHostException e) {
                    Logger.log("Bmulticast send() - " + e.getMessage(), "BM.txt");
                } catch (IOException e) {
                    Logger.log("Bmulticast send() - " + e.getMessage(), "BM.txt");
                } finally {
                    socket.close();
                }
            }
        }
    }
}
