package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.blockchain.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.Drawable;
import com.koala.bgp.visual.SetupPanel;

import java.awt.*;

public class General extends BlockchainNode implements Drawable
{
    private String name;
    private Decision decision;
    private Vector2 coords;

    private String algorithm;
    private boolean traitor = false;
    private boolean voted = false;

    // === KING
    private boolean king = false;
    private int numMajorityVotes = 0;
    private int subRound = 1;
    private final double KING_INTERVAL = 8;
    // ===

    // === VOTER
    private General[] group;
    private Long[] responseTimes;
    // ===

    private final double SEND_MESSAGE_INTERVAL = .05;
    private BlockingQueue<MessageDto> messagesToSend;
    private double sendMsgTimer = 0.0;

    // ====

    private int currentRound = 1;

    public General(String name, Vector2 coords, boolean traitor, String algorithm) throws NoSuchAlgorithmException {
        super(ByzantineMain.getEncLevel());
        this.name = name;
        this.coords = coords;
        this.traitor = traitor;
        this.algorithm = algorithm;

        this.decision = Decision.randomDecision(SetupPanel.getNumDecisions());
        this.group = new General[SetupPanel.getQ()];

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

    public int getCurrentRound() {
        return this.currentRound;
    }

    public boolean isKing() {
        return this.king;
    }
    public void setKing(boolean king) {
        this.king = king;
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
        if (voted && algorithm != "Voter" && algorithm != "q-Voter")
            return;

        if (transactionIsValid(msg) && !blockchain.contains(msg)) {

            Message recievedMsg = ((Message) msg);

            if (algorithm != "Voter" && algorithm != "q-Voter") {
                Transaction<?> pendingTransaction = blockchain.getPendingTransaction(msg);
                if (pendingTransaction == null) {
                    // add new msg to queue (memPool)
                    pendingTransaction = msg;
                    blockchain.getPendingTransactions().add(pendingTransaction);
                }
            } else {
                // only if response (not request)
                if (recievedMsg.getDecision() != null) {
                    Transaction<?> pendingTransaction = blockchain.getPendingTransaction(msg);
                    if (pendingTransaction == null) {
                        // add new msg to queue (memPool)
                        pendingTransaction = msg;
                        blockchain.getPendingTransactions().add(pendingTransaction);
                    }
                }
            }

            
            if (algorithm == "King") {
                if (subRound == 2) {
                    // handle king message
                    if (numMajorityVotes <= ByzantineMain.getNumOfGenerals() / 2 + ByzantineMain.getNumOfTraitors()) {
                        decision = recievedMsg.getDecision();
                    }
                }
            }
            else if (algorithm == "Voter" || algorithm == "q-Voter") {
                General sender = (General)CommandService.getNode(recievedMsg.getSenderPublicKey());
                if (recievedMsg.getDecision() == null) {
                    // recieved request
                    // (resend message)
                    if (traitor)
                        decision = Decision.randomDecision(decision, SetupPanel.getNumDecisions());
                    var msgForTarget = new Message(decision, keyPair.getPublic(), sender.getKeyPair().getPublic());
                    msgForTarget.setRoundIndex(recievedMsg.getRoundIndex());
                    msgForTarget.signTransaction(keyPair);
                    messagesToSend.add(new MessageDto(msgForTarget, sender));
                }
                else {
                    // recieved response
                    if (currentRound == 1) {
                        int gen_id = CommandService.getGenerals().indexOf(sender);
                        responseTimes[gen_id] = System.currentTimeMillis() - responseTimes[gen_id];
                    }
                    else {
                        if (algorithm == "Voter") {
                            decision = recievedMsg.getDecision();
                        }
                        else {
                            // q-Voter
                            if (blockchain.getPendingTransactions().size() == group.length) {
                                boolean allDecisionsAreSame = true;
                                Decision neighborsDecision = null;
                                for (var transaction : blockchain.getPendingTransactions()) {
                                    Message _msg = (Message)transaction;
                                    if (neighborsDecision == null)
                                        neighborsDecision = _msg.getDecision();
                                    else {
                                        if (!_msg.getDecision().equals(neighborsDecision)) {
                                            allDecisionsAreSame = false;
                                        }
                                    }
                                }
                                if (allDecisionsAreSame) {
                                    decision = neighborsDecision;
                                }
                                else {
                                    Random rand = new Random();
                                    float prob = rand.nextFloat();
                                    float prob_to_flip = .25f;
                                    if (prob < prob_to_flip) {
                                        decision = Decision.randomDecision(decision, SetupPanel.getNumDecisions());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected synchronized void processPendingTransactions(Blockchain.Verify verify) {
        if (!processingPendingTransactions && blockchain.getPendingTransactions().size() > 0) {
            processingPendingTransactions = true;
            new Thread(() -> {
                try {

                    // add block to the blockchain
                    super.processPendingTransactions(verify);

                    if (algorithm == "Lamport") {
                        // make decision based on majority of common decisions the latest block
                        Mathf.Tuple<Decision, Integer> tuple = makeDecision();
                        decision = tuple.x;
                        numMajorityVotes = tuple.y;
                    }
                    else if (algorithm == "King") {
                        if (subRound == 1) {
                            Mathf.Tuple<Decision, Integer> tuple = makeDecision();
                            decision = tuple.x;
                            numMajorityVotes = tuple.y;
                        }
                    }
                    else if (algorithm == "Voter" || algorithm == "q-Voter") {
                        if (currentRound == 1) {
                            // select q-sized group of the nearest neighbors
                            for (int i = 0; i < group.length; i++) {
                                int minIndex = (responseTimes[0] == null) ? 1 : 0;
                                for (int j = 0; j < responseTimes.length; j++) {
                                    if (responseTimes[j] == null)
                                        continue;
                                    if (responseTimes[j] < responseTimes[minIndex]) {
                                        minIndex = j;
                                    }
                                }

                                responseTimes[minIndex] = null;
                                group[i] = CommandService.getGenerals().get(minIndex);
                            }
                        }
                    }

                    if (algorithm == "Lamport") {
                        currentRound++;
                    }
                    else if (algorithm == "King") {
                        if (subRound == 1)
                            subRound = 2;
                        else if (subRound == 2) {
                            subRound = 1;
                            currentRound++;
                        }
                    }
                    else if (algorithm == "Voter" || algorithm == "q-Voter") {
                        currentRound++;
                    }

                    if (currentRound > getNumOfRounds()) {
                        // end decision phase
                        SimpleLogger.print(getName() + " voted to end decision phase");
                        CommandService.voteToEndSync();
                        voted = true;
                    } else {
                        // start next round
                        try {

                            if (algorithm == "Lamport") {
                                sendMyDecisionToAllGenerals(decision, currentRound);
                            }
                            else if (algorithm == "King") {
                                if (subRound == 1) {
                                    // first subround
                                    sendMyDecisionToAllGenerals(decision, currentRound);
                                }
                                else if (subRound == 2) {
                                    // second subround
                                    this.setKing(currentRound - 1 == CommandService.getGenerals().indexOf(this));
                                    if (this.isKing()) {
                                        CommandService.setKing(this);
                                        SimpleLogger.print("In round " + currentRound + " " + getName() + " is king");
                                        sendMyDecisionToAllGenerals(decision, currentRound);
                                    }
                                }
                            }
                            else if (algorithm == "Voter") {
                                // select random general from group
                                Random rand = new Random();
                                int ind = rand.nextInt(group.length);
                                General target = group[ind];
                                // and send him a decision request
                                var msgForTarget = new Message(null, keyPair.getPublic(), target.getKeyPair().getPublic());
                                msgForTarget.setRoundIndex(currentRound);
                                msgForTarget.signTransaction(keyPair);
                                messagesToSend.add(new MessageDto(msgForTarget, target));
                            }
                            else if (algorithm == "q-Voter") {
                                for (var neighbor: group) {
                                    // send neighbor decisin request 
                                    var msgForNeighbor = new Message(null, keyPair.getPublic(), neighbor.getKeyPair().getPublic());
                                    msgForNeighbor.setRoundIndex(currentRound);
                                    msgForNeighbor.signTransaction(keyPair);
                                    messagesToSend.add(new MessageDto(msgForNeighbor, neighbor));
                                }
                            }
                            else {
                                SimpleLogger.logWarning("Wrong subround value on new (sub)round: " + subRound);
                            }

                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                            SimpleLogger.pressAnyKeyToContinue();
                        }
                    }

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    SimpleLogger.pressAnyKeyToContinue();
                }
            }).start();
        }
    }

    private synchronized boolean shouldEndRound(double msgTimer) {

        int count = 0;
        for (Transaction<?> t : blockchain.getPendingTransactions()) {
            if (((Message) t).getRoundIndex() == currentRound) {
                count++;
            }
        }

        if (algorithm == "Lamport") {
            return count == ByzantineMain.getNumOfGenerals();
        }
        else if (algorithm == "King") {
            if (subRound == 1) {
                return count == ByzantineMain.getNumOfGenerals();
            }
            else if (subRound == 2) {
                if (!isKing())
                    return count == 1 && CommandService.getKing() == null;
                else {
                    if (msgTimer > KING_INTERVAL) {
                        CommandService.setKing(null);
                        return true;
                    }
                    else return false;
                }
            }
            else {
                SimpleLogger.logWarning("Wrong subround value: " + subRound);
                return true;
            }
        }
        else if (algorithm == "Voter" || algorithm == "q-Voter") {
            if (currentRound == 1)
                return count == ByzantineMain.getNumOfGenerals() - 1;
            else {
                if (algorithm == "Voter")
                    return count == 1;
                else
                    return count == group.length;
            }
            
        }
        else {
            return true;
        }
    }

    private synchronized int getNumOfRounds() {
        if (algorithm == "Lamport")
            return ByzantineMain.getNumOfTraitors() + 1;
        else if (algorithm == "King")
            return ByzantineMain.getNumOfTraitors() + 1;
        else if (algorithm == "Voter" || algorithm == "q-Voter") {
            return ByzantineMain.getNumOfGenerals();
        }
        else {
            SimpleLogger.logWarning("No algorithm specified!");
            return 0;
        }
    }

    public synchronized Mathf.Tuple<Decision, Integer> makeDecision() {
        Decision decision;
        int majority = 0;
        java.util.List<Decision> decisionList = new java.util.ArrayList<>();
        for (Transaction<?> t : blockchain.getLatestBlock().getTransactions()) {
            if (t != null)
                decisionList.add(((Message) t).getDecision());
        }

        if (decisionList.size() != 0) {
            // make decision based on majority
            // if num of decisions is the same for multiple decisions
            // take the one with lowest index
            var mostCommons = Mathf.mostCommons(decisionList);
            majority = mostCommons.y;
            Decision mostCommonDecision = mostCommons.x.get(0);
            for (var dec : mostCommons.x) {
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
            decision = Decision.randomDecision(decision, SetupPanel.getNumDecisions());
        }
        return new Mathf.Tuple<Decision, Integer>(decision, majority);
    }

    public synchronized void sendMyDecisionToAllGenerals(Decision decision, int currentRound) throws NoSuchAlgorithmException {

        if (algorithm == "Voter" || algorithm == "q-Voter") {
            this.responseTimes = new Long[CommandService.getGenerals().size()];
        }

        // send my decision to every other general
        for (BlockchainNode general : getOtherNodes()) {
            if (traitor) {
                decision = Decision.randomDecision(decision, SetupPanel.getNumDecisions());
            }
            if (algorithm == "Voter" || algorithm == "q-Voter") {
                responseTimes[CommandService.getGenerals().indexOf((General)general)] = System.currentTimeMillis();
                decision = null;
            }
            Message msgForGeneral;
            msgForGeneral = new Message(decision, keyPair.getPublic(), general.getKeyPair().getPublic());
            msgForGeneral.setRoundIndex(currentRound);
            msgForGeneral.signTransaction(keyPair);

            messagesToSend.add(new MessageDto(msgForGeneral, general));
        }

        if (algorithm != "Voter" || algorithm == "q-Voter") {
            // add your own decision
            Message msgToSelf = new Message(decision, keyPair.getPublic(), keyPair.getPublic());
            msgToSelf.setRoundIndex(currentRound);
            blockchain.getPendingTransactions().add(msgToSelf);
        }
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
        sendMsgTimer += Time.getDeltaTime();

        if (sendMsgTimer > SEND_MESSAGE_INTERVAL && messagesToSend.size() > 0) {
            MessageDto msgToSend = messagesToSend.poll();
            sendTransaction(msgToSend.getMsg(), msgToSend.getRecipient());
            sendMsgTimer = 0.0;
        }

        if (algorithm == "Voter" || algorithm == "q-Voter") {
            if (shouldEndRound(sendMsgTimer)) {
                processPendingTransactions((t) -> {
                    return ((Message) t).getRoundIndex() == currentRound;
                });
            }
        }
        else {
            if (!voted && shouldEndRound(sendMsgTimer)) {
                processPendingTransactions((t) -> {
                    return ((Message) t).getRoundIndex() == currentRound;
                });
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
        int sizeX = isKing() ? 60 : 30;
        int sizeY = isKing() ? 60 : 30;
        int thickness = 7;

        int x = (int) (coords.getX() - sizeX / 2);
        int y = (int) (coords.getY() - sizeY / 2);

        g2D.setStroke(new BasicStroke(thickness));

        // general / traitor
        if (traitor)
            g2D.setPaint(Color.decode("#ff6e6e"));
        else
            g2D.setPaint(Color.decode("#49b2fc"));
        g2D.fillOval(x, y, sizeX, sizeY);

        // outline (decision)
        g2D.setPaint(Decision.getColor(getDecision()));
        g2D.drawOval(x, y, sizeX, sizeY);
        if (SetupPanel.showDetails) {
            // name
            g2D.setPaint(traitor ? Color.RED : Color.WHITE);
            g2D.drawString(getName() + (isKing() ? "(KING)" : "") + " R:" + currentRound + " Pen:"
                    + blockchain.getPendingTransactions().size(), x - 8, y - 8);

            if (processingPendingTransactions)
                g2D.drawString("Mining...", x - 8, y + 1.8f * sizeY);
            if (voted)
                g2D.drawString("End Sync", x - 8, y + 1.5f * sizeY);
        }
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
