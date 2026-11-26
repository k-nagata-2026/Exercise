import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;


public class BattleGame extends JFrame {
    final private JLabel statusLabel;
    private JTextArea logTextArea;
   final  private JButton attackButton;
   final  private JButton speedButton;
   final  private JButton skillButton;
   final  private JButton potionButton;
   final private JButton itemButton;
   private JButton saveButton;
          private int playerX = 40;
          private int playerY = 50;
   final  private JLabel backgroundLabel;
   final  private JLabel playerImageLabel;
   final  private JLabel enemyImageLabel;
   final  private JProgressBar playerHpBar;
   final  private JProgressBar enemyHpBar;


    private Player player;
    private Enemy enemy;
    private int currentLevel = 1;
    private int enemyCount = 0;
private int potionUseCount = 0;
private final int MAX_POTION_USE = 3;

private int skillUseCount = 0;
private int getMaxPotionUse() {
    return Math.min(7, Math.max(3, currentLevel + 2));
}


private int getMaxSkillUse() {
    if (currentLevel <= 2) {
        return 0;
    } else if (currentLevel == 3 || currentLevel == 4) {
        return 2;
    } else {
        return 4;
    }
}


private Clip bgmClip;
private Item[] itemBox = new Item[10];
//MONSTAR AI SYSTEM
private Timer enemyAiTimer;
private long lastEnemyAttackTime = 0;
private boolean battleEnded = false;
private boolean enemyAggro = false;     // Player ले पहिलो आक्रमण गरेपछि मात्र true हुन्छ
private boolean isBreakTime = false;    // दुश्मन मरेपछिको ब्रेक टाइमको लागि
// SAFE SHIELD SYSTEM
private JButton safeZoneButton;
private JLabel shieldLabel;

private int safeZoneUseCount = 0;
private final int MAX_SAFE_ZONE_USE = 3;

private boolean safeZoneActive = false;
private Timer safeZoneTimer;
private int safeZoneSeconds = 3;
private boolean isSaving = false;
private int gold = 100; 
private JButton shopButton;
private JLabel goldLabel; // स्क्रिनमा गोल्ड देखाउन
// एनिमेसन र ड्यामेज पप-अपका लागि भेरिएबलहरू
private String floatingText = "";
private int floatingTextX = 0;
private int floatingTextY = 0;
private int floatingTextAlpha = 0; // पारदर्शिता (0 देखि 255 सम्म)
private Color floatingTextColor = Color.YELLOW;

private int shakeOffsetX = 0;
private int shakeFrames = 0;
private Timer effectTimer;
    

