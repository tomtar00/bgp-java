package com.koala.bgp.blockchain;

import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Blockchain 
{
    private List<Block> blocks; 
    private int encryptionLevel;

    public Blockchain(int encryptionLevel) throws NoSuchAlgorithmException {
        this.blocks = new ArrayList<>();
        this.encryptionLevel = encryptionLevel;

        blocks.add(createGenesicBlock());
    }
    public Blockchain(Blockchain blockchainToCopy) throws NoSuchAlgorithmException{
        this.blocks = new ArrayList<>(blockchainToCopy.getBlocks());
        this.encryptionLevel = blockchainToCopy.getEncryptionLevel();

        blocks.add(createGenesicBlock());
    }

    public int getEncryptionLevel() {
        return this.encryptionLevel;
    }
    public List<Block> getBlocks() {
        return this.blocks;
    }
    public Block getLatestBlock()
    {
        if (blocks.size() >= 1)
            return blocks.get(blocks.size() - 1);
        else return null;
    }


    private Block createGenesicBlock() throws NoSuchAlgorithmException {
        return new Block("0", "---");
    }
    public void addBlock(Block newBlock) throws NoSuchAlgorithmException
    {
        newBlock.setPreviousHash(getLatestBlock().getHash());
        newBlock.mineBlock(this.encryptionLevel);
        blocks.add(newBlock);   
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
        String result = "\t/--------\n";
        for (Block block : blocks)
        {
            result += "\tBlock: " + block.toString() + "\n";
        }
        result += "\t--------/";
        return result;
    }

}
