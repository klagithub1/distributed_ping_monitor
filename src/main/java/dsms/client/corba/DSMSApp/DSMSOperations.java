package dsms.client.corba.DSMSApp;


/**
* dsms/client/corba/DSMSApp/DSMSOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from dsms/client/corba/DSMS.idl
* Tuesday, August 9, 2016 2:17:05 AM EDT
*/

public interface DSMSOperations 
{
  void createDrecord (String MID, String id, String FirstName, String LastName, String PhoneNumber, String Address, String Specialization, String Location);
  void createNrecord (String MID, String id, String firstName, String lastName, String designation, String status, String statusDate);
  void EditData (String MID, String id, String fieldName, String newValue);
  void transferRecord (String managerID, String recordID, String remoteClinicServerName);
  void getRecordCount (String managerID);
} // interface DSMSOperations