  private void playBGM(String fileName) {

    try {

        // Purano BGM rokne
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }

        AudioInputStream audio =
                AudioSystem.getAudioInputStream(new File(fileName));

        bgmClip = AudioSystem.getClip();
        bgmClip.open(audio);

        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        bgmClip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void stopBGM() {

    if (bgmClip != null) {
        bgmClip.stop();
        bgmClip.flush();
        bgmClip.close();
        bgmClip = null;
    }

}

   private void playSound(String fileName) {
    try {
        File soundFile = new File(fileName);

        System.out.println("Playing: " + soundFile.getAbsolutePath());
        System.out.println("Exists: " + soundFile.exists());

        AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);

        Clip clip = AudioSystem.getClip();
        clip.open(audio);
        clip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    private void showVictoryScreen() {

    playSound("gameclearsound.wav");

    JFrame victoryFrame = new JFrame("GAME CLEAR!");
    victoryFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    victoryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JLabel bg = new JLabel(new ImageIcon("gameclear.png"));
    bg.setBounds(0, 0, 1920, 1080);
    bg.setLayout(null);

    JButton homeButton = new JButton();

    homeButton.setBounds(800, 540, 460, 90);

    homeButton.setOpaque(false);
    homeButton.setContentAreaFilled(false);
    homeButton.setBorderPainted(false);
    homeButton.setFocusPainted(false);

    // BACK TO HOME
    homeButton.addActionListener(e -> {

        victoryFrame.dispose();
        dispose();

        HomeScreen home = new HomeScreen();
        home.setVisible(true);
    });

    bg.add(homeButton);

    victoryFrame.add(bg);

    victoryFrame.setVisible(true);

    this.setVisible(false);
}


private void showGameOverScreen() {

    playSound("gameoversound.wav");

    JFrame gameOverFrame = new JFrame("GAME OVER");
    gameOverFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ImageIcon icon = new ImageIcon("GAME OVER.png");
    Image img = icon.getImage();

    Image resizedImg = img.getScaledInstance(
            1420,
            700,
            Image.SCALE_SMOOTH
    );

    JLabel label = new JLabel(new ImageIcon(resizedImg));
    label.setLayout(null);

    // BACK TO HOME BUTTON
    JButton homeButton = new JButton();
    homeButton.setBounds(780, 520, 350, 55);
    homeButton.setOpaque(false);
    homeButton.setContentAreaFilled(false);
    homeButton.setBorderPainted(false);
    homeButton.setFocusPainted(false);

    homeButton.addActionListener(e -> {
        gameOverFrame.dispose();
        dispose();
        stopBGM();

        HomeScreen home = new HomeScreen();
        home.setVisible(true);
    });

    // RETRY BUTTON
    JButton retryButton = new JButton();
    retryButton.setBounds(780, 420, 350, 55);
    retryButton.setOpaque(false);
    retryButton.setContentAreaFilled(false);
    retryButton.setBorderPainted(false);
    retryButton.setFocusPainted(false);

   retryButton.addActionListener(e -> {
    gameOverFrame.dispose();
    dispose();

    // Player HP full गर्ने
    player.setHp(player.getMaxHp());

    // नयाँ Battle Game सुरु गर्ने
    BattleGame game = new BattleGame(player);
    game.setVisible(true);
});

    label.add(homeButton);
    label.add(retryButton);

    gameOverFrame.add(label);
    gameOverFrame.setVisible(true);

    this.setVisible(false);
}

private void shakePlayer() {
    int x = playerImageLabel.getX();
    int y = playerImageLabel.getY();

    javax.swing.Timer timer = new javax.swing.Timer(40, null);
    final int[] count = {0};

    timer.addActionListener(e -> {
        if (count[0] % 2 == 0) {
            playerImageLabel.setLocation(x + 10, y);
        } else {
            playerImageLabel.setLocation(x - 10, y);
        }

        count[0]++;

        if (count[0] >= 8) {
            playerImageLabel.setLocation(x, y);
            timer.stop();
        }
    });

    timer.start();
}


private void showThunderEffect() {

    // Enemy ko position लिनु
    int enemyX = enemyImageLabel.getX();
    int enemyY = enemyImageLabel.getY();

    // Lightning
    JLabel lightning = new JLabel("⚡");
    lightning.setFont(new Font("Serif", Font.BOLD, 180));
    lightning.setForeground(Color.WHITE);
    lightning.setHorizontalAlignment(SwingConstants.CENTER);

    // Enemy ko mathi lightning राख्ने
    lightning.setBounds(
        enemyX + 100,
        enemyY - 120,
        300,
        300
    );

    // White flash
    JPanel flash = new JPanel();
    flash.setBackground(new Color(255, 255, 255, 170));
    flash.setOpaque(true);
    flash.setBounds(0, 0, getWidth(), getHeight());

    getLayeredPane().add(flash, JLayeredPane.POPUP_LAYER);
    getLayeredPane().add(lightning, JLayeredPane.POPUP_LAYER);

    getLayeredPane().repaint();

    // 150ms पछि हटाउने
    Timer timer = new Timer(200, e -> {
        getLayeredPane().remove(flash);
        getLayeredPane().remove(lightning);
        getLayeredPane().repaint();
    });

    timer.setRepeats(false);
    timer.start();
}

private void showFireEffect() {

    // Screen ko orange/red fire flash
    JPanel fireFlash = new JPanel();
    fireFlash.setBackground(new Color(255, 80, 0, 150));
    fireFlash.setOpaque(true);
    fireFlash.setBounds(0, 0, getWidth(), getHeight());

    // Fire emoji
    JLabel fire = new JLabel("🔥");
    fire.setFont(new Font("Serif", Font.BOLD, 180));
    fire.setForeground(Color.ORANGE);
    fire.setHorizontalAlignment(SwingConstants.CENTER);

    // Enemy ko mathi fire
    int enemyX = enemyImageLabel.getX();
    int enemyY = enemyImageLabel.getY();

    fire.setBounds(
        enemyX + 100,
        enemyY + 80,
        300,
        300
    );

    getLayeredPane().add(fireFlash, JLayeredPane.POPUP_LAYER);
    getLayeredPane().add(fire, JLayeredPane.POPUP_LAYER);

    getLayeredPane().repaint();

    // Fire effect हटाउने
    Timer timer = new Timer(500, e -> {
        getLayeredPane().remove(fireFlash);
        getLayeredPane().remove(fire);
        getLayeredPane().repaint();
    });

    timer.setRepeats(false);
    timer.start();
}

// NORMAL ATTACK EFFECT
private void showAttackEffect(int damage) {

    int x = enemyImageLabel.getX() + 150;
    int y = enemyImageLabel.getY() + 120;

    JLabel slash = new JLabel("⚔");
    slash.setFont(new Font("Serif", Font.BOLD, 110));
    slash.setForeground(Color.CYAN);
    slash.setBounds(x, y, 180, 150);

    JLabel damageLabel = new JLabel("-" + damage + " HP");
    damageLabel.setFont(new Font("Arial", Font.BOLD, 36));
    damageLabel.setForeground(Color.RED);
    damageLabel.setBounds(x + 30, y + 100, 200, 60);

    backgroundLabel.add(slash, 0);
    backgroundLabel.add(damageLabel, 0);
    backgroundLabel.repaint();

    final int[] count = {0};

    Timer timer = new Timer(30, e -> {
        count[0]++;

        slash.setLocation(x + (count[0] % 2 == 0 ? 15 : -15),
                          y - count[0] * 2);

        damageLabel.setLocation(x + 30, y + 100 - count[0] * 3);

        if (count[0] >= 12) {
            backgroundLabel.remove(slash);
            backgroundLabel.remove(damageLabel);
            backgroundLabel.repaint();
            ((Timer)e.getSource()).stop();
        }
    });

    timer.start();
}


// ENEMY HIT EFFECT
private void showPlayerHitEffect(int damage) {

    JLabel hit = new JLabel("💥");
    hit.setFont(new Font("Serif", Font.BOLD, 100));

    int x = playerImageLabel.getX() + 150;
    int y = playerImageLabel.getY() + 100;

    hit.setBounds(x, y, 180, 150);

    JLabel damageLabel = new JLabel("-" + damage + " HP");
    damageLabel.setFont(new Font("Arial", Font.BOLD, 32));
    damageLabel.setForeground(Color.RED);
    damageLabel.setBounds(x + 20, y + 100, 200, 50);

    backgroundLabel.add(hit, 0);
    backgroundLabel.add(damageLabel, 0);
    backgroundLabel.repaint();

    //shakePlayer();

    Timer timer = new Timer(450, e -> {
        backgroundLabel.remove(hit);
        backgroundLabel.remove(damageLabel);
        backgroundLabel.repaint();
        ((Timer)e.getSource()).stop();
    });

    timer.setRepeats(false);
    timer.start();
}



private void handleEnemyDefeat() {
    if (isBreakTime) return; // दोहोरिएर नचलोस् भन्नका लागि
    isBreakTime = true;
    enemyAggro = false; // अर्को दुश्मन आउँदा फेरि Player कै पहिलो आक्रमण कुर्छ

    playSound("sounds/enemy_dead.wav");
    logTextArea.append("★ " + enemy.getName() + " をたおした！\n");

    // दुश्मनको इमेज अस्थायी रूपमा लुकाउने र Death Box देखाउने
    enemyImageLabel.setVisible(false);
    showEnemyDeathBox(enemy.getName());

    enemyCount++;
    player.gainExp(50);
    logTextArea.append("EXP +50!\n");

    // २.५ सेकेन्डको ब्रेक (यो समयमा Player ले Potion/Shop चलाएर Recover गर्न सक्छ)
    Timer breakTimer = new Timer(2500, e -> {
        isBreakTime = false;

        // 1 Level = 7 enemies
        if (enemyCount >= 7) {
            if (currentLevel >= 5) {
                stopBGM();
                playSound("sounds/victory.wav");
                endGame();
                showVictoryScreen();
                return;
            }

            // Start next level
            currentLevel++;
            enemyCount = 0;
            updateBackground();
            potionUseCount = 0;
         skillUseCount = 0;
           safeZoneUseCount = 0;
            gold += 50;
            goldLabel.setText("Gold: " + gold + " G");

            player.setMaxHp(player.getMaxHp() + 50);
            player.setHp(player.getMaxHp());
            player.setAtk(player.getAtk() + 10);

            potionUseCount = 0;
            skillUseCount = 0;
            safeZoneUseCount = 0;
            safeZoneButton.setText("SAFE SHIELD (3)");
            safeZoneButton.setEnabled(true);

            JOptionPane.showMessageDialog(
                this,
                "LEVEL " + currentLevel + " START!\n"
                + "Max HP +50\n"
                + "Attack +10\n"
                + "HP fully recovered!"
            );
        }

        enemyImageLabel.setLocation(750, 50); // नयाँ दुश्मनलाई दायाँ छेउबाट पठाउने
        enemyImageLabel.setVisible(true);
        spawnEnemy();
        updateDisplay();
    });
    breakTimer.setRepeats(false);
    breakTimer.start();
}

// दुश्मन मर्दा स्क्रिनको बीचमा देखिने Death Box
private void showEnemyDeathBox(String enemyName) {
    JLabel deathBox = new JLabel(
        "<html><center>💀 <b>" + enemyName + " DEFEATED!</b> 💀<br>"
        + "<span style='font-size:14px; color:#f1c40f;'>Recover HP now! Next enemy in 2.5s...</span></center></html>",
        SwingConstants.CENTER
    );
    deathBox.setFont(new Font("Arial", Font.BOLD, 22));
    deathBox.setForeground(Color.WHITE);
    deathBox.setOpaque(true);
    deathBox.setBackground(new Color(20, 20, 20, 210));
    deathBox.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
    deathBox.setBounds(370, 200, 460, 100);

    backgroundLabel.add(deathBox, 0);
    backgroundLabel.repaint();

    Timer removeTimer = new Timer(2400, e -> {
        backgroundLabel.remove(deathBox);
        backgroundLabel.repaint();
    });
    removeTimer.setRepeats(false);
    removeTimer.start();
}

// MONSTAR automatically follows the player
private void startEnemyAI() {

    if (enemyAiTimer != null) {
        enemyAiTimer.stop();
    }

    enemyAiTimer = new Timer(40, e -> {

        // Save/Shop खोलेको बेला, ब्रेक टाइममा, वा Player ले पहिलो आक्रमण नगर्दासम्म Enemy चल्दैन
        if (isSaving || isBreakTime || !enemyAggro || battleEnded) {
            return;
        }

        int pX = playerImageLabel.getX();
        int eX = enemyImageLabel.getX();
        int distance = Math.abs(pX - eX);

        // Enemy moves toward player
        if (distance > 390) {
            int speed = 5;
            if (eX > pX) {
                enemyImageLabel.setLocation(eX - speed, enemyImageLabel.getY());
            } else {
                enemyImageLabel.setLocation(eX + speed, enemyImageLabel.getY());
            }
        } else {
            if (safeZoneActive) {
                return;
            }

            // Attack every 1.2 seconds
            long now = System.currentTimeMillis();
            if (now - lastEnemyAttackTime >= 1200) {
                lastEnemyAttackTime = now;

                int oldPlayerHp = player.getHp();
                String result = enemy.attack(player);
                int playerDamage = Math.max(0, oldPlayerHp - player.getHp());

                if (playerDamage > 0) {
                    showPlayerHitEffect(playerDamage);
                    showDamageEffect("-" + playerDamage + "!", playerImageLabel.getX() + 180, playerImageLabel.getY() + 80, Color.RED, true);
                }

                logTextArea.append("MONSTAR ATTACK! " + result + "\n");
                updateDisplay();

                if (!player.isAlive()) {
                    battleEnded = true;
                    enemyAiTimer.stop();
                    stopBGM();
                    showGameOverScreen();
                }
            }
        }
    });

    enemyAiTimer.start();
}


private void updateBackground() {
    String bgFile = "sky.png"; // Default Level 1

    if (currentLevel == 1) {
        bgFile = "sky.png";
    } else if (currentLevel == 2) {
        bgFile = "marubhumi.jpg";
    } else if (currentLevel == 3) {
        bgFile = "fier.jpg";
    } else if (currentLevel == 4) {
        bgFile = "dark.jpg";
    } else if (currentLevel >= 5) {
        bgFile = "finalbattle.jpg";
    }

    File file = new File(bgFile);
    if (file.exists()) {
        ImageIcon icon = new ImageIcon(bgFile);
        Image scaled = icon.getImage().getScaledInstance(1200, 600, Image.SCALE_SMOOTH);
        backgroundLabel.setIcon(new ImageIcon(scaled));
    } else {
        // यदि नयाँ इमेज फाइल भेटिएन भने पुरानै sky.png देखाउने
        backgroundLabel.setIcon(new ImageIcon("sky.png"));
    }
    backgroundLabel.repaint();
}

private void showDamageEffect(String text, int x, int y, Color color, boolean shakeScreen) {
    this.floatingText = text;
    this.floatingTextX = x;
    this.floatingTextY = y;
    this.floatingTextAlpha = 255;
    this.floatingTextColor = color;
    if (shakeScreen) {
        this.shakeFrames = 8; // ८ फ्रेमसम्म स्क्रिन हल्लिन्छ
    }
}
  
private void showGoldPopup(String text) {
    JWindow popup = new JWindow(this);
    JLabel label = new JLabel(text, SwingConstants.CENTER);
    label.setFont(new Font("SansSerif", Font.BOLD, 20));
    label.setForeground(new Color(255, 215, 0)); // सुनौलो (Gold) रङ
    label.setOpaque(true);
    label.setBackground(new Color(30, 30, 30, 220)); // कालो ब्याकग्राउन्ड
    label.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));

    popup.add(label);
    popup.setSize(180, 45);
    popup.setLocationRelativeTo(this); // गेम विन्डोको बीचमा देखाउँछ
    popup.setVisible(true);

    // १.५ सेकेन्डपछि आफैं हराउने बनाउने (गेम रोकिँदैन)
    Timer t = new Timer(1500, e -> popup.dispose());
    t.setRepeats(false);
    t.start();

    
}



public BattleGame(Player player) {
    this(player, null);
}

public BattleGame(GameSaveManager.SaveData data) {
    this(createPlayerFromSave(data), data);
}

private BattleGame(Player player, GameSaveManager.SaveData data) {

    this.player = player;

    if (data != null) {
        currentLevel = data.currentStage;
        enemyCount = data.enemyDefeated;
        potionUseCount = data.usedPotion;
        skillUseCount = data.usedSkill;
    }

    // Window settings
    setTitle("RPG BATTLE GAME");
    setSize(1200, 750);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    // Battle BGM
    playBGM("sounds/bgm.wav");

        // コンポーネントの初期化
        
        playBGM("sounds/bgm.wav");
       backgroundLabel = new JLabel(new ImageIcon("sky.png")) {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(shakeOffsetX, 0); // स्क्रिन हल्लाउन
        super.paintComponent(g);

        // पप-अप ड्यामेज नम्बर कोर्ने
        if (floatingTextAlpha > 0 && !floatingText.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 34));
            g2d.setColor(new Color(0, 0, 0, floatingTextAlpha));
            g2d.drawString(floatingText, floatingTextX + 2, floatingTextY + 2);

            g2d.setColor(new Color(
                floatingTextColor.getRed(),
                floatingTextColor.getGreen(),
                floatingTextColor.getBlue(),
                floatingTextAlpha
            ));
            g2d.drawString(floatingText, floatingTextX, floatingTextY);
        }
    }
};
        backgroundLabel.setLayout(null);

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);
        playerImageLabel.setBounds(40, 50, 500, 500);
        enemyImageLabel.setBounds(550, 50, 500, 500);
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);
        setupKeyboardControls();

        statusLabel = new JLabel("ステータス表示", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        // Game Buttons
attackButton = new JButton("こうげき");
speedButton = new JButton("speed");
skillButton = new JButton("skill");
skillButton.setEnabled(getMaxSkillUse() > 0);
potionButton = new JButton("potion");
itemButton = new JButton("item");
saveButton = new JButton("SAVE");
shopButton = new JButton("SHOP");
safeZoneButton = new JButton("SAFE SHIELD (3)");

safeZoneButton.setFocusable(false);
attackButton.setFocusable(false);
potionButton.setFocusable(false);
skillButton.setFocusable(false);
itemButton.setFocusable(false);
saveButton.setFocusable(false);
shopButton.setFocusable(false); // Space थिच्दा क्लिक नहोस् भन्नका लागि
shopButton.addActionListener(e -> {

    isSaving = true;
openShop();



isSaving = false;
        this.requestFocusInWindow();
});

goldLabel = new JLabel("Gold: " + gold + " G");
goldLabel.setForeground(new Color(255, 215, 0)); // पहेँलो रङ
goldLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
safeZoneButton.addActionListener(e -> activateSafeZone());

// HP Bars
playerHpBar = new JProgressBar();
enemyHpBar = new JProgressBar();

playerHpBar.setStringPainted(true);
enemyHpBar.setStringPainted(true);

JPanel hpPanel = new JPanel(new GridLayout(2, 1));
hpPanel.add(playerHpBar);
hpPanel.add(enemyHpBar);

// Bottom Panel
JPanel bottomPanel = new JPanel(new BorderLayout());

bottomPanel.add(statusLabel, BorderLayout.NORTH);
bottomPanel.add(scrollPane, BorderLayout.CENTER);

// Button Panel
JPanel buttonPanel = new JPanel();

buttonPanel.add(attackButton);
// buttonPanel.add(speedButton);
buttonPanel.add(skillButton);
buttonPanel.add(potionButton);
buttonPanel.add(itemButton);
buttonPanel.add(saveButton);
buttonPanel.add(safeZoneButton);
buttonPanel.add(shopButton);
buttonPanel.add(goldLabel);

bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

// Add Panels to Game Window
add(backgroundLabel, BorderLayout.CENTER);
add(bottomPanel, BorderLayout.SOUTH);

// Items
itemBox[0] = new Herb();
itemBox[1] = new Bomb();
itemBox[2] = new ThunderStick();
itemBox[3] = new FireOrb();
       
        // ボタンの処理




      attackButton.addActionListener(e -> {

    if (isBreakTime || battleEnded) {
        return; // ब्रेक टाइम वा गेम सकिएको बेला आक्रमण नचलोस्
    }

    // १. Player ले पहिलो आक्रमण गरेपछि Enemy सक्रिय (Aggro) हुन्छ
    if (!enemyAggro) {
        enemyAggro = true;
    }
    lastEnemyAttackTime = System.currentTimeMillis(); // AI टाइमर रिसेट (ताकि तुरुन्तै डबल नहानोस्)

    // २. पहिले Player ले Enemy लाई आक्रमण गर्छ (Shield अन भए पनि Player को आक्रमण चल्छ!)
    playSound("sounds/attack.wav");

    int oldEnemyHp = enemy.getHp();
    String playerResult = player.attack(enemy);
    logTextArea.append(playerResult);

    int damage = Math.max(0, oldEnemyHp - enemy.getHp());
    if (damage > 0) {
        showAttackEffect(damage);
        showDamageEffect("-" + damage + "!", enemyImageLabel.getX() + 180, enemyImageLabel.getY() + 80, Color.YELLOW, false);
    }

    updateDisplay();

    // ३. यदि Enemy मर्यो भने यहीँ रोकिन्छ
    if (!enemy.isAlive()) {
        handleEnemyDefeat();
        return;
    }

    // ४. Enemy जिउँदै छ भने तुरुन्तै फर्काएर आक्रमण गर्छ — तर Safe Shield अन छ भने ड्यामेज लाग्दैन!
    if (safeZoneActive) {
        logTextArea.append("🛡 Safe Shield active! Enemy attack blocked (0 damage)!\n");
        showDamageEffect("BLOCKED!", playerImageLabel.getX() + 160, playerImageLabel.getY() + 80, Color.CYAN, false);
        updateDisplay();
    } else {
        int oldPlayerHp = player.getHp();
        String enemyResult = enemy.attack(player);
        logTextArea.append(enemyResult);

        int playerDamage = Math.max(0, oldPlayerHp - player.getHp());
        if (playerDamage > 0) {
            showPlayerHitEffect(playerDamage);
            showDamageEffect("-" + playerDamage + "!", playerImageLabel.getX() + 180, playerImageLabel.getY() + 80, Color.RED, true);
        }

        updateDisplay();

        if (!player.isAlive()) {
            battleEnded = true;
            if (enemyAiTimer != null) enemyAiTimer.stop();
            logTextArea.append(player.getName() + " はたおれた… GAME OVER\n");
            playerImageLabel.setEnabled(false);
            stopBGM();
            showGameOverScreen();
        }
    }
});
    
skillButton.addActionListener(e -> {

    if (safeZoneActive) return;
    if (isBreakTime) return;

    int maxSkill = getMaxSkillUse();

    if (maxSkill == 0) {
        JOptionPane.showMessageDialog(
            this,
            "Stage " + currentLevel
            + " मा Skill प्रयोग गर्न मिल्दैन!"
        );
        return;
    }

    if (skillUseCount >= maxSkill) {
        JOptionPane.showMessageDialog(
            this,
            "Stage " + currentLevel
            + " मा जम्मा " + maxSkill
            + " पटक मात्र Skill प्रयोग गर्न मिल्छ!"
        );
        return;
    }

    enemyAggro = true;
    lastEnemyAttackTime = System.currentTimeMillis();

    skillUseCount++;

    playSound("sounds/skill.wav");

    String result = player.skillAttack(enemy);

    logTextArea.append(result + "\n");
    logTextArea.append(
        "Skill Used: " + skillUseCount
        + " / " + maxSkill + "\n"
    );

    updateDisplay();

    if (!enemy.isAlive()) {
        handleEnemyDefeat();
    }
});

    
potionButton.addActionListener(e -> {
    isSaving = true;

    try {
        int maxPotion = getMaxPotionUse();

        if (potionUseCount >= maxPotion) {
            JOptionPane.showMessageDialog(
                this,
                "Stage " + currentLevel
                + " मा जम्मा " + maxPotion
                + " वटा Potion मात्र प्रयोग गर्न मिल्छ!"
            );
            return;
        }

        String result = player.usePotion();

        potionUseCount++;

        logTextArea.append(result + "\n");
        logTextArea.append(
            "Potion Used: " + potionUseCount
            + " / " + maxPotion + "\n"
        );

        updateDisplay();

    } finally {
        isSaving = false;
        this.requestFocusInWindow();
    }
});


saveButton.addActionListener(e -> {

    isSaving = true;

    try {

        String slotName = JOptionPane.showInputDialog(
            this,
            "Enter Save Slot Name:"
        );

        if (slotName == null || slotName.trim().isEmpty()) {
            return;
        }

        String idText = JOptionPane.showInputDialog(
            this,
            "Enter Player ID:"
        );

        if (idText == null || idText.trim().isEmpty()) {
            return;
        }

        try {
            int playerId = Integer.parseInt(idText.trim());

            GameSaveManager.saveFullGameData(
                playerId,
                slotName.trim(),
                player,
                enemy,
                currentLevel,
                potionUseCount,
                skillUseCount,
                enemyCount,
                safeZoneUseCount,
                gold
            );

            JOptionPane.showMessageDialog(
                this,
                "Game Saved Successfully!"
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                this,
                "Invalid Player ID!"
            );
        }

    } finally {
        isSaving = false;
    }
});



itemButton.addActionListener(e -> {

    isSaving = true;

    if (safeZoneActive) {
    return;
}
enemyAggro = true;

        // 1. ダイアログに表示（ひょうじ）するための「選択肢（せんたくし）リスト（文字列（もじれつ））」を作る（つくる）
        String[] choices = new String[10];
        for (int i = 0; i < itemBox.length; i++) {
            if (itemBox[i] != null) {
                choices[i] = "スロット " + (i + 1) + " : " + itemBox[i].getName();
            } else {
                choices[i] = "スロット " + (i + 1) + " : (からっぽ)";
            }
        }

        // 2. Swingの便利（べんり）なインプットダイアログを表示（ひょうじ）（プルダウン形式（けいしき））
        Object selected = JOptionPane.showInputDialog(
            BattleGame.this,
            "使用するアイテムを選択してください",
            "アイテムボックス (最大10個)",
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices, // 10個（こ）のスロットの文字（もじ）配列（はいれつ）
            choices[0] // 最初（さいしょ）から選択（せんたく）されている項目（こうもく）
        );

        // キャンセルされた（×ボタンやCancel）場合（ばあい）は処理（しょり）を中断（ちゅうだん）する
        if (selected == null) { return; }

        // 3. 選択（せんたく）された項目（こうもく）が「何（なん）番（ばん）目（め）のスロットか」を特定（とくてい）する
        int selectedIndex = -1;
        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(selected)) {
                selectedIndex = i;
                break;
            }
        }

        // 4. そのスロットの中身（なかみ）が「からっぽ（null）」じゃないかチェック
        Item selectedItem = itemBox[selectedIndex];
        if (selectedItem == null) {
            logTextArea.append("そのスロットはからっぽです！\n");
            return;
        }
        if (selectedItem instanceof ThunderStick) {
            showThunderEffect();
        }

        if (selectedItem instanceof FireOrb) {
            showFireEffect();
        }

        // 5. アイテムを使用（しよう）し、使（つか）ったスロットをnullにして消（け）す
        String resultLog = selectedItem.use(player, enemy);
        itemBox[selectedIndex] = null; // ★ 使（つか）ったら消（け）える！
        logTextArea.append(resultLog + "\n");

        // 6. 敵（てき）が倒（たお）れたかチェック
        if (enemy.getHp() <= 0) {
            logTextArea.append(enemy.getName() + " を倒した！\n");
            // ★ 10%の確率（かくりつ）でアイテムドロップ
            if (Math.random() < 0.1) {
                addItemToBox(new Herb());
                logTextArea.append("★ " + enemy.getName() + " が薬草を落とした！\n");
       if (enemy.getHp() <= 0) {
    gold += 20;
    goldLabel.setText("Gold: " + gold + " G");
    logTextArea.append("Enemy defeated! +20 Gold!\n");
    showGoldPopup("+20 Gold!");
}
            }
            spawnEnemy();
        }
        updateDisplay();

        isSaving = false;
        this.requestFocusInWindow();
    });
        

        // 初期化
