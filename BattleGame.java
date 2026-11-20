import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*; 

public class BattleGame extends JFrame {
    public static int currentSaveSlot = 1; 
    public static String currentName;
    public static int currentLevel = 1;
    public static int currentHp = 0;
    public static int loadedEnemyHp = -1; 
    public static boolean isLoaded = false; 

    public static String loadedItem1 = null;
    public static String loadedItem2 = null;
    public static String loadedItem3 = null;

    private JLabel statusLabel;          
    private JProgressBar playerHpBar; 
    private JProgressBar enemyHpBar;  
    private JTextArea logTextArea;    
    private JButton attackButton;     
    private JButton specialButton;    
    private JButton runButton;
    private JButton healButton;
    private JButton itemButton;
    private JButton restartButton; 
    private JButton saveButton;

    private JLabel backgroundLabel;   
    private JLabel playerImageLabel;  
    private JLabel enemyImageLabel;   

    private Player player;
    private Enemy enemy;
    private static int currentplayerlevel = 1; 
    private static int playerChoice = 1;      

    private Item[] itemBox = new Item[10];

    private void playSound(String soundFileName) {
        try {
            java.io.File soundFile = new java.io.File(soundFileName);
            if (soundFile.exists()) {
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                System.out.println("サウンドファイルが見つかりません: " + soundFileName);
            }
        } catch (Exception e) {
            System.out.println("サウンド再生エラー: " + e.getMessage());
        }
    }

    private void selectAndLoadSlot() {
        List<Integer> existingSlots = GameSaveManager.getExistingSaveSlots();

        if (existingSlots.isEmpty()) {
            existingSlots.add(1);
        } else {
            int maxSlot = java.util.Collections.max(existingSlots);
            int nextSlot = maxSlot + 1;
            if (!existingSlots.contains(nextSlot)) {
                existingSlots.add(nextSlot);
            }
        }

        Integer[] slots = existingSlots.toArray(new Integer[0]);

        Integer selectedSlot = (Integer) JOptionPane.showInputDialog(
            null,
            "使用するセーブスロット番号を選んでください：",
            "スロット選択（自動拡張）",
            JOptionPane.QUESTION_MESSAGE,
            null,
            slots,
            slots[0]
        );

        if (selectedSlot != null) {
            currentSaveSlot = selectedSlot;
        } else {
            currentSaveSlot = 1;
        }

        String[] modeOptions = {"ニューゲーム（最初から始める）", "このスロットのデータをロード（続きから）"};
        int modeChoice = JOptionPane.showOptionDialog(
            null,
            "スロット " + currentSaveSlot + " の開始方法を選んでください：",
            "モード選択 - スロット " + currentSaveSlot,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            modeOptions,
            modeOptions[0]
        );

        if (modeChoice == 1) {
            boolean success = GameSaveManager.loadGameDataById(currentSaveSlot);
            if (success) {
                JOptionPane.showMessageDialog(null, "スロット " + currentSaveSlot + " のデータをロードしました！");
            } else {
                JOptionPane.showMessageDialog(null, "⚠️ スロット " + currentSaveSlot + " にはセーブデータがありません。ニューゲームとして開始します。");
                isLoaded = false;
            }
        } else {
            isLoaded = false;
        }
    }

    private Item createItemByName(String name) {
        if (name == null) return null;
        if (name.contains("Herb") || name.contains("薬草")) {
            return new Herb();
        } else if (name.contains("Bomb") || name.contains("爆弾")) {
            return new Bomb();
        }
        return new Herb(); // デフォルトとして薬草を返す
    }

    public BattleGame() {
        isLoaded = false;
        loadedItem1 = null;
        loadedItem2 = null;
        loadedItem3 = null;
        selectAndLoadSlot();

        playBGM("BGM.wav");
        setTitle("ターンせいコマンドバトル - スロット " + currentSaveSlot);
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout()); 

