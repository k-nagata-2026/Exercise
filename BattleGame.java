import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    private JButton runButton;
    private JButton healButton;
    
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
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3)); 
        attackButton = new JButton(" こうげきする");
        healButton = new JButton("回復する");   // ★ かいふくボタン
        runButton = new JButton("にげる");

        buttonPanel.add(attackButton);
        buttonPanel.add(healButton);
        buttonPanel.add(runButton);
        
        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false); // プレイヤーがちょくせつもじにゅうりょくできないようにする
        JScrollPane scrollPane = new JScrollPane(logTextArea); // スクロール（Scroll）できるようにする

        bottomPanel.add(statusLabel, BorderLayout.NORTH);  
        bottomPanel.add(scrollPane, BorderLayout.CENTER);   
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ぶひん（Parts）をメインウィンドウにはいち
        add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        add(bottomPanel, BorderLayout.SOUTH);       // そうさエリアをしたがわにはいち
        
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
                "【%s】 LV: %d | HP: %d/%d  vs  【%s】 HP: %d/%d", 
                player.getName(), player.getLevel(), player.getHp(), player.getMaxHp(), 
                enemy.getName(), enemy.getHp(), enemy.getMaxHp()));
    }

    // ゲームしゅうりょうじにボタンをおせなくするしょり
    private void endGame() {
        attackButton.setEnabled(false); // ボタンをむこうか（Disable）
        healButton.setEnabled(false);   // 回復ボタンもむこうか
        runButton.setEnabled(false);    // 逃げるボタンもむこうか
        logTextArea.append("【ゲームしゅうりょう（Game End）】ウィンドウをとじてください。\n");
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
} 