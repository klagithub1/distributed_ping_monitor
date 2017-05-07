package dsms.server.multicast;

import dsms.logger.Logger;



/**
 * Created by rsmith on 2016-08-09.
 */
public class Rmulticast {

    private int leaderId;
    public int sequenceNumber;

    public Rmulticast(int leaderId) {
        this.leaderId = leaderId;
        this.sequenceNumber = 0;
        Logger.log(String.format("Rmulticast ctor() - leaderID: %d, sequencenumber: %d\n", leaderId, sequenceNumber), "RM.txt");
    }

    public void send(int[] group, Message message) {
        String piggybackMessage = leaderId + "|" + sequenceNumber + "|" + message.toString();
        (new Bmulticast(leaderId)).send(group, piggybackMessage);
        sequenceNumber++;
        Logger.log(String.format("Rmulticast send() - piggybackMessage: %s, sequencenumber++: %d\n", piggybackMessage, sequenceNumber), "RM.txt");
    }
}
