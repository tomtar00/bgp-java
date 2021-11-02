package com.koala.bgp.blockchain;

public abstract class Transaction<T> 
{
    protected long id;
    protected int nonce;

    protected BlockchainNode<T> sender;
    protected BlockchainNode<T> recipient;

    private static volatile long id_counter = 0;

    protected Transaction(BlockchainNode<T> sender, BlockchainNode<T> recipient) {
        this.sender = sender;
        this.recipient = recipient;
        id = id_counter++;
    }

    public long getId() {
        return this.id;
    }

    public int getNonce() {
        return this.nonce;
    }
    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public void setId(long id) {
        this.id = id;
    }

    public abstract BlockchainNode<?> getSender();
    public abstract BlockchainNode<?> getRecipient();
}
