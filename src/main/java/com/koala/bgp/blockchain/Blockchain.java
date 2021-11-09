package com.koala.bgp.blockchain;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.koala.bgp.byzantine.CommandService;

public class Blockchain
{
    private List<Block> blocks; 
    private BlockingQueue<Transaction<?>> pendingTransactions;
    private int encryptionLevel;

    public Blockchain(int encryptionLevel) throws NoSuchAlgorithmException {
        this.blocks = Collections.synchronizedList(new ArrayList<>());
        this.pendingTransactions = new ArrayBlockingQueue<Transaction<?>>(1024);
        this.encryptionLevel = encryptionLevel;

        blocks.add(createGenesicBlock());
    }
    public Blockchain(Blockchain blockchainToCopy) throws NoSuchAlgorithmException{
        this.blocks = new ArrayList<>(blockchainToCopy.getBlocks());
        this.pendingTransactions = new ArrayBlockingQueue<Transaction<?>>(1024, false, blockchainToCopy.getPendingTransactions());
        this.encryptionLevel = blockchainToCopy.getEncryptionLevel();
    }

    public int getEncryptionLevel() {
        return this.encryptionLevel;
    }
    public List<Block> getBlocks() {
        return this.blocks;
    }
    public BlockingQueue<Transaction<?>> getPendingTransactions() {
        return this.pendingTransactions;
    }
    public Block getLatestBlock()
    {
        if (blocks.size() >= 1)
            return blocks.get(blocks.size() - 1);
        else return null;
    }
    public int getNumTransactions() {
        int count = 0;
        for (Block b : blocks) {
            count += b.getTransactions().size();
        }
        return count;
    }


    private Block createGenesicBlock() throws NoSuchAlgorithmException {
        return new Block("0", new ArrayList<>());
    }
    public void minePendingTransactions() throws NoSuchAlgorithmException {
        List<Transaction<?>> verifiedTransactions = new ArrayList<>();

        for (Transaction<?> transaction : pendingTransactions) {
            // if this transaction was confirmed by the majority of nodes
            // it can be added to the blockchain
            if (transaction.getConfirms() > CommandService.getGenerals().size() / 2) {
                verifiedTransactions.add(transaction);
                pendingTransactions.remove(transaction);
            }
        }

        if (verifiedTransactions.size() > 0) {
            Block newBlock = new Block(getLatestBlock().getHash(), verifiedTransactions);
            newBlock.mineBlock(this.encryptionLevel);
            blocks.add(newBlock);
        }
    }
    public void createTransaction(Transaction<?> transaction) {
        pendingTransactions.add(transaction);
    }
    public Transaction<?> contains(Transaction<?> transaction) {
        for (Block b : blocks) {
            for (Transaction<?> t : b.getTransactions()) {
                if (t.equals(transaction)) {
                    return t;
                }
            }
        }
        return null;
    }
    public Transaction<?> pendingContains(Transaction<?> transaction) {
        for (Transaction<?> t : pendingTransactions) {
            if (t.equals(transaction)) {
                return t;
            }
        }
        return null;
    }
    public boolean isChainValid() throws NoSuchAlgorithmException 
    {
        for (int i = 1; i < blocks.size(); i++) 
        {
            Block currentBlock = blocks.get(i);
            Block previousBlock = blocks.get(i - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateBlockHash()))
            {
                return false;
            }
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash()))
            {
                return false;
            }
        }

        return true;
    }


    @Override
    public String toString() {
        String result = "\t/-------- (" + blocks.size() + " blocks, " + getNumTransactions() + " transactions)\n";
        ArrayList<Block> currenBlocks = new ArrayList<>(blocks);
        for (Block block : currenBlocks)
        {
            result += "\tBlock: " + block.toString() + "\n";
        }
        result += "\t--------/";
        return result;
    }

}
