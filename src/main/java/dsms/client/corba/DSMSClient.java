package dsms.client.corba;

import dsms.client.corba.DSMSApp.DSMS;
import dsms.client.corba.DSMSApp.DSMSHelper;
import org.omg.CORBA.*;
import java.io.*; 
import java.util.*;
import static java.lang.System.*;
import java.net.*;
import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DSMSClient
{
    private final static Logger LOGGER = Logger.getLogger(DSMSClient.class.getName());
    public static void ShowMenu(){
        System.out.println("Welcome to DSMS");
        System.out.println("Please Select an oprtion(1-3)");
        System.out.println("1. Add Doctor Record");
        System.out.println("2. Add Nurse Record");
        System.out.println("3. Get Record Count");
        System.out.println("4. Edit The Record ");
        System.out.println("5. Transfer Record ");
        System.out.println("6. Exit ");
    }
    public static void main(String[] args){

        try{
            Properties props = getProperties();
            ORB orb = ORB.init(args, props);
            String ref = null;
            org.omg.CORBA.Object obj = null;
            try{
                Scanner reader = new Scanner(new File("DSMS.ref"));
                ref = reader.nextLine();
            } catch(Exception ex){
                out.println("File error: " + ex.getMessage());
                exit(2);
            }
            obj = orb.string_to_object(ref);
            if(obj == null){
                out.println("Invalid IOR");
                exit(4);
            }
            DSMS d = null;
            try{
                d = DSMSHelper.narrow(obj);
            }catch (BAD_PARAM ex){
                out.println("Narrowing Failed");
                exit(3);
            }
            int userChoice = 0;
            Scanner Keyboard = new Scanner(System.in);
            ShowMenu();
            while(true)
            {
                Boolean valid = false;
                while(!valid)
                {
                    try
                    {
                        userChoice = Keyboard.nextInt();
                        valid = true;
                    }
                    catch(Exception e) 
                    {
                        System.out.println("Invalid Input Please Enter an Integer");
                        valid = false;
                        Keyboard.nextLine();
                    }
                }
                Scanner in = new Scanner(System.in);
                switch(userChoice)
                {
                    case 1:
                    System.out.println("Unique Manager id");
                    String mid = in.nextLine();
                    System.out.println("Unique id");
                    String id = in.nextLine();
                    System.out.println("First Name");
                    String fn = in.nextLine();
                    System.out.println("Last Name");
                    String ln = in.nextLine();
                    System.out.println("address");
                    String addr = in.nextLine();
                    System.out.println("Phone Number");
                    String phn = in.nextLine();
                    System.out.println("SPCL");
                    String spcl = in.nextLine();
                    System.out.println("Location");
                    String lct = in.nextLine();
                    

                    d.createDrecord(mid,id,fn,ln,addr,phn,spcl,lct);

                    System.out.println("Doctor data is stored.");
                    ShowMenu();
                    break;

                    case 2:
                    System.out.println("Unique Manager id");
                    String mid1 = in.nextLine();
                    System.out.println("Unique id");
                    String id1 = in.nextLine();
                    System.out.println("First name");
                    String fn1 = in.nextLine();
                    System.out.println("Last name");
                    String ln1 = in.nextLine();
                    System.out.println("Designation");
                    String address = in.nextLine();
                    System.out.println("Status");
                    String phn1 = in.nextLine();
                    System.out.println("StatusDate");
                    String spl = in.nextLine();
                    
                    d.createNrecord(mid1,id1,fn1,ln1,address,phn1,spl);
                    System.out.println("Nurse data is stored");
                    ShowMenu();
                    break;

                    case 3:
                    System.out.println("Please Enter Your Unique Manager Id");
                    String MGRId4 = in.nextLine();
                    d.getRecordCount(MGRId4);
                    
                    ShowMenu();
                    break;

                    case 4:
                    System.out.println("Please Enter Your Unique Manager Id");
                    String MGRId2 = in.nextLine();
                    System.out.println("Please Enter the id of the Doctor/Nurse you want to edit.");
                    String id2 = in.nextLine();
                    System.out.println("Please enter the Field name you want to update");
                    String fieldName2 = in.nextLine();
                    System.out.println("Please enter the new value for the specific field");
                    String newValue2 = in.nextLine();
                    
                    d.EditData(MGRId2,id2,fieldName2,newValue2);
                    System.out.println("Data altered");
                    ShowMenu();
                    break;

                    case 5:
                    System.out.println("Please Enter Your Unique Manager Id");
                    String MGRId3 = in.nextLine();
                    System.out.println("Please Enter the id of the Doctor/Nurse you want to Transfer.");
                    String id3 = in.nextLine();
                    System.out.println("Please enter the Remote Server to which you want to tranfer the data");
                    String fieldName3 = in.nextLine();
                    
                    d.transferRecord (MGRId3,id3,fieldName3);
                    System.out.println("Data transfered");
                    ShowMenu();
                    break;

                    case 6:
                    System.out.println("Have a nice day!!!");
                    in.close();
                    Keyboard.close();
                    System.exit(0);
                    
                }
            }
        }catch (Exception ex){
            out.println("Exception: " + ex.getMessage());
            exit(1);
        }
    }
}