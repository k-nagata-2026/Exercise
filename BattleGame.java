import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

public class BattleGame extends JFrame {
    //private JLabel statusLabel;       // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;     // こうげきコマンドボタン（Command Button）
    private JButton runButton;        // 逃げるコマンドボタン
    private JButton defenseButton;    // ぼうぎょコマンドボタン
    private JButton itemButton;       // アイテムコマンドボタン
    private JProgressBar[] playerHpBars = new JProgressBar[4]; // プレイヤーのHPをひょうじするプログレスバー（Progress Bar）
    private JProgressBar[] enemyHpBars = new JProgressBar[5];  // 敵のHPをひょうじするプログレスバー（Progress Bar）
    private JPanel statusPanel; // ステータスをひょうじするパネル（Panel）

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
    private CardLayout cardLayout;//画面の切り替えに必要な部品
    private JPanel mainPanel;//画面の切り替えに必要な部品

    public BattleGame() {
        setTitle("ターン制コマンドバトル");
        setSize(1220,750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //カードレイアウトを準備する
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        //各画面のパネルを作る
        JPanel titlePanel = createTitlePanel();        //タイトル画面
        JPanel battlePanel = createBattlePanel();      //バトル画面
        JPanel gameOverPanel = createGameOverPanel();  //ゲームオーバーの画面
        JPanel gameClearPanel = createGameClearPanel();//ゲームクリアの画面

        //メインパネルに名前を付けて重ねる
        mainPanel.add(titlePanel,"TITLE");
        mainPanel.add(battlePanel,"BATTLE");
        mainPanel.add(gameOverPanel,"GAMEOVER");
        mainPanel.add(gameClearPanel,"GAMECLEAR");

        //全体をウィンドウに追加
        add(mainPanel);

        //最初はタイトル画面を表示する
        cardLayout.show(mainPanel,"TITLE");
    }

    public static void main(String[] args) {
        // わくぐみのみ（つぎのステップでなかみをかきます）
        BattleGame game = new BattleGame();
        game.setVisible(true); // がめんをひょうじ（Display）する
    }

    // がめんこうしんしょり（Screen Update Process）
    private void updateDisplay() {
        //4人のHPバーを表示する
        for (int i = 0; i < 4; i++){
            Player member = party.get(i);
            JProgressBar bar = playerHpBars[i];
            bar.setMaximum(member.getMaxHp());
            bar.setValue(member.getHp());
            bar.setString(member.getName() + " HP: " + member.getHp() + "/" + member.getMaxHp());
        }
    
        //5体の敵のHPバーを表示する
        for (int i = 0; i < 5; i++){
            if (i < enemyParty.size()){
                Enemy currentEnemy = enemyParty.get(i);
                JProgressBar bar = enemyHpBars[i];
                if (currentEnemy.isAlive()){
                    bar.setForeground(new Color(139,0,0));//生きている敵のHPバーを赤にする
                    bar.setMaximum(currentEnemy.getMaxHp());
                    bar.setValue(currentEnemy.getHp());
                    bar.setString(currentEnemy.getName() + " HP: " + currentEnemy.getHp() + "/" + currentEnemy.getMaxHp());
                } else {
                    bar.setForeground(new Color(255,255,255));//倒れた敵のHPバーを白にする
                }
            } else {
                //そもそも出現してない枠のHPバーは非表示にする
                enemyHpBars[i].setVisible(false);
            }
        }

       //生きている敵を探す
       Enemy showEnemy = null;
       for (Enemy enemyInside : enemyParty){
            if (enemyInside.isAlive()){
                showEnemy = enemyInside;
                break;
            }
        }

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
        //選んだキャラクターを選択肢に出ないようにするには可変長のArrayListにする
        java.util.List<String>availableOptions = new java.util.ArrayList<>(
            java.util.Arrays.asList("勇者(HERO)", "魔法使い(WIZARD)", "騎士(KNIGHT)", "盗賊(THIEF)", "召喚士(SUMMONER)", "祈祷師(SHAMAN)", "回復術師(HEALER)")
        );
    
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
                availableOptions.toArray(new String[0]),
                null
            );

            //×ボタンを押したらウィンドウごと閉じる
            if (choice == JOptionPane.CLOSED_OPTION) {
                System.exit(0);
            }

            String selectedCharacter = availableOptions.get(choice);

            Player newPlayer = null;

            //文字列(名前)で判定する   
            if (selectedCharacter.equals("勇者(HERO)")) {
                newPlayer = new Player("勇者(HERO)", 60, 20, 20,"yuusya_game.png",0);
            } else if(selectedCharacter.equals("魔法使い(WIZARD)")) {
                newPlayer = new Player("魔法使い(WIZARD)", 45, 25, 50,"mahoutsukai_man.png", 0);
            } else if(selectedCharacter.equals("騎士(KNIGHT)")) {
                newPlayer = new Player("騎士(KNIGHT)", 65, 30, 25,"knight.png",0);
            }else if (selectedCharacter.equals("盗賊(THIEF)")) {
                newPlayer = new Player("盗賊(THIEF)", 70, 10, 20,"dorobou_hokkamuri.png",0);
            }else if (selectedCharacter.equals("召喚士(SUMMONER)")){
                newPlayer = new Player("召喚士(SUMMONER)", 90, 5, 5,"mahoutsukai_necromancer.png",0);
            }else if (selectedCharacter.equals("祈祷師(SHAMAN)")){
                newPlayer = new Player("祈祷師(SHAMAN)", 50, 5, 45,"oharai_kannushi.png",0);
            } else {
                newPlayer = new Player("回復術師(HEALER)", 45, 5, 50,"job_doctor_man.png",0);
            }
            party.add(newPlayer); // 選んだキャラクターをパーティーに追加する
            // 選んだキャラクターを選択肢から削除する
            availableOptions.remove(selectedCharacter);
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

            //敵が生まれたら対応するHPバーを表示する
            if (enemyHpBars != null){
                enemyHpBars[i].setVisible(true);
            }
            
        }
        logTextArea.append("--------------------------------------------\n");
        updateDisplay();
        
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

