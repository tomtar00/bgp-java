package com.koala.bgp.byzantine;

import com.koala.bgp.blockchain.BlockchainNode;
import com.koala.bgp.blockchain.Transaction;

public class Message extends Transaction<Message>
{
    private Decision decision;

    public Message(Decision decision, BlockchainNode<Message> sender, BlockchainNode<Message> recipient) {
        super(sender, recipient);
        this.decision = decision;
    }
    public Message(Message msgToCopy) {
        super(msgToCopy.getSender(), msgToCopy.getRecipient());
        this.decision = msgToCopy.getDecision();
        this.nonce = msgToCopy.getNonce();
        this.id = msgToCopy.getId();
    }

    
    public Decision getDecision() {
        return this.decision;
    }

    public boolean equals(Message message) {
        return  this.decision.equals(message.getDecision()) &&
                this.nonce == message.getNonce();
    }

    @Override
    public BlockchainNode<Message> getSender() {
        return this.sender;
    }
    @Override
    public BlockchainNode<Message> getRecipient() {
        return this.recipient;
    }

    @Override
    public String toString() {
        return "{ decision='" + getDecision() + "' }";
    }

}
