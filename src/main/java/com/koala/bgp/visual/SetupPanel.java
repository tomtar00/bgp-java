package com.koala.bgp.visual;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.utils.Time;

import javax.swing.*;
import java.awt.*;

public class SetupPanel extends JPanel
{
    public final static int PANEL_SIZE_X = 300;
    private Thread renderThread;
    private static String genSystem = "PoissonDisc";
    private static String algorithm = "Lamport";
    public static boolean showDetails = false;
    private static int q = 1;

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

            int maxTraitors = algorithm == "Lamport" ? (value - 1) / 3 : (value - 1) / 4;
            for (int i = 0; i <= maxTraitors; i++) {
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
        JPanel generalsPanel = new JPanel();
        BoxLayout generalsLayout = new BoxLayout(generalsPanel, BoxLayout.X_AXIS);
        generalsPanel.setLayout(generalsLayout);
        generalsPanel.setMaximumSize(new Dimension(500, 40));
        generalsPanel.add(generalsLabel, BorderLayout.CENTER);
        generalsPanel.add(Box.createHorizontalStrut(10));
        generalsPanel.add(generalListComboBox, BorderLayout.CENTER);
        this.add(generalsPanel);
        // ======================================================



        // ==============================Traitors=================================
        JLabel traitorsLabel = new JLabel("Traitors");
        traitorsLabel.setSize(new Dimension(50, 25));
        JPanel traitorsPanel = new JPanel();
        BoxLayout traitorsLayout = new BoxLayout(traitorsPanel, BoxLayout.X_AXIS);
        traitorsPanel.setLayout(traitorsLayout);
        traitorsPanel.setMaximumSize(new Dimension(500, 60));
        traitorsPanel.add(traitorsLabel, BorderLayout.WEST);
        traitorsPanel.add(Box.createHorizontalStrut(10));
        traitorsPanel.add(traitorsListComboBox, BorderLayout.EAST);
        this.add(traitorsPanel);

        // ==============================encryptionLevelInput=================================
        JComboBox<Integer> encryptionLevelInputListComboBox = new JComboBox<Integer>();
        encryptionLevelInputListComboBox.setMaximumSize(new Dimension(200, 25));
        ((JLabel)encryptionLevelInputListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        for (int i = 1; i <= 5; i++) {
            encryptionLevelInputListComboBox.addItem(i);
        }

        JLabel encryptionLevelInputLabel = new JLabel("Encryption Level");
        encryptionLevelInputLabel.setMinimumSize(new Dimension(300, 100));

        JPanel encryptionLevelInputPanel = new JPanel();
        BoxLayout encryptionLevelInputLayout = new BoxLayout(encryptionLevelInputPanel, BoxLayout.X_AXIS);
        encryptionLevelInputPanel.setLayout(encryptionLevelInputLayout);
        encryptionLevelInputPanel.setMaximumSize(new Dimension(500, 60));
        encryptionLevelInputPanel.add(encryptionLevelInputLabel, BorderLayout.WEST);
        encryptionLevelInputPanel.add(Box.createHorizontalStrut(10));
        encryptionLevelInputPanel.add(encryptionLevelInputListComboBox, BorderLayout.EAST);
        this.add(encryptionLevelInputPanel);

        // ==============================Algorithm=================================
        JComboBox<String> algorithmDropdownListComboBox = new JComboBox<String>();
        algorithmDropdownListComboBox.setMaximumSize(new Dimension(300, 25));
        ((JLabel)algorithmDropdownListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        algorithmDropdownListComboBox.addItem("Lamport");
        algorithmDropdownListComboBox.addItem("King");
        algorithmDropdownListComboBox.addItem("q-Voter");

        JPanel qPanel = new JPanel();
        algorithmDropdownListComboBox.addActionListener(e->{
            algorithm = algorithmDropdownListComboBox.getSelectedItem().toString();

            int value = Integer.parseInt(generalListComboBox.getSelectedItem().toString());
            traitorsListComboBox.removeAllItems();

            int maxTraitors = algorithm == "Lamport" ? (value - 1) / 3 : (value - 1) / 4;
            for (int i = 0; i <= maxTraitors; i++) {
                traitorsListComboBox.addItem(i);
            }
            traitorsListComboBox.setSelectedIndex(traitorsListComboBox.getItemCount()-1);

            qPanel.setVisible(algorithm == "q-Voter");
        });

        JLabel algorithmDropdownInputLabel = new JLabel("Algorithm");
        algorithmDropdownInputLabel.setMinimumSize(new Dimension(300, 100));

        JPanel algorithmDropdownInputPanel = new JPanel();
        BoxLayout algorithmDropdownInputLayout = new BoxLayout(algorithmDropdownInputPanel, BoxLayout.X_AXIS);
        algorithmDropdownInputPanel.setLayout(algorithmDropdownInputLayout);
        algorithmDropdownInputPanel.setMaximumSize(new Dimension(500, 60));
        algorithmDropdownInputPanel.add(algorithmDropdownInputLabel, BorderLayout.WEST);
        algorithmDropdownInputPanel.add(Box.createHorizontalStrut(10));
        algorithmDropdownInputPanel.add(algorithmDropdownListComboBox, BorderLayout.EAST);
        this.add(algorithmDropdownInputPanel);

        // ==============================q-Voter q number=================================
        JComboBox<Integer> qComboBox = new JComboBox<Integer>();
        qComboBox.setMaximumSize(new Dimension(400, 25));
        ((JLabel)qComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        for (int i = 1; i <= 5; i++) {
            qComboBox.addItem(i);
        }

        qComboBox.addActionListener(e -> {
            q = Integer.parseInt(qComboBox.getSelectedItem().toString());
        });

        JLabel qInputLabel = new JLabel("q");
        qInputLabel.setMinimumSize(new Dimension(300, 100));

        BoxLayout qLayout = new BoxLayout(qPanel, BoxLayout.X_AXIS);
        qPanel.setLayout(qLayout);
        qPanel.setMaximumSize(new Dimension(500, 60));
        qPanel.add(qInputLabel, BorderLayout.WEST);
        qPanel.add(Box.createHorizontalStrut(10));
        qPanel.add(qComboBox, BorderLayout.EAST);
        qPanel.setVisible(false);
        this.add(qPanel);


        // ==============================Generation system=================================
        JComboBox<String> generationSystemListComboBox = new JComboBox<String>();
        generationSystemListComboBox.setMaximumSize(new Dimension(300, 25));
        ((JLabel)generationSystemListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        generationSystemListComboBox.addItem("PoissonDisc");
        generationSystemListComboBox.addItem("Random");
        generationSystemListComboBox.addItem("Circle");
        

        generationSystemListComboBox.addActionListener(e->{
            genSystem = generationSystemListComboBox.getSelectedItem().toString();
        });

        JLabel generationSystemInputLabel = new JLabel("Generation System");
        generationSystemInputLabel.setMinimumSize(new Dimension(300, 100));

        JPanel generationSystemInputPanel = new JPanel();
        BoxLayout generationSystemInputLayout = new BoxLayout(generationSystemInputPanel, BoxLayout.X_AXIS);
        generationSystemInputPanel.setLayout(generationSystemInputLayout);
        generationSystemInputPanel.setMaximumSize(new Dimension(500, 60));
        generationSystemInputPanel.add(generationSystemInputLabel, BorderLayout.WEST);
        generationSystemInputPanel.add(Box.createHorizontalStrut(10));
        generationSystemInputPanel.add(generationSystemListComboBox, BorderLayout.EAST);
        this.add(generationSystemInputPanel);

        // ==============================Time scale=================================
        JComboBox<Double> timeScaleListComboBox = new JComboBox<Double>();
        timeScaleListComboBox.setMaximumSize(new Dimension(400, 25));
        ((JLabel)timeScaleListComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        for (double i = 0.25; i <= 1; i+=0.25) {
            timeScaleListComboBox.addItem(i);
        }
        for (double i = 2; i <= 5; i+=1) {
            timeScaleListComboBox.addItem(i);
        }
        timeScaleListComboBox.addActionListener(e -> {
            Time.timeScale = Float.parseFloat(timeScaleListComboBox.getSelectedItem().toString());
        });

        timeScaleListComboBox.setSelectedIndex(3);
        JLabel timeScaleLabel = new JLabel("Time Scale");
        timeScaleLabel.setMinimumSize(new Dimension(300, 100));

        JPanel timeScalePanel = new JPanel();
        BoxLayout timeScaleLayout = new BoxLayout(timeScalePanel, BoxLayout.X_AXIS);
        timeScalePanel.setLayout(timeScaleLayout);
        timeScalePanel.setMaximumSize(new Dimension(500, 60));
        timeScalePanel.add(timeScaleLabel, BorderLayout.WEST);
        timeScalePanel.add(Box.createHorizontalStrut(10));
        timeScalePanel.add(timeScaleListComboBox, BorderLayout.EAST);
        this.add(timeScalePanel);

        // ==============================Show name =================================
        JLabel showNameLabel = new JLabel("Show names");
        timeScaleLabel.setMinimumSize(new Dimension(300, 100));
        JPanel showNamePanel = new JPanel();
        BoxLayout showNameLayout = new BoxLayout(showNamePanel, BoxLayout.X_AXIS);
        showNamePanel.setLayout(showNameLayout);
        showNamePanel.setMaximumSize(new Dimension(500, 60));

        JToggleButton showName = new JToggleButton("No");
        showName.setMaximumSize(new Dimension(400,30));

        showNamePanel.add(showNameLabel, BorderLayout.WEST);
        showNamePanel.add(Box.createHorizontalStrut(10));
        showNamePanel.add(showName, BorderLayout.EAST);
        this.add(showNamePanel);

        this.add(Box.createRigidArea(new Dimension(0,300)));

        // ==============================Start/Pause buttons=================================
        JPanel startButtonPanel = new JPanel();
        startButtonPanel.setMinimumSize(new Dimension(300, 100));
        BoxLayout startButtonLayout = new BoxLayout(startButtonPanel, BoxLayout.X_AXIS);
        startButtonPanel.setLayout(startButtonLayout);
        startButtonPanel.setSize(new Dimension(500, 100));

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

    public static String getGenSystem() {
        return genSystem;
    }
    public static String getAlgorithm() {
        return algorithm;
    }
    public static int getQ() {
        return q;
    }
}