    //ゲームの画面切り替えメソッド
    public void gameScreen (int state) {
        switch (state) {
            case 1 :
                //ゲームのスタート画面
                cardLayout.show(mainPanel, "TITLE");
                break;
            case 2 :
                //ゲームクリアの画面
                cardLayout.show(mainPanel, "GAMECLEAR");
                break;
            case 3 :
                //ゲームオーバーの画面
                cardLayout.show(mainPanel,"GAMEOVER");
                break;
            default:
                break;
        }
    }

    //バトル画面の設定
    private JPanel createBattlePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // ウィンドウ（Window）のきほんせってい（Basic Setting）
        setTitle("ターンせいコマンドバトル");
        setSize(1220, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // がめんのまんなかにひょうじ
        setLayout(new BorderLayout()); // ぜんたいのレイアウト（Layout）をせってい

        // 【うえはんぶん：キャラクターたいじエリア（はいけいのなかにキャラをいれる）】
        // ※はいけいがぞうファイル（bg.png）をよみこみます
        backgroundLabel = new JLabel(new ImageIcon("Background1.png"));
        backgroundLabel.setLayout(null); // ★重要：自由配置にするためにnullにする

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
        
        statusPanel = new JPanel(new GridLayout(1, 9, 10, 0)); // ステータスをひょうじするパネル（Panel）

        //プレイヤー４人のHPバーを表示する
        for (int i = 0; i < 4; i++) {
            playerHpBars[i] = new JProgressBar(0, 100); // 初期値は100に設定
            playerHpBars[i].setStringPainted(true);
            playerHpBars[i].setForeground(new Color(34,139,34)); // HPバーの色を設定
            playerHpBars[i].setString("Player " + (i + 1)); // 初期の文字列を設定
            statusPanel.add(playerHpBars[i]);
        }

        //敵５体のHPバーを表示する
        for (int i = 0; i < 5; i++) {
            enemyHpBars[i] = new JProgressBar(0, 100); // 初期値は100に設定
            enemyHpBars[i].setValue(100); // 初期値は100に設定
            enemyHpBars[i].setStringPainted(true);
            enemyHpBars[i].setForeground(new Color(139,0,0)); // HPバーの色を設定
            enemyHpBars[i].setString("Enemy " + (i + 1)); // 初期の文字列を設定
            statusPanel.add(enemyHpBars[i]);
        }

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false); // プレイヤーがちょくせつもじにゅうりょくできないようにする
        JScrollPane scrollPane = new JScrollPane(logTextArea); // スクロール（Scroll）できるようにする
        
        attackButton = new JButton("攻撃(ATTACK)");
        runButton = new JButton("逃げる(RUN)");
        defenseButton = new JButton("防御(DEFENSE)");
        itemButton = new JButton("アイテム(ITEM)");

        //bottomPanel.add(statusLabel, BorderLayout.NORTH); // ステータスラベルをしたがわのうえにはいち
        bottomPanel.add(scrollPane, BorderLayout.CENTER); // ログテキストエリアをしたがわのしたにはいち 
        bottomPanel.add(statusPanel, BorderLayout.NORTH); // ステータスパネルをしたがわのうえにはいち
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // ぶひん（Parts）をメインウィンドウにはいち
        panel.add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        panel.add(bottomPanel, BorderLayout.SOUTH);       // そうさエリアをしたがわにはいち
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
                        logTextArea.append(player.getName() + " はたおれた… \n");

