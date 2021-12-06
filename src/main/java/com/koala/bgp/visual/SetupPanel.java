package com.koala.bgp.visual;

import javax.swing.*;
import java.awt.*;

public class SetupPanel extends JPanel
{
    public final static int PANEL_SIZE_X = 300;

    public SetupPanel(int PANEL_SIZE_Y) 
    {
        this.setMinimumSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y)); 
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Settings"));

        this.add(Box.createVerticalStrut(10));

        // ======================================================
        // tworzenie dwoch elementow
        JLabel generalsLabel = new JLabel("Generals");
        JTextField generalsInput = new JTextField();
        generalsInput.setMaximumSize(new Dimension(300, 25));

        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel generalsPanel = new JPanel();
        BoxLayout generalsLayout = new BoxLayout(generalsPanel, BoxLayout.X_AXIS);
        generalsPanel.setLayout(generalsLayout);
        // rozmiar panelu
        generalsPanel.setMaximumSize(new Dimension(500, 25));
        // dodanie elementów
        generalsPanel.add(generalsLabel, BorderLayout.WEST);
        generalsPanel.add(Box.createHorizontalStrut(5)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        generalsPanel.add(generalsInput, BorderLayout.CENTER);
        // dodanie panelu generalow do glownego panelu
        this.add(generalsPanel);
        // ======================================================

        // w ten sam sposob:
        // traitorsInput
        // encryptionLevelInput
        // algorithmDropdown (do wyboru miedzy algorytmem standardowym i króla)
        // (moze dropdown do kontroli pozycji generałow - w kółku, losowo itd)
        // (moze toggle do wyswietlania nazw węzłów, bo czasami źle to wyglada gdy nazwy generalow sie zlewaja)
        // przyciski play, pause (i moze reset)...
        // ... i jeszcze jakas kontrola skalowania czasu (Time.timeScale)

        // w inputach bedzie trzeba pokazac w jakich przedzialach mozna podawac wartosci (poczatek funkcji main w ByzantineMain.java)
        // sprawdzac czy sa w tych przedzialach
        // jesli nie sa, to pokazac komunikat i nie startowac symulacji

        // ogolnie to zalezy nam na kontroli programu z poziomu okna (bez uzycia terminala)

        // no i pamietaj ze musze miec pełny dostep do wartosci
        // wiec rob duzo getterow albo cos xD
    }

    /* private void Play() {
        // tutaj powinna byc cala główna pętla z maina (ByznatineMain)
        // trzeba zrobic z niej funkcje z parametrami i tu wywolywac
        // no i najpierw sprawdzac czy symulacja juz nie trwa
    }
    private void Pause() {
        // Time.timeScale = 0
    }
    private void Stop() {

    } */
}
