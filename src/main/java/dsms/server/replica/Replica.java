package dsms.server.replica;

import dsms.logger.Logger;
import dsms.server.fd.PingEmitterThread;
import dsms.server.fd.PingListenerThread;
import dsms.server.multicast.Bdeliver;
import dsms.server.multicast.Rdeliver;
import dsms.server.multicast.Rmulticast;
import dsms.server.udp.UDPClient;
import dsms.server.udp.UDPListener;

import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.*;
import java.net.*;


public class Replica {

	// -------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------


	// Ping Monitor and Failure Detection

	public PingEmitterThread heartbeatEmitterThread;
	public PingListenerThread heartbeatListenerThread;
	private String replicaStringID = "";
	private int replicaPingListenerPort = 0;
	private String replicaIpAddress = "";

	// Logger

	private final String logFileName;


	// Election

	private final static int NUM_OF_REPLICAS = 3;
	private int replicaID; //Defined by ctor, must be between 1-3
	private static int leader = 3;
	private static int[] priorityArray; //higher index = higher priority to be leader
	private static int[] statusArray; //status of each replica. 1->alive, 0->dead
	public int[] getStatusArray() {
		return statusArray;
	}
	private static int[] electionMsgPortArray; //Port numbers for election msg thread
	private static int[] leaderMsgPortArray; //Port numbers for leader msg thread
	private static int[] frontEndListenerPortArray;
	private final int electionMsgListenerPort;
	private final int leaderMsgListenerPort;
	private final int frontEndListenerPort;
	public int getFrontEndListenerPort() {
		return frontEndListenerPort;
	}
	private Thread electionMsgListener;
	private Thread leaderMsgListener;
	private Thread frontEndListener;

	//HashMaps
	public static HashMap<Character, LinkedList<ArrayList>> DatabaseMTL;
	public static HashMap<Character, LinkedList<ArrayList>> DatabaseLVL;
	public static HashMap<Character, LinkedList<ArrayList>> DatabaseDDO;
	Character montreal = 'M';
	Character laval = 'L';
	Character ddo = 'D';

	// Multicast listener
	private Bdeliver bdeliver;
	// Multicast
	private Rmulticast rmulticast;
	public Rmulticast getRmulticast() {
		return rmulticast;
	}

	// Other
	public static final long instanceID = 42L;


	// -------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------


	public Replica(int id) {

		replicaID = id;
		priorityArray = new int[NUM_OF_REPLICAS];
		statusArray = new int[NUM_OF_REPLICAS];
		electionMsgPortArray = new int[]{2020, 2021, 2022};
		leaderMsgPortArray = new int[]{8080, 8081, 8082};
		frontEndListenerPortArray = new int[]{7070,7071,7072};


		//Determining priority ID and enabling each replica
		for (int i = 0; i < NUM_OF_REPLICAS; i++) {
			priorityArray[i] = i;
			statusArray[i] = 1;
		}

		//Setting udp listener ports and log file name based on ID.
		electionMsgListenerPort = electionMsgPortArray[replicaID - 1];
		leaderMsgListenerPort = leaderMsgPortArray[replicaID - 1];
		frontEndListenerPort = frontEndListenerPortArray[replicaID-1];
		logFileName = "Replica" + replicaID + "Log.txt";
		electionMsgListener = new Thread(new UDPListener(electionMsgListenerPort, this)); //Set this to replicaStatusPort
		leaderMsgListener = new Thread(new UDPListener(leaderMsgListenerPort, this));
		frontEndListener = new Thread(new UDPListener(frontEndListenerPort, this));
		electionMsgListener.start();
		leaderMsgListener.start();
		bdeliver = new Bdeliver(replicaID, leader, statusArray, this);

		//HashMaps
		DatabaseMTL = new HashMap<Character, LinkedList<ArrayList>>();
		DatabaseLVL = new HashMap<Character, LinkedList<ArrayList>>();
		DatabaseDDO = new HashMap<Character, LinkedList<ArrayList>>();

		//If you are the leader initially, start a thread to listen to FE requests
		if(leader == replicaID) {
			rmulticast = new Rmulticast(replicaID);
			(new Thread(new Rdeliver(replicaID, this))).start();
			frontEndListener.start();
		}
	}


