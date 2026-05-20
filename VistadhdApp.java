import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.sound.sampled.*;

public class VistadhdApp extends JFrame {
    private boolean isDyslexiaMode = false;
    private int currentRound = 0;
    private int correctAnswers = 0;
    private String targetLetter = "";
    private final String[] possibleLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};

    private ArrayList<Integer> colorSequence = new ArrayList<>();
    private ArrayList<Integer> playerSequence = new ArrayList<>();
    private JButton[] colorButtons = new JButton[3];
    private boolean[] colorFlashing = new boolean[3];
    private final Color[] colors = {
        new Color(239, 83, 80),   // crvena
        new Color(66, 133, 244),  // plava
        new Color(52, 168, 83)    // zelena
    };

    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Paleta boja
    private final Color BG_DARK       = new Color(18, 18, 30);
    private final Color BG_CARD       = new Color(28, 28, 45);
    private final Color ACCENT_PURPLE = new Color(140, 82, 255);
    private final Color ACCENT_CYAN   = new Color(0, 220, 200);
    private final Color ACCENT_ORANGE = new Color(255, 152, 0);
    private final Color TEXT_MAIN     = new Color(240, 240, 255);
    private final Color TEXT_DIM      = new Color(160, 160, 190);
    private final Color DYSLEXIA_BG   = new Color(255, 252, 220);
    private final Color DYSLEXIA_TEXT = new Color(30, 30, 60);

    public VistadhdApp() {
        setTitle("Vista ADHD Edukator");
        setSize(780, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_DARK);

        buildApp();
        add(mainPanel);
        cardLayout.show(mainPanel, "MENU");
    }

    // ─────────────────────────── ZVUKOVI ───────────────────────────

    private void playTone(double freq, int durationMs, float volume) {
        new Thread(() -> {
            try {
                int sampleRate = 44100;
                int samples = (int)(sampleRate * durationMs / 1000.0);
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double angle = 2 * Math.PI * i * freq / sampleRate;
                    // Primjeni envelope (fade out) kako bi zvuk bio glatki
                    double envelope = Math.min(1.0, Math.min((double)i / (sampleRate * 0.02),
                            (double)(samples - i) / (sampleRate * 0.05)));
                    short val = (short)(Math.sin(angle) * 32767 * volume * envelope);
                    buf[i * 2]     = (byte)(val & 0xFF);
                    buf[i * 2 + 1] = (byte)((val >> 8) & 0xFF);
                }
                AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private void playSuccess()  { playTone(880, 120, 0.15f); playTone(1100, 180, 0.15f); }
    private void playError()    { playTone(220, 300, 0.12f); }
    private void playClick()    { playTone(660, 80,  0.10f); }
    private void playFlash(int idx) {
        double[] freqs = {440, 523, 659};
        playTone(freqs[idx], 150, 0.12f);
    }
    private void playFanfare() {
        new Thread(() -> {
            try {
                double[] notes = {523, 659, 784, 1047};
                int[] durations = {120, 120, 120, 280};
                for (int i = 0; i < notes.length; i++) {
                    playTone(notes[i], durations[i], 0.15f);
                    Thread.sleep(durations[i] + 20);
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    // ─────────────────────────── GRADIJENT PANEL ───────────────────────────

    static class GradientPanel extends JPanel {
        private final Color c1, c2;
        GradientPanel(Color c1, Color c2, LayoutManager lm) {
            super(lm);
            this.c1 = c1; this.c2 = c2;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // ─────────────────────────── ZAOBLJENI GUMB ───────────────────────────

    static class RoundButton extends JButton {
        private Color bg, hover, press;
        private boolean hovered = false, pressed = false;
        private int arc;

        RoundButton(String text, Color bg, Color hover, Color press, int arc) {
            super(text);
            this.bg = bg; this.hover = hover; this.press = press; this.arc = arc;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; pressed = false; repaint(); }
                public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
                public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color current = pressed ? press : (hovered ? hover : bg);
            g2.setColor(current);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            // Lagani sjaj na vrhu
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fill(new RoundRectangle2D.Float(2, 2, getWidth()-4, getHeight()/2, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────── FONT ───────────────────────────

    private Font getAppFont(int size, boolean bold) {
        if (isDyslexiaMode)
            return new Font("Verdana", bold ? Font.BOLD : Font.PLAIN, size + 4);
        return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
    }

    private void styleMainButton(RoundButton btn) {
        btn.setFont(getAppFont(17, true));
        btn.setForeground(isDyslexiaMode ? DYSLEXIA_TEXT : TEXT_MAIN);
        btn.setPreferredSize(new Dimension(320, 62));
    }

    // ─────────────────────────── BUILD ───────────────────────────

    private void buildApp() {
        mainPanel.removeAll();
        mainPanel.add(createMenuScreen(), "MENU");
        mainPanel.add(createGame1(),      "GAME1");
        mainPanel.add(createGame2(),      "GAME2");
        mainPanel.add(createGame3(),      "GAME3");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ─────────────────────────── MENU ───────────────────────────

    private JPanel createMenuScreen() {
        Color panelBg = isDyslexiaMode ? DYSLEXIA_BG : BG_DARK;

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(panelBg);

        // Gornji dekorativni gradijent banner
        JPanel banner = new GradientPanel(new Color(80, 40, 160), new Color(0, 160, 180),
                new BorderLayout());
        banner.setPreferredSize(new Dimension(780, 100));
        JLabel logo = new JLabel("🧠 Vista ADHD Edukator", SwingConstants.CENTER);
        logo.setFont(getAppFont(26, true));
        logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(20, 0, 20, 0));
        banner.add(logo, BorderLayout.CENTER);
        outer.add(banner, BorderLayout.NORTH);

        // Centralni panel s gumbima
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(panelBg);
        center.setBorder(new EmptyBorder(30, 60, 10, 60));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        RoundButton btnG1 = new RoundButton("🔍  Pronađi slovo",
                new Color(80, 50, 180), new Color(100, 70, 210), new Color(60, 30, 150), 22);
        RoundButton btnG2 = new RoundButton("⭐  Brzi klik",
                new Color(180, 80, 30),  new Color(210, 100, 50),  new Color(150, 60, 20),  22);
        RoundButton btnG3 = new RoundButton("🎨  Zapamti boje",
                new Color(30, 140, 80),  new Color(50, 170, 100),  new Color(20, 110, 60),  22);

        styleMainButton(btnG1); styleMainButton(btnG2); styleMainButton(btnG3);

        btnG1.addActionListener(e -> { playClick(); cardLayout.show(mainPanel, "GAME1"); });
        btnG2.addActionListener(e -> { playClick(); cardLayout.show(mainPanel, "GAME2"); });
        btnG3.addActionListener(e -> { playClick(); cardLayout.show(mainPanel, "GAME3"); });

        // Opisi igara
        JLabel[] descs = {
            makeDescLabel("Prepoznaj pravo slovo između 4 opcije (5 rundi)"),
            makeDescLabel("Klikni pokretnu zvijezdicu što brže možeš (5 rundi)"),
            makeDescLabel("Upamti i ponovi niz boja u točnom redoslijedu (5 rundi)")
        };

        gbc.gridy = 0; center.add(btnG1,    gbc);
        gbc.gridy = 1; center.add(descs[0], gbc);
        gbc.gridy = 2; center.add(btnG2,    gbc);
        gbc.gridy = 3; center.add(descs[1], gbc);
        gbc.gridy = 4; center.add(btnG3,    gbc);
        gbc.gridy = 5; center.add(descs[2], gbc);

        outer.add(center, BorderLayout.CENTER);

        // Donji panel – mod za disleksiju
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        bottom.setBackground(panelBg);
        Color dysBg = isDyslexiaMode ? new Color(180, 140, 0) : new Color(70, 70, 90);
        RoundButton btnDys = new RoundButton(
                isDyslexiaMode ? "✓ Isključi mod za disleksiju" : "Aa  Uključi mod za disleksiju",
                dysBg, dysBg.brighter(), dysBg.darker(), 18);
        btnDys.setFont(getAppFont(14, false));
        btnDys.setForeground(Color.WHITE);
        btnDys.setPreferredSize(new Dimension(300, 44));
        btnDys.addActionListener(e -> {
            playClick();
            isDyslexiaMode = !isDyslexiaMode;
            buildApp();
            cardLayout.show(mainPanel, "MENU");
        });
        bottom.add(btnDys);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    private JLabel makeDescLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(getAppFont(12, false));
        l.setForeground(isDyslexiaMode ? DYSLEXIA_TEXT : TEXT_DIM);
        return l;
    }

    // ─────────────────────────── IGRA 1: SLOVA ───────────────────────────

    private JPanel createGame1() {
        Color panelBg = isDyslexiaMode ? DYSLEXIA_BG : BG_DARK;
        JPanel panel = new JPanel(new BorderLayout(10, 24));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(panelBg);

        JLabel instruction = new JLabel("", SwingConstants.CENTER);
        instruction.setFont(getAppFont(22, true));
        instruction.setForeground(isDyslexiaMode ? DYSLEXIA_TEXT : ACCENT_CYAN);

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setOpaque(false);

        // Traka napretka
        JProgressBar progress = new JProgressBar(0, 5);
        progress.setValue(0);
        progress.setStringPainted(false);
        progress.setForeground(ACCENT_PURPLE);
        progress.setBackground(BG_CARD);
        progress.setPreferredSize(new Dimension(0, 10));
        progress.setBorderPainted(false);

        Runnable setupRound = new Runnable() {
            public void run() {
                if (currentRound >= 5) { showFinalResult(correctAnswers, 5); return; }
                progress.setValue(currentRound);
                Random r = new Random();
                targetLetter = possibleLetters[r.nextInt(possibleLetters.length)];
                instruction.setText("Runda " + (currentRound + 1) + "/5 – Klikni slovo: " + targetLetter);
                grid.removeAll();
                ArrayList<String> options = new ArrayList<>();
                options.add(targetLetter);
                while (options.size() < 4) {
                    String rand = possibleLetters[r.nextInt(possibleLetters.length)];
                    if (!options.contains(rand)) options.add(rand);
                }
                Collections.shuffle(options);
                Color[] btnColors = {
                    new Color(100, 60, 200), new Color(30, 130, 180),
                    new Color(160, 60, 100), new Color(40, 150, 80)
                };
                int ci = 0;
                for (String s : options) {
                    Color bc = btnColors[ci++ % 4];
                    RoundButton btn = new RoundButton(s, bc, bc.brighter(), bc.darker(), 20);
                    btn.setFont(new Font("Monospaced", Font.BOLD, 64));
                    btn.setForeground(Color.WHITE);
                    btn.addActionListener(e -> {
                        if (s.equals(targetLetter)) { correctAnswers++; playSuccess(); }
                        else { playError(); }
                        currentRound++;
                        this.run();
                    });
                    grid.add(btn);
                }
                grid.revalidate(); grid.repaint();
            }
        };

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                currentRound = 0; correctAnswers = 0;
                progress.setValue(0);
                setupRound.run();
            }
        });

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(instruction, BorderLayout.CENTER);
        top.add(progress,    BorderLayout.SOUTH);

        panel.add(top,  BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        panel.add(makeBackButton(), BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────── IGRA 2: REAKCIJA ───────────────────────────

    private JPanel createGame2() {
        Color panelBg = isDyslexiaMode ? DYSLEXIA_BG : BG_DARK;

        JPanel panel = new JPanel(null);
        panel.setBackground(panelBg);

        JLabel roundLabel = new JLabel("", SwingConstants.CENTER);
        roundLabel.setBounds(0, 20, 780, 36);
        roundLabel.setFont(getAppFont(20, true));
        roundLabel.setForeground(isDyslexiaMode ? DYSLEXIA_TEXT : ACCENT_ORANGE);
        panel.add(roundLabel);

        // Traka napretka
        JProgressBar progress = new JProgressBar(0, 5);
        progress.setBounds(40, 60, 700, 10);
        progress.setForeground(ACCENT_ORANGE);
        progress.setBackground(BG_CARD);
        progress.setBorderPainted(false);
        panel.add(progress);

        // Zvijezdica kao kružni gumb
        JButton target = new JButton("⭐") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 200, 0, 220));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 230, 100, 60));
                g2.fillOval(6, 6, getWidth()-12, getHeight()-12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        target.setBounds(330, 270, 110, 110);
        target.setFont(new Font("Serif", Font.PLAIN, 58));
        target.setContentAreaFilled(false);
        target.setBorderPainted(false);
        target.setFocusPainted(false);
        target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        target.setOpaque(false);

        // Efekt pulsirajuće animacije
        Timer pulse = new Timer(600, null);
        pulse.addActionListener(e -> {
            Dimension cur = target.getSize();
            int newSize = (cur.width == 110) ? 120 : 110;
            int x = target.getX() - (newSize - cur.width) / 2;
            int y = target.getY() - (newSize - cur.height) / 2;
            target.setBounds(x, y, newSize, newSize);
        });
        pulse.start();

        Runnable moveTarget = new Runnable() {
            public void run() {
                if (currentRound >= 5) { pulse.stop(); showFinalResult(correctAnswers, 5); return; }
                progress.setValue(currentRound);
                roundLabel.setText("Runda: " + (currentRound + 1) + " / 5");
                Random r = new Random();
                int nx = 60 + r.nextInt(620);
                int ny = 120 + r.nextInt(350);
                target.setBounds(nx, ny, 110, 110);
            }
        };

        target.addActionListener(e -> {
            correctAnswers++; currentRound++;
            playSuccess();
            moveTarget.run();
        });

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                currentRound = 0; correctAnswers = 0;
                progress.setValue(0);
                pulse.restart();
                moveTarget.run();
            }
        });

        // Back gumb
        RoundButton back = makeBackButton();
        back.setBounds(20, 580, 150, 44);
        panel.add(back);

        panel.add(target);
        return panel;
    }

    // ─────────────────────────── IGRA 3: MEMORIJA BOJA ───────────────────────────

    private JPanel createGame3() {
        Color panelBg = isDyslexiaMode ? DYSLEXIA_BG : BG_DARK;
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(panelBg);

        JLabel infoLabel = new JLabel("Gledaj pažljivo!", SwingConstants.CENTER);
        infoLabel.setFont(getAppFont(22, true));
        infoLabel.setForeground(isDyslexiaMode ? DYSLEXIA_TEXT : ACCENT_CYAN);

        JProgressBar progress = new JProgressBar(0, 5);
        progress.setForeground(new Color(52, 168, 83));
        progress.setBackground(BG_CARD);
        progress.setBorderPainted(false);
        progress.setPreferredSize(new Dimension(0, 10));

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);
        topPanel.add(infoLabel, BorderLayout.CENTER);
        topPanel.add(progress,  BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 40));
        buttonPanel.setOpaque(false);

        String[] colorLabels = {"🔴", "🔵", "🟢"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            colorButtons[i] = new JButton(colorLabels[i]) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base;
                    if (colorFlashing[idx]) {
                        base = Color.WHITE;
                    } else if (!isEnabled()) {
                        base = colors[idx].darker().darker();
                    } else {
                        base = colors[idx];
                    }
                    g2.setColor(base);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    if (!colorFlashing[idx]) {
                        g2.setColor(new Color(255, 255, 255, isEnabled() ? 50 : 15));
                        g2.fillOval(10, 10, getWidth()/2, getHeight()/3);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            colorButtons[i].setPreferredSize(new Dimension(140, 140));
            colorButtons[i].setContentAreaFilled(false);
            colorButtons[i].setBorderPainted(false);
            colorButtons[i].setFocusPainted(false);
            colorButtons[i].setFont(new Font("Serif", Font.PLAIN, 48));
            colorButtons[i].setEnabled(false);
            colorButtons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            colorButtons[i].addActionListener(e -> handleColorClick(idx, infoLabel, progress));
            buttonPanel.add(colorButtons[i]);
        }

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                currentRound = 0; correctAnswers = 0;
                progress.setValue(0);
                nextMemoryRound(infoLabel, progress);
            }
        });

        panel.add(topPanel,     BorderLayout.NORTH);
        panel.add(buttonPanel,  BorderLayout.CENTER);
        panel.add(makeBackButton(), BorderLayout.SOUTH);
        return panel;
    }

    private void nextMemoryRound(JLabel label, JProgressBar progress) {
        if (currentRound >= 5) { playFanfare(); showFinalResult(correctAnswers, 5); return; }
        progress.setValue(currentRound);
        playerSequence.clear();
        colorSequence.clear();
        Random r = new Random();
        int len = 2 + (currentRound / 2);
        for (int i = 0; i < len; i++) colorSequence.add(r.nextInt(3));

        label.setText("Runda " + (currentRound + 1) + "/5: Pazi na niz...");
        for (JButton b : colorButtons) b.setEnabled(false);

        Timer seqTimer = new Timer(900, null);
        final int[] step = {0};
        seqTimer.addActionListener(e -> {
            if (step[0] < colorSequence.size()) {
                int ci = colorSequence.get(step[0]);
                flashButton(ci);
                playFlash(ci);
                step[0]++;
            } else {
                seqTimer.stop();
                label.setText("Tvoj red! Ponovi niz ▶");
                for (JButton b : colorButtons) b.setEnabled(true);
            }
        });
        seqTimer.start();
    }

    private void flashButton(int idx) {
        colorFlashing[idx] = true;
        colorButtons[idx].repaint();
        Timer t = new Timer(450, e -> { colorFlashing[idx] = false; colorButtons[idx].repaint(); });
        t.setRepeats(false); t.start();
    }

    private void handleColorClick(int index, JLabel label, JProgressBar progress) {
        // Odmah zasvijetli kliknuti gumb
        flashButton(index);
        playFlash(index);

        // Malo odgodi provjeru da se vidi flash
        Timer checkTimer = new Timer(200, null);
        checkTimer.setRepeats(false);
        checkTimer.addActionListener(ev -> {
        playerSequence.add(index);
        int ci = playerSequence.size() - 1;
        if (!playerSequence.get(ci).equals(colorSequence.get(ci))) {
            playError();
            for (JButton b : colorButtons) b.setEnabled(false);
            showToast("Pogrešan redoslijed! ❌", new Color(200, 50, 50));
            currentRound++;
            Timer t = new Timer(1400, e -> nextMemoryRound(label, progress));
            t.setRepeats(false); t.start();
        } else if (playerSequence.size() == colorSequence.size()) {
            playSuccess();
            correctAnswers++;
            currentRound++;
            for (JButton b : colorButtons) b.setEnabled(false);
            showToast("Točno! ⭐", new Color(40, 160, 80));
            Timer t = new Timer(1400, e -> nextMemoryRound(label, progress));
            t.setRepeats(false); t.start();
        }
        });
        checkTimer.start();
    }

    // ─────────────────────────── TOAST PORUKA ───────────────────────────

    private void showToast(String msg, Color bg) {
        JWindow toast = new JWindow(this);
        JLabel lbl = new JLabel(msg, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(getAppFont(18, true));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(14, 40, 14, 40));
        toast.add(lbl);
        toast.pack();
        // Centriraj toast
        Point loc = getLocation();
        int tx = loc.x + getWidth()/2  - toast.getWidth()/2;
        int ty = loc.y + getHeight()/2 - toast.getHeight()/2;
        toast.setLocation(tx, ty);
        toast.setVisible(true);
        Timer t = new Timer(1200, e -> toast.dispose());
        t.setRepeats(false); t.start();
    }

    // ─────────────────────────── KRAJ IGRE ───────────────────────────

    private void showFinalResult(int score, int total) {
        String pohvala = (score == total) ? "Savršeno! 🏆" :
                         (score >= total * 0.6) ? "Odličan posao! ⭐" : "Dobro si se potrudio! 👍";

        // Prilagođeni dialog
        JDialog dlg = new JDialog(this, "Kraj igre", true);
        dlg.setUndecorated(true);
        dlg.setSize(360, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(0, 16)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(40, 20, 100), getWidth(), getHeight(), new Color(0, 100, 120)));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 28, 28));
                g2.dispose();
            }
        };
        p.setBorder(new EmptyBorder(34, 40, 30, 40));
        p.setOpaque(false);

        JLabel title = new JLabel("Rezultat: " + score + " / " + total, SwingConstants.CENTER);
        title.setFont(getAppFont(26, true));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(pohvala, SwingConstants.CENTER);
        sub.setFont(getAppFont(18, false));
        sub.setForeground(new Color(180, 240, 200));

        RoundButton ok = new RoundButton("Natrag na izbornik",
                new Color(255, 255, 255, 50), new Color(255,255,255,80), new Color(255,255,255,30), 16);
        ok.setFont(getAppFont(15, true));
        ok.setForeground(Color.WHITE);
        ok.setPreferredSize(new Dimension(240, 48));
        ok.addActionListener(e -> { dlg.dispose(); cardLayout.show(mainPanel, "MENU"); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        p.add(title,  BorderLayout.NORTH);
        p.add(sub,    BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setShape(new RoundRectangle2D.Float(0, 0, 360, 260, 28, 28));
        dlg.setVisible(true);
    }

    // ─────────────────────────── BACK GUMB ───────────────────────────

    private RoundButton makeBackButton() {
        Color bc = isDyslexiaMode ? new Color(120, 80, 0) : new Color(60, 60, 90);
        RoundButton btn = new RoundButton("← Izbornik", bc, bc.brighter(), bc.darker(), 14);
        btn.setFont(getAppFont(13, false));
        btn.setForeground(isDyslexiaMode ? DYSLEXIA_TEXT : TEXT_DIM);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.addActionListener(e -> { playClick(); cardLayout.show(mainPanel, "MENU"); });
        return btn;
    }

    public static void main(String[] args) {
        // Moderan izgled
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new VistadhdApp().setVisible(true));
    }
}