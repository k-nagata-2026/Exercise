import java.awt.*;
import java.io.File;
import java.util.List;
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
          private int playerX = 150;
          private int playerY = 250;
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
private final int MAX_SKILL_USE = 2; // 0から開始するように修正

private Clip bgmClip;
private Item[] itemBox = new Item[10];
    


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

private void handleEnemyDefeat() {
    playSound("sounds/enemy_dead.wav");

    logTextArea.append(
        "★ " + enemy.getName() + " をたおした！\n"
    );

    enemyCount++;
    player.gainExp(50);

logTextArea.append("EXP +50!\n");

    // 1 Level = 6 enemies
    if (enemyCount >= 6) {

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

        // Player bonus for next level
        player.setMaxHp(player.getMaxHp() + 50);
        player.setHp(player.getMaxHp());
        player.setAtk(player.getAtk() + 10);

        // Reset potion and skill limits for new level
        potionUseCount = 0;
        skillUseCount = 0;

        JOptionPane.showMessageDialog(
            this,
            "LEVEL " + currentLevel + " START!\n"
            + "Max HP +50\n"
            + "Attack +10\n"
            + "HP fully recovered!"
        );
    }

    spawnEnemy();
    updateDisplay();
}

 public BattleGame(Player player) {
        // ウィンドウの基本設定
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        this.player = player;

        // LOAD SAVE DATA
List<Integer> idList = GameSaveManager.getSaveIdList();

if (!idList.isEmpty()) {

    Object[] idOptions = idList.toArray();

    Object selected = JOptionPane.showInputDialog(
        this,
        "ロードするセーブデータを選んでください",
        "LOAD GAME",
        JOptionPane.QUESTION_MESSAGE,
        null,
        idOptions,
        idOptions[idOptions.length - 1]
    );

    if (selected != null) {

        int chosenId = (Integer) selected;

        GameSaveManager.SaveData data =
            GameSaveManager.loadGameDataById(chosenId);

        if (data != null) {

            player.setLevel(data.playerLevel);

            potionUseCount = data.usedPotion;
            skillUseCount = data.usedSkill;
            enemyCount = data.enemyDefeated;

            JOptionPane.showMessageDialog(
                this,
                "SAVE DATA LOADED!\n" +
                "Player Level: " + data.playerLevel + "\n" +
                "Potion Used: " + data.usedPotion + "\n" +
                "Skill Used: " + data.usedSkill + "\n" +
                "Enemy Defeated: " + data.enemyDefeated
            );
        }
    }
}
       
        // コンポーネントの初期化
        
        playBGM("sounds/bgm.wav");
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
        itemButton = new JButton("item");
        saveButton = new JButton("SAVE");

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
        //JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15,5));
        buttonPanel.add(attackButton);
       // buttonPanel.add(speedButton);
        buttonPanel.add(skillButton);
        buttonPanel.add(potionButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(saveButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
         add(backgroundLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
       
      itemBox[0] = new Herb();
      itemBox[1] = new Bomb();
      itemBox[2] = new ThunderStick();
      itemBox[3] = new FireOrb();
      spawnEnemy();

       
        // ボタンの処理


       

       attackButton.addActionListener(e -> {

    playSound("sounds/attack.wav");

    String playerResult = player.attack(enemy);
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
                    stopBGM();

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



saveButton.addActionListener(e -> {
    JOptionPane.showMessageDialog(
    this,
    "SAVE BUTTON CLICKED!"
);

    System.out.println("SAVE BUTTON CLICKED!");

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
        System.out.println("CALLING SAVE MANAGER...");

        GameSaveManager.saveGameData(
            playerId,
            currentLevel,
            slotName.trim(),
            potionUseCount,
            skillUseCount,
            enemyCount
        );

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(
            this,
            "Invalid Player ID!"
        );
    }
});




itemButton.addActionListener(e -> {

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
            }
            spawnEnemy();
        }
        updateDisplay();
    });
        

        // 初期化
        this.player = player;
        spawnEnemy();
         playerHpBar.setBounds(40, 40, 250, 25);
playerHpBar.setMaximum(player.getMaxHp());
playerHpBar.setValue(player.getHp());
enemyHpBar.setMaximum(enemy.getMaxHp());
enemyHpBar.setValue(enemy.getHp());
playerHpBar.setStringPainted(true);
playerHpBar.setForeground(Color.GREEN);


backgroundLabel.add(playerHpBar);

        playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());
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
            player = new Player("BHOLA（Hero）", 200, 20, 20, 10, 30, "BHOLA.png");
        } else if (choice == 1) {
            player = new Player("BIYON（Mage）", 200, 25, 10, 15, 30, "BIYON.png");
        } else if (choice == 2) {
            player = new Player("LedyBoss", 200, 20, 20, 10, 30, "ladyboss.png");
        } else if (choice == 3) {
            player = new Player("boss", 150, 40, 0, 15, 30, "bigboss.png");
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

   }
    