import java.awt.*;
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
          private int playerX = 150;
          private int playerY = 250;
   final  private JLabel backgroundLabel;
   final  private JLabel playerImageLabel;
   final  private JLabel enemyImageLabel;
   final  private JProgressBar playerHpBar;
   final  private JProgressBar enemyHpBar;

   private JProgressBar hpBar;
    private Player player;
    private Enemy enemy;
    private int enemyCount = 0; // 0から開始するように修正
    private int potionUseCount = 0;
    private final int MAX_POTION_USE = 3;
    private int skillUseCount = 0;
    private final int MAX_SKILL_USE = 2;
    

    private void playSound(String fileName) {
    try {
        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(new File(fileName));

        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
     private void showVictoryScreen() {
        playSound("sounds/victory.wav");

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

   
    homeButton.addActionListener(e -> {
        System.exit(0);
    });

    homeButton.addActionListener(e -> {
        victoryFrame.dispose();
        enemyCount = 0;
        player.setHp(player.getMaxHp());
        spawnEnemy();
        updateDisplay();
        this.setVisible(true);
     });

    bg.add(homeButton);
    
    victoryFrame.add(bg);
    victoryFrame.setVisible(true);

    this.setVisible(false);
}

      
 private void showGameOverScreen() {
    playSound("sounds/gameover.wav");

    JFrame gameOverFrame = new JFrame("GAME OVER");
    gameOverFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ImageIcon icon = new ImageIcon("GAME OVER.png");
    Image img = icon.getImage();
    Image resizedImg = img.getScaledInstance(1420, 700, Image.SCALE_SMOOTH);

    JLabel label = new JLabel(new ImageIcon(resizedImg));
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setLayout(null);

    // BACK TO HOME button
    JButton homeButton = new JButton();
    homeButton.setBounds(780, 520, 350, 55);
    homeButton.setOpaque(false);
    homeButton.setContentAreaFilled(false);
    homeButton.setBorderPainted(false);
    homeButton.setFocusPainted(false);
    homeButton.setBorder(null);
     // RETRY button
    JButton retryButton = new JButton();
    retryButton.setBounds(780, 420, 350, 55);
    retryButton.setOpaque(false);
    retryButton.setContentAreaFilled(false);
    retryButton.setBorderPainted(false);
    retryButton.setFocusPainted(false);
    retryButton.setBorder(null);
    retryButton.addActionListener(e -> {
    gameOverFrame.dispose();

    // Player reset
    player.setHp(player.getMaxHp());
    player.setLevel(1);
    player.setExp(0);

    // Enemy reset
    enemyCount = 0;
    spawnEnemy();

    // UI reset
    logTextArea.setText("");
    playerImageLabel.setEnabled(true);
    enemyImageLabel.setEnabled(true);

    updateDisplay();
    this.setVisible(true);
});
    // Button actions
    homeButton.addActionListener(e -> {
        System.exit(0);
    });
    retryButton.addActionListener(e -> {
        gameOverFrame.dispose();
       enemyCount = 0;
        player.setHp(player.getMaxHp());
         spawnEnemy();
        updateDisplay();
     this.setVisible(true);
    });

    label.add(homeButton);
    label.add(retryButton);

    gameOverFrame.add(label);
    gameOverFrame.setVisible(true);

    this.dispose();
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


  public BattleGame() {
        // ウィンドウの基本設定
        setTitle("ターン制コマンドバトル");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        // コンポーネントの初期化
        backgroundLabel = new JLabel(new ImageIcon("battlebackround.png"));
        backgroundLabel.setLayout(null);

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);
        playerImageLabel.setBounds(40, 50, 500, 500);
        enemyImageLabel.setBounds(550, 50, 500, 500);
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);

        statusLabel = new JLabel("ステータス表示", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        attackButton = new JButton("こうげき");
        speedButton = new JButton("speed");
        skillButton = new JButton("skill");
        potionButton = new JButton("potion");

        playerHpBar = new JProgressBar();
        enemyHpBar = new JProgressBar();
        playerHpBar.setStringPainted(true);
        enemyHpBar.setStringPainted(true);

        JPanel hpPanel = new JPanel(new GridLayout(2, 1));
        hpPanel.add(playerHpBar);
        hpPanel.add(enemyHpBar);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(attackButton);
       // buttonPanel.add(speedButton);
        buttonPanel.add(skillButton);
        buttonPanel.add(potionButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(hpPanel, BorderLayout.NORTH);
        add(backgroundLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

       
        // ボタンの処理
        attackButton.addActionListener(e -> {
         playSound("attack.wav");
            String playerResult = player.attack(enemy);
          logTextArea.append(playerResult);
           shakePlayer();
            logTextArea.append(playerResult);
            updateDisplay();

            if (!enemy.isAlive()) {
                handleEnemyDefeat();
            } else {
                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);
                updateDisplay();
                if (!player.isAlive()) {
                    logTextArea.append(player.getName() + " はたおれた…     ゲームオーバー(GAME OVER)\n");
                    playerImageLabel.setEnabled(false);

                    playSound("sounds/attack.wav");

                    showGameOverScreen();

                    return;
                    
                }
            }
        });

       skillButton.addActionListener(e -> {

    if (skillUseCount >= MAX_SKILL_USE) {
        JOptionPane.showMessageDialog(this,
                "Skill can only be used 2 times in this battle!");
        return;
    }

    skillUseCount++;
    playSound("sounds/skill.wav");

    String result = player.skillAttack(enemy);   // timro skill method ko naam
    logTextArea.append(result + "\n");

    updateDisplay();

    if (!enemy.isAlive()) {
        handleEnemyDefeat();
    }

});

       potionButton.addActionListener(e -> {
        playSound("sounds/potion.wav");

    if (potionUseCount >= MAX_POTION_USE) {
        JOptionPane.showMessageDialog(this,
                "You can only use Potion 3 times in this battle!");
        return;
    }

    potionUseCount++;

    String result = player.usePotion();
    logTextArea.append(result + "\n");

    updateDisplay();

});
        

        // 初期化
        choicePlayer();
        spawnEnemy();
         hpBar = new JProgressBar();
     playerHpBar.setBounds(40, 40, 100, 25);
     playerHpBar.setMaximum(player.getMaxHp());
     playerHpBar.setValue(player.getHp());

    playerHpBar.setStringPainted(true);
    playerHpBar.setForeground(Color.GREEN);
    backgroundLabel.add(hpBar);

    add(hpBar);

        playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());
        updateDisplay();
        logTextArea.append("野生の" + enemy.getName() + " が現れた！\n");
    }

  private void handleEnemyDefeat() {
    playSound("sounds/enemy_dead.wav");

    logTextArea.append("★ " + enemy.getName() + " をたおした！\n");

    enemyCount++;

    // Last enemy defeated
    if (enemyCount >= 6) {
        logTextArea.append("世界に平和が訪れた！【ゲームクリア】\n");
        endGame();
        showVictoryScreen();
        return;
    }

    // EXP only if not the last enemy
    player.gainExp(50);

    // Next level unlock
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "LEVEL " + (enemyCount + 1) + " UNLOCKED!"
    );

    spawnEnemy();
    updateDisplay();

    potionUseCount = 0;
    skillUseCount = 0;
}
    // 他メソッド（updateDisplay, endGame, choicePlayer, spawnEnemy 等）はそのまま記述
    private void updateDisplay() {
        statusLabel.setText(String.format(
                "【%s】Lv.%d HP: %d/%d  vs  【%s】 Lv.%d HP: %d/%d",
                player.getName(), player.getLevel(), player.getHp(), player.getMaxHp(),
                enemy.getName(), enemy.getLevel(), enemy.getHp(), enemy.getMaxHp()));
                
               playerHpBar.setMaximum(player.getMaxHp());
playerHpBar.setValue(player.getHp());

if (player.getHp() > player.getMaxHp() * 0.6) {
    playerHpBar.setForeground(Color.GREEN);
} else if (player.getHp() > player.getMaxHp() * 0.3) {
    playerHpBar.setForeground(Color.ORANGE);
} else {
    playerHpBar.setForeground(Color.RED);
}
    }
    
       

    private void endGame() {
        attackButton.setEnabled(false); // ボタンをむこうか
        logTextArea.append("【ゲームしゅうりょう】ウィンドウをとじてください。\n");
    }


    private void choicePlayer() {


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
            player = new Player("BHOLA（Hero）", 150, 20, 10, 10, 30, "BHOLA.png");
        } else if (choice == 1) {
            player = new Player("BIYON（Mage）", 200, 25, 10, 15, 30, "BIYON.png");
        } else if (choice == 2) {
            player = new Player("LedyBoss", 100, 15, 20, 10, 30, "ladyboss.png");
        } else if (choice == 3) {
            player = new Player("boss", 150, 40, 0, 15, 30, "bigboss.png");
        }
    }

    private void spawnEnemy() {
        if (enemyCount == 0) {
            enemy = new Enemy("chotu", 100, 20, 5, 5, 10, "chotuenemy.png");
            logTextArea.append("【だい（No.）1せん（Battle）】スライム があらわれた！\n");
        } else if (enemyCount == 1) {
            enemy = new Enemy("boss", 90, 15, 5, 5, 10, "enemyboss.png");
            logTextArea.append("【だい（No.）2せん（Battle）】ゴブリン があらわれた！\n");
        } else if (enemyCount == 2) {
            enemy = new Enemy("tiger", 160, 24, 5, 5, 10, "tiger.png");
            logTextArea.append("⚠ WARNING ⚠\\nFINAL BOSS APPEARED! \n");
        } else if (enemyCount == 3) {
            enemy = new Enemy("lagartha", 180, 30, 10, 10, 10, "lagartha.png");
            logTextArea.append("WARNING\\nLAGARTHA IS COMMING! \n");
        } else if (enemyCount == 4) {
            enemy = new Enemy("IRONBOSS", 200, 20, 10, 7, 15, "ironman.png");
            logTextArea.append("WARNING\\nIRONMAN AAGAYA! \n");
        } else if (enemyCount == 5) {
            enemy = new Enemy("MONSTAR", 200, 15, 15, 8, 10, "MONSTAR.png");
            logTextArea.append("IM THE MONSTAR! \n");
        }
        enemyImageLabel.setIcon(enemy.getIcon());
        logTextArea.append("--------------------------------------------\n");
    }    public static void main(String[] args) {
    new HomeScreen().setVisible(true);
   }
    }