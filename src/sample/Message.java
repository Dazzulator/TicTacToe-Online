package sample;

import java.util.Map;

public class Message {

    public MessageType type;
    public Map<String, String> parameters;

    public Message() {

    }

    public Message(MessageType type, Map<String, String> parameters) {
        this.type = type;
        this.parameters = parameters;
    }
}