	public String getReplicaStringID() {
		return replicaStringID;
	}

	public PingEmitterThread getHeartbeatEmitterThread() {
		return heartbeatEmitterThread;
	}

	public void setHeartbeatEmitterThread(PingEmitterThread heartbeatEmitterThread) {
		this.heartbeatEmitterThread = heartbeatEmitterThread;
	}

	public PingListenerThread getHeartbeatListenerThread() {
		return heartbeatListenerThread;
	}

	public void setHeartbeatListenerThread(PingListenerThread heartbeatListenerThread) {
		this.heartbeatListenerThread = heartbeatListenerThread;
	}

	public void setReplicaStringID(String replicaStringID) {
		this.replicaStringID = replicaStringID;
	}

	public String getReplicaIpAddress() {
		return replicaIpAddress;
	}

	public void setReplicaIpAddress(String replicaIpAddress) {
		this.replicaIpAddress = replicaIpAddress;
	}

	public int getReplicaPingListenerPort() {
		return replicaPingListenerPort;
	}

	public void setReplicaPingListenerPort(int replicaPingListenerPort) {
		this.replicaPingListenerPort = replicaPingListenerPort;
	}

	// -------------------------------------------------------------------
	// Methods for modifying the Hash Map
	// -------------------------------------------------------------------


	public String createDrecord(String MID,String UID,String FirstName,String LastName,String Address,String Phone, String Specialization,String Location)
	{
		System.out.println("Replica "+replicaID + " has created a dRecord");
		char managerId = MID.charAt(0);
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

		Logger.log("Doctor record has been created in "+ managerId,"Replica1.txt");
		Logger.log("Doctor record has been created in "+ managerId,"Replica2.txt");
		Logger.log("Doctor record has been created in "+ managerId,"Replica3.txt");

		//--------------DO THIS -------------------
		HashMap<Character, LinkedList<ArrayList>> hmPointer;
		if(managerId == ddo)
			hmPointer = DatabaseDDO;
		else if(managerId == montreal)
			hmPointer = DatabaseMTL;
		else
			hmPointer = DatabaseLVL;

		if(hmPointer.get(a.charAt(0)) == null)
		{
			LinkedList list = new LinkedList();
			list.add(Doctor);
			synchronized(this)
			{
				hmPointer.put(a.charAt(0), list);
			}
			//System.out.println(hmPointer);
		}
		else
		{
			ArrayList check = new ArrayList();
			check = hmPointer.get(a.charAt(0)).get(0);
			String checkln = check.get(2).toString();
			String checkfn = check.get(1).toString();
			if(checkln.equals(a) && checkfn.equals(b))
			{
				System.out.println(hmPointer);
			}
			else
			{
				System.out.println(hmPointer.get(a.charAt(0)).get(0));
				synchronized(this)
				{
					hmPointer.get(a.charAt(0)).add(Doctor);
				}
				System.out.println(hmPointer);
			}
		}
		return "end of the method";
	}

