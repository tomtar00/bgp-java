package com.koala.bgp.visual;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.utils.Time;

import javax.swing.*;
import java.awt.*;

public class SetupPanel extends JPanel
{
    public final static int PANEL_SIZE_X = 300;
    private Thread renderThread;
    public static String ChooseAlgorithm = "Random";
    public static boolean showDetails = false;

    public SetupPanel(int PANEL_SIZE_Y) 
    {

        //==================================Initialization==================================
        Integer[] intArray = new Integer[150-2];
        for (int i = 0; i < 150-2; i++) {
            intArray[i] = i+3;
        }

        JComboBox<Integer> generalListComboBox = new JComboBox<Integer>(intArray);
        generalListComboBox.setMaximumSize(new Dimension(800, 25));
        ((JLabel)generalListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        JComboBox<Integer> traitorsListComboBox = new JComboBox<Integer>();
        traitorsListComboBox.addItem(0);
        ((JLabel)traitorsListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        traitorsListComboBox.setMaximumSize(new Dimension(800, 25));
        generalListComboBox.addActionListener(event -> {

            int value = Integer.parseInt(generalListComboBox.getSelectedItem().toString());
            traitorsListComboBox.removeAllItems();

            for (int i = 0; i <= (value - 1) / 3; i++) {
                traitorsListComboBox.addItem(i);
            }
            traitorsListComboBox.setSelectedIndex(traitorsListComboBox.getItemCount()-1);
        });

        //===============================Settings=================================
        this.setMinimumSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y)); 
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Settings"));

        this.add(Box.createVerticalStrut(10));

        // ==============================Generals=================================

        JLabel generalsLabel = new JLabel("Generals");


        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel generalsPanel = new JPanel();
        BoxLayout generalsLayout = new BoxLayout(generalsPanel, BoxLayout.X_AXIS);
        generalsPanel.setLayout(generalsLayout);
        // rozmiar panelu
        generalsPanel.setMaximumSize(new Dimension(500, 40));

        // dodanie elementów
        generalsPanel.add(generalsLabel, BorderLayout.CENTER);
        generalsPanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        //generalsPanel.add(generalsInput, BorderLayout.CENTER);
        generalsPanel.add(generalListComboBox, BorderLayout.CENTER);
        // dodanie panelu generalow do glownego panelu
        this.add(generalsPanel);
        // ======================================================



        // ==============================Traitors=================================
        JLabel traitorsLabel = new JLabel("Traitors");
        traitorsLabel.setSize(new Dimension(50, 25));
        //traitorsInput.setMaximumSize(new Dimension(300, 25));
        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel traitorsPanel = new JPanel();
        BoxLayout traitorsLayout = new BoxLayout(traitorsPanel, BoxLayout.X_AXIS);
        traitorsPanel.setLayout(traitorsLayout);
        // rozmiar panelu
        traitorsPanel.setMaximumSize(new Dimension(500, 60));
        // dodanie elementów
        traitorsPanel.add(traitorsLabel, BorderLayout.WEST);
        traitorsPanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        traitorsPanel.add(traitorsListComboBox, BorderLayout.EAST);
        // dodanie panelu generalow do glownego panelu
        this.add(traitorsPanel);

        // ==============================encryptionLevelInput=================================
        // encryptionLevelInput
        JComboBox<Integer> encryptionLevelInputListComboBox = new JComboBox<Integer>();
        encryptionLevelInputListComboBox.setMaximumSize(new Dimension(200, 25));
        ((JLabel)encryptionLevelInputListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        for (int i = 1; i <= 5; i++) {
            encryptionLevelInputListComboBox.addItem(i);
        }

        JLabel encryptionLevelInputLabel = new JLabel("Encryption Level");
        encryptionLevelInputLabel.setMinimumSize(new Dimension(300, 100));

        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel encryptionLevelInputPanel = new JPanel();
        BoxLayout encryptionLevelInputLayout = new BoxLayout(encryptionLevelInputPanel, BoxLayout.X_AXIS);
        encryptionLevelInputPanel.setLayout(encryptionLevelInputLayout);
        // rozmiar panelu
        encryptionLevelInputPanel.setMaximumSize(new Dimension(500, 60));
        // dodanie elementów
        encryptionLevelInputPanel.add(encryptionLevelInputLabel, BorderLayout.WEST);
        encryptionLevelInputPanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        encryptionLevelInputPanel.add(encryptionLevelInputListComboBox, BorderLayout.EAST);
        // dodanie panelu generalow do glownego panelu
        this.add(encryptionLevelInputPanel);

        // ==============================Algorithm=================================
        // algorithmDropdown (do wyboru miedzy algorytmem standardowym i króla)
        JComboBox<String> algorithmDropdownListComboBox = new JComboBox<String>();
        algorithmDropdownListComboBox.setMaximumSize(new Dimension(300, 25));
        ((JLabel)algorithmDropdownListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        algorithmDropdownListComboBox.addItem("Standard algorithm");
        algorithmDropdownListComboBox.addItem("King algorithm");


        JLabel algorithmDropdownInputLabel = new JLabel("Algorithm");
        algorithmDropdownInputLabel.setMinimumSize(new Dimension(300, 100));

        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel algorithmDropdownInputPanel = new JPanel();
        BoxLayout algorithmDropdownInputLayout = new BoxLayout(algorithmDropdownInputPanel, BoxLayout.X_AXIS);
        algorithmDropdownInputPanel.setLayout(algorithmDropdownInputLayout);
        // rozmiar panelu
        algorithmDropdownInputPanel.setMaximumSize(new Dimension(500, 60));
        // dodanie elementów
        algorithmDropdownInputPanel.add(algorithmDropdownInputLabel, BorderLayout.WEST);
        algorithmDropdownInputPanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        algorithmDropdownInputPanel.add(algorithmDropdownListComboBox, BorderLayout.EAST);
        // dodanie panelu generalow do glownego panelu
        this.add(algorithmDropdownInputPanel);


        // ==============================Generation system=================================

        // (moze dropdown do kontroli pozycji generałow - w kółku, losowo itd) generation system

        JComboBox<String> generationSystemListComboBox = new JComboBox<String>();
        generationSystemListComboBox.setMaximumSize(new Dimension(300, 25));
        ((JLabel)generationSystemListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);


        generationSystemListComboBox.addItem("Random");
        generationSystemListComboBox.addItem("Circle");

        generationSystemListComboBox.addActionListener(e->{
            ChooseAlgorithm = generationSystemListComboBox.getSelectedItem().toString();
        });

        JLabel generationSystemInputLabel = new JLabel("Generation System");
        generationSystemInputLabel.setMinimumSize(new Dimension(300, 100));

        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel generationSystemInputPanel = new JPanel();
        BoxLayout generationSystemInputLayout = new BoxLayout(generationSystemInputPanel, BoxLayout.X_AXIS);
        generationSystemInputPanel.setLayout(generationSystemInputLayout);
        // rozmiar panelu
        generationSystemInputPanel.setMaximumSize(new Dimension(500, 60));
        // dodanie elementów
        generationSystemInputPanel.add(generationSystemInputLabel, BorderLayout.WEST);
        generationSystemInputPanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        generationSystemInputPanel.add(generationSystemListComboBox, BorderLayout.EAST);
        // dodanie panelu generalow do glownego panelu
        this.add(generationSystemInputPanel);


        // ... i jeszcze jakas kontrola skalowania czasu (Time.timeScale)
        // timeScale
        JComboBox<Double> timeScaleListComboBox = new JComboBox<Double>();
        timeScaleListComboBox.setMaximumSize(new Dimension(400, 25));
        ((JLabel)timeScaleListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);


        for (double i = 0.25; i <= 2; i+=0.25) {
            timeScaleListComboBox.addItem(i);
        }
        timeScaleListComboBox.addActionListener(e -> {
            Time.timeScale = Float.parseFloat(timeScaleListComboBox.getSelectedItem().toString());
        });

        timeScaleListComboBox.setSelectedIndex(3);
        JLabel timeScaleLabel = new JLabel("Time Scale");
        timeScaleLabel.setMinimumSize(new Dimension(300, 100));

        // tworzenie panelu, ktory pomiesci te dwa elementy
        JPanel timeScalePanel = new JPanel();
        BoxLayout timeScaleLayout = new BoxLayout(timeScalePanel, BoxLayout.X_AXIS);
        timeScalePanel.setLayout(timeScaleLayout);
        // rozmiar panelu
        timeScalePanel.setMaximumSize(new Dimension(500, 60));
        // dodanie elementów
        timeScalePanel.add(timeScaleLabel, BorderLayout.WEST);
        timeScalePanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        timeScalePanel.add(timeScaleListComboBox, BorderLayout.EAST);
        // dodanie panelu generalow do glownego panelu
        this.add(timeScalePanel);


        // (moze toggle do wyswietlania nazw węzłów, bo czasami źle to wyglada gdy nazwy generalow sie zlewaja)
        JLabel showNameLabel = new JLabel("Show more information?");
        timeScaleLabel.setMinimumSize(new Dimension(300, 100));
        JPanel showNamePanel = new JPanel();
        BoxLayout showNameLayout = new BoxLayout(showNamePanel, BoxLayout.X_AXIS);
        showNamePanel.setLayout(showNameLayout);

        showNamePanel.setMaximumSize(new Dimension(500, 60));


        JToggleButton showName = new JToggleButton("No");

        showName.setMaximumSize(new Dimension(400,30));

        showNamePanel.add(showNameLabel, BorderLayout.WEST);
        showNamePanel.add(Box.createHorizontalStrut(10)); // niewidzialny separator (dla pionu: Box.createVerticalStrut(...))
        showNamePanel.add(showName, BorderLayout.EAST);
        this.add(showNamePanel);



        this.add(Box.createRigidArea(new Dimension(0,300)));

        JPanel startButtonPanel = new JPanel();
        startButtonPanel.setMinimumSize(new Dimension(300, 100));
        BoxLayout startButtonLayout = new BoxLayout(startButtonPanel, BoxLayout.X_AXIS);
        startButtonPanel.setLayout(startButtonLayout);
        startButtonPanel.setSize(new Dimension(500, 100));

        //Przycisk startu
        Button startButton = new Button("START");
        startButton.setBackground(Color.decode("#8ced9c"));
        startButton.setMaximumSize(new Dimension(500, 50));
        startButtonPanel.add(startButton);
        this.add(startButtonPanel);
        this.add(Box.createRigidArea(new Dimension(0,10)));

        Button pauseButton = new Button("PAUSE");
        pauseButton.setBackground(Color.decode("#8ced9c"));
        pauseButton.setMaximumSize(new Dimension(500, 50));
        this.add(pauseButton);

        //===========================listeners==================================
        pauseButton.addActionListener(e -> {
            if(Time.timeScale!=0 ) {
                Time.timeScale = 0;
                pauseButton.setLabel("Unpause");
            }
            else{
                Time.timeScale = Float.parseFloat(timeScaleListComboBox.getSelectedItem().toString());
                pauseButton.setLabel("Pause");
            }


        });

        
        startButton.addActionListener(e -> {
            int generalChoose = Integer.parseInt(generalListComboBox.getSelectedItem().toString());
            int traitorsChoose = Integer.parseInt(traitorsListComboBox.getSelectedItem().toString());
            int levelChoose = Integer.parseInt(encryptionLevelInputListComboBox.getSelectedItem().toString());

            ByzantineMain.interruptRenderThread();
            while(ByzantineMain.isRendering());

            BattlePanel.ResetTime();

            renderThread = new Thread(()->{
                ByzantineMain.Rendering(generalChoose,traitorsChoose,levelChoose);
                startButton.setLabel("Start");
            });

            startButton.setLabel("Reset");
            pauseButton.setLabel("Pause");
            renderThread.start();
        });
        showName.addActionListener(r->{
            if(showDetails)
            {
                showName.setText("No");
                showDetails = false;
            }
            else {
                showName.setText("Yes");
                showDetails = true;
            }
        });
    }

    public String getChooseAlgorithm() {
        return ChooseAlgorithm;
    }
}