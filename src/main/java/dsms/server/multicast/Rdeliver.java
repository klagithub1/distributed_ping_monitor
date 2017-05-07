package dsms.server.multicast;

import dsms.logger.Logger;
import dsms.server.replica.Replica;

import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

/**
 * Created by rsmith on 2016-08-09.
 */
public class Rdeliver implements Runnable {

    private static final int MAGIC_PORT_3 = 43000;
    private static final int BUFFER_SIZE = 1024;
    private static final String addressName = "127.0.0.1";
    private DatagramSocket socket;
    private int serverId;
    private Hashtable<Integer, Integer> group;
    private int sequenceNumber;
    Replica replica;

    public Rdeliver(int serverId, Replica replica) {

        this.serverId = serverId;
        try {
            socket = new DatagramSocket(MAGIC_PORT_3 + serverId);
        } catch (SocketException e) {
            Logger.log("Rdeliver ctor() - " + e.getMessage(), "RD.txt");
        }
        sequenceNumber = 0;
    }

    @Override
    public void run() {
        while (true) {
            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                Logger.log("Rdeliver run() - " + e.getMessage(), "RD.txt");
            }
            String received = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (sequenceNumber == Integer.parseInt(received.split("\\|")[1]) + 1) {
                // TODO CALL FRONT END OR SOMETHING ??
                byte[] buffer = received.getBytes();
                DatagramPacket sendPacket;
                try {
                    sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(addressName), replica.getFrontEndListenerPort());
                    socket.send(sendPacket);
                } catch (UnknownHostException e) {
                    Logger.log("Bdeliver run() - " + e.getMessage(), "BD.txt");
                } catch (IOException e) {
                    Logger.log("Bdeliver run() - " + e.getMessage(), "BD.txt");
                }
            }
            // HOLDBACK QUEUE if (S>R_g^q+1)
        }
    }
}
