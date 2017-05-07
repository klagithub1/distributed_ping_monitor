package dsms.server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class UDPClient {

    private int electionMsgTimeout = 500; //in ms

    public UDPClient() {}

    @SuppressWarnings("unchecked")
    public String sendMessage(final String message, final int portNumber) {

        String output = "";
        DatagramSocket aSocket = null;
        try {
            //System.out.println("message is:"+message);
            aSocket = new DatagramSocket();
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = portNumber;
            DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
            aSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.setSoTimeout(electionMsgTimeout);
            aSocket.receive(reply);
            String replyString;
            replyString = (new String(reply.getData(), reply.getOffset(), reply.getLength()));
            String replyString2 = "OK";
            output = replyString2;
            //System.out.println("ReplyString is:" + replyString2);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
        }

        return output;

    }

}





