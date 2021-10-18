package com.koala.bgp.byzantine;

public class Message
{
    private String text;
    private Decision decision;
    private int nonce;

    private long id;
    private static long id_counter = 0;

    public Message(String text, Decision decision) {
        this.text = text;
        this.decision = decision;
        id = id_counter++;
    }
    public Message(Message msgToCopy) {
        this.text = msgToCopy.getText();
        this.decision = msgToCopy.getDecision();
        this.nonce = msgToCopy.getNonce();
        id = id_counter++;
    }

    public int getNonce() {
        return this.nonce;
    }
    public String getText() {
        return this.text;
    }
    public Decision getDecision() {
        return this.decision;
    }
    public long getId() {
        return this.id;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
}
