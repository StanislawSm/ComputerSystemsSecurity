package bsk.project.chatapp;
import java.io.FileInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Message implements Serializable{



    private MessageType messageType;

    private String text = null;

    public Message(String text) {
        this.messageType = MessageType.TEXT;
        this.text = text;
    }

    public Message(MessageType messageType, String text) {
        this.messageType = messageType;
        this.text = text;
    }


    public String getText() {
        return text;
    }
    public MessageType getMessageType() {
        return messageType;
    }
}
