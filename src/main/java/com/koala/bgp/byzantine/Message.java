package com.koala.bgp.byzantine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;

import com.koala.bgp.blockchain.Transaction;
import com.koala.bgp.utils.SimpleLogger;

public class Message extends Transaction<Decision>
{
    private int roundIndex;
    private double general_authority;

    public Message(Decision decision, PublicKey senderPublicKey, PublicKey recipientPublicKey,double general_authority) {
        super(senderPublicKey, recipientPublicKey);
        this.data = decision;
        this.roundIndex = 1;
        this.general_authority = general_authority;
    }
    public Message(Message msg) {
        super(msg);
        this.data = msg.getDecision();
        this.roundIndex = 1;
        this.general_authority = 1;
    }
    public Message(Message msg, int roundIndex) {
        super(msg);
        this.data = msg.getDecision();
        this.roundIndex = roundIndex;
        this.general_authority = 1;
    }
    

    public int getRoundIndex() {
        return roundIndex;
    }
    public Decision getDecision() {
        return this.data;
    }

    public double getGeneral_authority() {
        return general_authority;
    }

    public void setRoundIndex(int roundIndex) {
        this.roundIndex = roundIndex;
    }



    @Override
    public String toString() {
        return getDecision() + " " + roundIndex;
    }

    @Override
    protected String calculateTransactionHash() {
        String dataToHash = 
            data != null ? data.toString() : "null" 
          + senderPublicKey 
          + recipientPublicKey;

        MessageDigest digest = null;
        byte[] bytes = null;

        try 
        {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } 
        catch (Exception ex) 
        {
            SimpleLogger.print("Error creating hash! ");
            ex.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }

        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) 
        {
            buffer.append(String.format("%02x", b));
        }
        return buffer.toString();
    }

}