spawnEnemy();
updateBackground(); // <--- ठ्याक्कै यहाँ राख्नुहोस्!

// Restore saved enemy HP
if (data != null && data.enemyHp > 0
        && data.enemyHp <= enemy.getMaxHp()) {
    enemy.setHp(data.enemyHp);
        }


updateDisplay();
         playerHpBar.setBounds(40, 40, 250, 25);
playerHpBar.setMaximum(player.getMaxHp());
playerHpBar.setValue(player.getHp());
enemyHpBar.setMaximum(enemy.getMaxHp());
enemyHpBar.setValue(enemy.getHp());
playerHpBar.setStringPainted(true);
playerHpBar.setForeground(Color.GREEN);


backgroundLabel.add(playerHpBar);

        playerImageLabel.setIcon(player.getIcon());
       ImageIcon originalIcon = enemy.getIcon();

     Image resizedImage = originalIcon.getImage().getScaledInstance(
    220,
    330,
    Image.SCALE_SMOOTH
);

      enemyImageLabel.setIcon(new ImageIcon(resizedImage));
        updateDisplay();
        logTextArea.append("野生の" + enemy.getName() + " が現れた！\n");
    
    
enemyHpBar.setBounds(900, 40, 250, 25);
enemyHpBar.setMaximum(enemy.getMaxHp());
enemyHpBar.setValue(enemy.getHp());
enemyHpBar.setStringPainted(true);
enemyHpBar.setForeground(Color.RED);


