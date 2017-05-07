package dsms.server.multicast;

import dsms.logger.Logger;
import dsms.server.replica.Replica;

import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

/**
 * Created by Dick on 2016-08-09.
 */
public class Bdeliver implements Runnable {

    private static final int MAGIC_PORT_2 = 42000;
    private static final int MAGIC_PORT_3 = 43000;
    private static final int BUFFER_SIZE = 1024;
    private static final String addressName = "127.0.0.1";
    private DatagramSocket socket;
    private int serverId;
    private int leaderRdeliverPort;
    private int[] group;
    private int sequenceNumber;
    private Replica replica;

    public Bdeliver(int serverId, int leaderId, int[] group, Replica replica) {

        this.serverId = serverId;
        try {
            socket = new DatagramSocket(MAGIC_PORT_2 + serverId);
        } catch (SocketException e) {
            Logger.log("Bdeliver ctor() - " + e.getMessage(), "BD.txt");
        }
        this.leaderRdeliverPort = MAGIC_PORT_3 + leaderId;
        this.group = group;
        this.sequenceNumber = 0;
        this.replica = replica;
    }

    @Override
    public void run() {
        while (true) {
            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                Logger.log("Bdeliver run() - " + e.getMessage(), "BD.txt");
            }
            String received = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (Integer.parseInt(received.split("\\|")[1]) == sequenceNumber) {
                sequenceNumber++;
                if (Integer.parseInt(received.split("\\|")[0]) != serverId) {
                    (new Bmulticast(serverId)).send(group, received);
                }

                //TODO: PERFORM ACTION
                Logger.log("Bdeliver run() - " + received, "BD.txt");
                String message = received.split("\\|")[3];
                Logger.log("Bdeliver run() - " + message, "BD.txt");
                String reply = "replied";
                String messageType = received.split("\\|")[2];
                Logger.log("Bdeliver run() - " + messageType, "BD.txt");
                String info; //info needed to create or edit record
                String[] parts; //array to store this info

                switch (messageType) {

                    case "CREATE_DOCTOR":
                        //create d record
                        //String Message = mid + "-" + id + "-" + fn + "-" + ln + "-" + addr + "-" + phn + "-" + spcl + "-" + lct;
                        info = message.substring(4);
                        System.out.println("info is:" + info);
                        parts = info.split("-");
                    {
                        String mid = parts[0];
                        String id = parts[1];
                        String fn = parts[2];
                        String ln = parts[3];
                        String addr = parts[4];
                        String phn = parts[5];
                        String specl = parts[6];
                        String lct = parts[7];
                        //TODO Create the actual record
                        replica.createDrecord(mid, id, fn, ln, addr, phn, specl, lct);
                        reply = "Doctor record has been created with ID" + id;
                    }

                    break;

                    case "CREATE_NURSE":
                        //create n record
                        //String Message = "crn" + "-" + mid + "-" + id + "-" + fn + "-" + ln + "-" + destination + "-" + status + "-" + statusdate ;
                        info = message.substring(4);
                        parts = info.split("-");
                    {
                        String mid = parts[0];
                        String id = parts[1];
                        String fn = parts[2];
                        String ln = parts[3];
                        String designation = parts[4];
                        String status = parts[5];
                        String statusDate = parts[6];
                        String lct = mid.substring(0, 3);
                        //TODO create the actual record
                        replica.createNrecord(mid, id, fn, ln, designation, status, statusDate);
                        reply = "Nurse record has been created with ID" + id;
                    }
                    break;

                    case "EDIT_RECORD":
                        //edit record
                        //String Message = "edr" + "-" + mid + "-" + id + "-" + fieldname + "-" + newvalue ;
                        info = message.substring(4);
                        parts = info.split("-");

                    {
                        String mid = parts[0];
                        String id = parts[1];
                        String fieldname = parts[2];
                        String newvalue = parts[3];
                        String lct = mid.substring(0, 3);
                        replica.EditData(mid, id, fieldname, newvalue);
                        //TODO call edit record method

                        reply = "Record has been edited";
                    }
                    break;

                    case "RECORD_COUNT":
                        //Get record count
                        //String Message = "grc" + "-" + mid ;
                        info = message.substring(4);
                        parts = info.split("-");

                    {
                        String mid = parts[0];
                        replica.getRecordCount(mid);
                        //TODO call getRecordCount method

                        reply = "multicast reply goes here";
                    }

                    case "TRANSFER_RECORD":
                        //transfer record
                        //String Message = "trr" + "-" + mid + "-" + id + "-" + remoteClinicServerName ;
                        info = message.substring(4);
                        parts = info.split("-");
                    {
                        String mid = parts[0];
                        String id = parts[1];
                        String remoteClinicServerName = parts[2];
                        String lct = mid.substring(0, 3);
                        replica.transferRecord(mid, id, remoteClinicServerName);
                        //TODO call transfer record method

                        reply = "multicast reply goes here";
                    }

                    break;

                    default:
                        //Not a valid call
                        reply = "This was not a valid message";
                        break;

                }

                byte[] buffer = reply.getBytes();
                DatagramPacket sendPacket;
                try {
                    sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(addressName), leaderRdeliverPort);
                    socket.send(sendPacket);
                } catch (UnknownHostException e) {
                    Logger.log("Bdeliver run() - " + e.getMessage(), "BD.txt");
                } catch (IOException e) {
                    Logger.log("Bdeliver run() - " + e.getMessage(), "BD.txt");
                }
            }
        }
    }
}
