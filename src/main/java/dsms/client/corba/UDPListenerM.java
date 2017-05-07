package dsms.client.corba;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;


public class UDPListenerM {
    public static void main(String args[]){
        //System.out.println("UDP server up");

        DatagramSocket aSocket = null;
        final int SERVER_PORT = 3030;

        try {
            aSocket = new DatagramSocket(SERVER_PORT);
            byte[] buffer = new byte[1000];
            while (true) {

                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String message = new String(request.getData(), request.getOffset(), request.getLength());
                //System.out.println("udplistener message is:"+message);
                UDPListenerM instance = new UDPListenerM();
                String replyString = instance.processMessage(message);
                byte[] m = replyString.getBytes();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
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
	public static HashMap<Character, LinkedList<ArrayList>> DatabaseMTL = new HashMap<Character, LinkedList<ArrayList>>();
	public static HashMap<Character, LinkedList<ArrayList>> DatabaseLVL = new HashMap<Character, LinkedList<ArrayList>>();
	public static HashMap<Character, LinkedList<ArrayList>> DatabaseDDO = new HashMap<Character, LinkedList<ArrayList>>();
	Character montreal = 'M';
	Character laval = 'L';
	Character ddo = 'D';
    public String processMessage(String message) throws InterruptedException, ExecutionException 
    {
        String string = message;
        String[] parts = string.split("-");
        String methodid = parts[0];
        String managerid = parts[1];
		
        if(methodid == "crd")
        {
            //Create doctor record
            String UID = parts[2];
    		String FirstName = parts[3];
    		String LastName = parts[4];
    		String Address = parts[5];
    		String Phone = parts[6];
    		String Specialization = parts[7];
    		String Location = parts[8];
    		//create doctor record
    		char managerId = managerid.charAt(0);
			ArrayList Doctor = new ArrayList();
			Doctor.add("DR"+UID);
			Doctor.add(FirstName);
			Doctor.add(LastName);
			Doctor.add(Address);
			Doctor.add(Phone);
			Doctor.add(Specialization);
			Doctor.add(Location);
			String a = Doctor.get(2).toString();
			String b = Doctor.get(1).toString();
			if(managerId == montreal)
			{
				if(DatabaseMTL.get(a.charAt(0)) == null)
				{
					LinkedList list = new LinkedList();
					list.add(Doctor);
					synchronized(this)
					{
						DatabaseMTL.put(a.charAt(0), list);
					}
					String reply = "Doctor Data Saved";
        			return reply;
				}
				else
				{
					ArrayList check = new ArrayList();
					check = DatabaseMTL.get(a.charAt(0)).get(0);
					String checkln = check.get(2).toString();
					String checkfn = check.get(1).toString();
					if(checkln.equals(a) && checkfn.equals(b)) 
					{
						String reply = "Doctor Data Already Saved";
        			return reply;
					}
					else
					{
						synchronized(this)
						{
							DatabaseMTL.get(a.charAt(0)).add(Doctor);
						}
						String reply = "Doctor Data Saved";
        				return reply;
					} 
				}
			} 
			else if(managerId == laval)
			{
				if(DatabaseLVL.get(a.charAt(0)) == null)
				{
					LinkedList list = new LinkedList();
					list.add(Doctor);
					synchronized(this)
					{
						DatabaseLVL.put(a.charAt(0), list);
					}
					String reply = "Doctor Data Saved";
        			return reply;
				}
				else
				{
					ArrayList check = new ArrayList();
					check = DatabaseLVL.get(a.charAt(0)).get(0);
					String checkln = check.get(2).toString();
					String checkfn = check.get(1).toString();
					if(checkln.equals(a) && checkfn.equals(b)) 
					{
						String reply = "Doctor Data Already Saved";
        			return reply;
					}
					else
					{
						synchronized(this)
						{
							DatabaseLVL.get(a.charAt(0)).add(Doctor);
						}
						String reply = "Doctor Data Saved";
        			return reply;
					} 
				}
			}
			else if(managerId == ddo)
			{
				if(DatabaseDDO.get(a.charAt(0)) == null)
				{
					LinkedList list = new LinkedList();
					list.add(Doctor);
					synchronized(this)
					{
						DatabaseDDO.put(a.charAt(0), list);
					}
					String reply = "Doctor Data Saved";
        			return reply;
				}
				else
				{
					ArrayList check = new ArrayList();
					check = DatabaseDDO.get(a.charAt(0)).get(0);
					String checkln = check.get(2).toString();
					String checkfn = check.get(1).toString();
					if(checkln.equals(a) && checkfn.equals(b)) 
					{
						String reply = "Doctor Data Already Saved";
        			return reply;
					}
					else
					{
						synchronized(this)
						{
							DatabaseDDO.get(a.charAt(0)).add(Doctor);
						}
						String reply = "Doctor Data Saved";
        			return reply;
					} 
				}
			} 
        }
        else if(methodid == "crn")
        {
            //create nurse record
			String UID = parts[2];
    		String firstName = parts[3];
    		String lastName = parts[4];
    		String designation = parts[5];
    		String status = parts[6];
    		String statusDate = parts[7]; 
    		ArrayList<String> Nurse = new ArrayList<String>();
			Nurse.add("NR"+UID);
			Nurse.add(firstName);
			Nurse.add(lastName);
			Nurse.add(designation);
			Nurse.add(status);
			Nurse.add(statusDate);
			String a = Nurse.get(2).toString();
			char managerId = managerid.charAt(0);
			if(managerId == ddo)
			{
				if(DatabaseDDO.get(a.charAt(0)) == null)
				{
					LinkedList<ArrayList> list = new LinkedList<ArrayList>();
					list.add(Nurse);
					synchronized(this)
					{
						DatabaseDDO.put(a.charAt(0), list);
					}
					String reply = "Data is saved";
        			return reply;
				}
				else
				{
					ArrayList check = new ArrayList();
					check = DatabaseDDO.get(a.charAt(0)).get(0);
					Object checkcharobj = check.get(2);
					String checkchar = checkcharobj.toString();
					if(checkchar.equals(a)) 
					{
					String reply = "Data is Already saved";
        			return reply;
					}
					else
					{
						synchronized(this)
						{
							DatabaseDDO.get(a.charAt(0)).add(Nurse);
						}
						String reply = "Data is saved";
        			return reply;

					} 
				}
			}
			else if(managerId == montreal)
			{
				if(DatabaseMTL.get(a.charAt(0)) == null)
				{
					LinkedList<ArrayList> list = new LinkedList<ArrayList>();
					list.add(Nurse);
					synchronized(this)
					{
						DatabaseMTL.put(a.charAt(0), list);
					}
					String reply = "Data is saved";
        			return reply;

				}
				else
				{
					ArrayList check = new ArrayList();
					check = DatabaseMTL.get(a.charAt(0)).get(0);
					Object checkcharobj = check.get(2);
					String checkchar = checkcharobj.toString();
					if(checkchar.equals(a)) 
					{
					String reply = "Data is Already saved";
        			return reply;
					}
					else
					{
						synchronized(this)
						{
							DatabaseMTL.get(a.charAt(0)).add(Nurse);
						}
						String reply = "Data is saved";
        			return reply;

					} 
				}
			}
			else if(managerId == laval)
			{
				if(DatabaseLVL.get(a.charAt(0)) == null)
				{
					LinkedList<ArrayList> list = new LinkedList<ArrayList>();
					list.add(Nurse);
					synchronized(this)
					{
						DatabaseLVL.put(a.charAt(0), list);
					}
					String reply = "Data is saved";
        			return reply;

				}
				else
				{
					ArrayList check = new ArrayList();
					check = DatabaseLVL.get(a.charAt(0)).get(0);
					Object checkcharobj = check.get(2);
					String checkchar = checkcharobj.toString();
					if(checkchar.equals(a)) 
					{
					String reply = "Data is Already saved";
        			return reply;

					}
					else
					{
						synchronized(this)
						{
							DatabaseMTL.get(a.charAt(0)).add(Nurse);
						}
						String reply = "Data is saved";
        			return reply;

					} 
				}
			}
        }
        else if(methodid == "grc")
        {
            //get record count
        }
        else if(methodid == "edr")
        {
            //edit record
        }
        else if(methodid == "tfr")
        {
            //transfer count
        }
        String reply = "Data Saved";
        return reply;
    }
}
