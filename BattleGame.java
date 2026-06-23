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
    private JLabel backgroundLabel;   // 背景画像のラベル
    private JLabel[] playerImageLabels = new JLabel[4];  // プレイヤー画像用のラベル
    private JLabel[] enemyImageLabels = new JLabel[5];//敵の画像用ラベル

    // ★ キャラクターのインスタンスをよういする
    private Player player;
    private java.util.List<Player> party = new java.util.ArrayList<>();//チームにするためのコード（ArrayListは可変式の配列）
    private Enemy enemy;
    private java.util.List<Enemy> enemyParty = new java.util.ArrayList<>();//敵をチーム（複数体）にするためのコード
    private int enemyCount = 1; // 対峙する敵の数を数えるためのフィールド
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

        //for文で横にずらしながらプレイヤーの画像をセットする
        for (int i = 0; i < 4; i++ ){
            playerImageLabels[i] = new JLabel("",JLabel.CENTER);
            playerImageLabels[i].setBounds(20 + (i * 140), 100, 130, 400);//（i * 140）は140マス間をあけるという意味
            backgroundLabel.add(playerImageLabels[i]);
        }
        
        //for文で横にずらしながら敵の画像をセットする
        for (int i = 0; i < 5; i++ ){
            enemyImageLabels[i] = new JLabel("",JLabel.CENTER);
            backgroundLabel.add(enemyImageLabels[i]);
        }
        enemyIcon();

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
            }
        });

        // ★ 攻撃ボタンをおしたときのしょりをついか
        attackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //生きている敵を探す
                Enemy aliveEnemy = null;
                for(Enemy enemyInside : enemyParty) {
                    if(enemyInside.isAlive()){
                        aliveEnemy = enemyInside;
                        break;
                    }
                }

                //敵が全滅している場合は何もしない
                if(aliveEnemy == null) return;

                // 1. プレイヤーのターン（Turn）
                String playerResult = player.attack(aliveEnemy);
                logTextArea.append(playerResult);
                
                //攻撃された敵が倒れたらその敵をグレーアウト
                if(!aliveEnemy.isAlive()){
                    logTextArea.append(aliveEnemy.getName() + "をたおした！\n");
                    //倒した敵のインデックスを調べてグレーにする
                    int index = enemyParty.indexOf(aliveEnemy);
                    enemyImageLabels[index].setEnabled(false);
                }

                // 2. エネミーがたおれたかチェック（Check）
                //敵が全員倒れたかチェック
                boolean allEnemiesDefeated = true;
                for (Enemy enemyInside : enemyParty){
                    if (enemyInside.isAlive()){
                        allEnemiesDefeated = false;
                        break;
                    }
                }

                if(allEnemiesDefeated){
                    logTextArea.append("敵の群れを倒した！\n");
                    if(enemyCount < 3){
                        enemyCount++;
                        spawnEnemy();
                        enemyIcon();
                        updateDisplay();
                        return;
                    } else {
                        logTextArea.append("すべての魔物を倒した！\n");
                        endGame();
                    }
                }
            
                //３．次のプレイヤーにチェンジ
                boolean isEnemyTrun = switchNextPlayer();

                //４．敵のターン
                if (isEnemyTrun){
                    Enemy currentEnemy = null;
                    for (Enemy enemyInside : enemyParty){
                        if(enemyInside.isAlive()){
                            currentEnemy = enemyInside;
                            break;
                        }
                    }

                    if (currentEnemy != null){
                        String aliveEnemyResult = currentEnemy.attack(player);
                        logTextArea.append(aliveEnemyResult);
                        updateDisplay();
                        updatePlayerVisuals();
                    }
                    
                }

                // ５. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
        
                    //６．パーティーが全滅してないかチェック
                    boolean allMembersDefeated = true;
                    for (Player member : party) {
                        if (member.isAlive() && member.getHp() > 0) { 
                            allMembersDefeated = false;
                            break;
                        }
                    }

                    if (allMembersDefeated) {
                        logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                        endGame();
                        return;
                    } else {
                        //全員死んでいないなら、生きているキャラクターと交代
                        switchNextPlayer(); 
                        updatePlayerVisuals();
                    }
                }
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

                //チェンジする前に今のプレイヤーのインデックスを覚えておく
                int oldIndex = currentPlayerIndex;

                //３．次のプレイヤーにチェンジ
                switchNextPlayer();

                //４．敵のターン
                if (currentPlayerIndex <= oldIndex){
                    String enemyResult = enemy.attack(player);
                    logTextArea.append(enemyResult);
                    updateDisplay();
                    updatePlayerVisuals();
                }

                // ５. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
        
                    //６．パーティーが全滅してないかチェック
                    boolean allMembersDefeated = true;
                    for (Player member : party) {
                        if (member.isAlive() && member.getHp() > 0) { 
                            allMembersDefeated = false;
                            break;
                        }
                    }

                    if (allMembersDefeated) {
                        logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                        endGame();
                        return;
                    } else {
                        //全員死んでいないなら、生きているキャラクターと交代
                        switchNextPlayer(); 
                        updatePlayerVisuals();
                    }
                }
                logTextArea.append("--------------------------------------------\n");
            }
        });

        //アイテムボタンの追加
        itemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append(player.getName() + " はアイテムをつかった！ HPが20かいふくした！\n");
                player.hp += 20; // HPを20かいふくする
                if (player.hp > player.maxHp) {
                    player.hp = player.maxHp; // HPがさいだいHPをこえないようにする
                }
                updateDisplay();

                //チェンジする前に今のプレイヤーのインデックスを覚えておく
                int oldIndex = currentPlayerIndex;

                //３．次のプレイヤーにチェンジ
                switchNextPlayer();

                //４．敵のターン
                if (currentPlayerIndex <= oldIndex){
                    String enemyResult = enemy.attack(player);
                    logTextArea.append(enemyResult);
                    updateDisplay();
                    updatePlayerVisuals();
                }   

                // ５. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
        
                    //６．パーティーが全滅してないかチェック
                    boolean allMembersDefeated = true;
                    for (Player member : party) {
                        if (member.isAlive() && member.getHp() > 0) { 
                            allMembersDefeated = false;
                            break;
                        }
                    }

                    if (allMembersDefeated) {
                        logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                        endGame();
                        return;
                    } else {
                        //全員死んでいないなら、生きているキャラクターと交代
                        switchNextPlayer(); 
                        updatePlayerVisuals();
                    }
                }
            logTextArea.append("--------------------------------------------\n");
            }
        });
 
        // ★ インスタンスをしょきか（Initialize）
        choicePlayer();//選択したキャラクターが出てくる
        spawnEnemy();

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

       //生きている敵を探す
       Enemy showEnemy = null;
       for (Enemy enemyInside : enemyParty){
        if (enemyInside.isAlive()){
            showEnemy = enemyInside;
            break;
        }
       }

       //組み立てたステータス＋敵のステータスを表示する
         statusText.append(String.format("  | %s HP: %d/%d atk: %d mgc: %d", showEnemy.getName(), showEnemy.getHp(), showEnemy.getMaxHp(), showEnemy.getAtk(), showEnemy.getMgc()));
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
    
    // 次のプレイヤーにきりかえるしょり
    private boolean switchNextPlayer() {
        //現在のプレイヤーが倒れているか覚えておく
        boolean currentPlayerDefeated = !player.isAlive();
        //1周したかどうかの判定のフラグ
        boolean turnedOver = false;

        //生きているプレイヤーを探す（最大4回ループする）
        for (int i = 0; i < party.size(); i++) {
            int oldIndex = currentPlayerIndex;
            currentPlayerIndex = (currentPlayerIndex + 1) % party.size(); 

            //インデックスが前の値より小さくなった、または0になったら一周したとみなす
            if (currentPlayerIndex <= oldIndex){
                turnedOver = true;
            }
            
            Player p = party.get(currentPlayerIndex);
            if (p.getHp() > 0){
                player = p;//次のプレイヤーに切り替える
                updatePlayerVisuals();
                if(currentPlayerDefeated){
                    logTextArea.append("次のプレイヤーは" + player.getName() + "だ！\n");
                }
                return turnedOver;
            }
        }
        return turnedOver;
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
        //playerImageLabel.setIcon(party.get(0).getIcon()); // プレイヤーのがぞうをせっていする
        
        JOptionPane.showMessageDialog(this,  " 4人パーティが結成されました。" ,"パーティ結成", JOptionPane.INFORMATION_MESSAGE); // 選んだキャラクターをひょうじする

        for (int i = 0; i < 4; i++){
            playerImageLabels[i].setIcon(party.get(i).getIcon());
        }
    }

    //敵のスポーンメソッド
    private void spawnEnemy() {
        //戦いの前に敵パーティをクリア
        enemyParty.clear();
        for (int i = 0; i < 5; i++){
            enemyImageLabels[i].setIcon(null);
        }

        int numberOfEnemies = 1;//出現する敵の数

        if (enemyCount == 1) {
            numberOfEnemies = (int)(Math.random() * 5) + 1;
            logTextArea.append("第一戦　スライムが" + numberOfEnemies + "体あらわれた！\n");

            for (int i = 0; i < numberOfEnemies; i++){
                //スライムA、スライムB…と名付ける
                char suffix = (char)('A' + i);
                enemyParty.add(new Enemy("スライム" + suffix, 20, 5, 5,"fantasy_game_character_slime.png"));
            }
        } else if (enemyCount == 2) {
            numberOfEnemies = (int)(Math.random() * 5) + 1;
            logTextArea.append("第二戦　ゴブリンが" + numberOfEnemies + "体あらわれた！\n");

            for (int i = 0; i < numberOfEnemies; i++){
                char suffix = (char)('A' + i);
                enemyParty.add(new Enemy("ゴブリン" + suffix, 25, 10, 5, "fantasy_goblin.png"));
            }
        } else if (enemyCount == 3) {
            //ドラゴンは一体だけに固定
            numberOfEnemies = 1;
            logTextArea.append("最終決戦　伝説の ドラゴン があらわれた！\n");
            enemyParty.add(new Enemy("ドラゴン" ,500, 30, 130, "fantasy_dragon.png"));
        }
        
        for (int i = 0; i < enemyParty.size(); i++){
            enemyImageLabels[i].setIcon(enemyParty.get(i).getIcon());
            enemyImageLabels[i].setEnabled(true);
        }
        logTextArea.append("--------------------------------------------\n");
        
    }
    // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
    public boolean isAlive() {
        return player.hp > 0;
    }
    
    //プレイヤーの状態をイラストで確認できるようにするメソッド
    public void updatePlayerVisuals(){
        for (int i = 0; i < 4; i++){
            Player p = party.get(i);

            if (!p.isAlive() || p.getHp() <= 0) {
                //死んでるキャラクターはグレーアウトして通常の位置に
                playerImageLabels[i].setEnabled(false);
                playerImageLabels[i].setBounds(20 + (i * 140), 100, 130, 400);
            } else if (i == currentPlayerIndex){
                //現在ターンが回ってきたキャラは少し上がる
                playerImageLabels[i].setEnabled(true);
                playerImageLabels[i].setBounds(20 + (i * 140), 40, 130, 400);
            } else {
                //待機中の生存キャラは通常位置
                playerImageLabels[i].setEnabled(true);
                playerImageLabels[i].setBounds(20 + (i * 140), 100, 130, 400);
            }
        }
        backgroundLabel.repaint();//画面をリフレッシュする
    }

    //敵の画像の切り替えメソッド
    public void enemyIcon(){
        //for文で横にずらしながら敵の画像をセットする
        for (int i = 0; i < 5; i++ ){
            if (enemyCount == 3){
                enemyImageLabels[i].setBounds(600 + (i * 110), 50, 550, 500);
                if (i == 0){
                    break;
                }
            }else{
                enemyImageLabels[i].setBounds(600 + (i * 110), 100, 100, 400);
            }
            backgroundLabel.revalidate();
            backgroundLabel.repaint();
        }
    }
}