backgroundLabel.add(enemyHpBar);
    enemyHpBar.setMaximum(enemy.getMaxHp());
enemyHpBar.setValue(enemy.getHp());
enemyHpBar.setMaximum(enemy.getMaxHp());
enemyHpBar.setValue(enemy.getHp());
startEnemyAI();

effectTimer = new Timer(30, e -> {
    boolean needRepaint = false;
    if (floatingTextAlpha > 0) {
        floatingTextY -= 2;
        floatingTextAlpha = Math.max(0, floatingTextAlpha - 10);
        needRepaint = true;
    }
    if (shakeFrames > 0) {
        shakeOffsetX = (shakeFrames % 2 == 0) ? 10 : -10;
        shakeFrames--;
        if (shakeFrames == 0) shakeOffsetX = 0;
        needRepaint = true;
    }
    if (needRepaint) {
        backgroundLabel.repaint();
    }
});
effectTimer.start();

 }
private void activateSafeZone() {

    if (safeZoneActive) {
        return;
    }

    if (safeZoneUseCount >= MAX_SAFE_ZONE_USE) {
        JOptionPane.showMessageDialog(
            this,
            "Safe Shield used 3 times in this stage!"
        );
        return;
    }

    safeZoneUseCount++;
    safeZoneActive = true;
    safeZoneSeconds = 3;

    safeZoneButton.setEnabled(false);

    // Only Attack and Potion allowed
    attackButton.setEnabled(true);
    potionButton.setEnabled(true);
    skillButton.setEnabled(true);
    itemButton.setEnabled(true);
    shopButton.setEnabled(true);

    shieldLabel = new JLabel(
        "🛡 SAFE SHIELD: 5 SEC",
        SwingConstants.CENTER
    );

    shieldLabel.setFont(
        new Font("Arial", Font.BOLD, 30)
    );

    shieldLabel.setForeground(Color.CYAN);
    shieldLabel.setBounds(350, 5, 500, 50);

    backgroundLabel.add(shieldLabel);
    backgroundLabel.setComponentZOrder(shieldLabel, 0);
    backgroundLabel.repaint();

    logTextArea.append(
        "🛡 SAFE SHIELD ACTIVATED!\n"
    );

    safeZoneTimer = new Timer(1000, e -> {

        safeZoneSeconds--;

        if (safeZoneSeconds > 0) {

            shieldLabel.setText(
                "🛡 SAFE SHIELD: "
                + safeZoneSeconds + " SEC"
            );

        } else {

            safeZoneTimer.stop();

            safeZoneActive = false;

            backgroundLabel.remove(shieldLabel);
            backgroundLabel.repaint();

            skillButton.setEnabled(true);
            itemButton.setEnabled(true);

            if (safeZoneUseCount < MAX_SAFE_ZONE_USE) {
                safeZoneButton.setEnabled(true);
            }

            safeZoneButton.setText(
                "SAFE SHIELD ("
                + (MAX_SAFE_ZONE_USE - safeZoneUseCount)
                + ")"
            );

            logTextArea.append(
                "SAFE SHIELD FINISHED!\n"
            );
        }
    });

    safeZoneTimer.start();
}



