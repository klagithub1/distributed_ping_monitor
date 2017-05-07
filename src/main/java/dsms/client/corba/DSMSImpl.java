package dsms.client.corba;

import dsms.client.corba.DSMSApp.DSMSPOA;
import dsms.client.corba.UDP.FrontEnd;
import dsms.client.corba.UDP.UDPClient;

public class DSMSImpl extends DSMSPOA {


//    public int leader = 3;
//    public int[] replicaPortArray = new int[]{7070,7071,7072};
//    public int destinationPort = replicaPortArray[leader-1];
    public FrontEnd fe = new FrontEnd();



    public synchronized void createDrecord(String mid,String id,String fn,String ln,String addr,String phn,String spcl,String lct)
    {
        String Message = "crd" + "-" +mid + "-" + id + "-" + fn + "-" + ln + "-" + addr + "-" + phn + "-" + spcl + "-" + lct;
        String answer = fe.sendMessage(Message);
       // UDPClient u = new UDPClient("Montreal");

        //answer += u.sendMessage(Message, destinationPort);

//        if (answer.isEmpty())
//        {
//            answer += u.sendMessage(Message, destinationPort);
//        }
//        else
//        {
//            System.out.println("Data Saved");
//        }
    }
    public synchronized void createNrecord(String mid,String id,String fn,String ln,String destination,String status,String statusdate)
    {
        String Message = "crn" + "-" + mid + "-" + id + "-" + fn + "-" + ln + "-" + destination + "-" + status + "-" + statusdate ;
        String answer = fe.sendMessage(Message);
//        UDPClient u = new UDPClient("Montreal");
//        String answer = "";
//        answer += u.sendMessage(Message, destinationPort);
//
//        if (answer.isEmpty())
//        {
//            answer += u.sendMessage(Message, destinationPort);
//        }
//        else
//        {
//            System.out.println("Data Saved");
//        }
    }
    public synchronized void EditData(String mid,String id,String fieldname,String newvalue)
    {
        String Message = "edr" + "-" + mid + "-" + id + "-" + fieldname + "-" + newvalue ;
        String answer = fe.sendMessage(Message);
//        UDPClient u = new UDPClient("Montreal");
//        String answer = "";
//        answer += u.sendMessage(Message, destinationPort);
//
//        if (answer.isEmpty())
//        {
//            answer += u.sendMessage(Message, destinationPort);
//        }
//        else
//        {
//            System.out.println("Data Saved");
//        }
    }
    public synchronized void transferRecord(String mid,String id,String remoteClinicServerName)
    {

        String Message = "tfr" + "-" + mid + "-" + id + "-" + remoteClinicServerName ;
        String answer = fe.sendMessage(Message);

//        UDPClient u = new UDPClient("Montreal");
//        String answer = "";
//        answer += u.sendMessage(Message, destinationPort);
//
//        if (answer.isEmpty())
//        {
//            answer += u.sendMessage(Message, destinationPort);
//        }
//        else
//        {
//            System.out.println("Data Saved");
//        }
    }
    public synchronized void getRecordCount(String mid)
    {
        String Message = "grc" + "-" + mid ;
        String answer = fe.sendMessage(Message);
//        UDPClient u = new UDPClient("Montreal");
//        String answer = "";
//        answer += u.sendMessage(Message, destinationPort);
//
//        if (answer.isEmpty())
//        {
//            answer += u.sendMessage(Message, destinationPort);
//        }
//        else
//        {
//            System.out.println("Data Saved");
//        }
    }
}
