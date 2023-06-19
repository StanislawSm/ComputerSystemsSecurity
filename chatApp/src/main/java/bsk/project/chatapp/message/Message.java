package bsk.project.chatapp.message;

import java.io.Serializable;

public class Message implements Serializable{
    private final MessageType messageType;
    private String _cipherMode;
    private String text = null;

    public Message(String text) {
        this.messageType = MessageType.TEXT;
        this.text = text;
    }

    public Message(MessageType messageType, String text) {
        this.messageType = messageType;
        this.text = text;
    }

    public Message(MessageType messageType, String text, String cipherMode) {
        this.messageType = messageType;
        this.text = text;
        _cipherMode = cipherMode;
    }

    public String getText() {
        return text;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public String getCypherMode() { return _cipherMode; }
}