private static Player createPlayerFromSave(
        GameSaveManager.SaveData data) {

    String name = data.playerName;

    if (name == null || name.trim().isEmpty()) {
        name = "BHOLA";
    }

    String imagePath;

    switch (name.toUpperCase()) {
        case "BHOLA":
            imagePath = "BHOLA.png";
            break;

        case "BIYON":
            imagePath = "BIYON.png";
            break;

        case "LADYBOSS":
        case "LADY BOSS":
            imagePath = "ladyboss.png";
            break;

        case "BIGBOSS":
        case "BIG BOSS":
            imagePath = "bigboss.png";
            break;

        default:
            imagePath = "BHOLA.png";
            break;
    }

    Player p = new Player(
        name,
        data.playerMaxHp,
        data.playerAtk,
        data.playerDef,
        data.playerSpeed,
        30,
        imagePath
    );

    p.setHp(data.playerHp);
    p.setLevel(data.playerLevel);

    return p;
}
    
 
    // 他メソッド（updateDisplay, endGame, choicePlayer, spawnEnemy 等）はそのまま記述
    private void updateDisplay() {
        statusLabel.setText(String.format(
    "【%s】 Player Lv.%d | Stage %d | HP: %d/%d  vs  【%s】 Stage %d | HP: %d/%d",
    player.getName(),
    player.getLevel(),
    currentLevel,
    player.getHp(),
    player.getMaxHp(),
    enemy.getName(),
    currentLevel,
    enemy.getHp(),
    enemy.getMaxHp()
));
                
               playerHpBar.setMaximum(player.getMaxHp());
playerHpBar.setValue(player.getHp());

enemyHpBar.setMaximum(enemy.getMaxHp());
enemyHpBar.setValue(enemy.getHp());


if (player.getHp() > player.getMaxHp() * 0.6) {
    playerHpBar.setForeground(Color.GREEN);
} else if (player.getHp() > player.getMaxHp() * 0.3) {
    playerHpBar.setForeground(Color.ORANGE);
} else {
    playerHpBar.setForeground(Color.RED);
}

    if (enemy.getHp() > enemy.getMaxHp() * 0.6) {
    enemyHpBar.setForeground(Color.GREEN);
} else if (enemy.getHp() > enemy.getMaxHp() * 0.3) {
    enemyHpBar.setForeground(Color.ORANGE);
} else {
    enemyHpBar.setForeground(Color.RED);
}
    }
       

    private void endGame() {
        attackButton.setEnabled(false); // ボタンをむこうか
        logTextArea.append("【ゲームしゅうりょう】ウィンドウをとじてください。\n");
    }


    private void choicePlayer() {
         this.player = player;
        


        // せんたく（Select）ダイアログ（Dialog）をひょうじ（Display）（えらんだボタン（Button）のばんごう（Number）が 0, 1

        // でかえってくる）

        int choice = JOptionPane.showOptionDialog(

                this,
                "しよう（Use）するキャラクターをせんたく（Select）してください",
                "キャラクターせんたく（Select）",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] { "BHOLA（Hero）", "BIYON（Mage）", "ladyboss", "bigboss" },
                null);

        if (choice == 0) {
            player = new Player("BHOLA（Hero）", 200, 20, 30, 10, 30, "BHOLA.png");
        } else if (choice == 1) {
            player = new Player("BIYON（Mage）", 200, 25, 30, 15, 30, "BIYON.png");
        } else if (choice == 2) {
            player = new Player("LedyBoss", 200, 20, 30, 10, 30, "ladyboss.png");
        } else if (choice == 3) {
            player = new Player("boss", 200, 40, 20, 15, 30, "bigboss.png");
        }
    }

    
