package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.koala.bgp.utils.Mathf;
import com.koala.bgp.utils.SimpleLogger;
import com.koala.bgp.utils.Time;
import com.koala.bgp.utils.Vector2;
import com.koala.bgp.visual.Drawable;
import com.koala.bgp.visual.SetupPanel;

import java.awt.Color;
import java.awt.Graphics2D;

public class Messenger implements Drawable
{
    private final static int MSG_INTERRUPT_PRECENT = 0;
    private final static int RANDOM_CIRCLE_OFFSET_RADIUS = 15;

    private Message message;
    private General recipient;
    private Vector2 startCoords;
    private Vector2 endCoords;

    private boolean spy = false;
    private boolean running = false;

    // ====

    private float timeCounter = 0;
    private float timeToDeliver = 0;

    public static void setMessCount(int messCount) {
        Messenger.messCount = messCount;
    }

    // delete
    private static int messCount = 0;

    public Messenger(Message message, General sender, General recipient) {
        this.message = message;
        Vector2 random_offset = Mathf.randomOneUnitCircle().multiply(RANDOM_CIRCLE_OFFSET_RADIUS);
        this.recipient = recipient;
        this.startCoords = sender.getCoords().add(random_offset);
        this.endCoords = recipient.getCoords().add(random_offset);
        messCount++;
    }

    public Message getMessage() {
        return this.message;
    }
    public Vector2 getStartCoords() {
        return this.startCoords;
    }
    public Vector2 getEndCoords() {
        return this.endCoords;
    }
    public boolean isRunning() {
        return this.running;
    }
    public boolean isSpy() {
        return spy;
    }
    public static int getMessengersCount() {
        return messCount;
    }

    public void run() 
    {
        running = true;

        double dst = Math.sqrt(
            Math.pow(startCoords.getX() - endCoords.getX(), 2) + 
            Math.pow(startCoords.getY() - endCoords.getY(), 2)
        );

        // spies (% for being a spy)
        Random rand = new Random();
        if (rand.nextInt(100) < MSG_INTERRUPT_PRECENT) {
            message = randomizeSpy(message);
        }

        // in seconds
        float timeOffset = SetupPanel.getAlgorithm() == "Voter" ? 2f : rand.nextFloat() * 4f;
        timeToDeliver = (float)dst / 100f + timeOffset;
    }

    private Message randomizeSpy(Message message) {
        message = new Message(Decision.randomDecision(message.getDecision(), SetupPanel.getNumDecisions()),
                              message.getSenderPublicKey(), 
                              message.getRecipientPublicKey(),
                              recipient.getBoltzmann_authority()
                            );           
        spy = true;
        return message;
    }

    private void onMessageDelivered()
    {
        try {
            if (recipient != null) {
                recipient.onTransactionRecieved(message);

                // remove an useless messenger
                CommandService.getMessengers().remove(this);
            }
            else
                SimpleLogger.print("No general found with given public key");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }
    }
 
    public void update() 
    {
        if (running)
        {
            timeCounter += Time.getDeltaTime();
            if (timeCounter > timeToDeliver) {
                new Thread(() -> {
                    onMessageDelivered();
                }).start();
                running = false;
                timeCounter = 0;
            }      
        }  
    }

    @Override
    public void draw(Graphics2D g2D) {
        
        float progress = timeCounter / timeToDeliver;
        Vector2 start = new Vector2( startCoords.getX(), startCoords.getY() );
        Vector2 end = new Vector2( endCoords.getX(), endCoords.getY() );
        Vector2 resultCoords = Mathf.lerp(start, end, progress);
        Color color = Color.GRAY;
        if (spy)
            color = Color.RED;
        g2D.setPaint(color);
        g2D.drawLine((int)startCoords.getX(), (int)startCoords.getY(), (int)resultCoords.getX(), (int)resultCoords.getY());
    }
}
