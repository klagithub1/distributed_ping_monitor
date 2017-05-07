package dsms.server.multicast;

public class Message {

    private MessageAction messageAction;

    private String data;

    public Message(MessageAction messageAction, String data) {

        this.messageAction = messageAction;
        this.data = data;
    }

    @Override
    public String toString() {
        return messageAction + "|" + data;
    }
}