private void spawnEnemy() {
    int extraHp = (currentLevel - 1) * 30;
    int extraAtk = (currentLevel - 1) * 15;
   if (enemyCount == 0) {
        enemy = new Enemy("chotu", 100 + extraHp, 20 + extraAtk, 5, 5, 10, "chotuenemy.png");
        logTextArea.append("【だい（No.）1せん（Battle）】スライム があらわれた！\n");

    } else if (enemyCount == 1) {
        enemy = new Enemy("boss", 90 + extraHp, 15 + extraAtk, 5, 5, 10, "enemyboss.png");
        logTextArea.append("【だい（No.）2せん（Battle）】ゴブリン があらわれた！\n");

    } else if (enemyCount == 2) {
        enemy = new Enemy("tiger", 160 + extraHp, 24 + extraAtk, 5, 5, 10, "tiger.png");
        logTextArea.append("⚠ WARNING ⚠\nFINAL BOSS APPEARED!\n");

    } else if (enemyCount == 3) {
        enemy = new Enemy("lagartha", 180 + extraHp, 25 + extraAtk, 10, 10, 10, "lagartha.png");
        logTextArea.append("WARNING\nLAGARTHA IS COMING!\n");

    } else if (enemyCount == 4) {
        enemy = new Enemy("IRONBOSS", 200 + extraHp, 20 + extraAtk, 10, 7, 15, "ironman.png");
        logTextArea.append("WARNING\nIRONMAN AAGAYA!\n");

    } else if (enemyCount == 5) {
        enemy = new Enemy("MONSTAR", 200 + extraHp, 15 + extraAtk, 15, 8, 10, "MONSTAR.png");
        logTextArea.append("I'M THE MONSTAR!\n");
    } else if (enemyCount == 6) {
        enemy = new Enemy("BULBORIN", 200 + extraHp, 20 + extraAtk, 20, 8, 20, "bulborin.png");
        logTextArea.append("BULBORINiscomming!\n");
    }
    
    

    playerImageLabel.setIcon(player.getIcon());
    enemyImageLabel.setIcon(enemy.getIcon());

    updateDisplay();

    logTextArea.append("野生の" + enemy.getName() + " が現れた！\n");
    logTextArea.append("--------------------------------------------\n");

    setVisible(true);
}

   private void addItemToBox(Item item) {
    for (int i = 0; i < itemBox.length; i++) {
        if (itemBox[i] == null) {       // 空（あ）きスロットを発見（はっけん）！
            itemBox[i] = item;           // アイテムをセット
            logTextArea.append("アイテムボックスのスロット " + (i + 1) + " に " + item.getName() + " を追加しました。\n");
            return;                      // 1つ入れたら終了（しゅうりょう）
        }
    }
    // ここまで来たら全スロットが埋まっている
    logTextArea.append("アイテムボックスがいっぱいです！ " + item.getName() + " は拾えませんでした。\n");
}

