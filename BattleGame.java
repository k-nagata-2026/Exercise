import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*; 

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JProgressBar playerHpBar; // ★ Player HP Bar
    private JProgressBar enemyHpBar;  // ★ Enemy HP Bar
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    private JButton specialButton;    // ★ Special Attack Button
    private JButton runButton;
    private JButton healButton;
    private JButton itemButton;
    private JButton restartButton; // ★ Restart Button
    
    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // はいけいがぞうようのラベル
    private JLabel playerImageLabel;  // プレイヤーがぞうようのラベル
    private JLabel enemyImageLabel;   // てきがぞうようのラベル

    // ★ キャラクターのインスタンスをよういする
    private Player player;
    private Enemy enemy;
    private Enemy enemy2;
    private Enemy enemy3;
    private static int currentplayerlevel = 1;

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
            System.out.println("Sound file bhetiyena: " + soundFileName);
        }
    } catch (Exception e) {
        System.out.println("Sound play garda error aayo: " + e.getMessage());
    }
}

    public BattleGame() {
        playBGM("BGM.wav");
        // ウィンドウ（Window）のきほんせってい（Basic Setting）
        setTitle("ターンせいコマンドバトル");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // がめんのまんなかにひょうじ
        setLayout(new BorderLayout()); // ぜんたいのレイアウト（Layout）をせってい
        // 【うえはんぶん：キャラクターたいじエリア（はいけいのなかにキャラをいれる）】
        // ※はいけいがぞうファイル（bg.png）をよみこみます
        backgroundLabel = new JLabel(new ImageIcon("background.png"));
        backgroundLabel.setLayout(null); // ★じゅうよう（Important）：じゆうはいち（Free Layout）にするためにnullにする

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        // ★はいけいラベルをきじゅん（Base）とした、キャラがぞうラベルの「いち（Position）(x, y)」と「サイズ（Size）（はば（Width）, たかさ（Height））」をしてい（Specify）
        playerImageLabel.setBounds(50, 10, 550, 700); // ひだりがわにはいち
        enemyImageLabel.setBounds(680, 10, 500, 700);  // みぎがわにはいち

        // ★はいけいラベルのなかにキャラがぞうラベルを「add」してかさねる！
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);
        
        // 【したはんぶん：そうさ・ログエリア】
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // ★ 3つのボタンをきれいに配置（Layout）するためのパネルをつくる
        JPanel buttonPanel = new JPanel(new GridLayout(1, 6)); 
        attackButton = new JButton(" こうげきする");
        specialButton = new JButton("必殺技");
        healButton = new JButton("回復する");   
        runButton = new JButton("にげる");
        itemButton = new JButton("アイテム");
        restartButton = new JButton("リスタート"); // ★ Restart Button
        restartButton.setEnabled(false); 

        buttonPanel.add(attackButton);
        buttonPanel.add(specialButton);
        buttonPanel.add(healButton);
        buttonPanel.add(runButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(restartButton); // ★ Add Restart Button

        // ★ Restart Button Click Action
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentplayerlevel = 1;
                choicePlayer();
                enemy = new Enemy("スライム", 180, 35, 15, "Picture5.png");
                playerImageLabel.setIcon(player.getIcon());
                playerImageLabel.setEnabled(true);
                enemyImageLabel.setIcon(enemy.getIcon());
                enemyImageLabel.setEnabled(true);

                attackButton.setEnabled(true);
                specialButton.setEnabled(true);
                healButton.setEnabled(true);
                runButton.setEnabled(true);
                itemButton.setEnabled(true);
                restartButton.setEnabled(false);

                logTextArea.setText("=== ゲームをリスタートしました！ ===\n");
                logTextArea.append("野生の" + enemy.getName() + " が現れた！\n");
                updateDisplay();
            }
        });
        
        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        // ★ [Naya Code] Visual HP Bars Setup
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

        // ぶひん（Parts）をメインウィンドウにはいち
        add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        add(bottomPanel, BorderLayout.SOUTH);       // そうさエリアをしたがわにはいち

        itemBox[0] = new Herb();
        itemBox[1] = new Bomb();
        
        // ★ ボタンをおしたときのしょりをついか
        attackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. プレイヤーのターン（Turn）
                String playerResult = player.attack(enemy);
                logTextArea.append(playerResult);
                updateDisplay();    

                // 2. エネミーがたおれたかチェック（Check Enemy Dead）
