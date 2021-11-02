package com.koala.bgp.blockchain;

import java.security.NoSuchAlgorithmException;

public abstract class BlockchainNode<T> 
{
    protected Blockchain<T> blockchain;
    protected volatile boolean miningPendingTransactions = false;

    public BlockchainNode(int encriptionLevel) throws NoSuchAlgorithmException {
        this.blockchain = new Blockchain<T>(encriptionLevel);
    }

    public Blockchain<T> getBlockchain() {
        return this.blockchain;
    }
    public boolean isMiningPendingTransactions() {
        return this.miningPendingTransactions;
    }

    public boolean isValidTransaction(T transaction) {
        return true;
    }
}
