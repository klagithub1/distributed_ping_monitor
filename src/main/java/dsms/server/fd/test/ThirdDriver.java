package dsms.server.fd.test;

import dsms.server.fd.PingEmitterThread;
import dsms.server.fd.PingListenerThread;
import dsms.server.replica.Replica;

/**
 * Created by klajdik on 2016-07-30.
 */
public class ThirdDriver
{
    public static void main(String[] args)
    {
        // Create a Replica
        Replica r3 = new Replica(3);

        //Set its IP address
        r3.setReplicaIpAddress("127.0.0.1");

        //Set its ping listen port
        r3.setReplicaPingListenerPort(6003);

        // Set the heartbeat listen thread
        r3.setHeartbeatListenerThread(new PingListenerThread(r3, "r3-l3"));

        // Set the heartbeat emitter thread
        r3.setHeartbeatEmitterThread(new PingEmitterThread(r3,"r3-e3"));


        // Add the ports that you want to monitor
        r3.getHeartbeatEmitterThread().monitorPort(6001,0);
        r3.getHeartbeatEmitterThread().monitorPort(6002,1);


        // start listener and start emitter
        r3.getHeartbeatListenerThread().start();
        r3.getHeartbeatEmitterThread().start();

    }
}
