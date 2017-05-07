package dsms.server.fd.test;

import dsms.server.fd.PingEmitterThread;
import dsms.server.fd.PingListenerThread;
import dsms.server.replica.Replica;

public class FirstDriver
{
    public static void main(String[] args)
    {

       // String test = "PING-5003-4003-099";

        //String[] tocken = test.split("-");

        //for(int i=1; i < tocken.length; i++)
       // {
        //    System.out.println(Integer.parseInt(tocken[i]));
       // }

        // Create a Replica
        Replica r1 = new Replica(1);

        //Set its IP address
        r1.setReplicaIpAddress("127.0.0.1");

        //Set its ping listen port
        r1.setReplicaPingListenerPort(6001);

        // Set the heartbeat listen thread
        r1.setHeartbeatListenerThread(new PingListenerThread(r1, "r1-l1"));

        // Set the heartbeat emitter thread
        r1.setHeartbeatEmitterThread(new PingEmitterThread(r1,"r1-e1"));


        // Add the ports that you want to monitor
        r1.getHeartbeatEmitterThread().monitorPort(6002,0);
        r1.getHeartbeatEmitterThread().monitorPort(6003,1);


        // start listener and start emitter
        r1.getHeartbeatListenerThread().start();
        r1.getHeartbeatEmitterThread().start();
    }
}
