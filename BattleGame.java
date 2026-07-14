import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.sound.sampled.*; 
import javax.swing.*;

public class BattleGame extends JFrame implements KeyListener {
    private JLabel statusLabel; 
    private JTextArea logTextArea; 

    private JLabel backgroundLabel; 
    private JLabel playerImageLabel; 
    private JLabel enemyImageLabel; 

    private JLabel playerDamageLabel;
    private JLabel enemyDamageLabel;

    private JProgressBar playerHpBar;
    private JProgressBar playerMpBar;
    private JProgressBar enemyHpBar;

    private JButton bigGameClearButton;
    private JButton bigGameOverButton;

    private JButton attackButton;
    private JButton skillButton; 
    private JButton defendButton;
    private JButton healButton;
    private JButton runButton;
    
    private JButton nextStageButton;
    private JButton restartButton;

    private JPanel topShopPanel;     
    private JLabel goldDisplayLabel; 
    private JButton buyPotionButton; 
    private JButton buySwordButton;  
    private JLabel goldAnimLabel;    

    // --- START SCREEN VARIABLES ---
    private JLabel startScreenLabel;
    private JPanel gamePlayPanel; 
    private boolean isGameStarted = false;

    private Player player;
    private Enemy enemy;
    private int enemyCount = 1;
    private int retryCount = 0;
    private static int playerWins = 0;

    private int itemUsesLeft = 3; 
    private boolean isPlayerDefending = false; 
    private int comboCount = 0;

    private static boolean isWizardUnlocked = false; 
    private static boolean isHumanUnlocked = false; 

    private static int heroLevel = 1;
    private static int wizardLevel = 1;
    private static int humanLevel = 1;
    private String currentSelectedType = "hero";

    // --- SHOP LOGIC VARIABLES ---
    private int playerGold = 0;      
    private int attackBuffCount = 0;  

    private int playerX = 80;
    private final int playerY = 150; 
    private int enemyX = 700;
    private final int enemyY = 150;
    
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private final int SPEED = 14; 

    private boolean isAttacking = false;
    private int enemyOriginalX = 700;
    private int shakeFrame = 0;
    private boolean isGameOverState = false;

    private final int WINDOW_WIDTH = 1220;
    private final int WINDOW_HEIGHT = 1050;

    private Clip bgmClip; 

    public BattleGame() {
        setTitle("本格RPG - ハイブリッド・アンロックバトルシステム + Visual FX");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new CardLayout()); 

        // ==========================================
        // १. START SCREEN process
        // ==========================================
        JPanel mainStartPanel = new JPanel(new GridBagLayout()); 
        mainStartPanel.setBackground(Color.decode("#1a1c23")); 

        startScreenLabel = new JLabel();
        startScreenLabel.setLayout(null);
        
        try {
            ImageIcon startIcon = new ImageIcon("start.png");
            int imgWidth = 570;  
            int imgHeight = 970; 
            
            Image scaledStart = startIcon.getImage().getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
            startScreenLabel.setIcon(new ImageIcon(scaledStart));
            startScreenLabel.setPreferredSize(new Dimension(imgWidth, imgHeight));
        } catch (Exception e) {
            System.out.println("start.png not found");
        }

        startScreenLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isGameStarted) return;
                
                int clickY = e.getY();
                int labelHeight = startScreenLabel.getHeight();
                
                if (clickY >= (labelHeight * 0.79) && clickY <= (labelHeight * 0.86)) {
                    isGameStarted = true;
                    showRulesDialog(); 
                    choicePlayer(); 
                    initGameSession(false);
                    
                    CardLayout cl = (CardLayout) getContentPane().getLayout();
                    cl.show(getContentPane(), "GAMEPLAY");
                    BattleGame.this.requestFocusInWindow(); 
                }
                else if (clickY >= (labelHeight * 0.89) && clickY <= (labelHeight * 0.96)) {
                    System.exit(0); 
                }
            }
        });

        mainStartPanel.add(startScreenLabel);

        // ==========================================
        // २. GAMEPLAY PANEL
        // ==========================================
        gamePlayPanel = new JPanel(new BorderLayout());

        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(null); 
        
        setBackgroundImage("fristbackgroud.png");

        // 🛒 SCREEN KO MATHI GOLD RA SHOP BUTTON PANEL SETTING
        topShopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        topShopPanel.setBackground(new Color(0, 0, 0, 150)); 
        topShopPanel.setBounds(0, 0, WINDOW_WIDTH, 50); 

        goldDisplayLabel = new JLabel("💰 Gold: 0 G");
        goldDisplayLabel.setFont(new Font("MS ゴシック", Font.BOLD, 18));
        goldDisplayLabel.setForeground(Color.YELLOW);

        buyPotionButton = new JButton("💚 ポーション (+1回) : 50G");
        buyPotionButton.setBackground(Color.decode("#228B22"));
        buyPotionButton.setForeground(Color.WHITE);
        buyPotionButton.setFocusable(false);
        buyPotionButton.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        buySwordButton = new JButton("⚔️ 伝説の剣 (ATK +15) : 100G");
        buySwordButton.setBackground(Color.decode("#B22222"));
        buySwordButton.setForeground(Color.WHITE);
        buySwordButton.setFocusable(false);
        buySwordButton.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        buyPotionButton.addActionListener(e -> {
            if (playerGold >= 50) {
                playerGold -= 50;
                itemUsesLeft++;
                healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")");
                playSE("hp.wav");
                logTextArea.append("🛒 [SHOP] ポーションを購入しました！(残り: " + itemUsesLeft + "回)\n");
                updateDisplay();
            } else {
                logTextArea.append("❌ [SHOP] ゴールドが足りません！\n");
            }
        });

        buySwordButton.addActionListener(e -> {
            if (playerGold >= 100) {
                playerGold -= 100;
                attackBuffCount++;
                playSE("hp.wav");
                logTextArea.append("🛒 [SHOP] 伝説の剣を購入！攻撃力 +15 (合計バフ: +" + (attackBuffCount * 15) + ")\n");
                updateDisplay();
            } else {
                logTextArea.append("❌ [SHOP] ゴールドが足りません！\n");
            }
        });

        topShopPanel.add(goldDisplayLabel);
        topShopPanel.add(buyPotionButton);
        topShopPanel.add(buySwordButton);
        backgroundLabel.add(topShopPanel);

        // 💰 GOLD COLLECT ANIMATION LABEL
        goldAnimLabel = new JLabel("", JLabel.CENTER);
        goldAnimLabel.setFont(new Font("Arial", Font.BOLD, 36));
        goldAnimLabel.setForeground(Color.YELLOW);
        goldAnimLabel.setBounds((WINDOW_WIDTH - 300) / 2, 70, 300, 50); 
        backgroundLabel.add(goldAnimLabel);

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        playerImageLabel.setBounds(playerX, playerY, 500, 500); 
        enemyImageLabel.setBounds(enemyX, enemyY, 500, 500); 

        playerDamageLabel = new JLabel("", JLabel.CENTER);
        playerDamageLabel.setFont(new Font("Arial", Font.BOLD, 42));
        playerDamageLabel.setForeground(Color.RED);
        playerDamageLabel.setBounds(playerX + 150, playerY - 40, 200, 50);

        enemyDamageLabel = new JLabel("", JLabel.CENTER);
        enemyDamageLabel.setFont(new Font("Arial", Font.BOLD, 42));
        enemyDamageLabel.setForeground(Color.ORANGE);
        enemyDamageLabel.setBounds(enemyX + 150, enemyY - 40, 200, 50);

        backgroundLabel.add(playerDamageLabel);
        backgroundLabel.add(enemyDamageLabel);

        try {
            ImageIcon clearIcon = new ImageIcon("game clear.png");
            Image scaledClear = clearIcon.getImage().getScaledInstance(800, 350, Image.SCALE_SMOOTH);
            bigGameClearButton = new JButton(new ImageIcon(scaledClear));
        } catch (Exception e) {
            bigGameClearButton = new JButton("🎉 STAGE CLEAR");
        }
        bigGameClearButton.setBounds((WINDOW_WIDTH - 800) / 2, 300, 800, 350); 
        bigGameClearButton.setBorderPainted(false);
        bigGameClearButton.setContentAreaFilled(false);
        bigGameClearButton.setFocusable(false);
        bigGameClearButton.setVisible(false);

        try {
            ImageIcon overIcon = new ImageIcon("game over.png");
            Image scaledOver = overIcon.getImage().getScaledInstance(800, 350, Image.SCALE_SMOOTH);
            bigGameOverButton = new JButton(new ImageIcon(scaledOver));
        } catch (Exception e) {
            bigGameOverButton = new JButton("💀 GAME OVER");
        }
        bigGameOverButton.setBounds((WINDOW_WIDTH - 800) / 2, 300, 800, 350);
        bigGameOverButton.setBorderPainted(false);
        bigGameOverButton.setContentAreaFilled(false);
        bigGameOverButton.setFocusable(false);
        bigGameOverButton.setVisible(false);

        backgroundLabel.add(bigGameClearButton);
        bigGameOverButton.addActionListener(e -> resetToTitle());
        backgroundLabel.add(bigGameOverButton);
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("【操作】 SPACE : 攻撃 | S : スキル | D : 防御 | H : 回復 | R : 逃げる", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

        JPanel barsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        barsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        playerHpBar = new JProgressBar(0, 100);
        playerHpBar.setStringPainted(true);
        playerHpBar.setForeground(new Color(34, 139, 34)); 
        playerHpBar.setBackground(Color.RED);
        
        playerMpBar = new JProgressBar(0, 100);
        playerMpBar.setStringPainted(true);
        playerMpBar.setForeground(new Color(0, 191, 255)); 
        playerMpBar.setBackground(Color.DARK_GRAY);

        enemyHpBar = new JProgressBar(0, 100);
        enemyHpBar.setStringPainted(true);
        enemyHpBar.setForeground(new Color(148, 0, 211)); 
        enemyHpBar.setBackground(Color.RED);

        barsPanel.add(new JLabel("プレイヤー HP:", JLabel.RIGHT)); barsPanel.add(playerHpBar);
        barsPanel.add(new JLabel("プレイヤー MP:", JLabel.RIGHT)); barsPanel.add(playerMpBar);
        barsPanel.add(new JLabel("敵 HP:", JLabel.RIGHT)); barsPanel.add(enemyHpBar);

        logTextArea = new JTextArea(10, 30); 
        logTextArea.setEditable(false); 
        logTextArea.setFont(new Font("MS ゴシック", Font.PLAIN, 15));
        logTextArea.setBackground(Color.BLACK); 
        logTextArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(logTextArea); 

        JPanel buttonPanel = new JPanel(new GridLayout(1, 7, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        attackButton = new JButton("⚔️ 攻撃");
        attackButton.setBackground(Color.decode("#A30000")); 
        attackButton.setForeground(Color.WHITE);

        skillButton = new JButton("✨ スキル (MP 40)");
        skillButton.setBackground(Color.decode("#8A2BE2"));
        skillButton.setForeground(Color.WHITE);

        defendButton = new JButton("🛡️ 防御");
        defendButton.setBackground(Color.decode("#004080")); 
        defendButton.setForeground(Color.WHITE);

        healButton = new JButton("💚 回復 (残り: " + itemUsesLeft + ")");
        healButton.setBackground(Color.decode("#006400")); 
        healButton.setForeground(Color.WHITE);

        runButton = new JButton("🏃 逃げる");
        runButton.setBackground(Color.decode("#5A5A5A")); 
        runButton.setForeground(Color.WHITE);

        nextStageButton = new JButton("⏭️ 次へ");
        nextStageButton.setBackground(Color.decode("#D2691E")); 
        nextStageButton.setForeground(Color.WHITE);
        nextStageButton.setVisible(false);

        restartButton = new JButton("🔄 キャラ選択へ");
        restartButton.setBackground(Color.decode("#2F4F4F"));
        restartButton.setForeground(Color.WHITE);
        restartButton.setVisible(false);

        JButton[] allButtons = {attackButton, skillButton, defendButton, healButton, runButton, nextStageButton, restartButton};
        Font btnFont = new Font("MS ゴシック", Font.BOLD, 16); 

        for (JButton btn : allButtons) {
            btn.setFont(btnFont);
            btn.setFocusPainted(false);
            btn.setFocusable(false); 
        }

        attackButton.addActionListener(e -> handleAttackAction());
        skillButton.addActionListener(e -> handleSkillAction());
        defendButton.addActionListener(e -> handleDefendAction());
        healButton.addActionListener(e -> useHealItem());
        runButton.addActionListener(e -> triggerRunDialog());
        nextStageButton.addActionListener(e -> goToNextStage());
        restartButton.addActionListener(e -> resetToTitle());

        bigGameClearButton.addActionListener(e -> {
            if (enemyCount >= 4) resetToTitle();
            else goToNextStage();
        });

        buttonPanel.add(attackButton);
        buttonPanel.add(skillButton);
        buttonPanel.add(defendButton);
        buttonPanel.add(healButton);
        buttonPanel.add(runButton);
        buttonPanel.add(nextStageButton);
        buttonPanel.add(restartButton);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(barsPanel, BorderLayout.CENTER);
        
        JPanel logAndButtonPanel = new JPanel(new BorderLayout());
        logAndButtonPanel.add(scrollPane, BorderLayout.CENTER);
        logAndButtonPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.add(logAndButtonPanel, BorderLayout.SOUTH);

        gamePlayPanel.add(backgroundLabel, BorderLayout.CENTER); 
        gamePlayPanel.add(bottomPanel, BorderLayout.SOUTH); 

        add(mainStartPanel, "START_SCREEN");
        add(gamePlayPanel, "GAMEPLAY");

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        Timer gameLoop = new Timer(16, e -> {
            if (isGameStarted) {
                updateMovement();
                updateAnimation();
            }
        });
        gameLoop.start();
        
        playBGM("backgroundmusic.wav");
    }

    private void playBGM(String fileName) {
        try {
            File soundFile = new File(fileName);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioStream);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY); 
                bgmClip.start();
            }
        } catch (Exception e) {
            System.out.println("Error playing BGM: " + e.getMessage());
        }
    }

    private void playSE(String fileName) {
        try {
            File soundFile = new File(fileName);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip seClip = AudioSystem.getClip();
                seClip.open(audioStream);
                seClip.start();
            }
        } catch (Exception e) {
            System.out.println("Error playing SE: " + e.getMessage());
        }
    }

    private void showDamageText(JLabel label, String text) {
        label.setText(text);
        Timer timer = new Timer(800, e -> label.setText(""));
        timer.setRepeats(false);
        timer.start();
    }

    private void setBackgroundImage(String fileName) {
        try {
            ImageIcon bgIcon = new ImageIcon(fileName);
            Image scaledBg = bgIcon.getImage().getScaledInstance(WINDOW_WIDTH, WINDOW_HEIGHT - 250, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(scaledBg));
        } catch (Exception e) {
            System.out.println("Background image load failed: " + fileName);
        }
    }

    private void showRulesDialog() {
        String rules = "【ゲームのルールと操作方法】\n\n"
                     + "1. ターン制のバトルシステムです。\n"
                     + "2. 攻撃(SPACE)を続けるとコンボが繋がり、大ダメージ！\n"
                     + "3. スキル(S)はMPを40消費して2.5倍の大ダメージ！\n"
                     + "4. 防御(D)をすると敵のダメージを減らし、MPが回復します。\n"
                     + "5. 回復(H)はHPを100回復しますが、回数制限(3回)があります。\n"
                     + "6. キャラクターのレベルが上がると能力が強化され、新しい英雄がアンロックされます！\n"
                     + "※ 敵もプレイヤーのレベルに合わせて強力になり、HP減少で暴走(RAGE)します！";
        JOptionPane.showMessageDialog(this, rules, "📢 遊び方ガイド", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateMovement() {
        if (isGameOverState) return;
        if (moveLeft && playerX > 10) playerX -= SPEED;
        if (moveRight && playerX < 600) playerX += SPEED;
        playerImageLabel.setLocation(playerX, playerY);
    }
        
    private void updateAnimation() {
        if (shakeFrame > 0) {
            shakeFrame--;
            if (shakeFrame % 2 == 0) {
                enemyImageLabel.setLocation(enemyOriginalX + 15, enemyY);
            } else {
                enemyImageLabel.setLocation(enemyOriginalX - 15, enemyY);
            }
            if (shakeFrame == 0) enemyImageLabel.setLocation(enemyOriginalX, enemyY); 
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isGameStarted) return; 
        
        if (isGameOverState) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && nextStageButton.isVisible()) goToNextStage();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && restartButton.isVisible()) resetToTitle();
            return;
        }

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) moveLeft = true;
        if (key == KeyEvent.VK_RIGHT) moveRight = true;
        
        if (key == KeyEvent.VK_SPACE) handleAttackAction();
        if (key == KeyEvent.VK_S) handleSkillAction();
        if (key == KeyEvent.VK_D) handleDefendAction();
        if (key == KeyEvent.VK_H) useHealItem();
        if (key == KeyEvent.VK_R) triggerRunDialog();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) moveLeft = false;
        if (key == KeyEvent.VK_RIGHT) moveRight = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void handleAttackAction() {
        if (isGameOverState || isAttacking) return;
        animateAttackAndExecute(false);
    }

    private void handleSkillAction() {
        if (isGameOverState || isAttacking) return;
        if (player.getMp() < 40) {
            logTextArea.append("⚠ MPが足りません！ (必要MP: 40)\n");
            return;
        }
        player.setMp(player.getMp() - 40);
        animateAttackAndExecute(true);
    }

    private void animateAttackAndExecute(boolean isSkill) {
        isAttacking = true;
        int originalX = playerX;
        
        Timer forwardTimer = new Timer(10, null);
        forwardTimer.addActionListener(e -> {
            playerX += 25; 
            if (playerX >= originalX + 150) {
                ((Timer)e.getSource()).stop();
                
                executePlayerTurn(isSkill);
                
                Timer backwardTimer = new Timer(10, null);
                backwardTimer.addActionListener(ev -> {
                    playerX -= 15;
                    if (playerX <= originalX) {
                        playerX = originalX;
                        ((Timer)ev.getSource()).stop();
                        isAttacking = false;
                    }
                });
                backwardTimer.start();
            }
        });
        forwardTimer.start();
    }

    private void handleDefendAction() {
        if (isGameOverState) return;
        isPlayerDefending = true;
        int mpRecovery = player.getName().contains("wizard") && player.getLevel() >= 5 ? 35 : 20;
        player.setMp(Math.min(100, player.getMp() + mpRecovery)); 
        logTextArea.append("🛡️ " + player.getName() + " は身を護っている！ (MP+" + mpRecovery + ")\n");
        comboCount = 0; 
        updateDisplay();
        executeEnemyTurn();
    }

    private void checkUnlockConditions() {
        if (heroLevel >= 3 && !isWizardUnlocked) {
            isWizardUnlocked = true;
            triggerFlashEffect(Color.CYAN); 
            logTextArea.append("\n👑 【UNLOCK】 ゆうしゃのレベルが3に達したため『wizard』が解放されました！\n\n");
        }
        if (wizardLevel >= 4 && !isHumanUnlocked) {
            isHumanUnlocked = true;
            triggerFlashEffect(new Color(255, 215, 0)); 
            logTextArea.append("\n👑 【UNLOCK】 wizardのレベルが4に達したため『human』が解放されました！\n\n");
        }
    }

    private void triggerRunDialog() {
        if (isGameOverState) return;
        logTextArea.append("🏃 " + player.getName() + " は戦場から離脱した！\n");
        showGameClearOrEndScreen(false, "🏃 戦場から離脱しました！タイトルに戻って仕切り直しましょう。");
    }

    private void executePlayerTurn(boolean isSkill) {
        shakeFrame = 12; 
        playSE("pouch.wav");

        if (player.getName().contains("wizard")) {
            int passiveMp = player.getLevel() >= 3 ? 10 : 5;
            player.setMp(Math.min(100, player.getMp() + passiveMp));
        }

        double critChance = 0.15;
        if (player.getName().contains("human")) {
            critChance = player.getLevel() >= 5 ? 0.40 : 0.30;
        }
        
        boolean isCritical = Math.random() < critChance && !isSkill;
        
        int baseDamage = (int)(player.getAtk() * (0.9 + Math.random() * 0.2));
        if (isSkill) baseDamage *= 2.5; 
        
        baseDamage += (attackBuffCount * 15);

        int finalDamage = isCritical ? baseDamage * 2 : baseDamage;

        enemy.setHp(Math.max(0, enemy.getHp() - finalDamage));
        if (!isSkill) player.setMp(Math.min(100, player.getMp() + 15));

        showDamageText(enemyDamageLabel, "-" + finalDamage);

        if (isSkill) {
            triggerFlashEffect(Color.MAGENTA);
            logTextArea.append("✨【SKILL】 " + player.getName() + " の大魔法が炸裂！ " + enemy.getName() + " に " + finalDamage + " のダメージ！\n");
        } else if (isCritical) {
            triggerFlashEffect(Color.YELLOW); 
            logTextArea.append("💥 【CRITICAL】 " + player.getName() + " の会心の一撃！ " + enemy.getName() + " に " + finalDamage + " のダメージ！\n");
            comboCount++; 
        } else {
            logTextArea.append("⚔️ " + player.getName() + " の攻撃！ " + enemy.getName() + " に " + finalDamage + " のダメージを与えた！\n");
            comboCount++; 
        }
        updateDisplay();

        if (!enemy.isAlive()) {
            logTextArea.append("\n★━━━━━━━━━━━━━━━━━━━━★\n");
            logTextArea.append("★ " + enemy.getName() + " を完全に見事に倒した！\n");
            logTextArea.append("★━━━━━━━━━━━━━━━━━━━━★\n\n");
            playerWins++;
            
            int droppedGold = 50 + (enemyCount * 30);
            playerGold += droppedGold;

            goldAnimLabel.setText("💰 +" + droppedGold + " Gold!");
            Timer goldFadeTimer = new Timer(1500, ev -> {
                goldAnimLabel.setText("");
                updateDisplay();
            });
            goldFadeTimer.setRepeats(false);
            goldFadeTimer.start();

            logTextArea.append("💰 " + droppedGold + " ゴールドを獲得した！\n");

            player.levelUp(); 

            if (currentSelectedType.equals("hero")) heroLevel = player.getLevel();
            else if (currentSelectedType.equals("wizard")) wizardLevel = player.getLevel();
            else if (currentSelectedType.equals("human")) humanLevel = player.getLevel();

            logTextArea.append("✨✨【LEVEL UP】✨✨\n" + player.getName() + " は レベル: " + player.getLevel() + " に上がった！\n");
            logTextArea.append("👉 (HP と 攻撃力 が大幅にアップしました！)\n\n");

            checkUnlockConditions();
            showGameClearOrEndScreen(true, "🎉 STAGE CLEAR! 次のステージへ進むか、上のショップで強化しましょう！");
            return;
        }

        Timer delayTimer = new Timer(400, ev -> executeEnemyTurn());
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private void useHealItem() {
        if (isGameOverState) return;
        if (itemUsesLeft <= 0) {
            logTextArea.append("⚠ アイテムがなくなりました！\n");
            return;
        }
        if (player.getHp() >= player.getMaxHp()) {
            logTextArea.append("⚠ HPはすでに満タンです！\n");
            return;
        }

        playSE("hp.wav");
        itemUsesLeft--;
        healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")"); 
        
        int previousHp = player.getHp();
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + 100));
        int healedAmount = player.getHp() - previousHp;
        
        logTextArea.append("💚 アイテムを使用！ " + player.getName() + " のHPが " + healedAmount + " 回復した！\n");
        
        playerDamageLabel.setForeground(new Color(34, 139, 34)); 
        showDamageText(playerDamageLabel, "+" + healedAmount);                 

        triggerFlashEffect(Color.GREEN); 
        comboCount = 0; 
        updateDisplay();

        Timer delayEnemyTimer = new Timer(1000, ev -> {
            executeEnemyTurn();
        });
        delayEnemyTimer.setRepeats(false);
        delayEnemyTimer.start();
    }

    private void executeEnemyTurn() {
        if (!enemy.isAlive() || isGameOverState) return;
        
        double enemyHpPercent = (double) enemy.getHp() / enemy.getMaxHp();
        boolean isEnemyEnraged = enemyHpPercent <= 0.40; 

        int originalAtk = enemy.getAtk(); 
        
        if (isEnemyEnraged) {
            triggerFlashEffect(Color.RED);
            logTextArea.append("🔥 【RAGE MODE】 " + enemy.getName() + " の攻撃力が1.5倍に暴走中！\n");
            enemy.setAtk((int)(originalAtk * 1.5)); 
        }

        boolean passiveGuard = player.getName().contains("ゆうしゃ") && player.getHp() <= (player.getMaxHp() * 0.3);
        boolean playerGuarding = isPlayerDefending || passiveGuard;

        boolean shieldBreakerAttack = Math.random() < 0.35 && isEnemyEnraged;
        boolean lifeStealAttack = Math.random() < 0.30 && !shieldBreakerAttack;

        if (shieldBreakerAttack) {
            logTextArea.append("💥 【SHIELD BREAKER】 " + enemy.getName() + " がガードを貫通する一撃を放った！\n");
            playerGuarding = false; 
        }

        int oldHp = player.getHp();
        String enemyResult = enemy.attack(player, playerGuarding);
        int damageTaken = oldHp - player.getHp();
        
        if (lifeStealAttack && damageTaken > 0) {
            int healAmount = (int)(damageTaken * 0.4);
            enemy.setHp(Math.min(enemy.getMaxHp(), enemy.getHp() + healAmount));
            logTextArea.append("🧛 【LIFE STEAL】 " + enemy.getName() + " はプレイヤーのHPを吸収した！ (敵 HP +" + healAmount + ")\n");
            enemyDamageLabel.setForeground(new Color(34, 139, 34));
            showDamageText(enemyDamageLabel, "+" + healAmount);
        }

        if (damageTaken > 0) {
            playerDamageLabel.setForeground(Color.RED);
            showDamageText(playerDamageLabel, "-" + damageTaken);
        }

        logTextArea.append(enemyResult);
        
        enemy.setAtk(originalAtk);

        if (!isPlayerDefending) comboCount = 0;
        isPlayerDefending = false; 
        updateDisplay();

        if (!player.isAlive()) {
            retryCount++;
            if (retryCount == 1) {
                logTextArea.append("💀 覚醒：最後の希望として human が参戦する！\n");
                int calculatedHp = 300 + (humanLevel - 1) * 35;
                int calculatedAtk = 130 + (humanLevel - 1) * 15;
                player = new Player("human (human)", calculatedHp, calculatedAtk, 10, 50, "human.png");
                player.setLevel(humanLevel); 
                
                enemy = new Enemy("裏ボス (BOSS - TRUE FORM)", 2500, 60, 10, 50, "dragon.png"); 
                playerX = 80;
                updateDisplay();
            } else {
                showGameClearOrEndScreen(false, "💀 GAME OVER... 敗北しました。タイトルに戻ってレベルを確認しましょう。"); 
            }
        }
    }

    private void showGameClearOrEndScreen(boolean isWin, String message) {
        isGameOverState = true;
        logTextArea.setText("\n==================================\n" + message + "\n==================================\n");
        
        attackButton.setVisible(false);
        skillButton.setVisible(false);
        defendButton.setVisible(false);
        healButton.setVisible(false);
        runButton.setVisible(false);

        if (isWin) {
            triggerFlashEffect(Color.YELLOW);
            bigGameClearButton.setVisible(true); 
            if (enemyCount < 4) {
                nextStageButton.setVisible(true);
            } else {
                restartButton.setVisible(true);
            }
        } else {
            triggerFlashEffect(Color.BLACK);
            bigGameOverButton.setVisible(true); 
            restartButton.setVisible(true);
        }
    }

    private void goToNextStage() {
        enemyCount++;
        isGameOverState = false;
        nextStageButton.setVisible(false);
        bigGameClearButton.setVisible(false); 
        
        attackButton.setVisible(true);
        skillButton.setVisible(true);
        defendButton.setVisible(true);
        healButton.setVisible(true);
        runButton.setVisible(true);

        playerX = 80;
        spawnEnemy();
        updateDisplay();
        requestFocusInWindow();
    }

    private void resetToTitle() {
        enemyCount = 1;
        isGameOverState = false;
        playerGold = 0;         
        attackBuffCount = 0;    
        
        attackButton.setVisible(true);
        skillButton.setVisible(true);
        defendButton.setVisible(true);
        healButton.setVisible(true);
        runButton.setVisible(true);

        restartButton.setVisible(false);
        bigGameClearButton.setVisible(false); 
        bigGameOverButton.setVisible(false); 

        initGameSession(true); 

        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "GAMEPLAY");
        this.requestFocusInWindow();
    }

    private void triggerFlashEffect(Color color) {
        JPanel flashPanel = new JPanel();
        flashPanel.setBackground(color);
        flashPanel.setBounds(0, 0, getWidth(), getHeight());
        backgroundLabel.add(flashPanel);
        backgroundLabel.repaint();
        Timer timer = new Timer(150, e -> { backgroundLabel.remove(flashPanel); backgroundLabel.repaint(); });
        timer.setRepeats(false);
        timer.start();
    }

    private void initGameSession(boolean needToChooseCharacter) {
        playerX = 80; itemUsesLeft = 3; retryCount = 0; isPlayerDefending = false; comboCount = 0; 
        healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")"); 

        if (needToChooseCharacter) choicePlayer(); 
        spawnEnemy();
        updateDisplay();
        logTextArea.setText(""); 
        logTextArea.append("\n⚔--- ステージ " + enemyCount + " 開始！ ---\n");
    }

    private void updateDisplay() {
        if (player == null || enemy == null) return; 
        
        playerHpBar.setMaximum(player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerHpBar.setString(player.getHp() + " / " + player.getMaxHp());

        playerMpBar.setMaximum(100);
        playerMpBar.setValue(player.getMp());
        playerMpBar.setString(player.getMp() + " / 100");

        enemyHpBar.setMaximum(enemy.getMaxHp());
        enemyHpBar.setValue(enemy.getHp());
        enemyHpBar.setString(enemy.getHp() + " / " + enemy.getMaxHp());

        statusLabel.setText(String.format(
                "【%s (Lv.%d)】[%d Combo]  |  敵: 【%s】",
                player.getName(), player.getLevel(), comboCount, enemy.getName()));

        if (goldDisplayLabel != null) {
            goldDisplayLabel.setText("💰 Gold: " + playerGold + " G ");
        }
    }

    private void choicePlayer() {
        String option1 = "ゆうしゃ (Lv." + heroLevel + ") [開放済み]";
        String option2 = isWizardUnlocked ? "wizard (Lv." + wizardLevel + ") [開放済み]" : "🔒 wizard (ゆうしゃLv.3で開放)";
        String option3 = isHumanUnlocked ? "human (Lv." + humanLevel + ") [開放済み]" : "🔒 human (wizardLv.4で開放)";

        while (true) {
            int choice = JOptionPane.showOptionDialog(this, "使用するキャラクターを選択してください", "キャラクター選択", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
                    new String[] { option1, option2, option3 }, null);
            
            if (choice == JOptionPane.CLOSED_OPTION || choice == 0) {
                int calculatedHp = 100 + (heroLevel - 1) * 20; 
                int calculatedAtk = 120 + (heroLevel - 1) * 10; 
                player = new Player("ゆうしゃ（Hero）", calculatedHp, calculatedAtk, 10, 10, "k night pyayer.png");
                player.setLevel(heroLevel); 
                currentSelectedType = "hero";
                break;
            } else if (choice == 1) {
                if (isWizardUnlocked) {
                    int calculatedHp = 80 + (wizardLevel - 1) * 15;  
                    int calculatedAtk = 125 + (wizardLevel - 1) * 12; 
                    player = new Player("wizard（wizard）", calculatedHp, calculatedAtk, 10, 10, "wizard player.png");
                    player.setLevel(wizardLevel); 
                    currentSelectedType = "wizard";
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "❌ wizardはまだロックされています！\nゆうしゃのレベルを3以上にしてください。", "ロック中", JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 2) {
                if (isHumanUnlocked) {
                    int calculatedHp = 300 + (humanLevel - 1) * 35;  
                    int calculatedAtk = 130 + (humanLevel - 1) * 15; 
                    player = new Player("human (human)", calculatedHp, calculatedAtk, 10, 50, "human.png");
                    player.setLevel(humanLevel); 
                    currentSelectedType = "human";
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "❌ humanはまだロックされています！\nwizardのレベルを4以上にしてください。", "ロック中", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        try {
            ImageIcon playerIcon = new ImageIcon(player.getImagePath());
            playerImageLabel.setIcon(new ImageIcon(playerIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.out.println("Player image load failed!");
        }
    }

    private void spawnEnemy() {
        int currentMaxLevel = Math.max(heroLevel, Math.max(wizardLevel, humanLevel));
        double levelMultiplier = 1.0 + (currentMaxLevel - 1) * 0.15; 

        if (enemyCount == 1) {
            int hp = (int)(150 * levelMultiplier);
            int atk = (int)(18 * levelMultiplier);
            enemy = new Enemy("スライム (Lv." + currentMaxLevel + ")", hp, atk, 25, 20, "smile enamy.png"); 
            setBackgroundImage("fristbackgroud.png"); 
        } else if (enemyCount == 2) {
            int hp = (int)(300 * levelMultiplier);
            int atk = (int)(28 * levelMultiplier);
            enemy = new Enemy("ゴブリン (Lv." + currentMaxLevel + ")", hp, atk, 25, 20, "dargon enamy.png"); 
            setBackgroundImage("second backgroud.png"); 
        } else if (enemyCount == 3) {
            int hp = (int)(500 * levelMultiplier);
            int atk = (int)(42 * levelMultiplier);
            enemy = new Enemy("オークキング (Lv." + currentMaxLevel + ")", hp, atk, 25, 20, "Demon enamy.png"); 
            setBackgroundImage("backgroundthree.png"); 
        } else { 
            int hp = (int)(1000 * levelMultiplier);
            int atk = (int)(65 * levelMultiplier);
            enemy = new Enemy("魔王 (BOSS - FINAL FORM)", hp, atk, 25, 20, "dargon.png"); 
            setBackgroundImage("final backgroud.png"); 
        }

        try {
            ImageIcon enemyIcon = new ImageIcon(enemy.getImagePath());
            enemyImageLabel.setIcon(new ImageIcon(enemyIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.out.println("Enemy image load failed!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BattleGame game = new BattleGame();
            game.setVisible(true);
        });
    }
}