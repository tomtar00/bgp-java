package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.blockchain.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.Drawable;

import java.awt.*;

public class General extends BlockchainNode implements Drawable
{    
    private String name;
    private Decision decision;
    private Vector2 coords;

    private boolean traitor = false;
    private boolean voted = false;

    private Map<Decision, Boolean> opinions;

    // ====
    
    private final double NO_MESSAGE_INTERVAL = 15.0;
    private double endRoundTimer = 0.0;
    private final double CREATE_BLOCK_INTERVAL = 5.0;
    private double createBlockTimer;

    private int currentRound = 1;

    public General(String name, Vector2 coords, boolean traitor) throws NoSuchAlgorithmException {
        super(ByzantineMain.getEncLevel());
        this.name = name;
        this.coords = coords;
        this.traitor = traitor;

        opinions = new HashMap<Decision, Boolean>();
        for (int i = 0; i < Decision.values().length; i++) {
            Random rand = new Random();
            opinions.put(Decision.values()[i], rand.nextInt(100) < 50);
        }

        this.decision = Decision.randomDecision();
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

    @Override
    protected synchronized ArrayList<BlockchainNode> getOtherNodes()
    {
        ArrayList<BlockchainNode> otherGenerals = new ArrayList<>();
        for (BlockchainNode g : CommandService.getGenerals())
            if (g != this)
                otherGenerals.add(g);

        return otherGenerals;
    }
    @Override
    public synchronized void sendTransaction(Transaction<?> msg, BlockchainNode recipient) {
        Messenger messenger = new Messenger((Message)msg, this, (General)recipient); 
        CommandService.getMessengers().add(messenger); 
        messenger.run();
    }
    @Override
    public synchronized void onTransactionRecieved(Transaction<?> msg) throws NoSuchAlgorithmException
    { 
        if (voted) return;

        if (transactionIsValid(msg) && !blockchain.contains(msg)) 
        {
            if (!blockchain.pendingContains(msg)) 
            {
                // add new msg to mining queue
                blockchain.getPendingTransactions().add(msg);
            
                resendTransactionToAllGenerals(msg);
            }
            else {
                // search for the message...
                for (Transaction<?> t : blockchain.getPendingTransactions()) {
                    if (t.equals(msg)) {
                        // ...and increment number of verifications
                        // if the number of confirmations will be bigger 50% of NUM_GENERALS
                        // add it to the blockchain
                        t.incrementConfirms();
                        break;
                    }
                }
            }
        }  

        // reset timer
        endRoundTimer = 0f;
    }
    @Override
    protected synchronized void processPendingTransactions()
    {
        if (!miningPendingTransactions && blockchain.getPendingTransactions().size() > 0) {
            miningPendingTransactions = true;
            new Thread(() -> {
                try {

                    super.processPendingTransactions();

                    // make decision based on majority of common decisions in blockchain  
                    decision = makeDecision();

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    SimpleLogger.pressAnyKeyToContinue();
                }
            }).start();
        }
    }
    
    private synchronized boolean shouldEndRound() {
        return endRoundTimer > NO_MESSAGE_INTERVAL && !miningPendingTransactions;
    }
    public synchronized Decision makeDecision() {
        java.util.List<Decision> decisionList = new java.util.ArrayList<>();
        for (Block b : blockchain.getBlocks()) {
            for (Transaction<?> t : b.getTransactions()) {
                if (t != null /* && opinions.get(((Message)t).getDecision()) */)
                    decisionList.add(((Message)t).getDecision());
            }
        }
        decision = decisionList.size() == 0 ? getDecision() : Mathf.mostCommon(decisionList);
        if (traitor) {
            decision = Decision.randomDecision(decision);
        }
        return decision;
    }
    public synchronized void sendMyDecisionToAllGenerals(Decision decision) throws NoSuchAlgorithmException
    {
        // send my decision to every other general
        for (BlockchainNode general : getOtherNodes())
        {  
            Message msgForGeneral = new Message(decision, keyPair.getPublic(), general.getKeyPair().getPublic());
            msgForGeneral.signTransaction(keyPair);
            for (BlockchainNode recipient : getOtherNodes())
                sendTransaction(new Message(msgForGeneral), recipient);  
        }  
    }
    public synchronized void resendTransactionToAllGenerals(Transaction<?> transaction) throws NoSuchAlgorithmException
    {
        // resend message to every other general
        for (BlockchainNode general : getOtherNodes())
        {  
            Message msg = (Message)transaction;
            Message copy = new Message(msg);
            sendTransaction(copy, general);  
        }   
    }
    public void update() {
        endRoundTimer += Time.getDeltaTime();
        createBlockTimer += Time.getDeltaTime();
 
        if (!voted) 
        {
            // create block
            if (createBlockTimer > CREATE_BLOCK_INTERVAL) {
                processPendingTransactions();
                createBlockTimer = 0;
            }

            // vote to end synchronization
            if (shouldEndRound()) 
            {
                processPendingTransactions();
                currentRound++;

                if (currentRound > ByzantineMain.getNumOfTraitors() + 1) {
                    SimpleLogger.logWarning(getName() + " voted to end decision phase");
                    CommandService.voteToEndSync(); 
                    voted = true;
                }
                else {
                    try {
                        sendMyDecisionToAllGenerals(decision);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        SimpleLogger.pressAnyKeyToContinue();
                    }
                    endRoundTimer = 0;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "{\n" +
            "   name='" + getName() + "'\n" +
            "   mining='" + isMiningPendingTransactions() + "'\n" +
            "   pending transactions='" + getBlockchain().getPendingTransactions().size() + "'\n" +
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
        g2D.drawString(getName(), coords.getX() - sizeX / 2, coords.getY() - sizeY);

        if (miningPendingTransactions)
            g2D.drawString("Mining..." , coords.getX() - sizeX / 2, coords.getY() + sizeY + 5);
        if (voted)
            g2D.drawString("End Sync" , coords.getX() - sizeX / 2, coords.getY() + sizeY + 20);
    } 
}
