import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    
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
        setSize(720, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // がめんのまんなかにひょうじ
        setLayout(new BorderLayout()); // ぜんたいのレイアウト（Layout）をせってい

        // 【うえはんぶん：キャラクターたいじエリア（はいけいのなかにキャラをいれる）】
        // ※はいけいがぞうファイル（bg.png）をよみこみます
        backgroundLabel = new JLabel(new ImageIcon(".Background1.png"));
        backgroundLabel.setLayout(null); // ★じゅうよう（Important）：じゆうはいち（Free Layout）にするためにnullにする

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        // ★はいけいラベルをきじゅん（Base）とした、キャラがぞうラベルの「いち（Position）(x, y)」と「サイズ（Size）(はば（Width）, たかさ（Height））」をしてい（Specify）
        playerImageLabel.setBounds(20, 20, 300, 300); // ひだりがわにはいち
        enemyImageLabel.setBounds(360, 20, 300, 300);  // みぎがわにはいち

        // ★はいけいラベルのなかにキャラがぞうラベルを「add」してかさねる！
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);
        
        // 【したはんぶん：そうさ・ログエリア】
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false); // プレイヤーがちょくせつもじにゅうりょくできないようにする
        JScrollPane scrollPane = new JScrollPane(logTextArea); // スクロール（Scroll）できるようにする

        attackButton = new JButton(" こうげきする");

        bottomPanel.add(statusLabel, BorderLayout.NORTH);  
        bottomPanel.add(scrollPane, BorderLayout.CENTER);   
        bottomPanel.add(attackButton, BorderLayout.SOUTH); 

        // ぶひん（Parts）をメインウィンドウにはいち
        add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        add(bottomPanel, BorderLayout.SOUTH);       // そうさエリアをしたがわにはいち

        // ★ ボタンをおしたときのしょりをついか
        attackButton.addActionListener(new ActionListener() {
          @Override
           public void actionPerformed(ActionEvent e) {
            // ボタンがおされたらじっこうされるしょり。つぎのステップでかく
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
 
        // ★ インスタンスをしょきか（Initialize）
        player   = new Player("プレイヤー", 100, 20, "yuusya_game.png", 5);
        enemy    = new Enemy("スライム", 50, 10, "fantasy_game_character_slime.png", 10);

        // ★ がぞうをがめんのラベルにセットする
        playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());

        // ★ しょきステータスをひょうじする
        updateDisplay();
        logTextArea.append("野生の" + enemy.getName() + " が現れた！\n");
    }

    public static void main(String[] args) {
        // わくぐみのみ（つぎのステップでなかみをかきます）
        BattleGame game = new BattleGame();
        game.setVisible(true); // がめんをひょうじ（Display）する
    }

    // がめんこうしんしょり（Screen Update Process）
    private void updateDisplay() {
       statusLabel.setText(String.format(
            "【%s】 HP: %d/%d防御%d  vs  【%s】 HP: %d/%d防御%d",
            player.getName(), player.getHp(), player.getMaxHp(), player.getDefense(),
            enemy.getName(), enemy.getHp(), enemy.getMaxHp(), enemy.getDefense()));
    }

    // ゲームしゅうりょうじにボタンをおせなくするしょり
    private void endGame() {
    attackButton.setEnabled(false); // ボタンをむこうか（Disable）
    logTextArea.append("【ゲームしゅうりょう（Game End）】ウィンドウをとじてください。\n");
    }
}