	public String createNrecord(String MID,String UID,String firstName, String lastName, String designation, String status, String statusDate)
	{
		ArrayList<String> Nurse = new ArrayList<String>();
		Nurse.add("NR"+UID);
		Nurse.add(firstName);
		Nurse.add(lastName);
		Nurse.add(designation);
		Nurse.add(status);
		Nurse.add(statusDate);
		String a = Nurse.get(2).toString();
		char managerId = MID.charAt(0);

		Logger.log("Nurse record has been created in "+ managerId,logFileName);

		//--------------DO THIS -------------------
		HashMap<Character, LinkedList<ArrayList>> hmPointer;
		if(managerId == ddo)
			hmPointer = DatabaseDDO;
		else if(managerId == montreal)
			hmPointer = DatabaseMTL;
		else
			hmPointer = DatabaseLVL;


		if(hmPointer.get(a.charAt(0)) == null)
		{
			LinkedList<ArrayList> list = new LinkedList<ArrayList>();
			list.add(Nurse);
			synchronized(this)
			{
				hmPointer.put(a.charAt(0), list);
			}
			System.out.println(hmPointer);
		}
		else {
			ArrayList check = new ArrayList();
			check = hmPointer.get(a.charAt(0)).get(0);
			Object checkcharobj = check.get(2);
			String checkchar = checkcharobj.toString();
			if (checkchar.equals(a)) {
				System.out.println(hmPointer);
			} else {
				synchronized (this) {
					hmPointer.get(a.charAt(0)).add(Nurse);
				}
				System.out.println(hmPointer);

			}
		}
		return "End of the method";
	}
	
	public String getRecordCount(String managerID)
	{
		String output = "Montreal : " + DatabaseMTL.size() + "Laval : " + DatabaseLVL.size() + "DDO : " + DatabaseDDO.size();
		Logger.log(output,logFileName);

		String reply = output;
		return output;

	}
	
