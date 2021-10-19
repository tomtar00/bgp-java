package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.koala.bgp.utils.Mathf;
import com.koala.bgp.utils.SimpleLogger;
import com.koala.bgp.utils.Time;
import com.koala.bgp.visual.Drawable;

import java.awt.Color;
import java.awt.Graphics2D;

public class Messenger implements Drawable
{
    private final int MSG_INTERRUPT_PRECENT = 15;

    private Message message;
    private General recipient;

    private Coords startCoords;
    private Coords endCoords;

    private boolean isSpy = false;

    // ====

    private float timeCounter = 0;
    private float timeToDeliver = 0;
    private long msgSendRecord = 0;
    private boolean running = false;

    public Messenger(Message message, General recipient, Coords startCoords, Coords endCoords) {
        this.message = message;
        this.recipient = recipient;
        this.startCoords = startCoords;
        this.endCoords = endCoords;
    }

    public Message getMessage() {
        return this.message;
    }
    public General getRecipient() {
        return this.recipient;
    }
    public Coords getStartCoords() {
        return this.startCoords;
    }
    public Coords getEndCoords() {
        return this.endCoords;
    }
    public boolean isRunning() {
        return this.running;
    }
    public boolean isSpy() {
        return isSpy;
    }

    public void run() 
    {
        running = true;

        double dst = Math.sqrt(
            Math.pow(startCoords.getX() - endCoords.getX(), 2) + 
            Math.pow(startCoords.getY() - endCoords.getY(), 2)
        );

        // spies (% for being a spy)
        message = randomizeSpy(message);

        // in seconds
        Random rand = new Random();
        timeToDeliver = (float)dst / 100f + rand.nextFloat() * 4;
        msgSendRecord = System.currentTimeMillis();
        SimpleLogger.print("Message (id: " + message.getId() + ") will be delivered in " + timeToDeliver + " seconds...");
    }

    private Message randomizeSpy(Message message) {
        Random rand = new Random();
        if (rand.nextInt(100) < MSG_INTERRUPT_PRECENT) {
            int nonce = message.getNonce();
            message = new Message("Retreat", Decision.RETREAT);           
            message.setNonce(nonce);
            isSpy = true;
        }
        return message;
    }

    private void onMessageDelivered()
    {
        long msgDeliveredRecord = System.currentTimeMillis();
        SimpleLogger.print("Message (id: " + message.getId() + ") delievered. Recorded time: " +
             (msgDeliveredRecord - msgSendRecord) / 1000f + " seconds");

        try {
            recipient.onMessageRecieved(this);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
 
    public void update() 
    {
        if (running)
        {
            timeCounter += Time.getDeltaTime();
            if (timeCounter > timeToDeliver) {
                onMessageDelivered();
                running = false;
                timeCounter = 0;
            }      
        }  
    }

    @Override
    public void draw(Graphics2D g2D) {
        
        float progress = timeCounter / timeToDeliver;
        int[] start = new int[] { startCoords.getX(), startCoords.getY() };
        int[] end = new int[] { endCoords.getX(), endCoords.getY() };
        int[] resultCoords = Mathf.Lerp(start, end, progress);
        Color color = Color.GRAY;
        if (isSpy)
            color = Color.RED;
        g2D.setPaint(color);
        g2D.drawLine(startCoords.getX(), startCoords.getY(), resultCoords[0], resultCoords[1]);
        
    }
}
