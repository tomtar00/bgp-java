package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

    private final double SEND_MESSAGE_INTERVAL = .05;
    private BlockingQueue<MessageDto> messagesToSend;
    private double sendMsgTimer = 0.0;

    // ====
    
    private final double NO_MESSAGE_INTERVAL = 15.0;
    private double endRoundTimer = 0.0;

    private int currentRound = 1;

    public General(String name, Vector2 coords, boolean traitor) throws NoSuchAlgorithmException {
        super(ByzantineMain.getEncLevel());
        this.name = name;
        this.coords = coords;
        this.traitor = traitor;

        this.decision = Decision.randomDecision();

        this.messagesToSend = new ArrayBlockingQueue<MessageDto>(4096);
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
            Transaction<?> pendingTransaction = blockchain.getPendingTransaction(msg);
            if (pendingTransaction == null) 
            {
                // add new msg to queue (memPool)
                pendingTransaction = msg;
                blockchain.getPendingTransactions().add(pendingTransaction);               
            }

            // increment number of verifications
            // if the number of confirmations will be bigger than 50% of NUM_GENERALS
            // add it to the blockchain ( processPendingTransactions() )
            pendingTransaction.incrementConfirms();        


            if (pendingTransaction.getConfirms() <= ByzantineMain.getNumOfGenerals() / 2) {
                resendTransactionToAllGenerals(pendingTransaction);
            }
        }  

        // reset timer
        endRoundTimer = 0f;
    }
    @Override
    protected synchronized void processPendingTransactions()
    {
        if (!processingPendingTransactions && blockchain.getPendingTransactions().size() > 0) {
            processingPendingTransactions = true;
            new Thread(() -> {
                try {

                    super.processPendingTransactions();

                    // make decision based on majority of common decisions in the blockchain  
                    decision = makeDecision();

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    SimpleLogger.pressAnyKeyToContinue();
                }
            }).start();
        }
    }
    
    private synchronized boolean shouldEndRound() {
        return endRoundTimer > NO_MESSAGE_INTERVAL && !processingPendingTransactions;
    }
    private synchronized int getNumOfRounds() {
        return ByzantineMain.getNumOfTraitors() + 1;
    }
    public synchronized Decision makeDecision() {
        java.util.List<Decision> decisionList = new java.util.ArrayList<>();
        for (Block b : blockchain.getBlocks()) {
            for (Transaction<?> t : b.getTransactions()) {
                if (t != null)
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
            {
                //sendTransaction(new Message(msgForGeneral), recipient); 
                messagesToSend.add(new MessageDto(new Message(msgForGeneral), recipient));
            } 

            msgForGeneral.incrementConfirms();
            blockchain.getPendingTransactions().add(msgForGeneral);
        }  
    }
    public synchronized void resendTransactionToAllGenerals(Transaction<?> transaction) throws NoSuchAlgorithmException
    {
        // resend message to every other general
        for (BlockchainNode general : getOtherNodes())
        {  
            Message msg = (Message)transaction;
            Message copy = new Message(msg);

            //sendTransaction(copy, general);  
            messagesToSend.add(new MessageDto(copy, general));
        }   
    }
    public void update() 
    {
        endRoundTimer += Time.getDeltaTime();
        sendMsgTimer += Time.getDeltaTime();
        
        if (sendMsgTimer > SEND_MESSAGE_INTERVAL && messagesToSend.size() > 0) {
            MessageDto msgToSend = messagesToSend.poll();
            sendTransaction(msgToSend.getMsg(), msgToSend.getRecipient());
            sendMsgTimer = 0.0;
        }

        if (!voted && shouldEndRound()) {  

            processPendingTransactions();
            currentRound++;

            if (currentRound > getNumOfRounds()) {
                SimpleLogger.print(getName() + " voted to end decision phase");
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

    @Override
    public String toString() {
        return "{\n" +
            "   name='" + getName() + "'\n" +
            "   mining='" + isMiningPendingTransactions() + "'\n" +
            "   pending transactions " + getBlockchain().getPendingTransactions().size() + "='" + getBlockchain().getPendingTransactions() + "'\n" +
            "   blockchain=\n" + getBlockchain() + "\n" +
            "   decision='" + getDecision() + "'\n" +
            "}\n";
    }   
    @Override
    public void draw(Graphics2D g2D) {
        int sizeX = 50;
        int sizeY = 50;
        int thickness = 15;
        
        // decision
        g2D.setPaint(Decision.getColor(getDecision()));
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

        if (processingPendingTransactions)
            g2D.drawString("Mining..." , coords.getX() - sizeX / 2, coords.getY() + sizeY + 5);
        if (voted)
            g2D.drawString("End Sync" , coords.getX() - sizeX / 2, coords.getY() + sizeY + 20);
    } 

    class MessageDto {
        private Transaction<?> msg;
        private BlockchainNode recipient;

        public MessageDto(Transaction<?> msg, BlockchainNode recipient) {
            this.msg = msg;
            this.recipient = recipient;
        }

        public Transaction<?> getMsg() {
            return this.msg;
        }        
        public BlockchainNode getRecipient() {
            return this.recipient;
        }
    }
}
