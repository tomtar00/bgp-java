package com.koala.bgp.blockchain;


import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.koala.bgp.utils.SimpleLogger;

public class Block 
{
    private String hash;
    private String previousHash;
    private String data;
    // timestamp

    private int nonce;

    public Block(String previousHash, String data) throws NoSuchAlgorithmException 
    {
        this.previousHash = previousHash;
        this.data = data;
        this.hash = calculateBlockHash();
    }
    public Block(String previousHash, String data, int nonce) throws NoSuchAlgorithmException 
    {
        this.previousHash = previousHash;
        this.data = data;
        this.nonce = nonce;
        this.hash = calculateBlockHash();
    }
    public Block(Block blockToCopy)
    {
        this.previousHash = blockToCopy.getPreviousHash();
        this.data = blockToCopy.getData();
        this.nonce = blockToCopy.getNonce();
        this.hash = blockToCopy.getHash();
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

    public String getData() {
        return this.data;
    }

    public int getNonce() {
        return this.nonce;
    }

    public String calculateBlockHash() 
    {
        String dataToHash = 
            previousHash  
          + Integer.toString(nonce) 
          + data;

        MessageDigest digest = null;
        byte[] bytes = null;

        try 
        {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } 
        catch (Exception ex) 
        {
            SimpleLogger.print("Error creating hash! " + ex.getMessage());
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

        SimpleLogger.print("Block mined: " +
                            "\n\tHash: " + hash + 
                            "\n\tPreviousHash: " + previousHash +
                            "\n\tData: " + data + " Nonce: " + nonce + " Difficulty: " + encryptionLevel
                        );
    }


    @Override
    public String toString() {
        return "{" +
            " hash='" + getHash() + "'" +
            ", previousHash='" + getPreviousHash() + "'" +
            ", data='" + getData() + "'" +
            ", nonce='" + getNonce() + "'" +
            "}";
    }

}
