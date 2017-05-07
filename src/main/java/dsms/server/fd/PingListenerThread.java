package dsms.server.fd;

import dsms.server.replica.Replica;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import dsms.logger.Logger;

public class PingListenerThread extends Thread {
    // -------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------

    // Keep a pointer to an instance of a serverReplica
    private Replica replicaServer;
    private String threadStringID = "";
    private String loggerPath = "";
    private static final String PING_MESSAGE = "PING";

    // -------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------

    // Initialize a status thread by giving it a pointer to the replica server that initializes it
    public PingListenerThread(Replica replicaServerPtr, String internalID) {
        this.replicaServer = replicaServerPtr;
        this.threadStringID = internalID;

        this.loggerPath = this.threadStringID+".txt";
    }

    private String processIncomingMessage(String messageIncoming)
    {
        Logger.log("[PingListenerThread "+threadStringID+"] Processing "+messageIncoming,loggerPath);

        String returnMessage = PING_MESSAGE;
        String[] token = messageIncoming.split("-");

        if(messageIncoming.equalsIgnoreCase(PING_MESSAGE))
        {
            return PING_MESSAGE;
        }
        else if(token.length == 0 )
        {
            return PING_MESSAGE;
        }

        //Iterate through tokens, ignore token[0] which is PING
        for(int i=1; i < token.length; i++)
        {
            Logger.log("[PingListenerThread "+threadStringID+"] Processing "+messageIncoming+" which has tokens. Token: "+token[i],loggerPath);
            for(int j=0; j < this.replicaServer.getHeartbeatEmitterThread().getReplicasPortSuspected().length; j++)
            {
                if(Integer.parseInt(token[i]) == this.replicaServer.getHeartbeatEmitterThread().getReplicasPortSuspected()[j])
                {
                    Logger.log("[PingListenerThread "+threadStringID+"] found port: "+this.replicaServer.getHeartbeatEmitterThread().getReplicasPortSuspected()[j]+" also on my suspected list... adding it to response...",loggerPath);
                    returnMessage = returnMessage + "-"+token[i];
                }
            }
        }

        return returnMessage;
    }

    @Override
    public void run() {
        // Declare a socket
        DatagramSocket serverSocket = null;

        try {
            Logger.log("[PingListenerThread "+threadStringID+"] started running...",loggerPath);

            serverSocket = new DatagramSocket(replicaServer.getReplicaPingListenerPort());
            byte[] receiveDataBuffer = new byte[1024];

            while (true)
            {

                // -------- RECEIVE PART ------------ //
                // Initialize packet received
                DatagramPacket receivePacket = new DatagramPacket(receiveDataBuffer, receiveDataBuffer.length);

                // Wait here to receive a packet ..... (blocked here until smth is received)
                Logger.log("[PingListenerThread "+threadStringID+"] is waiting or an incoming packet on port: "+replicaServer.getReplicaPingListenerPort(),loggerPath);
                serverSocket.receive(receivePacket); // Wait here to receive a packet



                // Decode receivedPacket to String representation, trim it and capitalize just to be sure
                String receivedPacketStringSentence = new String(receivePacket.getData()).trim();
                Logger.log("[PingListenerThread "+threadStringID+"]  >> received a packet "+receivedPacketStringSentence,loggerPath);

                String capitalizedSentence = receivedPacketStringSentence.toUpperCase();

                if(capitalizedSentence.equalsIgnoreCase(PING_MESSAGE))
                {
                    Logger.log("[PingListenerThread "+threadStringID+"] received packet content is: "+capitalizedSentence,loggerPath);
               }
               else
                {
                    Logger.log("[PingListenerThread "+threadStringID+"] received packet content expected : "+PING_MESSAGE+" but received : "+capitalizedSentence,loggerPath);

                    capitalizedSentence = this.processIncomingMessage(capitalizedSentence);
               }
                // ------------------------------------ //



                // -------- SEND PART ------------ //


                Logger.log("[PingListenerThread "+threadStringID+"] packet content to be sent after being processed is: "+capitalizedSentence,loggerPath);


                // Send back the ack packet received... (reply with same response)

                // Resolve IP and Port of the sender
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                byte[] sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

                // Reply to sender with the same message...
                Logger.log("[PingListenerThread "+threadStringID+"] is replying back to sender with message: "+capitalizedSentence+" --> at port: "+port,loggerPath);
                serverSocket.send(sendPacket);
                Logger.log("[PingListenerThread "+threadStringID+"] finished replying.",loggerPath);
                // ------------------------------------ //
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (serverSocket != null)
            {
                serverSocket.close();
            }
        }
    }
}
