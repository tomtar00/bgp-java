package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.koala.bgp.blockchain.*;
import com.koala.bgp.utils.SimpleLogger;
import com.koala.bgp.visual.Drawable;

import java.awt.*;

public class General implements Drawable
{
    private final int NUM_RESENDS = 3;
    
    private String name;
    private Battle battle;
    private Blockchain blockchain;
    private Decision decision;

    private Coords coords;

    private int resendCounter = 0;
    private boolean voted = false;

    public General(String name, Battle battle, int encriptionLevel) throws NoSuchAlgorithmException {
        this.name = name;
        this.battle = battle;
        this.decision = Decision.NONE;
        this.blockchain = new Blockchain(encriptionLevel);
    }

    public String getName() {
        return this.name;
    }
    public Battle getBattle() {
        return this.battle;
    }
    public Blockchain getBlockchain() {
        return this.blockchain;
    }
    public Decision getDecision() {
        return this.decision;
    }

    public Coords getCoords() {
        return this.coords;
    }
    public void setCoords(Coords coords) {
        this.coords = new Coords(coords);
    }

    private ArrayList<General> getOtherGererals()
    {
        ArrayList<General> otherGenerals = new ArrayList<General>();
        for (General g : battle.getGenerals())
            if (g != this)
                otherGenerals.add(g);

        return otherGenerals;
    }

    public void sendMessage(Message msg) throws NoSuchAlgorithmException
    {
        SimpleLogger.print("-------- " + getName() + " sends message(s) --------"); 

        // simulate adding block to blockchain, so that nonce can be calculated
        Block messageBlock = new Block(null, msg.getText());
        //messageBlock.setPreviousHash(blockchain.getLatestBlock().getHash());
        messageBlock.mineBlock(blockchain.getEncryptionLevel());
        msg.setNonce(messageBlock.getNonce());

        // send message to every other general
        for (General general : getOtherGererals())
        {
            Message msgForGeneral = new Message(msg);  
            Messenger messenger = new Messenger(msgForGeneral, general, coords, general.getCoords()); 
            battle.getMessengers().add(messenger); 
            messenger.run();            
        }       
    }
    public void onMessageRecieved(Messenger msger) throws NoSuchAlgorithmException
    { 
        Message msg = msger.getMessage();
        if (msgIsValid(msg)) 
        {
            decision = msg.getDecision();
            blockchain.addBlock(new Block(null, msg.getText()));

            // resend message to show its valid
            if (resendCounter < NUM_RESENDS)
                sendMessage(msg);

            // print all generals
            for (General general : battle.getGenerals())
            {
                SimpleLogger.print(general);
            }
        }
        
        if (++resendCounter >= NUM_RESENDS) 
        {
            ArrayList<Messenger> messengers = new ArrayList<>(battle.getMessengers());
            for (Messenger m : messengers) {
                if (!m.isRunning()) {
                    battle.getMessengers().remove(m);
                }
            }
            SimpleLogger.logWarning(getName() + " is out of messengers! Messengers left in battle: " + battle.getMessengers().size());
            if (!voted /*&& battle.getMessengers().size() == 0*/) 
            {
                battle.voteToEndSync();
                voted = true;
            }
        }

        // remove an useless messenger
        battle.getMessengers().remove(msger);
    }
    public boolean msgIsValid(Message msg) throws NoSuchAlgorithmException
    {
        SimpleLogger.print("-------- " + getName() + " verifies message (id: " + msg.getId() + ") --------"); 

        // simulate adding new block to the blockchain
        Block block = new Block(null, msg.getText());
        Blockchain bCopy = new Blockchain(blockchain);
        bCopy.addBlock(block);
        boolean isValid = block.getNonce() == msg.getNonce();
        if (isValid) {
            SimpleLogger.print(getName() + " says, that message (id: " + msg.getId() + ") was valid. (" + block.getNonce() + " == " + msg.getNonce() + ")");
        }
        else {
            SimpleLogger.print(getName() + " says, that message (id: " + msg.getId() + ") was not valid! (" + block.getNonce() + " != " + msg.getNonce() + ")");
        }
        return isValid;

        // if Message could contain hash of the new block (well... it can, but its not that secure)
        // the validation would be much faster
        // Block block = new Block(null, msg.getText(), msg.getNonce());
        // return block.getHash() == msg.getHash();   
    }

    @Override
    public String toString() {
        return "{\n" +
            "   name='" + getName() + "'\n" +
            "   coords='" + getCoords() + "'\n" +
            "   blockchain=\n" + getBlockchain() + "\n" +
            "   decision='" + getDecision() + "'\n" +
            "}";
    }   

    @Override
    public void draw(Graphics2D g2D) {
        int sizeX = 50;
        int sizeY = 50;
        int thickness = 10;

        Color color;
        if (getDecision() == Decision.NONE) 
            color = Color.WHITE;
        else if (getDecision() == Decision.ATTACK)
            color = Color.GREEN;
        else if (getDecision() == Decision.RETREAT)
            color = Color.RED;
        else
            color = Color.GRAY;
        
        g2D.setPaint(color);
        g2D.fillOval(coords.getX() - (sizeX + thickness) / 2, coords.getY() - (sizeY + thickness) / 2, sizeX + thickness, sizeY + thickness);
        g2D.setPaint(Color.BLUE);
        g2D.fillOval(coords.getX() - sizeX / 2, coords.getY() - sizeY / 2, sizeX, sizeY);
        g2D.setPaint(Color.WHITE);
        g2D.drawString(getName(), coords.getX() - sizeX / 2, coords.getY() - sizeY);
    } 
}
