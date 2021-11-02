package com.koala.bgp.blockchain;

import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.koala.bgp.utils.SimpleLogger;

public class Block<T> 
{
    private String hash;
    private String previousHash;
    private T transaction;
    private long timestamp;

    private int nonce;

    public Block(String previousHash, T transaction) throws NoSuchAlgorithmException 
    {
        this.previousHash = previousHash;
        this.transaction = transaction;
        this.timestamp = System.currentTimeMillis();
        this.hash = calculateBlockHash();
    }
    public Block(String previousHash, T transaction, int nonce) throws NoSuchAlgorithmException 
    {
        this.previousHash = previousHash;
        this.transaction = transaction;
        this.nonce = nonce;
        this.timestamp = System.currentTimeMillis();
        this.hash = calculateBlockHash();
    }
    public Block(Block<T> blockToCopy)
    {
        this.previousHash = blockToCopy.getPreviousHash();
        this.transaction = blockToCopy.getTransaction();
        this.nonce = blockToCopy.getNonce();
        this.hash = blockToCopy.getHash();
        // timestamp
    }


    public String getHash() {
        return this.hash;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }
    public void setPreviousHash(String hash) {
        this.previousHash = hash;
    }

    public T getTransaction() {
        return this.transaction;
    }

    public int getNonce() {
        return this.nonce;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String calculateBlockHash() 
    {
        String dataToHash = 
            previousHash  
          + Integer.toString(nonce) 
          + (transaction == null ? "" : transaction.toString())
          + Long.toString(timestamp);

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

    public void mineBlock(int encryptionLevel) throws NoSuchAlgorithmException 
    {
        String prefixString = new String(new char[encryptionLevel]).replace('\0', '0');
        while (!hash.substring(0, encryptionLevel).equals(prefixString))
        {
            nonce++;
            hash = calculateBlockHash();
        }
    }


    @Override
    public String toString() {
        return "{" +
            " hash='" + getHash() + "'" +
            ", previousHash='" + getPreviousHash() + "'" +
            ", data='" + getTransaction() + "'" +
            ", timestamp='" + getTimestamp() + "'" +
            ", nonce='" + getNonce() + "'" +
            "}";
    }

}
