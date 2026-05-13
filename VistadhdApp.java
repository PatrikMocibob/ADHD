import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class VistadhdApp extends JFrame {
    private boolean isDyslexiaMode = false;
    private int currentRound = 0;
    private int correctAnswers = 0;
    private String targetLetter = "";
    private final String[] possibleLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};

    // Za igru memorije
    private ArrayList<Integer> colorSequence = new ArrayList<>();
    private ArrayList<Integer> playerSequence = new ArrayList<>();
    private JButton[] colorButtons = new JButton[3];
    private final Color[] colors = {Color.RED, Color.BLUE, Color.GREEN};

    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    private final Color SOFT_WHITE = new Color(250, 250, 250);
    private final Color DYSLEXIA_BG = new Color(255, 255, 200); 
    
    public VistadhdApp() {
        setTitle("Vista ADHD Edukator");
        setSize(750, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        buildApp();
        
        add(mainPanel);
        cardLayout.show(mainPanel, "MENU");
    }

    private void buildApp() {
        mainPanel.removeAll();
        mainPanel.add(createMenuScreen(), "MENU");
        mainPanel.add(createGame1(), "GAME1"); 
        mainPanel.add(createGame2(), "GAME2"); 
        mainPanel.add(createGame3(), "GAME3"); 
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private Font getAppFont(int size, boolean bold) {
        if (isDyslexiaMode) {
            return new Font("Verdana", bold ? Font.BOLD : Font.PLAIN, size + 4);
        }
        return new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setFont(getAppFont(18, true));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- IZBORNIK ---
    private JPanel createMenuScreen() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));
        panel.setBackground(isDyslexiaMode ? DYSLEXIA_BG : SOFT_WHITE);

        JLabel title = new JLabel("Odaberi igru", SwingConstants.CENTER);
        title.setFont(getAppFont(28, true));
        panel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        centerPanel.setOpaque(false);

        JButton btnG1 = new JButton("1. Pronađi slovo 🔍");
        JButton btnG2 = new JButton("2. Brzi klik ⭐");
        JButton btnG3 = new JButton("3. Zapamti boje 🧠");
        
        styleButton(btnG1, new Color(200, 220, 255));
        styleButton(btnG2, new Color(255, 230, 200));
        styleButton(btnG3, new Color(200, 255, 200));

        btnG1.addActionListener(e -> cardLayout.show(mainPanel, "GAME1"));
        btnG2.addActionListener(e -> cardLayout.show(mainPanel, "GAME2"));
        btnG3.addActionListener(e -> cardLayout.show(mainPanel, "GAME3"));

        centerPanel.add(btnG1); centerPanel.add(btnG2); centerPanel.add(btnG3);
        panel.add(centerPanel, BorderLayout.CENTER);

        JButton btnDyslexia = new JButton(isDyslexiaMode ? "ISKLJUČI MOD ZA DISLEKSIJU" : "UKLJUČI MOD ZA DISLEKSIJU");
        styleButton(btnDyslexia, Color.LIGHT_GRAY);
        btnDyslexia.addActionListener(e -> {
            isDyslexiaMode = !isDyslexiaMode;
            buildApp();
            cardLayout.show(mainPanel, "MENU");
        });
        panel.add(btnDyslexia, BorderLayout.SOUTH);

        return panel;
    }

    // --- IGRA 1: SLOVA ---
    private JPanel createGame1() {
        JPanel panel = new JPanel(new BorderLayout(10, 30));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(isDyslexiaMode ? DYSLEXIA_BG : SOFT_WHITE);
        
        JLabel instruction = new JLabel("", SwingConstants.CENTER);
        instruction.setFont(getAppFont(22, true));
        
        JPanel grid = new JPanel(new GridLayout(2, 2, 30, 30));
        grid.setOpaque(false);

        Runnable setupRound = new Runnable() {
            public void run() {
                if (currentRound >= 5) { 
                    showFinalResult(correctAnswers, 5); 
                    return; 
                }
                Random r = new Random();
                targetLetter = possibleLetters[r.nextInt(possibleLetters.length)];
                instruction.setText("Runda " + (currentRound + 1) + "/5: Klikni slovo " + targetLetter);
                grid.removeAll();
                ArrayList<String> options = new ArrayList<>();
                options.add(targetLetter);
                while(options.size() < 4) {
                    String rand = possibleLetters[r.nextInt(possibleLetters.length)];
                    if(!options.contains(rand)) options.add(rand);
                }
                Collections.shuffle(options);
                for (String s : options) {
                    JButton btn = new JButton(s);
                    btn.setFont(new Font("Monospaced", Font.BOLD, 70));
                    styleButton(btn, Color.WHITE);
                    btn.addActionListener(e -> {
                        if (s.equals(targetLetter)) { correctAnswers++; }
                        currentRound++; 
                        this.run();
                    });
                    grid.add(btn);
                }
                grid.revalidate(); grid.repaint();
            }
        };
        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) { currentRound = 0; correctAnswers = 0; setupRound.run(); }
        });
        panel.add(instruction, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    // --- IGRA 2: REAKCIJA ---
    private JPanel createGame2() {
        JPanel panel = new JPanel(null);
        panel.setBackground(isDyslexiaMode ? DYSLEXIA_BG : SOFT_WHITE);
        JLabel roundLabel = new JLabel("", SwingConstants.CENTER);
        roundLabel.setBounds(0, 20, 750, 30);
        roundLabel.setFont(getAppFont(20, true));
        panel.add(roundLabel);
        
        JButton target = new JButton("⭐");
        target.setBounds(300, 250, 100, 100);
        target.setFont(new Font("Serif", Font.PLAIN, 60));
        target.setContentAreaFilled(false);
        target.setBorderPainted(false);

        Runnable moveTarget = new Runnable() {
            public void run() {
                if (currentRound >= 5) { showFinalResult(correctAnswers, 5); return; }
                roundLabel.setText("Runda: " + (currentRound + 1) + "/5");
                Random r = new Random();
                target.setLocation(r.nextInt(600), 150 + r.nextInt(300));
            }
        };
        target.addActionListener(e -> {
            correctAnswers++; currentRound++;
            moveTarget.run();
        });
        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) { currentRound = 0; correctAnswers = 0; moveTarget.run(); }
        });
        panel.add(target);
        return panel;
    }

    // --- IGRA 3: MEMORIJA BOJA (Fiksirano na 5 rundi) ---
    private JPanel createGame3() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(isDyslexiaMode ? DYSLEXIA_BG : SOFT_WHITE);

        JLabel infoLabel = new JLabel("Gledaj pažljivo!", SwingConstants.CENTER);
        infoLabel.setFont(getAppFont(22, true));
        panel.add(infoLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 50));
        buttonPanel.setOpaque(false);

        for (int i = 0; i < 3; i++) {
            final int index = i;
            colorButtons[i] = new JButton();
            colorButtons[i].setPreferredSize(new Dimension(120, 120));
            colorButtons[i].setBackground(colors[i]);
            colorButtons[i].setEnabled(false);
            colorButtons[i].addActionListener(e -> handleColorClick(index, infoLabel));
            buttonPanel.add(colorButtons[i]);
        }
        panel.add(buttonPanel, BorderLayout.CENTER);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) { 
                currentRound = 0;
                correctAnswers = 0;
                nextMemoryRound(infoLabel); 
            }
        });
        return panel;
    }

    private void nextMemoryRound(JLabel label) {
        if (currentRound >= 5) { 
            showFinalResult(correctAnswers, 5); 
            return; 
        }
        playerSequence.clear();
        Random r = new Random();
        colorSequence.clear();
        
        // Dužina niza se povećava kako bi bilo izazovnije
        int sequenceLength = 2 + (currentRound / 2); 
        for(int i=0; i < sequenceLength; i++) colorSequence.add(r.nextInt(3));
        
        label.setText("Runda " + (currentRound + 1) + "/5: Gledaj niz...");
        for(JButton b : colorButtons) b.setEnabled(false);

        Timer sequenceTimer = new Timer(800, null);
        final int[] step = {0};
        sequenceTimer.addActionListener(e -> {
            if (step[0] < colorSequence.size()) {
                flashButton(colorSequence.get(step[0]));
                step[0]++;
            } else {
                sequenceTimer.stop();
                label.setText("Tvoj red!");
                for(JButton b : colorButtons) b.setEnabled(true);
            }
        });
        sequenceTimer.start();
    }

    private void flashButton(int idx) {
        Color original = colors[idx];
        colorButtons[idx].setBackground(Color.WHITE);
        Timer t = new Timer(400, e -> colorButtons[idx].setBackground(original));
        t.setRepeats(false); t.start();
    }

    private void handleColorClick(int index, JLabel label) {
        playerSequence.add(index);
        int currentClickIdx = playerSequence.size() - 1;

        // Ako je pogriješio
        if (playerSequence.get(currentClickIdx) != colorSequence.get(currentClickIdx)) {
            for(JButton b : colorButtons) b.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Pogrešan redoslijed! ❌", "Ups", JOptionPane.ERROR_MESSAGE);
            currentRound++; // Ide na iduću rundu bez boda
            nextMemoryRound(label);
        } 
        // Ako je završio niz točno
        else if (playerSequence.size() == colorSequence.size()) {
            correctAnswers++;
            currentRound++;
            JOptionPane.showMessageDialog(this, "Točno! ⭐", "Bravo", JOptionPane.PLAIN_MESSAGE);
            nextMemoryRound(label);
        }
    }

    private void showFinalResult(int score, int total) {
        String poruka = "Igre su završene!\nRezultat: " + score + " od " + total + " točnih.";
        String pohvala = (score >= (total/2)) ? "\nOdličan posao! 🏆" : "\nDobro si se potrudio! 👍";
        JOptionPane.showMessageDialog(this, poruka + pohvala, "Kraj", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "MENU");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VistadhdApp().setVisible(true));
    }
}