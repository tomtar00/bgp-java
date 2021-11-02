package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.koala.bgp.utils.Mathf;
import com.koala.bgp.utils.SimpleLogger;
import com.koala.bgp.utils.Time;
import com.koala.bgp.utils.Vector2;
import com.koala.bgp.visual.Drawable;

import java.awt.Color;
import java.awt.Graphics2D;

public class Messenger implements Drawable
{
    private final static int MSG_INTERRUPT_PRECENT = 0;
    private final static int RANDOM_CIRCLE_OFFSET_RADIUS = 15;

    private Message message;
    private Vector2 startCoords;
    private Vector2 endCoords;

    private boolean spy = false;
    private boolean running = false;

    // ====

    private float timeCounter = 0;
    private float timeToDeliver = 0;
    private long msgSendRecord = 0; 

    public Messenger(Message message) {
        this.message = message;
        Vector2 random_offset = Mathf.randomOneUnitCircle().multiply(RANDOM_CIRCLE_OFFSET_RADIUS);
        General sender = (General)message.getSender();
        General recipient = (General)message.getRecipient();
        this.startCoords = sender.getCoords().add(random_offset);
        this.endCoords = recipient.getCoords().add(random_offset);
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
        timeToDeliver = (float)dst / 100f + rand.nextFloat() * 4;
        msgSendRecord = System.currentTimeMillis();
        SimpleLogger.print("Message (id: " + message.getId() + ") will be delivered in " + timeToDeliver + " seconds...");
    }

    private Message randomizeSpy(Message message) {
        
        int nonce = message.getNonce();
        message = new Message(Decision.randomDecision(message.getDecision()), message.getSender(), message.getRecipient());           
        message.setNonce(nonce);
        spy = true;
        
        return message;
    }

    private void onMessageDelivered()
    {
        long msgDeliveredRecord = System.currentTimeMillis();
        SimpleLogger.print("Message (id: " + message.getId() + ") delievered. Recorded time: " +
             (msgDeliveredRecord - msgSendRecord) / 1000f + " seconds");

        try {
            General recipient = (General)message.getRecipient();
            recipient.onMessageRecieved(this);
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
