package com.koala.bgp.blockchain;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.koala.bgp.utils.SimpleLogger;

public class Block
{
    // HEADER
    private String hash;
    private String previousHash;
    private long timestamp;
    private int nonce;

    private List<Transaction<?>> transactions;
    

    public Block(String previousHash, List<Transaction<?>> transactions) throws NoSuchAlgorithmException 
    {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = System.currentTimeMillis();
        this.hash = calculateBlockHash();
    }
    public Block(String previousHash, List<Transaction<?>> transactions, int nonce) throws NoSuchAlgorithmException 
    {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.nonce = nonce;
        this.timestamp = System.currentTimeMillis();
        this.hash = calculateBlockHash();
    }

    public String getHash() { return this.hash; }
    public String getPreviousHash() { return this.previousHash; }
    public List<Transaction<?>> getTransactions() { return this.transactions; }
    public int getNonce() { return this.nonce; }
    public long getTimestamp() { return this.timestamp; }

    public String calculateBlockHash() 
    {
        String dataToHash = 
            previousHash  
          + Integer.toString(nonce) 
          + (transactions == null ? "" : transactions.toString())
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
            ", timestamp='" + getTimestamp() + "'" +
            ", nonce='" + getNonce() + "'" +
            (getTransactions().size() != 0 ? ", \n\t(" + getTransactions().size() + " transactions) data=\n\t\t" + getTransactions() : "") +
            "\n\t}";
    }

}
