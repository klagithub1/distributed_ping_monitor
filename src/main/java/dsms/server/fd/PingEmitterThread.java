package dsms.server.fd;

import dsms.logger.Logger;
import dsms.server.replica.Replica;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class PingEmitterThread extends Thread {

    // -------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------

    // Predefined sleep time
    private static final int THREAD_SLEEP_TIME = 500; // 500 milliseconds
    private static final int SOCKET_RECEIVE_TIMEOUT_TIME = 500; // 500 milliseconds
    private static final String PING_MESSAGE = "PING";
    private static final String EMPTY_MESSAGE = "EMPTY";
    private static final int LIMIT_TIMEOUT_TOLERANCE = 10;

    // Keep a pointer to an instance of a serverReplica
    private Replica replicaServerReference;

    // Thread's inner ID
    private String threadStringID;

    // File where the output is logged
    private String loggerPath = "";

    // An array keeping other server's replicas ping monitor port
    private int[] replicasPortToMonitor;

    // An array keeping other server's replicas suspected ports
    private int[] replicasPortSuspected;

    // An array keeping other server's replicas suspected ports
    private int[] replicasPortFailed;

    // Number of other replica's port
    private static final int MAX_NUMBER_OF_PORTS_TO_MONITOR = 2;

    // Crash counter
    private Map<String, Integer> suspectCounter = new HashMap<String, Integer>();


    //Port of PING of the leader
    private int leaderPingMonitorPort = 0;


    // -------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------


    public PingEmitterThread(Replica replicaArg, String threadID)
    {
        this.replicaServerReference = replicaArg;
        this.threadStringID = threadID;
        this.replicasPortToMonitor = new int[MAX_NUMBER_OF_PORTS_TO_MONITOR];
        this.replicasPortSuspected = new int[MAX_NUMBER_OF_PORTS_TO_MONITOR];
        this.replicasPortFailed = new int[MAX_NUMBER_OF_PORTS_TO_MONITOR];
        this.loggerPath = this.threadStringID + ".txt";
    }

    @Override
    public void run()
    {
        Logger.log("[PingEmitterThread " + threadStringID + "]  started to run()", loggerPath);
        System.out.println("[PingEmitterThread " + threadStringID + "]  started to run()");

        try
        {
            while (true)
            {
                Logger.log("[PingEmitterThread " + threadStringID + "] \n \n --", loggerPath);
                Logger.log("[PingEmitterThread " + threadStringID + "] --STARTED LOOP ITERATION IN PINGING ALL THE OTHER PROCESSES--", loggerPath);

                //Sleep
                Logger.log("[PingEmitterThread " + threadStringID + "] go to sleep for 500 ms", loggerPath);
                Thread.sleep(THREAD_SLEEP_TIME);
                Logger.log("[PingEmitterThread " + threadStringID + "] wake up after 500 ms", loggerPath);

                // Start iteration
                Logger.log("[PingEmitterThread " + threadStringID + "] iterate through all other replica's ping ports", loggerPath);

                // Consistent check for emptiness
                int totalPorts = 0;
                for (int i = 0; i < replicasPortToMonitor.length; i++)
                {
                    if( replicasPortToMonitor[i] > 0 )
                    {
                        totalPorts += replicasPortToMonitor[i];
                    }
                }

                // If the replicas to monitor list is empty then dont monitor
                if(totalPorts == 0)
                {
                    Logger.log("[PingEmitterThread " + threadStringID + "] There is no replicas to monitor, they have all failed, skipping rest of loop", loggerPath);
                    continue;
                }

                for (int i = 0; i < replicasPortToMonitor.length; i++)
                {
                    if( replicasPortToMonitor[i] > 0 )
                    {
                        Logger.log("[PingEmitterThread " + threadStringID + "] ", loggerPath);
                        Logger.log("[PingEmitterThread " + threadStringID + "] Thread [" + threadStringID + "] send Ping to port: " + replicasPortToMonitor[i], loggerPath);

                        this.sendPingToReplica(replicasPortToMonitor[i], "127.0.0.1");
                        Logger.log("[PingEmitterThread " + threadStringID + "] ", loggerPath);
                    }
                }

                Logger.log("[PingEmitterThread " + threadStringID + "] --ENDED LOOP ITERATION IN PINGING ALL THE OTHER PROCESSES--", loggerPath);
                Logger.log("[PingEmitterThread " + threadStringID + "] \n \n --", loggerPath);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void sendPingToReplica(int replicaPingListenPort, String replicaAddress)
    {
        String replicaServerResponse = "";

        Logger.log("[PingEmitterThread " + threadStringID + "] Checking for suspected ports to ask for consensus ", loggerPath);

        String udpMessage = this.checkForConsensus();

        Logger.log("[PingEmitterThread " + threadStringID + "] Prepared  message : "+udpMessage+" sending it to sendPingMessage", loggerPath);

        // Send the actual message
        replicaServerResponse = this.sendMessage(udpMessage, replicaAddress, replicaPingListenPort).trim();

        // Process the answer from the server

        //Split the return message in tokens
        //String[] token = replicaServerResponse.split("-");

        if (replicaServerResponse.equalsIgnoreCase(PING_MESSAGE))
        {
            Logger.log("[PingEmitterThread " + threadStringID + "] received a simple : "+replicaServerResponse+" from replica, replica at port: "+replicaPingListenPort+ " is alive!", loggerPath);

        }
        else if(replicaServerResponse.equalsIgnoreCase(EMPTY_MESSAGE))
        {
            Logger.log("[PingEmitterThread " + threadStringID + "] received: "+replicaServerResponse+" instead of "+udpMessage+" from replica, maybe is failed or suspected to be failed...", loggerPath);

        }
        else if(replicaServerResponse.contains("-"))
        {
            Logger.log("[PingEmitterThread " + threadStringID + "] received: "+replicaServerResponse+", while sent: "+udpMessage+", need to check further...", loggerPath);

            //Split the return message in tokens
            String[] token = replicaServerResponse.split("-");

            // Iterate through all such tokens
            for(int i=1; i < token.length; i ++)
            {
                Logger.log("[PingEmitterThread " + threadStringID + "] checking confirmation on port: "+token[i], loggerPath);

                // For each token that we got, if that port is suspected flag it as failed as it was part of my consensus asked, without double check
                for (int j=0; j < this.replicasPortSuspected.length; j++)
                {
                    if(this.replicasPortSuspected[j] == Integer.parseInt(token[i]))
                    {
                        Logger.log("[PingEmitterThread " + threadStringID + "] port: "+token[i]+" matches the suspected port: "+replicasPortSuspected[j]+", removing this port as failed", loggerPath);
                        this.markPermanentlyFailed(this.replicasPortSuspected[j]);
                    }
                }
            }
            token = null;
        }
    }

    private String sendMessage(String message, String address, int port)
    {
        DatagramSocket socket = null;

        try
        {
            // -------- SEND PART ------------ //
            socket = new DatagramSocket();
            InetAddress host = null;
            host = InetAddress.getByName(address);

            String requestData = message;
            byte[] udpMessage = requestData.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(udpMessage, udpMessage.length, host, port);
            Logger.log("[PingEmitterThread " + threadStringID + "] Open socket and send message: "+requestData, loggerPath);
            socket.send(sendPacket);
            Logger.log("[PingEmitterThread " + threadStringID + "] message was sent, now waiting for reply", loggerPath);
            // ------------------------------------ //


            // -------- RECEIVE PART ------------ //
            Logger.log("[PingEmitterThread " + threadStringID + "] socket receive timeout is set", loggerPath);
            socket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT_TIME);

            byte[] buffer = new byte[1000];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            Logger.log("[PingEmitterThread " + threadStringID + "] waiting to receive "+requestData+" back", loggerPath);
            socket.receive(receivedPacket);

            Logger.log("[PingEmitterThread " + threadStringID + "] Received message, building string", loggerPath);
            String result = new String(receivedPacket.getData());
            Logger.log("[PingEmitterThread " + threadStringID + "] Received message " + result.trim(), loggerPath);

            Logger.log("[PingEmitterThread " + threadStringID + "] returning message", loggerPath);


            // Check and, If this port was flagged as suspected before, remove the flag as we now received data
            for(int i=0; i < this.replicasPortSuspected.length; i++)
            {
                if(this.replicasPortSuspected[i] == port)
                {
                    this.unSuspectPort(port);
                }
            }
            // END check before was flagged as a suspect

            return result.trim();
            // ------------------------------------ //
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (SocketTimeoutException e)
        {

            Logger.log("******** [PingEmitterThread " + threadStringID + "] timeout exception on port: " + port + " *********", loggerPath);

            // Adding it to the suspected ports list

            suspectPort(port);

            // If all ports are timing out and this current one that just timed out did it more than the limit, then fail it.
            if( this.allPortsSuspected() )
            {
                Integer currentValue = this.suspectCounter.get( Integer.toString(port) );

                if(currentValue.intValue() > LIMIT_TIMEOUT_TOLERANCE) // has timed out more times than allowed
                {
                    Logger.log("[PingEmitterThread " + threadStringID + "] Port: " + port + " is suspected more than "+LIMIT_TIMEOUT_TOLERANCE+" times in a row, and all other ports are suspected, so remove it WITHOUT CONSENSUS", loggerPath);
                    this.markPermanentlyFailed(port);
                }
            }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (socket != null) socket.close();
        }

        Logger.log("[PingEmitterThread " + threadStringID + "] returning EMPTY message", loggerPath);

        return EMPTY_MESSAGE;
    }

    public void monitorPort(int portNumber, int index) {
        Logger.log("[PingEmitterThread " + threadStringID + "] added port " + portNumber, loggerPath);

        if (index < MAX_NUMBER_OF_PORTS_TO_MONITOR)
        {
            this.replicasPortToMonitor[index] = portNumber;

            suspectCounter.put( Integer.toString(portNumber),(new Integer(0)) );
        }
        else
        {
            Logger.log("[PingEmitterThread " + threadStringID + "] trying to add port " + portNumber + " but cannot monitor more than 2 ports!", loggerPath);
        }
    }

    private void suspectPort(int portNumber)
    {
        boolean alreadySuspected = false;
        for(int i=0; i < this.replicasPortSuspected.length; i ++)
        {
            if(replicasPortSuspected[i] == portNumber)
            {
                Logger.log("[PingEmitterThread " + threadStringID + "]   Port: " + portNumber + " is already suspected, incrementing counter ...", loggerPath);
                alreadySuspected = true;

                //Increment Counter of how many time it has been a suspect
                Logger.log("[PingEmitterThread " + threadStringID + "]   Port is suspected : "+this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) ).intValue()+" times", loggerPath);

                Integer suspectCounter = this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) ).intValue();

                this.suspectCounter.put( Integer.toString(this.replicasPortSuspected[i]), new Integer(suspectCounter.intValue()+1));

                Logger.log("[PingEmitterThread " + threadStringID + "]   Port is now suspected : "+this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) ).intValue()+" times", loggerPath);
            }
        }

        if(alreadySuspected == false)
        {
            for(int i=0; i < this.replicasPortSuspected.length; i ++)
            {
                if(replicasPortSuspected[i] == 0)
                {
                    replicasPortSuspected[i] = portNumber;
                    Logger.log("[PingEmitterThread " + threadStringID + "] added  Port: " + portNumber + " to its suspected list... may be crashed ...", loggerPath);

                    // Must be entered only in one position!

                    //Increment Counter of how many time it has been a suspect
                    Logger.log("[PingEmitterThread " + threadStringID + "]   Port is suspected (more than 0): "+this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) ).intValue()+" times", loggerPath);

                    Integer suspectCounter = this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) ).intValue();
                    this.suspectCounter.put( Integer.toString(this.replicasPortSuspected[i]), new Integer(suspectCounter.intValue()+1));

                    Logger.log("[PingEmitterThread " + threadStringID + "]   Port is now suspected (more than 0): "+this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) ).intValue()+" times", loggerPath);

                    break;
                }
            }
        }
    }

    private boolean allPortsSuspected()
    {
        for(int i=0; i < this.replicasPortSuspected.length; i++)
        {
            if(this.replicasPortSuspected[i] == 0) // If there is at least one empty spot then there is someone alive
            {
                return false;
            }
        }

        Logger.log("[PingEmitterThread " + threadStringID + "] All Ports are now Suspected as failed", loggerPath);
        return true;
    }

    private void unSuspectPort(int portNumber)
    {
        for(int i=0; i < this.replicasPortSuspected.length; i ++)
        {
            if(replicasPortSuspected[i] == portNumber)
            {
                replicasPortSuspected[i] = 0;
                this.suspectCounter.put(Integer.toString(portNumber), new Integer(0));
                Logger.log("[PingEmitterThread " + threadStringID + "] Port: " + portNumber + " is no longer a suspect and its failed counter has been reset to: "+(this.suspectCounter.get( Integer.toString(portNumber) )).intValue(), loggerPath);
                break;
            }
        }
    }

    public int[] getReplicasPortSuspected()
    {
        return replicasPortSuspected;
    }

    private String checkForConsensus()
    {
        String returnValue = PING_MESSAGE;

        // Go through all the suspected ports
        for( int i=0; i < this.replicasPortSuspected.length; i ++)
        {
            if( this.replicasPortSuspected[i] > 0 ) // A valid suspected port
            {
                if( isFailed(this.replicasPortSuspected[i]) == false) // Never marked as failed
                {
                    Logger.log("[PingEmitterThread " + threadStringID + "] Port: "+replicasPortSuspected[i]+" is suspected and is not marked previously as failed, being checked", loggerPath);

                    Integer currentValue = this.suspectCounter.get( Integer.toString(this.replicasPortSuspected[i]) );

                    if(currentValue.intValue() > LIMIT_TIMEOUT_TOLERANCE) // has timed out more times than allowed
                    {
                        Logger.log("[PingEmitterThread " + threadStringID + "] Port: " + this.replicasPortSuspected[i] + " is suspected more than "+LIMIT_TIMEOUT_TOLERANCE+" times in a row, ask peer for consensus to remove it as failed", loggerPath);
                        returnValue = returnValue + "-"+this.replicasPortSuspected[i];
                    }
                }
            }
        }

        Logger.log("[PingEmitterThread " + threadStringID + "] Returning message: "+returnValue+" after asking for consensus.", loggerPath);
        return returnValue;
    }

    private boolean isFailed(int portNr)
    {
        for(int i=0; i < this.replicasPortFailed.length; i++)
        {
            if(this.replicasPortFailed[i] == portNr)
            {
                Logger.log("[PingEmitterThread " + threadStringID + "] Port: "+portNr+" is in the Failed List", loggerPath);
               // System.out.println("[PingEmitterThread " + threadStringID + "] Port: "+portNr+" is in the Failed List");

                return true;
            }
        }
        return false;
    }

    public int getLeaderPingMonitorPort()
    {
        return leaderPingMonitorPort;
    }

    public void setLeaderPingMonitorPort(int leaderPingMonitorPort)
    {
        this.leaderPingMonitorPort = leaderPingMonitorPort;
    }

    private void markPermanentlyFailed(int portNr)
    {
        Logger.log("[PingEmitterThread " + threadStringID + "] :: markPermanentlyFailed", loggerPath);

        // Check if this port is already on the list of the failed ports
        for(int i=0; i < this.replicasPortFailed.length; i++)
        {
            if( this.replicasPortFailed[i] == portNr )
            {
                // Found port already in the list, don't insert it
                Logger.log("[PingEmitterThread " + threadStringID + "] Port: "+portNr+" has already failed, attempt aborted.", loggerPath);
                return;
            }
        }

        for(int j=0; j < this.replicasPortFailed.length; j++)
        {
            if( this.replicasPortFailed[j] == 0 ) //Find an empty spot and insert it
            {
                // Mark this port as failed
                Logger.log("[PingEmitterThread " + threadStringID + "] we are now sure that port: "+portNr+" has permanently failed", loggerPath);

                this.replicasPortFailed[j] = portNr;

                // Stop checking it in the future iterations
                for(int k=0; k < this.replicasPortToMonitor.length; k++)
                {
                    if(this.replicasPortToMonitor[k] == portNr)
                    {
                        Logger.log("[PingEmitterThread " + threadStringID + "] Stop monitoring port: "+portNr+" as it has permanently failed", loggerPath);
                        //System.out.println("[PingEmitterThread " + threadStringID + "] Stop monitoring port: "+portNr+" as it has permanently failed");

                        this.replicasPortToMonitor[k] = 0;
                    }
                }

                // @KARL -- pls check this...
                //Check if the leader has failed to trigger an election, ugly hack :(
                if(this.replicaServerReference.getLeader() == (portNr - 6000))
                {
                    Logger.log("[PingEmitterThread " + threadStringID + "] Leader has failed", loggerPath);
                    System.out.println("[PingEmitterThread " + threadStringID + "] Leader has failed");

                    try
                    {
                        Logger.log("[PingEmitterThread " + threadStringID + "] Begin election", loggerPath);
                        System.out.println("[PingEmitterThread " + threadStringID + "] Begin election");
                        this.replicaServerReference.changeReplicaStatus((portNr - 6000),0);
                        this.replicaServerReference.beginElection();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                }
                // @Karl pls check up....

                Logger.log("[PingEmitterThread " + threadStringID + "] breaking out of the loop", loggerPath);
                break;
            }
        }
    }
}
