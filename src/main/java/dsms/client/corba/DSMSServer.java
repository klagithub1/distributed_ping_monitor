package dsms.client.corba;

import dsms.client.corba.DSMSApp.DSMS;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import dsms.server.replica.Replica;
import dsms.server.udp.*;
import dsms.server.replica.Replica;
import dsms.server.udp.*;

import static java.lang.System.*;

public class DSMSServer {

    public static int newLeaderPort = 3333;
    public static Thread newLeaderThread;

    public static void main(String[] args) {

//        Replica r1 = new Replica(1);
//        Replica r2 = new Replica(2);
//        Replica r3 = new Replica(3);


        try {
            Properties props = getProperties();
            ORB orb = ORB.init(args, props);
            org.omg.CORBA.Object obj = null;
            POA rootPOA = null;
            try {
                obj = orb.resolve_initial_references("RootPOA");
                rootPOA = POAHelper.narrow(obj);
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            }
            DSMSImpl d_impl = new DSMSImpl();
            DSMS d = d_impl._this(orb);
            try {
                FileOutputStream file = new FileOutputStream("DSMS.ref");
                PrintWriter writer = new PrintWriter(file);
                String ref = orb.object_to_string(d);
                writer.println(ref);
                writer.flush();
                file.close();
                out.println("Server Started." + "Stop: Ctrl-C");
            } catch (IOException ex) {
                out.println("File error: " + ex.getMessage());
                exit(2);
            }
            rootPOA.the_POAManager().activate();
            orb.run();
        } catch (Exception ex) {
            out.println("Exception: " + ex.getMessage());
            exit(1);
        }

    }
}