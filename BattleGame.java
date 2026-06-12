import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    private JButton runButton;        // 逃げるコマンドボタン
    private JButton defenseButton;
    
    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // はいけいがぞうようのラベル
    private JLabel playerImageLabel;  // プレイヤーがぞうようのラベル
    private JLabel enemyImageLabel;   // てきがぞうようのラベル

    // ★ キャラクターのインスタンスをよういする
    private Player player;
    private Enemy enemy;
    private int enemyCount = 1;

    public BattleGame() {
        // ウィンドウ（Window）のきほんせってい（Basic Setting）
        setTitle("ターンせいコマンドバトル");
        setSize(1220, 750);
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
        playerImageLabel.setBounds(20, 20, 500, 500); // ひだりがわにはいち
        enemyImageLabel.setBounds(560, 20, 500, 500);  // みぎがわにはいち

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
        runButton = new JButton("逃げる");
        defenseButton = new JButton("守る");

        bottomPanel.add(statusLabel, BorderLayout.NORTH);  
        bottomPanel.add(scrollPane, BorderLayout.CENTER);   
        bottomPanel.add(attackButton, BorderLayout.SOUTH);
        bottomPanel.add(runButton, BorderLayout.EAST);
        bottomPanel.add(defenseButton,BorderLayout.WEST);

        // ぶひん（Parts）をメインウィンドウにはいち
        add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        add(bottomPanel, BorderLayout.SOUTH);       // そうさエリアをしたがわにはいち

        
        // ★「にげる（Escape）ボタン（Button）」をおした（Press）ときのしょり（Process）をついか（Add）
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append( player.getName() + " は逃げ出そうとした！\n");

                // Math.random() は 0.0 以上 1.0 未満のランダムな数字を返す
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
                    logTextArea.append("--------------------------------------------\n");
                }
            }
        });

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
            logTextArea.append("★ " + enemy.getName() + " をたおした！ " + player.getName() + "のしょうり（Victory）！\n");
            enemyImageLabel.setEnabled(false);
            if (enemyCount < 3) {
               enemyCount++;
                spawnEnemy();
                enemyImageLabel.setEnabled(true);
            updateDisplay();
            } else {
              logTextArea.append("すべてのまもの（Monster）をたいじ（Defeat）した！せかい（World）にへいわ（Peace）がおとずれた！【ゲームクリア（Game Clear）】\n");
               enemyImageLabel.setEnabled(false);
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

        //ガードボタンの追加
        defenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append( player.getName() + "はガードした！\n");

                //ガードしたら、受けるダメージが半減する
                player.hp -= Math.max(1, enemy.atk - player.defense)*0.5;
            if (player.hp < 0) {
               player.hp = 0; // HPがマイナス（Minus）にならないようにする
            }
            updateDisplay();


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
        choicePlayer();//選択したキャラクターが出てくる
        spawnEnemy();

        // ★ がぞうをがめんのラベルにセットする
        playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());

      
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
    runButton.setEnabled(false);//逃げるボタンを無効化
    defenseButton.setEnabled(false);//守るボタンを無効化
    logTextArea.append("【ゲームしゅうりょう（Game End）】ウィンドウをとじてください。\n");
    }

    // キャラクターせんたく（Select）メソッド
    private void choicePlayer() {
    // せんたく（Select）ダイアログ（Dialog）をひょうじ（Display）（えらんだボタン（Button）のばんごう（Number）が 0, 1 でかえってくる）
    int choice = JOptionPane.showOptionDialog(
            this,
            "使用するキャラクターを選択してください",
            "キャラクター選択",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[] { "勇者", "魔法使い","騎士", "盗賊"},
            null);
        if (choice == 0) {
           player = new Player("勇者", 100, 20, "yuusya_game.png",4);
        } else if(choice == 1) {
           player = new Player("魔法使い", 80, 25, "mahoutsukai_man.png",8);
        } else if(choice == 2) {
           player = new Player("騎士", 80, 25, "knight.png",8);
        }else {
           player = new Player("盗賊", 80, 25, "dorobou_hokkamuri.png",8);
        }
    }
    private void spawnEnemy() {
        if (enemyCount == 1) {
          enemy = new Enemy("スライム", 40, 8, "fantasy_game_character_slime.png", 4);
          logTextArea.append("【だい（No.）1せん（Battle）】スライム があらわれた！\n");
        } else if (enemyCount == 2) {
          enemy = new Enemy("ゴブリン", 90, 15, "fantasy_goblin.png", 5);
          logTextArea.append("【だい（No.）2せん（Battle）】ゴブリン があらわれた！\n");
        } else if (enemyCount == 3) {
          enemy = new Enemy("ドラゴン", 160, 24, "fantasy_dragon.png", 10);
          logTextArea.append("【さいしゅう（Final）けっせん（Battle）】でんせつ（Legend）の ドラゴン があらわれた！\n");
        }
          enemyImageLabel.setIcon(enemy.getIcon());
          logTextArea.append("--------------------------------------------\n");
        
    }
                // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
            public boolean isAlive() {
             return player.hp > 0;
            }
}      