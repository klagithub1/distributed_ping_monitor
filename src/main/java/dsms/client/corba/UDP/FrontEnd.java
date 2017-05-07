package dsms.client.corba.UDP;

import dsms.server.udp.*;
import dsms.server.udp.UDPClient;

public class FrontEnd {

    public static int leader = 3;
    public final static int newLeaderListenerPort = 3333;
    public static int[] replicaPortArray = new int[]{7070,7071,7072};
    public static int destinationPort = replicaPortArray[leader-1];
    public Thread newLeaderThread;


   public FrontEnd(){
       newLeaderThread = new Thread(new UDPListener(newLeaderListenerPort, this));
        newLeaderThread.start();
    }

    public static String sendMessage(String message){
        UDPClient c1 = new UDPClient();
        c1.sendMessage(message,destinationPort);
        return "message successful";
    }

    public static void changeLeader(int ldr){
        leader = ldr;
    }

}