	public String transferRecord (String managerID, String recordID,String remoteClinicServerName)
	{
		char managerId = managerID.charAt(0);
		Logger.log("Record with ID " + recordID + " was transferred",logFileName);
		if(managerId == ddo)
		{
			for(char alphabet = 'A'; alphabet <= 'Z';alphabet++)
			{
				if(DatabaseDDO.get(alphabet) != null)
				{
					for(int i = 0; i < DatabaseDDO.get(alphabet).size(); i++)
					{
						Character checkid = recordID.charAt(0);
						ArrayList list1 = new ArrayList();
						list1 = DatabaseDDO.get(alphabet).get(i);
						String ab = list1.get(0).toString();
						if(ab.equals(recordID))
						{
							char RemoteClinicServerName = remoteClinicServerName.charAt(0);
							if(RemoteClinicServerName == montreal)
							{
								String LastName = list1.get(2).toString();
								Character LastNameChar = LastName.charAt(0);
								if(DatabaseMTL.get(LastName.charAt(0)) == null)
								{
									LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									list.add(list1);
									synchronized(this)
									{
										DatabaseMTL.put(LastName.charAt(0), list);
									}
									DatabaseDDO.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
								else
								{
									DatabaseMTL.get(LastName.charAt(0)).add(list1);
									DatabaseDDO.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
							}
							else if(RemoteClinicServerName == laval)
							{
								String LastName = list1.get(2).toString();
								Character LastNameChar = LastName.charAt(0);
								if(DatabaseLVL.get(LastName.charAt(0)) == null)
								{
									LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									list.add(list1);
									synchronized(this)
									{
										DatabaseLVL.put(LastName.charAt(0), list);
									}
									DatabaseDDO.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
								else
								{
									synchronized(this)
									{
										DatabaseLVL.get(LastName.charAt(0)).add(list1);
									}
									DatabaseDDO.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
							}
						}
					}
				}
			}
		}else if(managerId == montreal)
		{
			for(char alphabet = 'A'; alphabet <= 'Z';alphabet++)
			{
				if(DatabaseMTL.get(alphabet) != null)
				{
					for(int i = 0; i < DatabaseMTL.get(alphabet).size(); i++)
					{
						Character checkid = recordID.charAt(0);
						ArrayList list1 = new ArrayList();
						list1 = DatabaseMTL.get(alphabet).get(i);
						String ab = list1.get(0).toString();
						if(ab.equals(recordID))
						{
							char RemoteClinicServerName = remoteClinicServerName.charAt(0);
							if(RemoteClinicServerName == ddo)
							{
								String LastName = list1.get(2).toString();
								Character LastNameChar = LastName.charAt(0);
								if(DatabaseDDO.get(LastName.charAt(0)) == null)
								{
									LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									list.add(list1);
									synchronized(this)
									{
										DatabaseDDO.put(LastName.charAt(0), list);
									}
									DatabaseMTL.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
								else
								{
									DatabaseDDO.get(LastName.charAt(0)).add(list1);
									DatabaseMTL.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
							}
							else if(RemoteClinicServerName == laval)
							{
								String LastName = list1.get(2).toString();
								Character LastNameChar = LastName.charAt(0);
								if(DatabaseLVL.get(LastName.charAt(0)) == null)
								{
									LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									list.add(list1);
									synchronized(this)
									{
										DatabaseLVL.put(LastName.charAt(0), list);
									}
									DatabaseMTL.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
								else
								{
									synchronized(this)
									{
										DatabaseLVL.get(LastName.charAt(0)).add(list1);
									}
									DatabaseMTL.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
							}
						}
					}
				}
			}
		}else if(managerId == laval) {
			for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
				if (DatabaseLVL.get(alphabet) != null) {
					for (int i = 0; i < DatabaseLVL.get(alphabet).size(); i++) {
						Character checkid = recordID.charAt(0);
						ArrayList list1 = new ArrayList();
						list1 = DatabaseLVL.get(alphabet).get(i);
						String ab = list1.get(0).toString();
						if (ab.equals(recordID)) {
							char RemoteClinicServerName = remoteClinicServerName.charAt(0);
							if (RemoteClinicServerName == ddo) {
								String LastName = list1.get(2).toString();
								Character LastNameChar = LastName.charAt(0);
								if (DatabaseDDO.get(LastName.charAt(0)) == null) {
									LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									list.add(list1);
									synchronized (this) {
										DatabaseDDO.put(LastName.charAt(0), list);
									}
									DatabaseLVL.get(alphabet).remove();
									String reply = "Data transfered";
									return reply;
								} else {
									DatabaseDDO.get(LastName.charAt(0)).add(list1);
									DatabaseLVL.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
							} else if (RemoteClinicServerName == montreal) {
								String LastName = list1.get(2).toString();
								Character LastNameChar = LastName.charAt(0);
								if (DatabaseMTL.get(LastName.charAt(0)) == null) {
									LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									list.add(list1);
									synchronized (this) {
										DatabaseMTL.put(LastName.charAt(0), list);
									}
									DatabaseLVL.get(alphabet).remove();
									System.out.println("Data Transferred");
								} else {
									synchronized (this) {
										DatabaseMTL.get(LastName.charAt(0)).add(list1);
									}
									DatabaseLVL.get(alphabet).remove();
									System.out.println("Data Transferred");
								}
							}
						}
					}
				}
			}
		}
		return "end of method";
	}
	public String EditData(String MID,String UID, String fieldName, String newValue)
	{
	char managerId = MID.charAt(0);
		Logger.log("Record " + UID + " was edited",logFileName);
		if(managerId == ddo)
		{
			for(char alphabet = 'A'; alphabet <= 'Z';alphabet++)
			{
				if(DatabaseDDO.get(alphabet) != null)
				{
					for(int i = 0; i < DatabaseDDO.get(alphabet).size(); i++)
					{
						Character checkid = UID.charAt(0);
						ArrayList list1 = new ArrayList();
						list1 = DatabaseDDO.get(alphabet).get(i);
						Object ob = list1.get(0);
						String ab = ob.toString();
						if(ab.equals(UID))
						{
							if(checkid == 'D'){
								if(fieldName.equals("First Name"))
								{
									synchronized(this)
									{
										list1.set(1, newValue);
									}
									System.out.println("Data Altered");
								}
								if(fieldName.equals("Last Name"))
								{
									synchronized(this)
									{
										list1.set(2, newValue);
									}
									if(DatabaseDDO.get(newValue.charAt(0)) == null)
									{
										LinkedList<ArrayList> list = new LinkedList<ArrayList>();
										synchronized(this)
										{
											list.add(list1);
										}
										DatabaseDDO.get(alphabet).remove();
										synchronized(this)
										{
											DatabaseDDO.put(newValue.charAt(0), list);
										}
									}
									else
									{
										synchronized(this)
										{
											DatabaseDDO.get(newValue.charAt(0)).add(list1);
										}
										System.out.println("Data Altered");
									}
								}
								if(fieldName.equals("Address"))
								{
									synchronized(this)
									{
										list1.set(3, newValue);
									}
									String reply = "Data Altered";
									return reply;
								}
								if(fieldName.equals("Phone"))
								{
									synchronized(this)
									{
										list1.set(4, newValue);
									}
									System.out.println("Data Altered");
								}
								if(fieldName.equals("Specialization"))
								{
									synchronized(this)
									{
										list1.set(5, newValue);
									}
									System.out.println("Data Altered");
								}
								if(fieldName.equals("Location"))
								{
									synchronized(this)
									{
										list1.set(6, newValue);
									}
									System.out.println("Data Altered");
								}
							}
							else if(checkid == 'N')
							{
								if(fieldName.equals("First Name"))
								{
									synchronized(this)
									{
										list1.set(1, newValue);
									}
									System.out.println("Data Altered");
								}
								if(fieldName.equals("Last Name"))
								{
									list1.set(2, newValue);
									if(DatabaseDDO.get(newValue.charAt(0)) == null)
									{
										LinkedList<ArrayList> list = new LinkedList<ArrayList>();
										synchronized(this)
										{
											list.add(list1);
										}
										DatabaseDDO.get(alphabet).remove();
										DatabaseDDO.put(newValue.charAt(0), list);
									}
									else
									{
										synchronized(this)
										{
											DatabaseDDO.get(newValue.charAt(0)).add(list1);
										}
										System.out.println("Data Altered");
									}
								}
								if(fieldName.equals("Designation"))
								{
									synchronized(this)
									{
										list1.set(3, newValue);
									}
									System.out.println("Data Altered");
								}
								if(fieldName.equals("Status"))
								{
									synchronized(this)
									{
										list1.set(4, newValue);
									}
									System.out.println("Data Altered");
								}
								if(fieldName.equals("StatusDate"))
								{
									synchronized(this)
									{
										list1.set(5, newValue);
									}
									System.out.println("Data Altered");
								}
							}
						}
					}
				}
			}
		}else if(managerId == montreal)
		{
			for(char alphabet = 'A'; alphabet <= 'Z';alphabet++)
			{
			 if(DatabaseMTL.get(alphabet) != null){
				 System.out.println("Hello");
				 for(int i = 0; i < DatabaseMTL.get(alphabet).size(); i++)
				 {
					 Character checkid = UID.charAt(0);
					 ArrayList<String> list1 = new ArrayList<String>();
					 list1 = DatabaseMTL.get(alphabet).get(i);
					 Object ob = list1.get(0);
					 String ab = ob.toString();
					 if(ab.equals(UID))
					 {

						 if(checkid == 'D'){
							 if(fieldName.equals("First Name"))
							 {
								 synchronized(this)
								 {
								 	list1.set(1, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Last Name"))
							 {

								 list1.set(2, newValue);
								 if(DatabaseMTL.get(newValue.charAt(0)) == null)
								 {
									 LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									 list.add(list1);
									 DatabaseMTL.get(alphabet).remove();
									 synchronized(this)
									 {
									 	DatabaseMTL.put(newValue.charAt(0), list);
									 }
								 }
								 else
								 {
									 synchronized(this)
									 {
									 	DatabaseMTL.get(newValue.charAt(0)).add(list1);
									 }
									 System.out.println("Data Altered");
								 }
							 }
							 if(fieldName.equals("Address"))
							 {
								 synchronized(this)
								 {
								 	list1.set(3, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Phone"))
							 {
								 synchronized(this)
								 {
								 	list1.set(4, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Specialization"))
							 {
								 synchronized(this)
								 {
								 	list1.set(5, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Location"))
							 {
								 synchronized(this)
								 {
								 	list1.set(6, newValue);
								 }
								 System.out.println("Data Altered");
							 }
						 }else if(checkid == 'N')
						 {
							 if(fieldName.equals("First Name"))
							 {
								 synchronized(this)
								 {
								 	list1.set(1, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Last Name"))
							 {
								 list1.set(2, newValue);
								 if(DatabaseMTL.get(newValue.charAt(0)) == null)
								 {
									 LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									 list.add(list1);
									 DatabaseMTL.get(alphabet).remove();
									 synchronized(this)
									 {
									 	DatabaseMTL.put(newValue.charAt(0), list);
									 }
									 System.out.println("Data Altered");
								 }
								 else
								 {
									 System.out.println(DatabaseMTL.get(newValue.charAt(0)).get(0));
									 synchronized(this)
									 {
									 	DatabaseMTL.get(newValue.charAt(0)).add(list1);
									 }
									 System.out.println("Data Altered");
								 }
							 }
							 if(fieldName.equals("Designation"))
							 {
								 synchronized(this)
								 {
								 	list1.set(3, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Status"))
							 {
								 synchronized(this)
								 {
								 	list1.set(4, newValue);
								 }
								 String reply = "Data Altered";
									return reply;
							 }
							 if(fieldName.equals("StatusDate"))
							 {
								 synchronized(this)
								 {
								 	list1.set(5, newValue);
								 }
								 System.out.println("Data Altered");
							 }
						 }
					 }

				 }
			 }
			}
		}else if(managerId == laval)
		{
			for(char alphabet = 'A'; alphabet <= 'Z';alphabet++)
			{
			 if(DatabaseLVL.get(alphabet) != null){
				 for(int i = 0; i < DatabaseLVL.get(alphabet).size(); i++)
				 {
					 Character checkid = UID.charAt(0);
					 ArrayList list1 = new ArrayList();
					 list1 = DatabaseLVL.get(alphabet).get(i);
					 Object ob = list1.get(0);
					 String ab = ob.toString();
					 if(ab.equals(UID))
					 {
						 if(checkid == 'D'){
							 if(fieldName.equals("First Name"))
							 {
								 synchronized(this)
								 {
								 	list1.set(1, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Last Name"))
							 {
								 list1.set(2, newValue);
								 if(DatabaseLVL.get(newValue.charAt(0)) == null)
								 {
									 LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									 list.add(list1);
									 synchronized(this)
									 {
									 	DatabaseLVL.get(alphabet).remove();
									 }
									 synchronized(this)
									 {
									 	DatabaseLVL.put(newValue.charAt(0), list);
									 }
								 }
								 else
								 {
									 synchronized(this)
									 {
									 	DatabaseLVL.get(newValue.charAt(0)).add(list1);
									 }
									 System.out.println("Data Altered");
								 }
							 }
							 if(fieldName.equals("Address"))
							 {
								 synchronized(this)
								 {
								 	list1.set(3, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Phone"))
							 {
								 synchronized(this)
								 {
								 	list1.set(4, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Specialization"))
							 {
								 synchronized(this)
								 {
								 	list1.set(5, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Location"))
							 {
								 synchronized(this)
								 {
								 	list1.set(6, newValue);
								 }
								 System.out.println("Data Altered");
							 }
						 }else if(checkid == 'N')
						 {
							 if(fieldName.equals("First Name"))
							 {
								 synchronized(this)
								 {
								 	list1.set(1, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Last Name"))
							 {
								 list1.set(2, newValue);
								 if(DatabaseLVL.get(newValue.charAt(0)) == null)
								 {
									 LinkedList<ArrayList> list = new LinkedList<ArrayList>();
									 list.add(list1);
									 synchronized(this)
									 {
									 	DatabaseLVL.get(alphabet).remove();
									 }
									 synchronized(this)
									 {
									 	DatabaseLVL.put(newValue.charAt(0), list);
									 }
								 }
								 else
								 {
									 synchronized(this)
									 {
									 	DatabaseLVL.get(newValue.charAt(0)).add(list1);
									 }
									 System.out.println("Data Altered");
								 }
							 }
							 if(fieldName.equals("Designation"))
							 {
								 synchronized(this)
								 {
								 	list1.set(3, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("Status"))
							 {
								 synchronized(this)
								 {
								 	list1.set(4, newValue);
								 }
								 System.out.println("Data Altered");
							 }
							 if(fieldName.equals("StatusDate"))
							 {
								 synchronized(this)
								 {
								 	list1.set(5, newValue);
								 }
								 System.out.println("Data Altered");
							 }
						 }

					 }

				 }
			 }
			}
		}

		return "end of method";
	}


	//Sends an election message to every replica with a higher priority
	@SuppressWarnings("unchecked")
	public void sendElectionMessage() throws InterruptedException, ExecutionException {

		if (replicaID == NUM_OF_REPLICAS) {
			sendLeaderMessage(replicaID);
			return;
		}

		UDPClient c1 = new UDPClient();
		int processInArray = replicaID - 1;
		String answer = "";

		//Thread.sleep(10000); //Sleep 10 seconds to allow time for a replica to be killed

		//Send pre-election message to all higher priority processes to see if others are alive
		for (int i = 0; i < NUM_OF_REPLICAS; ++i) {
			if (processInArray < priorityArray[i] && statusArray[i] == 1) {
				answer += c1.sendMessage("pre", electionMsgPortArray[i]);
				String input = "Election message has been sent to replica " + (i + 1);
				System.out.println(input);
				Logger.log(input, logFileName);
			}
		}
		//If no answers were received, you are the leader
		if (answer.isEmpty()) {
			sendLeaderMessage(replicaID);
		} else {
			//Tell other processes to send election messages
			for (int i = 0; i < NUM_OF_REPLICAS; ++i) {
				if (processInArray < priorityArray[i] && statusArray[i] == 1) {
					c1.sendMessage("ele", electionMsgPortArray[i]);
				}
			}
		}
	}

	//Sends a leader message to every replica
	@SuppressWarnings("unchecked")
	public void sendLeaderMessage(final int leader) throws InterruptedException, ExecutionException {
		setLeader(leader);
		UDPClient c1 = new UDPClient();
		c1.sendMessage("nld"+leader,3333); //Notify Front End of new leader
		for (int i = 0; i < NUM_OF_REPLICAS && i != replicaID - 1; i++) {
			c1.sendMessage("ldr" + leader, leaderMsgPortArray[i]);
		}
		String input = "Replica " + leader + " has been elected as leader.";
		System.out.println(input);
		Logger.log(input, logFileName);
	}


	public void beginElection() throws InterruptedException, ExecutionException {
		String input = "Replica " + replicaID + " has begun an election.";
		Logger.log(input, logFileName);
		sendElectionMessage();
	}

	public int getLeader() {
		return this.leader;
	}


	public void setLeader(int leader) {
		//If you are the newly elected leader, start Thread to listen to FE requests
		if(leader == replicaID && this.leader!=replicaID) {
			frontEndListener.start();
			(new Thread(new Rdeliver(replicaID, this))).start();
		}
		this.leader = leader;
		bdeliver = new Bdeliver(replicaID, leader, statusArray, this);

		//Send UDP message to FE notifying it of change

	}

	//status = 1 -> Replica is alive, status = 0 -> Replica is dead
	public void changeReplicaStatus(int replicaID, int status) {
		statusArray[replicaID - 1] = status;
	}

}
