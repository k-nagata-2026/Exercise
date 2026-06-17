import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    private JButton runButton;        // 逃げるコマンドボタン
    private JButton defenseButton;    // ぼうぎょコマンドボタン
    private JButton itemButton;       // アイテムコマンドボタン
    
    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // はいけいがぞうようのラベル
    private JLabel playerImageLabel;  // プレイヤーがぞうようのラベル
    private JLabel enemyImageLabel;   // てきがぞうようのラベル

    // ★ キャラクターのインスタンスをよういする
    private Player player;
    private java.util.List<Player> party = new java.util.ArrayList<>();//チームにするためのコード（ArrayListは可変式の配列）
    private Enemy enemy;
    private int enemyCount = 1; // たいじするてきのかずをかぞえるためのフィールド
    private int guardFlg = 0; // ガードフラグ（0: ガードしていない、1: ガードしている）
    private int currentPlayerIndex = 0; // 今何人目のプレイヤーのターンかを数えるためのフィールド（０から３）

    public BattleGame() {
        // ウィンドウ（Window）のきほんせってい（Basic Setting）
        setTitle("ターンせいコマンドバトル");
        setSize(1220, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // がめんのまんなかにひょうじ
        setLayout(new BorderLayout()); // ぜんたいのレイアウト（Layout）をせってい

        // 【うえはんぶん：キャラクターたいじエリア（はいけいのなかにキャラをいれる）】
        // ※はいけいがぞうファイル（bg.png）をよみこみます
        backgroundLabel = new JLabel(new ImageIcon("Background1.png"));
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // ボタンをならべるためのパネル（Panel）
        
        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false); // プレイヤーがちょくせつもじにゅうりょくできないようにする
        JScrollPane scrollPane = new JScrollPane(logTextArea); // スクロール（Scroll）できるようにする
        
        attackButton = new JButton("こうげき");
        runButton = new JButton("逃げる");
        defenseButton = new JButton("守る");
        itemButton = new JButton("アイテム");

        bottomPanel.add(statusLabel, BorderLayout.NORTH); // ステータスラベルをしたがわのうえにはいち
        bottomPanel.add(scrollPane, BorderLayout.CENTER); // ログテキストエリアをしたがわのしたにはいち 
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // ぶひん（Parts）をメインウィンドウにはいち
        add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        add(bottomPanel, BorderLayout.SOUTH);       // そうさエリアをしたがわにはいち
        buttonPanel.add(attackButton); // こうげきボタンをしたがわのひだりにはいち
        buttonPanel.add(runButton); // にげるボタンをしたがわのひだりにはいち
        buttonPanel.add(defenseButton); // ぼうぎょボタンをしたがわのひだりにはいち
        buttonPanel.add(itemButton); // アイテムボタンをしたがわのひだりにはいち

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
                        //endGame();

                        //パーティーが全滅してないかチェック
                    boolean allMembersDefeated = true;
                      for (Player member : party) {
                        if (member.isAlive()) {
                          if (member.getHp() > 0) {
                            allMembersDefeated = false;
                             break;
                     }
                }
            }
        
                        if (allMembersDefeated) {
                         logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                         playerImageLabel.setEnabled(false);
                         endGame();
                          return;
                        } else {
                          switchNextPlayer(); // 次のプレイヤーにきりかえる
                        }
                        return;
                    
                    }
                    
                
            
                    logTextArea.append("--------------------------------------------\n");
                }
            }
            });

        // ★ 攻撃ボタンをおしたときのしょりをついか
        attackButton.addActionListener(new ActionListener() {
          @Override
           public void actionPerformed(ActionEvent e) {
            //攻撃する前に生存チェック
            if (player.getHp() <=0) {
                switchNextPlayer(); // 次のプレイヤーにきりかえる
                return;
            }
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
            
            currentPlayerIndex++; // 次のプレイヤーにきりかえるためにインデックスをふやす
             if (currentPlayerIndex >= party.size()) {
                currentPlayerIndex = 0; // インデックスがパーティーのサイズをこえたら、最初のプレイヤーにきりかえる
            
                // 3. エネミーのターン（はんげき）
            String enemyResult = enemy.attack(player);
             logTextArea.append(enemyResult);
             currentPlayerIndex = 0; // 次のプレイヤーにきりかえるためにインデックスをリセット
            updateDisplay();
             }

                // 4. プレイヤーがたおれたかチェック
            if (!player.isAlive()) {
             logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
             playerImageLabel.setEnabled(false); // プレイヤーのがぞうをグレーアウト
             //endGame();

             //パーティーが全滅してないかチェック
               boolean allMembersDefeated = true;
                for (Player member : party) {
                     if (member.isAlive()) {
                        if (member.getHp() > 0) {
                          allMembersDefeated = false;
                          break;
                     }
                }
            }
                if (allMembersDefeated) {
                    logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    endGame();
                    return;
                } else {
                    switchNextPlayer(); // 次のプレイヤーにきりかえる
                }

             return;

            

            
            }

            player = party.get(currentPlayerIndex); // 次のプレイヤーにきりかえる
            playerImageLabel.setIcon(player.getIcon()); // プレイヤーのがぞうをせっていする
               
            logTextArea.append("--------------------------------------------\n");
            }
        });

        //ガードボタンの追加
        defenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append( player.getName() + "はガードした！\n");

                //ガードしたら、ダメージを受けないようにするためのフラグをたてる
                player.guard(); // プレイヤーのガードフラグをたてる
                 guardFlg = 1; // ガードフラグをたてる
                updateDisplay();
                    // エネミーのターン（反撃）
                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);    
                updateDisplay();
                // プレイヤーが倒れたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(player.getName() + " はたおれた… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    //endGame();

                     //パーティーが全滅してないかチェック
               boolean allMembersDefeated = true;
                for (Player member : party) {
                     if (member.isAlive()) {
                        if (member.getHp() > 0) {
                          allMembersDefeated = false;
                          break;
                     }
                }
            }
                if (allMembersDefeated) {
                    logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    endGame();
                    return;
                } else {
                    switchNextPlayer(); // 次のプレイヤーにきりかえる
                }

                    return;
                }       

               
                logTextArea.append("--------------------------------------------\n");
                }
        });

        itemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append(player.getName() + " はアイテムをつかった！ HPが20かいふくした！\n");
                player.hp += 20; // HPを20かいふくする
                if (player.hp > player.maxHp) {
                    player.hp = player.maxHp; // HPがさいだいHPをこえないようにする
                }
                updateDisplay();

                // エネミーのターン（反撃）
                String enemyResult = enemy.attack(player);
                logTextArea.append(enemyResult);    
                updateDisplay();
                // プレイヤーが倒れたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(player.getName() + " はたおれた… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    //endGame();

                     //パーティーが全滅してないかチェック
               boolean allMembersDefeated = true;
                for (Player member : party) {
                     if (member.isAlive()) {
                        if (member.getHp() > 0) {
                           allMembersDefeated = false;
                           break;
                     }
                }
            }
                if (allMembersDefeated) {
                    logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                    playerImageLabel.setEnabled(false);
                    endGame();
                    return;
                } else {
                    switchNextPlayer(); // 次のプレイヤーにきりかえる
                    logTextArea.append("次のプレイヤーは " + player.getName() + " だ！\n");
                }

                    return;
                }       

               
                logTextArea.append("--------------------------------------------\n");
            }
        });
 
        // ★ インスタンスをしょきか（Initialize）
        choicePlayer();//選択したキャラクターが出てくる
        spawnEnemy();

        // ★ がぞうをがめんのラベルにセットする
        //playerImageLabel.setIcon(player.getIcon());
        enemyImageLabel.setIcon(enemy.getIcon());

        //updateDisplay();

      
    }

    public static void main(String[] args) {
        // わくぐみのみ（つぎのステップでなかみをかきます）
        BattleGame game = new BattleGame();
        game.setVisible(true); // がめんをひょうじ（Display）する
    }

    // がめんこうしんしょり（Screen Update Process）
    private void updateDisplay() {
       //4人のステータスを表示する
       StringBuilder statusText = new StringBuilder();
       for (Player member : party) {
           statusText.append(String.format("%s HP: %d/%d atk: %d mgc: %d | ", member.getName(), member.getHp(), member.getMaxHp(), member.getAtk(), member.getMgc()));
       }

       //組み立てたステータス＋敵のステータスを表示する
         statusText.append(String.format("  | %s HP: %d/%d atk: %d mgc: %d", enemy.getName(), enemy.getHp(), enemy.getMaxHp(), enemy.getAtk(), enemy.getMgc()));
         statusLabel.setText(statusText.toString());

    }

    // ゲームしゅうりょうじにボタンをおせなくするしょり
    private void endGame() {
    attackButton.setEnabled(false); // ボタンをむこうか（Disable）
    runButton.setEnabled(false);//逃げるボタンを無効化
    defenseButton.setEnabled(false);//守るボタンを無効化
    itemButton.setEnabled(false);//アイテムボタンを無効化
    logTextArea.append("【ゲームしゅうりょう（Game End）】ウィンドウをとじてください。\n");
    }

    private void switchNextPlayer() {
        logTextArea.append("debug: switchNextPlayerが呼び出されました！\n");
        //現在のプレイヤーが倒れているか覚えておく
         boolean currentPlayerDefeated = !player.isAlive();

        for (Player p : party) {
            if (p.getHp() > 0) {
                player = p; // 次のプレイヤーにきりかえる
                playerImageLabel.setIcon(player.getIcon()); // プレイヤーのがぞうをせっていする
                playerImageLabel.setEnabled(true); // プレイヤーのがぞうのクレーアウトを解除
                updateDisplay();
                if (currentPlayerDefeated) {
                    logTextArea.append("次のプレイヤーは " + player.getName() + " だ！\n");
                }
                return;
            }
        }
    }

    // キャラクターせんたく（Select）メソッド
    private void choicePlayer() {
        String[] options = { "勇者", "魔法使い", "騎士", "盗賊", "召喚士", "祈祷師", "回復術師" };
        party.clear(); // 選択前にパーティーをクリアする
        //４人選ばれるまでループする
        while (party.size() < 4) {
            int correctMemberNom = party.size() + 1; // 正しいメンバー番号（1から始まる）

            int choice = JOptionPane.showOptionDialog(
                this,
                "キャラクターを選んでください（" + correctMemberNom + "人目）",
                "キャラクター選択",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null
            );

            String selectedCharacter = options[choice];

            //同じキャラクターが選ばれてないかのチェック
            boolean isDuplicate = false;
            for (Player member : party) {
                if (member.getName().equals(selectedCharacter)) {
                    isDuplicate = true;
                    break;
                }
            }
            if (isDuplicate) {
                JOptionPane.showMessageDialog(this, "すでに選ばれたキャラクターです。別のキャラクターを選んでください。");
                continue; // もう一度選択させる
            }

        Player newPlayer = null;    
        if (choice == 0) {
           newPlayer = new Player("勇者", 60, 20, 20,"yuusya_game.png",0);
        } else if(choice == 1) {
           newPlayer = new Player("魔法使い", 45, 25, 50,"mahoutsukai_man.png", 0);
        } else if(choice == 2) {
           newPlayer = new Player("騎士", 65, 30, 25,"knight.png",0);
        }else if (choice == 3) {
           newPlayer = new Player("盗賊", 70, 10, 20,"dorobou_hokkamuri.png",0);
        }else if (choice == 4){
            newPlayer = new Player("召喚士", 90, 5, 5,"mahoutsukai_necromancer.png",0);
        }else if (choice == 5){
            newPlayer = new Player("祈祷師", 50, 5, 45,"oharai_kannushi.png",0);
        } else {
            newPlayer = new Player("回復術師", 45, 5, 50,"job_doctor_man.png",0);
        }
        party.add(newPlayer); // 選んだキャラクターをパーティーに追加する
    }

    //4人選び終わったら、最初のキャラクターをプレイヤーとしてセットする
        player = party.get(0); // 最初のキャラクターをプレイヤーとしてセットする
        playerImageLabel.setIcon(party.get(0).getIcon()); // プレイヤーのがぞうをせっていする
        
        JOptionPane.showMessageDialog(this,  " 4人パーティが結成されました。" ,"パーティ結成", JOptionPane.INFORMATION_MESSAGE); // 選んだキャラクターをひょうじする
        }

    private void spawnEnemy() {
        if (enemyCount == 1) {
          enemy = new Enemy("スライム", 20, 5, 5, "fantasy_game_character_slime.png");
          logTextArea.append("【だい（No.）1せん（Battle）】スライム があらわれた！\n");
        } else if (enemyCount == 2) {
          enemy = new Enemy("ゴブリン", 25, 10, 5, "fantasy_goblin.png");
          logTextArea.append("【だい（No.）2せん（Battle）】ゴブリン があらわれた！\n");
        } else if (enemyCount == 3) {
          enemy = new Enemy("ドラゴン", 10000, 70, 130, "fantasy_dragon.png");
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