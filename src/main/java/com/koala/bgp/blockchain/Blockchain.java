package com.koala.bgp.blockchain;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Blockchain<T>
{
    private List<Block<T>> blocks; 
    private BlockingQueue<T> pendingTransactions;
    private int encryptionLevel;

    public Blockchain(int encryptionLevel) throws NoSuchAlgorithmException {
        this.blocks = Collections.synchronizedList(new ArrayList<>());
        this.pendingTransactions = new ArrayBlockingQueue<T>(1024);
        this.encryptionLevel = encryptionLevel;

        blocks.add(createGenesicBlock());
    }
    public Blockchain(Blockchain<T> blockchainToCopy) throws NoSuchAlgorithmException{
        this.blocks = new ArrayList<>(blockchainToCopy.getBlocks());
        this.pendingTransactions = new ArrayBlockingQueue<T>(1024, false, blockchainToCopy.getPendingTransactions());
        this.encryptionLevel = blockchainToCopy.getEncryptionLevel();
    }

    public int getEncryptionLevel() {
        return this.encryptionLevel;
    }
    public List<Block<T>> getBlocks() {
        return this.blocks;
    }
    public BlockingQueue<T> getPendingTransactions() {
        return this.pendingTransactions;
    }
    public Block<T> getLatestBlock()
    {
        if (blocks.size() >= 1)
            return blocks.get(blocks.size() - 1);
        else return null;
    }


    private Block<T> createGenesicBlock() throws NoSuchAlgorithmException {
        return new Block<T>("0", null);
    }
    public void minePendingTransactions() throws NoSuchAlgorithmException {
        while (pendingTransactions.size() > 0) {
            Block<T> newBlock = new Block<T>(getLatestBlock().getHash(), pendingTransactions.remove());
            newBlock.mineBlock(this.encryptionLevel);
            blocks.add(newBlock);
        }
    }
    public void createTransaction(T transaction) {
        pendingTransactions.add(transaction);
    }

    public boolean isChainValid() throws NoSuchAlgorithmException 
    {
        for (int i = 1; i < blocks.size(); i++) 
        {
            Block<T> currentBlock = blocks.get(i);
            Block<T> previousBlock = blocks.get(i - 1);

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
        String result = "\t/-------- (" + blocks.size() + " blocks)\n";
        ArrayList<Block<T>> currenBlocks = new ArrayList<>(blocks);
        for (Block<T> block : currenBlocks)
        {
            result += "\tBlock: " + block.toString() + "\n";
        }
        result += "\t--------/";
        return result;
    }

}