// Keyboard Controls
private void setupKeyboardControls() {

    

    JRootPane root = getRootPane();

    // Arrow Keys (↑ ↓ ← →) बाट दायाँ, बायाँ, तल, माथि सार्ने
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("UP"), "up");

    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("DOWN"), "down");

    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("LEFT"), "left");

    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("RIGHT"), "right");

    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("SPACE"), "attack");

    root.getActionMap().put("up",
        new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(0, -20);
            }
        });

    root.getActionMap().put("down",
        new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(0, 20);
            }
        });

    root.getActionMap().put("left",
        new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(-20, 0);
            }
        });

    root.getActionMap().put("right",
        new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                movePlayer(20, 0);
            }
        });

    root.getActionMap().put("attack",
        new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                attackButton.doClick();
            }
        });

        root.getActionMap().put("playerAttack",
    new javax.swing.AbstractAction() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            attackButton.doClick();
        }
    });

    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("ENTER"), "activateSafeZone");

getRootPane().getActionMap().put("activateSafeZone",
        new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (safeZoneButton.isEnabled()) {
                    activateSafeZone();
                }
            }
        });
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("P"), "usePotion");

    root.getActionMap().put("usePotion",
        new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (potionButton.isEnabled()) {
                    potionButton.doClick();
                }
            }
        });
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("I"), "useItem");

    root.getActionMap().put("useItem",
        new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (itemButton.isEnabled()) {
                    itemButton.doClick();
                }
            }
        });

    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
         .put(KeyStroke.getKeyStroke("F"), "useSkill");

    root.getActionMap().put("useSkill",
        new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (skillButton.isEnabled()) {
                    skillButton.doClick();
                }
            }
        });
}