                        //パーティーが全滅してないかチェック
                        java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                            for (Player member : party) {
                                if (member.isAlive() && member.getHp() > 0) {
                                    aliveMembers.add(member);
                                }
                            }

                        if (aliveMembers.isEmpty()) {
                         logTextArea.append("パーティーは全滅した… \n");
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
                updateDisplay();
                
                //攻撃された敵が倒れたらその敵をグレーアウト
                if(!aliveEnemy.isAlive()){
                    logTextArea.append(aliveEnemy.getName() + "をたおした！\n");
                    //倒した敵のインデックスを調べてグレーにする
                    int index = enemyParty.indexOf(aliveEnemy);
                    enemyImageLabels[index].setEnabled(false);
                }

                // 2. エネミーがたおれたかチェック（Check）
                //敵が全員倒れたかチェック
                java.util.List<Enemy> aliveEnemies = new java.util.ArrayList<>();
                for (Enemy enemyInside : enemyParty){
                    if (enemyInside.isAlive()){
                        aliveEnemies.add(enemyInside);
                    }
                }

                if(aliveEnemies.isEmpty()){
                    logTextArea.append("敵の群れを倒した！\n");
                    if(enemyCount < 3){
                        enemyCount++;
                        spawnEnemy();
                        enemyIcon();
                        updateDisplay();
                        return;
                    } else {
                        logTextArea.append("すべての魔物を倒した！\n");
                        gameScreen(2);
                    }
                }
            
                //３．次のプレイヤーにチェンジ
                boolean isEnemyTrun = switchNextPlayer();

                //４．敵のターン
                if (isEnemyTrun){
                    logTextArea.append("敵のターンだ！\n");
                    //敵パーティをループで回す
                    for (Enemy enemyInside : enemyParty) {
                        //生きている敵は攻撃する
                        if(enemyInside.isAlive()){
                            //生きている味方だけをリストに集める
                            java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                            for (Player member : party) {
                                if (member.isAlive() && member.getHp() > 0) {
                                    aliveMembers.add(member);
                                }
                            }

                            //プレイヤーが全滅していたら攻撃をやめる
                            if (aliveMembers.isEmpty()) {
                                break;
                            }

                            //ランダムに生きているプレイヤーを選ぶ
                            int randomIndex = (int)(Math.random() * aliveMembers.size());
                            Player targetPlayer = aliveMembers.get(randomIndex);

                            //現在のプレイヤーに攻撃する
                            String aliveEnemyResult = enemyInside.attack(targetPlayer);
                            logTextArea.append(aliveEnemyResult);

                            //一体ごとに画面を更新
                            updateDisplay();
                            updatePlayerVisuals();
                        }
                    }
                }

                // ５. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
        
                    //６．パーティーが全滅してないかチェック
                    java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                    for (Player member : party) {
                        if (member.isAlive() && member.getHp() > 0) {
                            aliveMembers.add(member);
                        }
                    }

                    if (aliveMembers.isEmpty()) {
                        logTextArea.append("パーティーは全滅した… ゲームオーバー\n");
                        gameScreen(3);
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
                if (currentPlayerIndex <= oldIndex) {
                    logTextArea.append("敵のターンだ！\n");
                    //敵パーティをループで回す
                    for (Enemy enemyInside : enemyParty) {
                        //生きている敵は攻撃する
                        if(enemyInside.isAlive()){
                            //敵の攻撃でプレイヤーが全滅したら終わる
                            java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                            for (Player member : party) {
                                if (member.isAlive() && member.getHp() > 0) {
                                    aliveMembers.add(member);
                                }
                            }
                            if (aliveMembers.isEmpty()) {
                                break;//全滅していたらループを抜ける
                            }

                            //生きているプレイヤーをランダムで選ぶ
                            int randomIndex = (int)(Math.random() * aliveMembers.size());
                            Player targetPlayer = aliveMembers.get(randomIndex);

                            //選んだプレイヤーに攻撃する
                            String aliveEnemyResult = enemyInside.attack(targetPlayer);
                            logTextArea.append(aliveEnemyResult);

                            //一体ごとに画面を更新
                            updateDisplay();
                            updatePlayerVisuals();
                        }
                    }
                }

                // ５. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
        
                    //６．パーティーが全滅してないかチェック
                    java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                    for (Player member : party) {
                        if (member.isAlive() && member.getHp() > 0) {
                            aliveMembers.add(member);
                        }
                    }

                    if (aliveMembers.isEmpty()) {
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
                if (currentPlayerIndex <= oldIndex) {
                    logTextArea.append("敵のターンだ！\n");
                    //敵パーティをループで回す
                    for (Enemy enemyInside : enemyParty) {
                        //生きている敵は攻撃する
                        if(enemyInside.isAlive()){
                            //敵の攻撃でプレイヤーが全滅したら終わる
                            java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                            for (Player member : party) {
                                if (member.isAlive() && member.getHp() > 0) {
                                    aliveMembers.add(member);
                                }
                            }
                            if (aliveMembers.isEmpty()) {
                                break;//全滅していたらループを抜ける
                            }

                            //生きているプレイヤーをランダムで選ぶ
                            int randomIndex = (int)(Math.random() * aliveMembers.size());
                            Player targetPlayer = aliveMembers.get(randomIndex);

                            //選んだプレイヤーに攻撃する
                            String aliveEnemyResult = enemyInside.attack(targetPlayer);
                            logTextArea.append(aliveEnemyResult);

                            //一体ごとに画面を更新
                            updateDisplay();
                            updatePlayerVisuals();
                        }
                    }
                }  

                // ５. プレイヤーがたおれたかチェック
                if (!player.isAlive()) {
                    logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
        
                    //６．パーティーが全滅してないかチェック
                    java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                    for (Player member : party) {
                        if (member.isAlive() && member.getHp() > 0) {
                            aliveMembers.add(member);
                        }
                    }

                    if (aliveMembers.isEmpty()) {
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
        gameScreen(1);

        return panel;
    }

    //タイトル画面のメソッド
    private JPanel createTitlePanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);

        JLabel titleLabel = new JLabel("ターン制コマンドバトル", JLabel.CENTER);
        titleLabel.setFont(new Font("MS　ゴシック",Font.BOLD,36));
        titleLabel.setForeground(Color.WHITE); // 白文字

        JButton startButton = new JButton("START");
        startButton.setFont(new Font("Arial",Font.PLAIN,24));

        //スタートボタンを押したらバトル画面（キャラクター選択画面）に移動する
        startButton.addActionListener(e -> {
            choicePlayer();
            cardLayout.show(mainPanel, "BATTLE");
            spawnEnemy();
            enemyIcon();
            updateDisplay();
            updatePlayerVisuals();
            cardLayout.show(mainPanel,"BATTLE");//バトル画面に切り替え
        });

        JPanel menu = new JPanel(new GridLayout(2,1,0,30));
        menu.setBackground(Color.BLACK);
        menu.add(titleLabel);
        menu.add(startButton);
        panel.add(menu);
        return panel;
    }

    //ゲームオーバーの画面のメソッド
    private JPanel createGameOverPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);//画面が透けないようにする
        panel.setBackground(Color.BLACK);

        JLabel label = new JLabel("あなたのチームは力尽きた",JLabel.CENTER);
        label.setFont(new Font("MS　ゴシック",Font.BOLD,32));
        label.setForeground(Color.WHITE);

        JButton retryButton = new JButton("さっきの戦闘をやり直す(RETRY)");
        JButton restartButton = new JButton("最初から始める(RESTART)");
        JButton quitButton = new JButton("諦める(GIVE UP)");

        //諦めるボタンを押したら、ウィンドウごと閉じる
        quitButton.addActionListener(e -> System.exit(0));

        //縦に並べる
        JPanel menu = new JPanel(new GridLayout(4,1,0,20));
        menu.setBackground(Color.BLACK);
        menu.add(label);
        menu.add(retryButton);
        menu.add(restartButton);
        menu.add(quitButton);
        panel.add(menu);

        return panel;
    }

    //ゲームクリアの画面のメソッド
    private JPanel createGameClearPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel label1 = new JLabel("あなたのおかげで世界は平和になった",JLabel.CENTER);
        JLabel label2 = new JLabel("感謝",JLabel.CENTER);
        label1.setFont(new Font("MS ゴシック",Font.BOLD,28));
        label2.setFont(new Font("MS ゴシック",Font.BOLD,48));

        JButton continueButton = new JButton("まだ冒険を続ける？(CONTINUE)");
        JButton replayButton = new JButton("もう一回世界を救う？(PLAY AGAIN)");
        JButton endButton = new JButton("終わる(FINISH)");

        endButton.addActionListener(e -> System.exit(0));

        JPanel menu = new JPanel(new GridLayout(5,1,0,15));
        menu.setBackground(Color.WHITE);
        menu.add(label1);
        menu.add(label2);
        menu.add(continueButton);
        menu.add(replayButton);
        menu.add(endButton);
        panel.add(menu);

        return panel;
    }
}