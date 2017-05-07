package dsms.server.udp;

import dsms.server.replica.Replica;

import java.util.concurrent.ExecutionException;

public class driverTest {

    /**
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Replica r1 = new Replica(1);
        Replica r2 = new Replica(2);
        Replica r3 = new Replica(3);

        r1.beginElection();
        //System.out.println("Leader is:"+r1.getLeader());
    }
}
