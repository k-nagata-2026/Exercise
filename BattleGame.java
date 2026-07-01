import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

public class BattleGame extends JFrame implements KeyListener {
    private JLabel statusLabel; 
    private JTextArea logTextArea; 

    private JLabel backgroundLabel; 
    private JLabel playerImageLabel; 
    private JLabel enemyImageLabel; 

    private JButton bigGameClearButton;
    private JButton bigGameOverButton;

    private JButton attackButton;
    private JButton defendButton;
    private JButton healButton;
    private JButton runButton;
    
    private JButton nextStageButton;
    private JButton restartButton;

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

    public BattleGame() {
        setTitle("本格RPG - ハイブリッド・アンロックバトルシステム");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout()); 

        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(null); 
        setBackgroundImage("first_background.png");

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        playerImageLabel.setBounds(playerX, playerY, 500, 500); 
        enemyImageLabel.setBounds(enemyX, enemyY, 500, 500); 

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
        backgroundLabel.add(bigGameOverButton);
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("【操作】 SPACE : 攻撃 | D : 防御 | H : 回復 | R : 逃げる", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

        logTextArea = new JTextArea(10, 30); 
        logTextArea.setEditable(false); 
        logTextArea.setFont(new Font("MS ゴシック", Font.PLAIN, 15));
        logTextArea.setBackground(Color.BLACK); 
        logTextArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(logTextArea); 

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        attackButton = new JButton("⚔️ 攻撃");
        attackButton.setBackground(Color.decode("#A30000")); 
        attackButton.setForeground(Color.WHITE);

        defendButton = new JButton("🛡️ 防御");
        defendButton.setBackground(Color.decode("#004080")); 
        defendButton.setForeground(Color.WHITE);

        healButton = new JButton("💚 回復 (残り: " + itemUsesLeft + ")");
        healButton.setBackground(Color.decode("#006400")); 
        healButton.setForeground(Color.WHITE);

        runButton = new JButton("🏃 逃げる");
        runButton.setBackground(Color.decode("#5A5A5A")); 
        runButton.setForeground(Color.WHITE);

        nextStageButton = new JButton("⏭️ 次のステージへ");
        nextStageButton.setBackground(Color.decode("#D2691E")); 
        nextStageButton.setForeground(Color.WHITE);
        nextStageButton.setVisible(false);

        restartButton = new JButton("🔄 タイトルに戻る");
        restartButton.setBackground(Color.decode("#2F4F4F"));
        restartButton.setForeground(Color.WHITE);
        restartButton.setVisible(false);

        JButton[] allButtons = {attackButton, defendButton, healButton, runButton, nextStageButton, restartButton};
        Font btnFont = new Font("MS ゴシック", Font.BOLD, 16); 

        for (JButton btn : allButtons) {
            btn.setFont(btnFont);
            btn.setFocusPainted(false);
            btn.setFocusable(false); 
        }

        attackButton.addActionListener(e -> handleAttackAction());
        defendButton.addActionListener(e -> handleDefendAction());
        healButton.addActionListener(e -> useHealItem());
        runButton.addActionListener(e -> triggerRunDialog());
        nextStageButton.addActionListener(e -> goToNextStage());
        restartButton.addActionListener(e -> resetToTitle());

        bigGameClearButton.addActionListener(e -> {
            if (enemyCount >= 4) resetToTitle();
            else goToNextStage();
        });
        bigGameOverButton.addActionListener(e -> resetToTitle());

        buttonPanel.add(attackButton);
        buttonPanel.add(defendButton);
        buttonPanel.add(healButton);
        buttonPanel.add(runButton);
        buttonPanel.add(nextStageButton);
        buttonPanel.add(restartButton);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(backgroundLabel, BorderLayout.CENTER); 
        add(bottomPanel, BorderLayout.SOUTH); 

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        Timer gameLoop = new Timer(16, e -> {
            updateMovement();
            updateAnimation();
        });
        gameLoop.start();

        showRulesDialog(); 
        choicePlayer(); 
        initGameSession(false); 
        
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
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
                     + "3. 防御(D)をすると敵のダメージを減らし、MPが+20回復します。\n"
                     + "4. 回復(H)はHPを100回復しますが、回数制限(3回)があります。\n"
                     + "5. キャラクターのレベルが上がると、新しい英雄がアンロックされます！\n"
                     + "※ レベルはゲームオーバーになっても保持されます。";
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
        if (isGameOverState) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && nextStageButton.isVisible()) goToNextStage();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && restartButton.isVisible()) resetToTitle();
            return;
        }

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) moveLeft = true;
        if (key == KeyEvent.VK_RIGHT) moveRight = true;
        
        if (key == KeyEvent.VK_SPACE) handleAttackAction();
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
        if (isGameOverState) return;
        if (!isAttacking) {
            executePlayerTurn(); 
        }
    }

    private void handleDefendAction() {
        if (isGameOverState) return;
        isPlayerDefending = true;
        player.setMp(player.getMp() + 20); 
        logTextArea.append("🛡️ " + player.getName() + " は身を護っている！ (MP+20)\n");
        comboCount = 0; 
        updateDisplay();
        executeEnemyTurn();
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

        itemUsesLeft--;
        healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")"); 
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + 100));
        logTextArea.append("💚 アイテムを使用！ " + player.getName() + " のHPが 100 回復した！\n");
        triggerFlashEffect(Color.GREEN); 
        comboCount = 0; 
        updateDisplay();
        executeEnemyTurn();
    }

    private void triggerRunDialog() {
        if (isGameOverState) return;
        logTextArea.append("🏃 " + player.getName() + " は戦場から離脱した！\n");
        showGameClearOrEndScreen(false, "🏃 戦場から離脱しました！タイトルに戻って仕切り直しましょう。");
    }

    private void executePlayerTurn() {
        isAttacking = true;
        shakeFrame = 12; 

        if (player.getName().contains("wizard")) player.setMp(player.getMp() + 5);

        double critChance = player.getName().contains("human") ? 0.30 : 0.15; 
        boolean isCritical = Math.random() < critChance;
        comboCount++; 
        int baseDamage = (int)(player.getAtk() * (0.9 + Math.random() * 0.2));
        int finalDamage = isCritical ? baseDamage * 2 : baseDamage;

        enemy.setHp(Math.max(0, enemy.getHp() - finalDamage));
        player.setMp(player.getMp() + 15);

        if (isCritical) {
            triggerFlashEffect(Color.YELLOW); 
            logTextArea.append("💥 【CRITICAL】 " + player.getName() + " の会心の一撃！ " + enemy.getName() + " に " + finalDamage + " のダメージ！\n");
        } else {
            logTextArea.append("⚔️ " + player.getName() + " の攻撃！ " + enemy.getName() + " に " + finalDamage + " のダメージを与えた！\n");
        }
        updateDisplay();

        if (!enemy.isAlive()) {
            logTextArea.append("\n★━━━━━━━━━━━━━━━━━━━━★\n");
            logTextArea.append("★ " + enemy.getName() + " を完全に見事に倒した！\n");
            logTextArea.append("★━━━━━━━━━━━━━━━━━━━━★\n\n");
            playerWins++;
            
            player.levelUp(); 

            if (currentSelectedType.equals("hero")) heroLevel = player.getLevel();
            else if (currentSelectedType.equals("wizard")) wizardLevel = player.getLevel();
            else if (currentSelectedType.equals("human")) humanLevel = player.getLevel();

            logTextArea.append("✨✨【LEVEL UP】✨✨\n" + player.getName() + " は レベル: " + player.getLevel() + " に上がった！\n\n");

            checkUnlockConditions();
            showGameClearOrEndScreen(true, "🎉 STAGE CLEAR! 次のステージのロックが解除されました！");
            isAttacking = false;
            return;
        }

        Timer delayTimer = new Timer(300, e -> { executeEnemyTurn(); isAttacking = false; });
        delayTimer.setRepeats(false);
        delayTimer.start();
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

    private void executeEnemyTurn() {
        if (Math.random() > 0.6) triggerFlashEffect(Color.RED);

        boolean passiveGuard = player.getName().contains("ゆうしゃ") && player.getHp() <= (player.getMaxHp() * 0.3);
        String enemyResult = enemy.attack(player, isPlayerDefending || passiveGuard);
        logTextArea.append(enemyResult);
        
        if (!isPlayerDefending) comboCount = 0;
        isPlayerDefending = false; 
        updateDisplay();

        if (!player.isAlive()) {
            retryCount++;
            if (retryCount == 1) {
                logTextArea.append("💀 覚醒：最後の希望として human が参戦する！\n");
                player = new Player("human (human)", 300, 130, 10, 50, "human.png");
                player.setLevel(humanLevel); 
                enemy = new Enemy("裏ボス (BOSS)", 1500, 45, 10, 50, "dragon.png"); 
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
        restartButton.setVisible(false);
        bigGameClearButton.setVisible(false); 
        bigGameOverButton.setVisible(false); 
        
        attackButton.setVisible(true);
        defendButton.setVisible(true);
        healButton.setVisible(true);
        runButton.setVisible(true);

        initGameSession(true); 
        requestFocusInWindow();
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
        logTextArea.append("\n⚔--- ステージ " + enemyCount + " 開始！ ---\n");
    }

    private void updateDisplay() {
        if (player == null || enemy == null) return; 
        statusLabel.setText(String.format(
                "【%s (Lv.%d)】 HP: %d/%d  MP: %d/100 [%d Combo]  |  敵: 【%s】 HP: %d/%d",
                player.getName(), player.getLevel(), player.getHp(), player.getMaxHp(), player.getMp(), comboCount,
                enemy.getName(), enemy.getHp(), enemy.getMaxHp()));
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
                player = new Player("ゆうしゃ（Hero）", 100, 120, 10, 10, "hero.png");
                player.setLevel(heroLevel); 
                currentSelectedType = "hero";
                break;
            } else if (choice == 1) {
                if (isWizardUnlocked) {
                    player = new Player("wizard（wizard）", 80, 125, 10, 10, "wizard player.png");
                    player.setLevel(wizardLevel); 
                    currentSelectedType = "wizard";
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "❌ wizardはまだロックされています！\nゆうしゃのレベルを3以上にしてください。", "ロック中", JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 2) {
                if (isHumanUnlocked) {
                    player = new Player("human (human)", 300, 130, 10, 50, "human.png");
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
        if (enemyCount == 1) {
            enemy = new Enemy("スライム", 150, 18, 25, 20, "smile enamy.png"); // HP 100->150, ATK 12->18
            setBackgroundImage("fristbackgroud.png"); 
        } else if (enemyCount == 2) {
            enemy = new Enemy("フォレストドラゴン", 280, 26, 10, 25, "dargon enamy.png"); // HP 160->280, ATK 18->26
            setBackgroundImage("second backgroud.png");
        } else if (enemyCount == 3) {
            enemy = new Enemy("デーモン", 450, 38, 10, 30, "Demon enamy.png"); // HP 220->450, ATK 24->38
            setBackgroundImage("backgroundthree.png"); 
        } else if (enemyCount == 4) {
            enemy = new Enemy("裏ボス (ドラゴングランド)", 1800, 55, 10, 50, "dargon.png"); // HP 1200->1800, ATK 30->55
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
        new BattleGame().setVisible(true); 
    }
}