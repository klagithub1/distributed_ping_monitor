package dsms.server.fd.test;

import dsms.server.fd.PingEmitterThread;
import dsms.server.fd.PingListenerThread;
import dsms.server.replica.Replica;

public class SecondDriver
{
    public static void main(String[] args)
    {
        // Create a Replica
        Replica r2 = new Replica(2);

        //Set its IP address
        r2.setReplicaIpAddress("127.0.0.1");

        //Set its ping listen port
        r2.setReplicaPingListenerPort(6002);

        // Set the heartbeat listen thread
        r2.setHeartbeatListenerThread(new PingListenerThread(r2, "r2-l2"));

        // Set the heartbeat emitter thread
        r2.setHeartbeatEmitterThread(new PingEmitterThread(r2,"r2-e2"));


        // Add the ports that you want to monitor
        r2.getHeartbeatEmitterThread().monitorPort(6001,0);
        r2.getHeartbeatEmitterThread().monitorPort(6003,1);


        // start listener and start emitter
        r2.getHeartbeatListenerThread().start();
        r2.getHeartbeatEmitterThread().start();
    }
}