        backgroundLabel = new JLabel(new ImageIcon("background.png"));
        backgroundLabel.setLayout(null); 

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        playerImageLabel.setBounds(50, 10, 550, 700); 
        enemyImageLabel.setBounds(680, 10, 500, 700);  

        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7)); 
        attackButton = new JButton("こうげきする");
        specialButton = new JButton("必殺技");
        healButton = new JButton("回復する");   
        runButton = new JButton("にげる");
        itemButton = new JButton("アイテム");
        saveButton = new JButton("save");
        
        // ★ 保存ボタン：ポップアップなしで直接データベースに保存（新しい履歴として追加）
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameSaveManager.saveGameDataToSlot(currentSaveSlot, playerChoice, currentplayerlevel, player.getHp(), player.getLevel(), enemy.getHp(), itemBox);
                logTextArea.append("💾 ゲームデータをデータベースに正常に保存しました！\n");
                logTextArea.append("--------------------------------------------\n");
            }
        });
        
        restartButton = new JButton("リスタート"); 
        restartButton.setEnabled(false); 

        buttonPanel.add(attackButton);
        buttonPanel.add(specialButton);
        buttonPanel.add(healButton);
        buttonPanel.add(runButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(restartButton); 

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isLoaded = false;
                loadedEnemyHp = -1;
                loadedItem1 = null;
                loadedItem2 = null;
                loadedItem3 = null;
                selectAndLoadSlot();

                if (isLoaded) {
                    initPlayerByChoice(playerChoice);
                    for (int i = 1; i < currentLevel; i++) {
                        player.levelUp();
                    }
                    player.setHp(currentHp);
                    itemBox[0] = createItemByName(loadedItem1);
                    itemBox[1] = createItemByName(loadedItem2);
                    itemBox[2] = createItemByName(loadedItem3);
                } else {
                    currentplayerlevel = 1;
                    choicePlayer();
                    itemBox[0] = new Herb();
                    itemBox[1] = new Bomb();
                    itemBox[2] = new Herb();
                }

                if (currentplayerlevel == 2) {
                    enemy = new Enemy("インフェルノドラゴン", 250, 40, 20, "dragon1.png");
                } else if (currentplayerlevel == 3) {
                    enemy = new Enemy("魔王 (Demon King)", 500, 65, 35, "Demon king1.png");
                } else {
                    enemy = new Enemy("スライム", 180, 35, 15, "Picture5.png");
                }

                if (isLoaded && loadedEnemyHp >= 0) {
                    enemy.setHp(loadedEnemyHp);
                }

                playerImageLabel.setIcon(player.getIcon());
                playerImageLabel.setEnabled(true);
                enemyImageLabel.setIcon(enemy.getIcon());
                enemyImageLabel.setEnabled(true);

                attackButton.setEnabled(true);
                specialButton.setEnabled(true);
                healButton.setEnabled(true);
                runButton.setEnabled(true);
                itemButton.setEnabled(true);
                saveButton.setEnabled(true);
                restartButton.setEnabled(false);

                logTextArea.setText("=== ゲームをリスタートしました！ ===\n");
                if (isLoaded) {
                    logTextArea.append("📂 セーブスロット " + currentSaveSlot + " をロードしました！ (Stage: " + currentplayerlevel + ")\n");
                }
                logTextArea.append("野生の " + enemy.getName() + " が現れた！\n");
                updateDisplay();
            }
        });
        
        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        playerHpBar = new JProgressBar();
        playerHpBar.setStringPainted(true);
        playerHpBar.setForeground(Color.GREEN);

        enemyHpBar = new JProgressBar();
        enemyHpBar.setStringPainted(true);
        enemyHpBar.setForeground(Color.RED);

        JPanel hpPanel = new JPanel(new GridLayout(2, 1));
        hpPanel.add(playerHpBar);
        hpPanel.add(enemyHpBar);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.NORTH);    
        statusPanel.add(hpPanel, BorderLayout.SOUTH);   

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        bottomPanel.add(statusPanel, BorderLayout.NORTH);    
        bottomPanel.add(scrollPane, BorderLayout.CENTER);   
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(backgroundLabel, BorderLayout.CENTER); 
        add(bottomPanel, BorderLayout.SOUTH);      

        if (isLoaded) {
            itemBox[0] = createItemByName(loadedItem1);
            itemBox[1] = createItemByName(loadedItem2);
            itemBox[2] = createItemByName(loadedItem3);
        } else {
            itemBox[0] = new Herb();
            itemBox[1] = new Bomb();
            itemBox[2] = new Herb();
        }
        
        attackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerResult = player.attack(enemy);
                logTextArea.append(playerResult);
                updateDisplay();    

                if (!enemy.isAlive()) {
                    logTextArea.append("★ " + enemy.getName() + " を倒した！ " + player.getName() + "の勝利！\n");
                    player.levelUp(); 
                    logTextArea.append("レベルアップ！ レベル " + player.getLevel() + " になりました！\n");

                    if (currentplayerlevel == 1) {
                        currentplayerlevel = 2; 
                        player.setHp(player.getMaxHp()); 

                        enemy = new Enemy("インフェルノドラゴン", 250, 40, 20, "dragon1.png"); 
                        enemyImageLabel.setIcon(enemy.getIcon()); 
                        updateDisplay(); 

                        logTextArea.append("🔥 次のステージ！ 中ボス 「" + enemy.getName() + "」 が現れた！\n");
                        logTextArea.append("--------------------------------------------\n");

                    } else if (currentplayerlevel == 2) {
                        currentplayerlevel = 3; 
                        player.setHp(player.getMaxHp()); 

                        enemy = new Enemy("魔王 (Demon King)", 500, 65, 35, "Demon king1.png"); 
                        enemyImageLabel.setIcon(enemy.getIcon()); 
                        updateDisplay(); 

                        logTextArea.append("👿 👑 ついに現れた！ 最終ボス 「" + enemy.getName() + "」 との決戦だ！\n");
                        logTextArea.append("--------------------------------------------\n");

                    } else {
                        enemyImageLabel.setEnabled(false); 
                        updateDisplay();
                        logTextArea.append("🎉 🎉 おめでとう！ 伝説の魔王を倒し、世界に平和が戻った！\n");
                        endGame();
                    }
                    return; 
                }

                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);
                updateDisplay();

                if (!player.isAlive()) {
                    logTextArea.append(player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
                    playerImageLabel.setEnabled(false); 
                    endGame();
                    return;
                }

                logTextArea.append("--------------------------------------------\n");
            }
        });

        specialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 playSound("attack.wav");
                logTextArea.append("🔥 " + player.getName() + " の必殺技！ (Special Attack)\n");
                
                if (Math.random() < 0.2) { 
                    logTextArea.append("⚡⚡ クリティカルヒット！ (CRITICAL HIT! Double Damage!) ⚡⚡\n");
                    logTextArea.append(player.attack(enemy)); 
                    logTextArea.append(player.attack(enemy)); 
                } else {
                    String playerResult = player.attack(enemy);
                    logTextArea.append(playerResult);
                }
                updateDisplay();
                if (!enemy.isAlive()) {
                    logTextArea.append("★ " + enemy.getName() + " を倒した！ " + player.getName() + "の勝利！\n");
                    player.levelUp(); 
                    logTextArea.append("レベルアップ！ レベル " + player.getLevel() + " になりました！\n");

                    if (currentplayerlevel == 1) {
                        currentplayerlevel = 2; 
                        player.setHp(player.getMaxHp());
                        enemy = new Enemy("インフェルノドラゴン", 250, 40, 20, "dragon1.png");
                        enemyImageLabel.setIcon(enemy.getIcon()); 
                        updateDisplay(); 
                        logTextArea.append("🔥 次のステージ！ 中ボス 「" + enemy.getName() + "」 が現れた！\n");
                        logTextArea.append("--------------------------------------------\n");
                    } else if (currentplayerlevel == 2) {
                        currentplayerlevel = 3; 
                        player.setHp(player.getMaxHp());
                        enemy = new Enemy("魔王 (Demon King)", 500, 65, 35, "Demon king1.png");
                        enemyImageLabel.setIcon(enemy.getIcon()); 
                        updateDisplay(); 
                        logTextArea.append("👿 👑 ついに現れた！ 最終ボス 「" + enemy.getName() + "」 との決戦だ！\n");
                        logTextArea.append("--------------------------------------------\n");
                    } else {
                        enemyImageLabel.setEnabled(false); 
                        updateDisplay();
                        logTextArea.append("🎉 🎉 おめでとう！ 伝説の魔王を倒し、世界に平和が戻った！\n");
                        endGame();
                    }
                    return; 
                }

                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);
                updateDisplay();

                if (!player.isAlive()) {
                    logTextArea.append(player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
                    playerImageLabel.setEnabled(false); 
                    endGame();
                    return;
                }

                logTextArea.append("--------------------------------------------\n");
            }
        });

        healButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String healResult = player.heal(); 
                logTextArea.append(healResult);
                updateDisplay();

                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);
                updateDisplay();

                if (!player.isAlive()) {
                    logTextArea.append(player.getName() + " はたおれた… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    endGame();
                    return;
                }
                logTextArea.append("--------------------------------------------\n");
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentplayerlevel == 3) {
                    logTextArea.append("「" + enemy.getName() + "」 からは逃げられない！ 決戦に集中しろ！\n");
                    logTextArea.append("--------------------------------------------\n");
                    return;
                }

                logTextArea.append(player.getName() + " は逃げ出そうとした！\n");
                
                if (Math.random() < 0.5) {
                    logTextArea.append("うまくにげきれた！\n");
                    endGame(); 
                } else {
                    logTextArea.append("しかし にげきれなかった！\n");

                    String monsterResult = enemy.attack(player);
                    logTextArea.append(monsterResult);
                    updateDisplay();
    
                    if (!player.isAlive()) {
                        logTextArea.append(player.getName() + " はたおれた… ゲームオーバー\n");
                        playerImageLabel.setEnabled(false);
                        endGame();
                        return;
                    }
                    logTextArea.append("--------------------------------------------\n");
                }
            }
        });

        itemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] choices = new String[10];
                for (int i = 0; i < itemBox.length; i++) {
                    if (itemBox[i] != null) {
                        choices[i] = "スロット " + (i + 1) + " : " + itemBox[i].getName();
                    } else {
                        choices[i] = "スロット " + (i + 1) + " : (からっぽ)";
                    }
                }

                Object selected = JOptionPane.showInputDialog(
                    BattleGame.this,
                    "使用するアイテムを選択してください",
                    "アイテムボックス (最大10個)",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    choices, 
                    choices[0] 
                );

                if (selected == null) { return; }

                int selectedIndex = -1;
                for (int i = 0; i < choices.length; i++) {
                    if (choices[i].equals(selected)) {
                        selectedIndex = i;
                        break;
                    }
                }

                Item selectedItem = itemBox[selectedIndex];
                if (selectedItem == null) {
                    logTextArea.append("そのスロットはからっぽです！\n");
                    return;
                }

                String resultLog = selectedItem.use(player, enemy);
                itemBox[selectedIndex] = null; 
                logTextArea.append(resultLog + "\n");

                if (enemy.isAlive()) {
                    String enemyResult = enemy.attack(player);
                    logTextArea.append(enemyResult);
                    updateDisplay();

                    if (!player.isAlive()) {
                        logTextArea.append(player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
                        playerImageLabel.setEnabled(false);
                        endGame();
                        return;
                    }
                } else {
                    logTextArea.append(enemy.getName() + " を倒した！\n");
                    if (Math.random() < 0.1) {
                        addItemToBox(new Herb());
                        logTextArea.append("★ " + enemy.getName() + " が薬草を落とした！\n");
                    }
                }
                updateDisplay();
            }
        });
        
        if (!isLoaded) {
            choicePlayer(); 
        } else {
            initPlayerByChoice(playerChoice);
            for (int i = 1; i < currentLevel; i++) {
                player.levelUp();
            }
            player.setHp(currentHp);
        }

        if (currentplayerlevel == 2) {
            enemy = new Enemy("インフェルノドラゴン", 250, 40, 20, "dragon1.png");
        } else if (currentplayerlevel == 3) {
            enemy = new Enemy("魔王 (Demon King)", 500, 65, 35, "Demon king1.png");
        } else {
            enemy = new Enemy("スライム", 180, 35, 15, "Picture5.png");
        }

        if (isLoaded && loadedEnemyHp >= 0) {
            enemy.setHp(loadedEnemyHp);
        }

        playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());
        
        updateDisplay();

        if (isLoaded) {
            logTextArea.append("📂 セーブスロット " + currentSaveSlot + " のデータをロードしました！ (Stage: " + currentplayerlevel + ")\n");
        }
        logTextArea.append("野生の " + enemy.getName() + " が現れた！\n");
    }

    public static void main(String[] args) {
        BattleGame game = new BattleGame();
        game.setVisible(true); 
    }

    private void updateDisplay() {
        statusLabel.setText(String.format(
                "【%s】 LV: %d  vs  【%s】 (スロット: %d)", 
                player.getName(), player.getLevel(), enemy.getName(), currentSaveSlot));

        playerHpBar.setMaximum(player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerHpBar.setString(player.getName() + " HP: " + player.getHp() + "/" + player.getMaxHp());

        enemyHpBar.setMaximum(enemy.getMaxHp());
        enemyHpBar.setValue(enemy.getHp());
        enemyHpBar.setString(enemy.getName() + " HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
    }

    private void endGame() {
        attackButton.setEnabled(false);  
        specialButton.setEnabled(false); 
        healButton.setEnabled(false);    
        runButton.setEnabled(false);     
        itemButton.setEnabled(false); 
        saveButton.setEnabled(false);   
        restartButton.setEnabled(true);  
        logTextArea.append("【ゲームしゅうりょう（Game End）】リスタートボタンを押すと最初から遊べます。\n");
    }

    private void choicePlayer() {
        int choice = JOptionPane.showOptionDialog(
                this,
                "しよう（Use）するキャラクターをせんたく（Select）してください",
                "キャラクターせんたく（Select）",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] { "ゆうしゃ（Hero）", "まほうつかい（Mage）", "ninja"},
                null);
        playerChoice = choice + 1;
        initPlayerByChoice(playerChoice);
    } 

    private void initPlayerByChoice(int choiceNum) {
        if (choiceNum == 2) {
            player = new Player("まほうつかい（Mage）", 120, 130, 15, "Aelina.png");
        } else if (choiceNum == 3) {
            player = new Player("ninja（ninja）", 110, 140, 12, "Picture2.png");
        } else {
            player = new Player("ゆうしゃ（Hero）", 150, 120, 15, "sagar.png");
        }
    }

    public static void loadGameVariables(int playerNo, int stage, int hp, int level, int enemyHp, String item1, String item2, String item3) {
        playerChoice = playerNo;
        currentplayerlevel = stage;
        currentHp = hp;
        currentLevel = level;
        loadedEnemyHp = enemyHp;
        isLoaded = true;
        loadedItem1 = item1;
        loadedItem2 = item2;
        loadedItem3 = item3;
    }

    private void playBGM(String musicFile) {
        try {
            java.io.File file = new java.io.File(musicFile);
            if (file.exists()) {
                javax.sound.sampled.AudioInputStream audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioStream);
                clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItemToBox(Item item) {
        for (int i = 0; i < itemBox.length; i++) {
            if (itemBox[i] == null) {      
                itemBox[i] = item;         
                logTextArea.append("アイテムボックスのスロット " + (i + 1) + " に " + item.getName() + " を追加しました。\n");
                return;                     
            }
        }
        logTextArea.append("アイテムボックスがいっぱいです！ " + item.getName() + " は拾えませんでした。\n");
    }
}