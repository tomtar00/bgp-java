package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.blockchain.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.Drawable;

import java.awt.*;

public class General extends BlockchainNode<Message> implements Drawable
{    
    private String name;
    private Decision decision;
    private Vector2 coords;

    private boolean traitor = false;

    private int resendCounter = 0;
    private boolean voted = false;

    public General(String name, Vector2 coords, boolean traitor) throws NoSuchAlgorithmException {
        super(ByzantineMain.getEncLevel());
        this.name = name;
        this.coords = coords;
        this.traitor = traitor;

        this.decision = Decision.randomDecision();
        blockchain.getPendingTransactions().add(new Message(decision, this, this));
    }
    
    public String getName() {
        return this.name;
    }
    public Decision getDecision() {
        return this.decision;
    }
    public Vector2 getCoords() {
        return this.coords;
    }
    public boolean isTraitor() {
        return this.traitor;
    }

    public synchronized ArrayList<General> getOtherGererals(General excGeneral)
    {
        ArrayList<General> otherGenerals = new ArrayList<General>();
        for (General g : CommandService.getGenerals())
            if (g != excGeneral && g != this)
                otherGenerals.add(g);

        return otherGenerals;
    }
    public synchronized void sendMessage(Decision decision, General excGeneral) throws NoSuchAlgorithmException
    {
        SimpleLogger.print("-------- " + getName() + " sends message(s) --------"); 

        // simulate adding block to blockchain, so that nonce can be calculated
        // Block<Message> messageBlock = new Block<Message>(null, msg);
        // messageBlock.setPreviousHash(blockchain.getLatestBlock().getHash());
        // mining = true;
        // messageBlock.mineBlock(blockchain.getEncryptionLevel());
        // mining = false;

        // msg.setNonce(messageBlock.getNonce());

        // send message to every other general except one general who sended
        for (General general : getOtherGererals(excGeneral))
        {  
            Message msgForGeneral = new Message(decision, this, general);
            Messenger messenger = new Messenger(msgForGeneral); 
            CommandService.getMessengers().add(messenger); 
            messenger.run();            
        }   
    }
    public synchronized void onMessageRecieved(Messenger msger) throws NoSuchAlgorithmException
    { 
        Message msg = msger.getMessage();
        if (msgIsValid(msg)) 
        {
            // add new msg to mining queue
            blockchain.getPendingTransactions().add(msg);

            // resend message...
            if (shouldResend()) {
                Decision newDecision = traitor ? Decision.randomDecision(msg.getDecision()) : msg.getDecision();
                                                // ... to everyone except the sender
                sendMessage(newDecision, (General)msg.getSender());
                //sendMessage(newDecision, null);
                resendCounter++;
            }
        }
        
        // remove an useless messenger
        CommandService.getMessengers().remove(msger);

        // vote to end synchronization
        if (!voted && !shouldResend()) 
        {
            SimpleLogger.logWarning(getName() + " voted to end decision phase");
            CommandService.voteToEndSync();
            voted = true;
        }
    }
    private synchronized void processPendingMessages() throws NoSuchAlgorithmException 
    {
        blockchain.minePendingTransactions();

        // make decision based on majority of common decisions in blockchain  
        decision = makeDecision();

        miningPendingTransactions = false;
    }
    public synchronized boolean msgIsValid(Message msg) throws NoSuchAlgorithmException
    {
        SimpleLogger.print("-------- " + getName() + " verifies message (id: " + msg.getId() + ") --------"); 

        // // simulate adding new block to the blockchain
        // Block<Message> block = new Block<Message>(null, msg);
        // Blockchain<Message> bCopy = new Blockchain<Message>(blockchain);
        // mining = true;
        // bCopy.addBlock(block);
        // mining = false;
        // boolean isValid = block.getNonce() == msg.getNonce();
        // if (isValid) {
        //     SimpleLogger.print(getName() + " says, that message (id: " + msg.getId() + ") was valid. (" + block.getNonce() + " == " + msg.getNonce() + ")");
        // }
        // else {
        //     SimpleLogger.print(getName() + " says, that message (id: " + msg.getId() + ") was not valid! (" + block.getNonce() + " != " + msg.getNonce() + ")");
        // }
        // return isValid;   

        return isValidTransaction(msg);
    }
    private synchronized boolean shouldResend() {
        return resendCounter < (ByzantineMain.getNumOfGenerals() - 1) * (ByzantineMain.getNumOfTraitors() + 1);
    }
    public synchronized Decision makeDecision() {
        java.util.List<Decision> decisionList = new java.util.ArrayList<>();
        for (Block<Message> b: blockchain.getBlocks()) {
            if (b.getTransaction() != null)
                decisionList.add(b.getTransaction().getDecision());
        }
        decision = decisionList.size() == 0 ? getDecision() : Mathf.mostCommon(decisionList);
        return decision;
    }

    public void update() {
        if (!miningPendingTransactions && blockchain.getPendingTransactions().size() > 0) {
            miningPendingTransactions = true;
            new Thread(() -> {
                try {
                    processPendingMessages();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    SimpleLogger.pressAnyKeyToContinue();
                }
            }).start();
        }
    }

    @Override
    public String toString() {
        return "{\n" +
            "   name='" + getName() + "'\n" +
            "   coords='" + getCoords() + "'\n" +
            "   blockchain=\n" + getBlockchain() + "\n" +
            "   decision='" + getDecision() + "'\n" +
            "}\n";
    }   
    @Override
    public void draw(Graphics2D g2D) {
        int sizeX = 50;
        int sizeY = 50;
        int thickness = 15;

        Color color;
        if (getDecision() == Decision.ATTACK_AT_DAWN)
            color = Color.GREEN;
        else if (getDecision() == Decision.ATTACK_AT_NOON)
            color = Color.CYAN;
        else if (getDecision() == Decision.ATTACK_IN_THE_EVENING)
            color = Color.ORANGE;
        else if (getDecision() == Decision.RETREAT)
            color = Color.RED;
        else
            color = Color.GRAY;
        
        // decision
        g2D.setPaint(color);
        g2D.fillOval((int)(coords.getX() - (sizeX + thickness) / 2), (int)(coords.getY() - (sizeY + thickness) / 2), sizeX + thickness, sizeY + thickness);

        // general / traitor
        if (traitor)
            g2D.setPaint(Color.decode("#ff6e6e"));
        else
            g2D.setPaint(Color.decode("#49b2fc"));
        g2D.fillOval((int)(coords.getX() - sizeX / 2), (int)(coords.getY() - sizeY / 2), sizeX, sizeY);

        // outline
        g2D.setPaint(Color.BLACK);
        g2D.drawOval((int)(coords.getX() - sizeX / 2), (int)(coords.getY() - sizeY / 2), sizeX, sizeY);

        // name
        g2D.setPaint(Color.WHITE);
        g2D.drawString(getName() + " -- R: " + resendCounter, coords.getX() - sizeX / 2, coords.getY() - sizeY);

        if (miningPendingTransactions)
            g2D.drawString("Mining..." , coords.getX() - sizeX / 2, coords.getY() + sizeY + 5);
        if (voted)
            g2D.drawString("End Sync" , coords.getX() - sizeX / 2, coords.getY() + sizeY + 20);
    } 
}
