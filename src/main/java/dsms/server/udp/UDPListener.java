package dsms.server.udp;

import dsms.client.corba.DSMSImpl;
import dsms.client.corba.UDP.FrontEnd;
import dsms.logger.Logger;
import dsms.server.multicast.Message;
import dsms.server.multicast.MessageAction;
import dsms.server.multicast.Rmulticast;
import dsms.server.replica.Replica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;


public class UDPListener implements Runnable {

    int portNumber;
    Replica replica;
    FrontEnd fe;

    public UDPListener(int portNumber, Replica r) {

        this.portNumber = portNumber;
        this.replica = r;
    }

    public UDPListener(int portNumber, FrontEnd fe){
        this.portNumber = portNumber;
        this.fe = fe;
    }


    public void run() {
        //System.out.println("UDP server up");

        DatagramSocket aSocket = null;
        final int SERVER_PORT = portNumber;

        try {
            aSocket = new DatagramSocket(SERVER_PORT);
            byte[] buffer = new byte[1000];
            String replyString; //What will be replied to UDP client
            //char rType; //record type

            while (true) {

                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String message = new String(request.getData(), request.getOffset(), request.getLength());
                //System.out.println("udplistener message is:"+message);
                replyString = processMessage(message);
                byte[] m = replyString.getBytes();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
//                String path = "ServerLog" + ".txt";
//                Logger.log(replyString, path);
                aSocket.send(reply);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (aSocket != null) aSocket.close();
        }

    }

    public String processMessage(String message) throws InterruptedException, ExecutionException {
        String reply = "replied";
        String messageType = message.substring(0, 3);
        String info; //info needed to create or edit record
        String[] parts; //array to store this info

        switch(messageType){

            case "pre":
                //Reply to election message, notifying that replica is still alive
                reply = "Election message has been sent";
                break;
            case "ele":
                //Send out your own election message
                replica.sendElectionMessage();
                reply = "Replica sent out election message";
                break;
            case "ldr":
                //Set current leader
                int leader = Character.getNumericValue(message.charAt(3));
                replica.setLeader(leader);
                reply = "leader has been set to:" + leader;
                break;
            case "beg":
                //Begin an election
                replica.beginElection();
                reply = "Election has begun";
                break;
            case "crd":
                //create d record
                //String Message = mid + "-" + id + "-" + fn + "-" + ln + "-" + addr + "-" + phn + "-" + spcl + "-" + lct;
                info = message.substring(4);
                System.out.println("info is:"+info);
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
               //replica.getRmulticast().send(replica.getStatusArray(), new Message(MessageAction.CREATE_DOCTOR, info));
               replica.createDrecord(mid,id,fn,ln,addr,phn,specl,lct);
                reply = "Doctor record has been created with ID" + id;
            }

            break;

            case "crn":
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
                String lct = mid.substring(0,3);
                //TODO create the actual record
                replica.getRmulticast().send(replica.getStatusArray(), new Message(MessageAction.CREATE_NURSE, info));
//                replica.createNrecord(mid,id,fn,ln,designation,status,statusDate);
                reply = "Nurse record has been created with ID" + id;
            }
            break;

            case "edr":
                //edit record
                //String Message = "edr" + "-" + mid + "-" + id + "-" + fieldname + "-" + newvalue ;
                info = message.substring(4);
                parts = info.split("-");

            {
                String mid = parts[0];
                String id = parts[1];
                String fieldname = parts[2];
                String newvalue = parts[3];
                String lct = mid.substring(0,3);
                //replica.EditData(mid,id,fieldname,newvalue);
                //TODO call edit record method
                replica.getRmulticast().send(replica.getStatusArray(), new Message(MessageAction.EDIT_RECORD, info));
                reply = "Record has been edited";
            }
            break;

            case "grc":
                //Get record count
                //String Message = "grc" + "-" + mid ;
                info = message.substring(4);
                parts = info.split("-");

            {
                String mid = parts[0];
                //replica.getRecordCount(mid);
                //TODO call getRecordCount method
                replica.getRmulticast().send(replica.getStatusArray(), new Message(MessageAction.RECORD_COUNT, info));
                reply = "multicast reply goes here";
            }

            case "tfr":
                //transfer record
                //String Message = "trr" + "-" + mid + "-" + id + "-" + remoteClinicServerName ;
                info = message.substring(4);
                parts = info.split("-");
            {
                String mid = parts[0];
                String id = parts[1];
                String remoteClinicServerName = parts[2];
                String lct = mid.substring(0,3);
                //replica.transferRecord(mid,id,remoteClinicServerName);
                //TODO call transfer record method
                replica.getRmulticast().send(replica.getStatusArray(), new Message(MessageAction.TRANSFER_RECORD, info));
                reply = "multicast reply goes here";
            }

            break;

            case "nld":
                //notify FE of new leader
                int newLeader = Character.getNumericValue(message.charAt(3));
                fe.changeLeader(newLeader);
                break;

            default:
                //Not a valid call
                reply = "This was not a valid message";
                break;

        }

        return reply;
    }
}
