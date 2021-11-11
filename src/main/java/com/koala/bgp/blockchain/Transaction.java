package com.koala.bgp.blockchain;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import com.koala.bgp.utils.SimpleLogger;

public abstract class Transaction<T>
{
    protected long id;
    protected static long id_counter; 

    protected PublicKey senderPublicKey;
    protected PublicKey recipientPublicKey;
    protected T data;

    private Signature signature;
    private byte[] signatureBytes;
    private boolean signed;

    protected int confirms;

    protected Transaction(PublicKey sender, PublicKey recipient) {
        this.senderPublicKey = sender;
        this.recipientPublicKey = recipient;
        this.id = id_counter++;

        try {
            signature = Signature.getInstance("SHA256withDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }

        this.signed = false;
    }
    protected Transaction(Transaction<T> transaction) {
        this.senderPublicKey = transaction.getSenderPublicKey();
        this.recipientPublicKey = transaction.getRecipientPublicKey();
        this.data = transaction.getData();
        this.signed = transaction.isSigned();
        this.signatureBytes = transaction.SignatureBytes().clone();
        this.id = transaction.getId();

        try {
            signature = Signature.getInstance("SHA256withDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }
    }

    public PublicKey getSenderPublicKey() { return senderPublicKey; }
    public PublicKey getRecipientPublicKey() { return recipientPublicKey; }
    public long getId() { return id; }
    public T getData() { return data; }
    public boolean isSigned() { return signed; }
    public byte[] SignatureBytes() { return signatureBytes; }
    public int getConfirms() { return confirms; }

    public void incrementConfirms() { confirms++; }

    public synchronized void signTransaction(KeyPair signingKey) {
        if (!signingKey.getPublic().equals(senderPublicKey)) {
            throw new SecurityException("You cannot sign transactions for other wallets");
        }

        try {
            signature.initSign(signingKey.getPrivate());
            signature.update(calculateTransactionHash().getBytes());
            signatureBytes = signature.sign();
            signed = true;
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }
    }
    public synchronized boolean isValid() throws SignatureException, InvalidKeyException {
        if (senderPublicKey == null) return true;
        if (signature == null) { throw new SignatureException("No signature in this transaction"); }

        signature.initVerify(senderPublicKey);
        signature.update(calculateTransactionHash().getBytes());
        return signed && signature.verify(signatureBytes);
    }

    protected abstract String calculateTransactionHash();

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
         
        Transaction<?> c = (Transaction<?>) o;
         
        return senderPublicKey.equals(c.getSenderPublicKey()) && 
               recipientPublicKey.equals(c.getRecipientPublicKey()) &&
               data.equals(c.getData()) && 
               id == c.getId();
    }
}