// Player Movement
private void movePlayer(int dx, int dy) {
    if (safeZoneActive) {
    return;
}

    int newX = playerImageLabel.getX() + dx;
    int newY = playerImageLabel.getY() + dy;

    int maxX = backgroundLabel.getWidth()
            - playerImageLabel.getWidth();

    int maxY = backgroundLabel.getHeight()
            - playerImageLabel.getHeight();

    newX = Math.max(0, Math.min(newX, maxX));
    newY = Math.max(0, Math.min(newY, maxY));

    playerImageLabel.setLocation(newX, newY);
}

   public static void main(String[] args) {

    SwingUtilities.invokeLater(() -> {

        Player player = new Player(
            "BHOLA（Hero）",
            200, 20, 20, 10, 30,
            "BHOLA.png"
        );

        BattleGame game = new BattleGame(player);
        game.setVisible(true);

    });

}
private void openShop() {
        String[] options = {
            "1. Potion (+1) - 40 G",
            "2. Safe Shield (+1) - 60 G",
            "3. Upgrade Attack (+10 ATK) - 100 G",
            "Close / बन्द गर्नुहोस्"
        };

        int choice = JOptionPane.showOptionDialog(
            this,
            "🛒 WELCOME TO THE SHOP! / ショップへようこそ\n"
            + "Your Current Gold: " + gold + " G\n\n"
            + "के किन्न चाहनुहुन्छ?",
            "Item Shop",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == 0) { // Potion किन्ने भाग
            if (gold >= 40) {
                gold -= 40;
                
                if (potionUseCount > 0) {
                    potionUseCount--; 
                }
                //player.addpotion(1); // वा player.setPotionCount(player.getPotionCount() + 1);
                
                int remainingPotions = MAX_POTION_USE - potionUseCount;
                potionButton.setText("POTION (" + remainingPotions + ")");
                potionButton.setEnabled(true); 
                
                JOptionPane.showMessageDialog(this, "Potion किनियो!");
            } else {
                JOptionPane.showMessageDialog(this, "गोल्ड पुगेन!");
            }
        }
        // =========================================================================
        else if (choice == 2) { // Attack Power
            if (gold >= 100) {
                gold -= 100;
                player.setAtk(player.getAtk() + 10);
                JOptionPane.showMessageDialog(this, "Attack Power +10 ले बढ्यो!");
            } else {
                JOptionPane.showMessageDialog(this, "गोल्ड पुगेन! (Not enough Gold)");
            }
        }
        // === ठ्याक्कै यहाँ राख्नुहोस् (पुरानो else if (choice == 1) लाई हटाएर) ===
        else if (choice == 1) { // Safe Shield किन्ने भाग
            if (gold >= 60) {
                gold -= 60;
                
                // १. प्रयोग भएको संख्या १ घटाउने
                safeZoneUseCount--; 
                
                // २. बाँकी कतिवटा छ भनेर हिसाब निकाल्ने
                int remaining = 3 - safeZoneUseCount;
                
                // ३. बटनको टेक्स्ट अपडेट गर्ने र बटनलाई फेरि अन (Enable) गर्ने
                safeZoneButton.setText("SAFE SHIELD (" + remaining + ")");
                safeZoneButton.setEnabled(true); 
                
                JOptionPane.showMessageDialog(this, "Safe Shield किनियो! अब " + remaining + " पटक बाँकी छ।");
            } else {
                JOptionPane.showMessageDialog(this, "गोल्ड पुगेन! (Not enough Gold)");
            }
        } 
        // =========================================================================
        else if (choice == 2) { // Attack Power
            if (gold >= 100) {
                gold -= 100;
                player.setAtk(player.getAtk() + 10);
                JOptionPane.showMessageDialog(this, "Attack Power +10 ले बढ्यो!");
            } else {
                JOptionPane.showMessageDialog(this, "गोल्ड पुगेन! (Not enough Gold)");
            }
        }  
        goldLabel.setText("Gold: " + gold + " G");
    }
   } 
   
    