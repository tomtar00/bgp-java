package com.koala.bgp.blockchain;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;

import com.koala.bgp.utils.SimpleLogger;

public abstract class BlockchainNode 
{
    protected Blockchain blockchain;
    protected volatile boolean processingPendingTransactions = false;

    protected KeyPair keyPair;

    public BlockchainNode(int encriptionLevel) throws NoSuchAlgorithmException {
        this.blockchain = new Blockchain(encriptionLevel);

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            keyGen.initialize(1024);
            keyPair = keyGen.generateKeyPair();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }
    }

    public Blockchain getBlockchain() { return this.blockchain; }
    public boolean isMiningPendingTransactions() { return this.processingPendingTransactions; }
    public KeyPair getKeyPair() { return this.keyPair; }

    public boolean transactionIsValid(Transaction<?> transaction)
    {
        try {
            return transaction.isValid();
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
            return false;
        }
    }
    protected void processPendingTransactions() throws NoSuchAlgorithmException 
    {
        blockchain.minePendingTransactions();
        processingPendingTransactions = false;
    }
    
    protected abstract ArrayList<BlockchainNode> getOtherNodes();
    public abstract void sendTransaction(Transaction<?> transaction, BlockchainNode recipient);
    public abstract void onTransactionRecieved(Transaction<?> transaction) throws NoSuchAlgorithmException;
}
