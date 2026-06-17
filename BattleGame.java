import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    private JButton runButton;
    
    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // はいけいがぞうようのラベル
    private JLabel playerImageLabel;  // プレイヤーがぞうようのラベル
    private JLabel enemyImageLabel;   // てきがぞうようのラベル

    // ★ キャラクターのインスタンスをよういする
private Player player;
private Enemy enemy;

    public BattleGame() {
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

        // ★はいけいラベルをきじゅん（Base）とした、キャラがぞうラベルの「いち（Position）(x, y)」と「サイズ（Size）(はば（Width）, たかさ（Height））」をしてい（Specify）
        playerImageLabel.setBounds(200, 10, 500, 700); // ひだりがわにはいち
        enemyImageLabel.setBounds(700, 10, 500, 700);  // みぎがわにはいち

        // ★はいけいラベルのなかにキャラがぞうラベルを「add」してかさねる！
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);
        
        // 【したはんぶん：そうさ・ログエリア】
        JPanel bottomPanel = new JPanel(new BorderLayout());
        runButton = new JButton("にげる");
        
        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false); // プレイヤーがちょくせつもじにゅうりょくできないようにする
        JScrollPane scrollPane = new JScrollPane(logTextArea); // スクロール（Scroll）できるようにする

        attackButton = new JButton(" こうげきする");

        bottomPanel.add(statusLabel, BorderLayout.NORTH);  
        bottomPanel.add(scrollPane, BorderLayout.CENTER);   
        bottomPanel.add(attackButton, BorderLayout.SOUTH); 
        bottomPanel.add(runButton, BorderLayout.EAST);

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

    // 2. エネミーがたおれたかチェック（Check）
    if (!enemy.isAlive()) {
        logTextArea.append("★ " + enemy.getName() + " を倒した！ " + player.getName() + "の勝利！\n");
        enemyImageLabel.setEnabled(false); // てきのがぞうをグレーアウト
        endGame();
        return; // てきがたおれたらここでしょりをしゅうりょう（End）
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
    // ★「にげる（Escape）ボタン（Button）」をおした（Press）ときのしょり（Process）をついか（Add）
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append( player.getName() + " は逃げ出そうとした！\n");

                
                // 0.5 未満（50% の確率）なら逃亡成功とする
                if (Math.random() < 0.5) {
                    logTextArea.append("うまくにげきれた！\n");
                    endGame(); // ゲームをしゅうりょう（End）させる
                } else {
                    // 逃亡失敗の場合
                    logTextArea.append("しかし にげきれなかった！\n");

                    // モンスターのターン（ペナルティとして敵の反撃を受ける）
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
                    logTextArea.append("-----------// Math.random() は 0.0 以上 1.0 未満のランダムな数字を返す---------------------------------\n");
                }
            }
        });
    // ★ インスタンスをしょきか（Initialize）
choicePlayer();
enemy    = new Enemy("スライム", 50, 10,10, "Picture5.png");

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
            "【%s】 HP: %d/%d  vs  【%s】 HP: %d/%d",
            player.getName(), player.getHp(), player.getMaxHp(),
            enemy.getName(), enemy.getHp(), enemy.getMaxHp()));
}
// ゲームしゅうりょうじにボタンをおせなくするしょり
private void endGame() {
 attackButton.setEnabled(false); // ボタンをむこうか（Disable）
 logTextArea.append("【ゲームしゅうりょう（Game End）】ウィンドウをとじてください。\n");
}
// キャラクターせんたく（Select）メソッド
private void choicePlayer() {
    // せんたく（Select）ダイアログ（Dialog）をひょうじ（Display）（えらんだボタン（Button）のばんごう（Number）が 0, 1 でかえってくる）
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
        player = new Player("ゆうしゃ（Hero）", 100, 20, 10, "sagar.png");
    } else if (choice == 1)
        player = new Player("まほうつかい（Mage）", 80, 25, 10, "Aelina.png");

       else if (choice == 2) player = new Player("ninja（ninja）", 80, 25, 10, "Picture2.png");
}
}