if (!enemy.isAlive()) {
    logTextArea.append("★ " + enemy.getName() + " を倒した！ " + player.getName() + "の勝利！\n");
    player.levelUp(); 
    logTextArea.append(" レベルアップ！ レベル " + player.getLevel() + " になりました！\n");

    // --- ステージシステム（Stage System Handling） ---
    if (currentplayerlevel == 1) {
        // 【ステージ 1 クリア：中ボスが現れる】
        currentplayerlevel = 2; 
        player.setHp(player.getMaxHp()); // プレイヤーのHPを満タンに回復

        enemy = enemy2; // 敵をインフェルノドラゴンに変更
        enemyImageLabel.setIcon(enemy.getIcon()); 
        updateDisplay(); 

        logTextArea.append("🔥 次のステージ！ 中ボス 「" + enemy.getName() + "」 が現れた！\n");
        logTextArea.append("--------------------------------------------\n");

    } else if (currentplayerlevel == 2) {
        // 【ステージ 2 クリア：最終ボスが現れる】
        currentplayerlevel = 3; 
        player.setHp(player.getMaxHp()); // プレイヤーのHPを満タンに回復

        enemy = enemy3; // 敵を魔王（Final Boss）に変更
        enemyImageLabel.setIcon(enemy.getIcon()); 
        updateDisplay(); 

        logTextArea.append("👿 👑 ついに現れた！ 最終ボス 「" + enemy.getName() + "」 との決戦だ！\n");
        logTextArea.append("👿 魔王: 'よくぞここまで来ただが、ここがお前の墓場だ！'\n");
        logTextArea.append("--------------------------------------------\n");

    } else {
        // 【ステージ 3 クリア：ゲームクリア（Game Clear）】
        enemyImageLabel.setEnabled(false); 
        updateDisplay();
        logTextArea.append("🎉 🎉 おめでとう！ 伝説の魔王を倒し、世界に平和が戻った！\n");
        endGame();
    }
    return; 
}

                // 3. エネミーのターン（はんげき）
                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);
                updateDisplay();

                // 4. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
                    playerImageLabel.setEnabled(false); // プレイヤーのがぞうをグレーアウト
                    endGame();
                    return;
                }

                logTextArea.append("--------------------------------------------\n");
            }
        });

        // ★ [Naya Code] Special Attack Button Action
        specialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                  playSound("attack.wav");
                logTextArea.append("🔥 " + player.getName() + " の必殺技！ (Special Attack)\n");
                
                // 1. プレイヤーのターン（Turn - 20% Critical Hit Chance）
                if (Math.random() < 0.2) { // 20% Chance
                    logTextArea.append("⚡⚡ クリティカルヒット！ (CRITICAL HIT! Double Damage!) ⚡⚡\n");
                    logTextArea.append(player.attack(enemy)); // First hit
                    logTextArea.append(player.attack(enemy)); // Second extra hit
                } else {
                    String playerResult = player.attack(enemy);
                    logTextArea.append(playerResult);
                }
                updateDisplay();
                if (!enemy.isAlive()) {
                    logTextArea.append("★ " + enemy.getName() + " を倒した！ " + player.getName() + "の勝利！\n");
                    player.levelUp(); 
                    logTextArea.append(" レベルアップ！ レベル " + player.getLevel() + " になりました！\n");

                    if (currentplayerlevel == 1) {
                        currentplayerlevel = 2; 
                        player.setHp(player.getMaxHp());
                        enemy = enemy2; 
                        enemyImageLabel.setIcon(enemy.getIcon()); 
                        updateDisplay(); 
                        logTextArea.append("🔥 次のステージ！ 中ボス 「" + enemy.getName() + "」 が現れた！\n");
                        logTextArea.append("--------------------------------------------\n");
                    } else if (currentplayerlevel == 2) {
                        currentplayerlevel = 3; 
                        player.setHp(player.getMaxHp());
                        enemy = enemy3; 
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
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
                    playerImageLabel.setEnabled(false); 
                    endGame();
                    return;
                }

                logTextArea.append("--------------------------------------------\n");
            }
        });

        // ★「回復（Heal）ボタン」をおしたときのしょり
        healButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String healResult = player.heal(); 
                logTextArea.append(healResult);
                updateDisplay();

                // エネミーのターン（はんげき）
                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);
                updateDisplay();

                // プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    endGame();
                    return;
                }
                logTextArea.append("--------------------------------------------\n");
            }
        });
        // ★「にげる（Escape）ボタン」をおしたときのしょり
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 【最終ボス：逃げられない設定】
                if (currentplayerlevel == 3) {
                    logTextArea.append(" 「" + enemy.getName() + "」 からは逃げられない！ 決戦に集中しろ！\n");
                    logTextArea.append("--------------------------------------------\n");
                    return;
                }

                logTextArea.append(player.getName() + " は逃げ出そうとした！\n");

                
                
                // 0.5 未満（50% の確率）なら逃亡成功とする
                if (Math.random() < 0.5) {
                    logTextArea.append("うまくにげきれた！\n");
                    endGame(); 
                } else {
                    // 逃亡失敗の場合
                    logTextArea.append("しかし にげきれなかった！\n");

                    // モンスターのターン
                    String monsterResult = enemy.attack(player);
                    logTextArea.append(monsterResult);
                    updateDisplay();

                    // プレイヤーが倒れたかチェック     
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

        // 5. アイテムを使用（しよう）し、使（つか）ったスロットをnullにして消（け）す
        String resultLog = selectedItem.use(player, enemy);
        itemBox[selectedIndex] = null; // ★ 使（つか）ったら消（け）える！
        logTextArea.append(resultLog + "\n");

        // 6. Enemy Turn / Check Dead
        if (enemy.isAlive()) {
            String enemyResult = enemy.attack(player);
            logTextArea.append(enemyResult);
            updateDisplay();

            if (!player.isAlive()) {
                logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
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
        // ★ インスタンスをしょきか（Initialize）
        choicePlayer();
        enemy = new Enemy("スライム", 180, 35, 15, "Picture5.png");
        enemy2 = new Enemy("インフェルノドラゴン", 250, 40, 20, "dragon1.png");
        enemy3 = new Enemy("魔王 (Demon King)", 500, 65, 35, "Demon king1.png");
        // ★ がぞうをがめんのラベルにセットする
        playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());

        // ★ しょきステータスをひょうじする
        updateDisplay();
        logTextArea.append("野生の" + enemy.getName() + " が現れた！\n");
    }

    public static void main(String[] args) {
        BattleGame game = new BattleGame();
        game.setVisible(true); // がめんをひょうじ（Display）する
    }

    // がめんこうしんしょり（Screen Update Process）
    private void updateDisplay() {
        statusLabel.setText(String.format(
                "【%s】 LV: %d  vs  【%s】", 
                player.getName(), player.getLevel(), enemy.getName()));

        // ★ [Naya Code] HP Bar Values Update
        playerHpBar.setMaximum(player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerHpBar.setString(player.getName() + " HP: " + player.getHp() + "/" + player.getMaxHp());

        enemyHpBar.setMaximum(enemy.getMaxHp());
        enemyHpBar.setValue(enemy.getHp());
        enemyHpBar.setString(enemy.getName() + " HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
    }

    // ゲームしゅうりょうじにボタンをおせなくするしょり
    private void endGame() {
        attackButton.setEnabled(false);  
        specialButton.setEnabled(false); 
        healButton.setEnabled(false);    
        runButton.setEnabled(false);     
        itemButton.setEnabled(false);    
        restartButton.setEnabled(true);  
        logTextArea.append("【ゲームしゅうりょう（Game End）】リスタートボタンを押すと最初から遊べます。\n");
    }
    // キャラクターせんたく（Select）メソッド
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
        if (choice == 0) {
            player = new Player("ゆうしゃ（Hero）", 150, 120, 15, "sagar.png");
        } else if (choice == 1) {
            player = new Player("まほうつかい（Mage）", 120, 130, 15, "Aelina.png");
        } else if (choice == 2) {
            player = new Player("ninja（ninja）", 110, 140, 12, "Picture2.png");
        }
    } // choicePlayer ko brackets yaha banda bhayo!

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
        if (itemBox[i] == null) {       // 空（あ）きスロットを発見（はっけん）！
            itemBox[i] = item;           // アイテムをセット
            logTextArea.append("アイテムボックスのスロット " + (i + 1) + " に " + item.getName() + " を追加しました。\n");
            return;                      // 1つ入れたら終了（しゅうりょう）
        }
    }
    // ここまで来たら全スロットが埋まっている
    logTextArea.append("アイテムボックスがいっぱいです！ " + item.getName() + " は拾えませんでした。\n");
}
} 