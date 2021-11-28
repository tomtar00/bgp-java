package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.blockchain.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.Drawable;

import java.awt.*;

public class General extends BlockchainNode implements Drawable {
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
    protected synchronized ArrayList<BlockchainNode> getOtherNodes() {
        ArrayList<BlockchainNode> otherGenerals = new ArrayList<>();
        for (BlockchainNode g : CommandService.getGenerals())
            if (g != this)
                otherGenerals.add(g);

        return otherGenerals;
    }

    @Override
    public synchronized void sendTransaction(Transaction<?> msg, BlockchainNode recipient) {
        Messenger messenger = new Messenger((Message) msg, this, (General) recipient);
        CommandService.getMessengers().add(messenger);
        messenger.run();
    }

    @Override
    public synchronized void onTransactionRecieved(Transaction<?> msg) throws NoSuchAlgorithmException {
        if (voted)
            return;

        if (transactionIsValid(msg) && !blockchain.contains(msg)) {
            Transaction<?> pendingTransaction = blockchain.getPendingTransaction(msg);
            if (pendingTransaction == null) {
                // add new msg to queue (memPool)
                pendingTransaction = msg;
                blockchain.getPendingTransactions().add(pendingTransaction);
            }
        }

        // reset timer
        endRoundTimer = 0f;
    }

    @Override
    protected synchronized void processPendingTransactions() {
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
        Decision decision;
        java.util.List<Decision> decisionList = new java.util.ArrayList<>();
        for (Transaction<?> t : blockchain.getLatestBlock().getTransactions()) {
            if (t != null)
                decisionList.add(((Message) t).getDecision());
        }

        if (decisionList.size() != 0) {
            // make decision based on majority
            // if num of decisions is the same for multiple decisions
            // take the one with lowest index
            List<Decision> mostCommons = Mathf.mostCommons(decisionList);
            Decision mostCommonDecision = mostCommons.get(0);
            for (var dec : mostCommons) {
                int ind1 = Decision.valueOf(dec.toString()).ordinal();
                int ind2 = Decision.valueOf(mostCommonDecision.toString()).ordinal();
                if (ind1 < ind2) {
                    mostCommonDecision = dec;
                }
            }
            decision = mostCommonDecision;
        }
        else {
            decision = getDecision();
        }

        if (traitor) {
            decision = Decision.randomDecision(decision);
        }
        return decision;
    }

    public synchronized void sendMyDecisionToAllGenerals(Decision decision) throws NoSuchAlgorithmException {
        // send my decision to every other general
        for (BlockchainNode general : getOtherNodes()) {
            if (traitor) {
                decision = Decision.randomDecision(decision);
            }
            Message msgForGeneral = new Message(decision, keyPair.getPublic(), general.getKeyPair().getPublic());
            msgForGeneral.signTransaction(keyPair);

            messagesToSend.add(new MessageDto(new Message(msgForGeneral), general));
        }

        // add your own decision
        blockchain.getPendingTransactions().add(new Message(decision, keyPair.getPublic(), keyPair.getPublic()));
    }

    public synchronized void resendTransactionToAllGenerals(Transaction<?> transaction)
            throws NoSuchAlgorithmException {
        // resend message to every other general
        for (BlockchainNode general : getOtherNodes()) {
            Message msg = (Message) transaction;
            messagesToSend.add(new MessageDto(msg, general));
        }
    }

    public void update() {
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
            } else {
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
                "   pending transactions " + getBlockchain().getPendingTransactions().size() + "='"
                + getBlockchain().getPendingTransactions() + "'\n" +
                "   blockchain=\n" + getBlockchain() + "\n" +
                "   decision='" + getDecision() + "'\n" +
                "}\n";
    }

    @Override
    public void draw(Graphics2D g2D) {
        int sizeX = 30;
        int sizeY = 30;
        int thickness = 10;

        int x = (int) (coords.getX() - sizeX / 2);
        int y = (int) (coords.getY() - sizeY / 2);

        // decision
        g2D.setPaint(Decision.getColor(getDecision()));
        g2D.fillOval(x - thickness / 2, y - thickness / 2, sizeX + thickness, sizeY + thickness);

        // general / traitor
        if (traitor)
            g2D.setPaint(Color.decode("#ff6e6e"));
        else
            g2D.setPaint(Color.decode("#49b2fc"));
        g2D.fillOval(x, y, sizeX, sizeY);

        // outline
        g2D.setPaint(Color.BLACK);
        g2D.drawOval(x, y, sizeX, sizeY);

        // name
        g2D.setPaint(Color.WHITE);
        g2D.drawString(getName(), x - 10, y - 10);

        if (processingPendingTransactions)
            g2D.drawString("Mining...", x - 10, y + 1.8f * sizeY);
        if (voted)
            g2D.drawString("End Sync", x - 10, y + 1.5f * sizeY);
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
