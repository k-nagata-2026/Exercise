import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;
    private JButton speedButton;     // こうげきコマンドボタン（Command Button）
    private JButton skillButton;
    private JButton potionButton;
    private int playerX = 150;
    private int playerY = 250;
    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // はいけいがぞうようのラベル
    private JLabel playerImageLabel;  // プレイヤーがぞうようのラベル
    private JLabel enemyImageLabel;   // てきがぞうようのラベル
    private JProgressBar playerHpBar;
    private JProgressBar enemyHpBar;

    

    JPanel hPanel = new JPanel(new GridLayout(2, 1));

    private void showVictoryScreen() {
        JOptionPane.showMessageDialog(this,
        "ITS YOUR TIME!\nYou defeated all enemies!" );
    }

    // ★ キャラクターのインスタンスをよういする
private Player player;
private Enemy enemy;
private int enemyCount = 1;

    public BattleGame() {
        playerImageLabel = new JLabel();
        add(playerImageLabel);
        playerImageLabel.setBounds(playerX, playerY, 200, 200);

setFocusable(true);

addKeyListener(new java.awt.event.KeyAdapter() {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {

        switch (e.getKeyCode()) {
            case java.awt.event.KeyEvent.VK_LEFT:
                playerX -= 10;
                break;

            case java.awt.event.KeyEvent.VK_RIGHT:
                playerX += 10;
                break;

            case java.awt.event.KeyEvent.VK_UP:
                playerY -= 10;
                break;

            case java.awt.event.KeyEvent.VK_DOWN:
                playerY += 10;
                break;
        }

        playerImageLabel.setBounds(
            playerX,
            playerY,
            200,
            200
        );
    }
});
        // ウィンドウ（Window）のきほんせってい（Basic Setting）
        setTitle("ターンせいコマンドバトル");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // がめんのまんなかにひょうじ
        setLayout(new BorderLayout()); // ぜんたいのレイアウト（Layout）をせってい

        // 【うえはんぶん：キャラクターたいじエリア（はいけいのなかにキャラをいれる）】
        // ※はいけいがぞうファイル（bg.png）をよみこみます
        backgroundLabel = new JLabel(new ImageIcon("battlebackround.png"));
        backgroundLabel.setLayout(null); // ★じゅうよう（Important）：じゆうはいち（Free Layout）にするためにnullにする

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        // ★はいけいラベルをきじゅん（Base）とした、キャラがぞうラベルの「いち（Position）(x, y)」と「サイズ（Size）(はば（Width）, たかさ（Height））」をしてい（Specify）
        playerImageLabel.setBounds(40, 50, 500, 500); // ひだりがわにはいち
        enemyImageLabel.setBounds(550, 50, 500, 500);  // みぎがわにはいち

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

        add(hpPanel, BorderLayout.NORTH);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);  
        bottomPanel.add(scrollPane, BorderLayout.CENTER);   
        bottomPanel.add(attackButton, BorderLayout.SOUTH);
        bottomPanel.add(speedButton, BorderLayout.EAST); 
        bottomPanel.add(skillButton, BorderLayout.NORTH);
        bottomPanel.add(potionButton, BorderLayout.WEST);
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
            player.gainExp(50);
            logTextArea.append("★ " + enemy.getName() + " をたおした！ " + player.getName() + "のしょうり（Victory）！\n");
            enemyImageLabel.setEnabled(false);
            if (enemyCount < 4) {
                enemyCount++;
                spawnEnemy();
                enemyImageLabel.setEnabled(true);
                updateDisplay();
                if (enemy.getHp() <= 0) {
                    if (enemyCount >= 4) { 
                        javax.swing.JOptionPane.showMessageDialog(BattleGame.this,
                            "GAME CLEAR! \nYou defeated all monsters!","VICTORY",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE
                        );
                        showVictoryScreen();
                    }
                }
            }
            else { 
            logTextArea.append("すべてのまもの（Monster）をたいじ（Defeat）した！せかい（World）にへいわ（Peace）がおとずれた！【ゲームクリア（Game Clear）】\n");
            enemyImageLabel.setEnabled(false);
            endGame();
            return;
        }
    
        

       skillButton.addActionListener(a -> {
        int damage = player.getAtk() * 2;
        enemy.setHp(enemy.getHp() - damage);

        logTextArea.append("Fire Ball -" + damage + " HP\n");
        updateDisplay();
       });

        // 3. エネミーのターン（はんげき）
        String enemyResult = enemy.attack(player);
        logTextArea.append(enemyResult);
        updateDisplay();

        if (enemy.getHp() <= 0) {
            showVictoryScreen();
        }

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
choicePlayer();
spawnEnemy();

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
            "【%s】Lv.%d HP: %d/%d  vs  【%s】 HP: %d/%d",
          player.getName(),
          player.getLevel(),
          player.getHp(),
          player.getMaxHp(),
          enemy.getName(),
          enemy.getHp(),
          enemy.getMaxHp()
            ));

            playerHpBar.setMaximum(player.getMaxHp());
            playerHpBar.setValue(player.getHp());
            playerHpBar.setString(
                player.getHp() + "/" + player.getMaxHp()
            );

            enemyHpBar.setMaximum(enemy.getMaxHp());
            enemyHpBar.setValue(enemy.getHp());
            enemyHpBar.setString(
                enemy.getHp() + "/" + enemy.getMaxHp()
            );
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
            new String[] { "BHOLA（Hero）", "BIYON（Mage）" , "ladyboss" , "bigboss"},
            null);
    if (choice == 0) {
        player = new Player("BHOLA（Hero）", 100, 20, 10, 10, 30, "BHOLA.png");
    } else if (choice == 1) {
        player = new Player("BIYON（Mage）", 100, 25, 10, 15, 30, "BIYON.png");
    }
    else if (choice == 2){
        player = new Player("LedyBoss", 100, 15, 20, 10, 30, "ladyboss.png");
    }
    else if (choice == 3) {
        player = new Player("boss", 100, 25, 10, 15, 30, "bigboss.png");
    }
}
private void spawnEnemy() {
    if (enemyCount == 0)  {
        enemy = new Enemy("chotu", 40, 8, 5, 5, 10, "chotuenemy.png");
        logTextArea.append("【だい（No.）1せん（Battle）】スライム があらわれた！\n");
    } else if (enemyCount == 1) {
        enemy = new Enemy("boss", 90, 15, 5, 5, 10,  "enemyboss.png");
        logTextArea.append("【だい（No.）2せん（Battle）】ゴブリン があらわれた！\n");
    } else if (enemyCount == 2) {
        enemy = new Enemy("tiger", 160, 24, 5, 5, 10,  "tiger.png");
        logTextArea.append("⚠ WARNING ⚠\\nFINAL BOSS APPEARED! \n");
    } else if (enemyCount == 3) {
         enemy = new Enemy("lagartha", 180, 30, 10, 10, 10, "lagartha.png");
         logTextArea.append("WARNING\\nLAGARTHA IS COMMING! \n");
    }
   




    enemyImageLabel.setIcon(enemy.getIcon());
    logTextArea.append("--------------------------------------------\n");
}

}
