import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BattleGame extends JFrame {
    private JTextArea logTextArea;    // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton skillButton;      // スキルコマンドボタン（Command Button）
    private JButton runButton;        // 逃げるコマンドボタン
    private JButton defenseButton;    // ぼうぎょコマンドボタン
    private JButton itemButton;       // アイテムコマンドボタン
    private JButton dictionaryButton; // 技辞典コマンドボタン
    private JProgressBar[] playerHpBars = new JProgressBar[4]; // プレイヤーのHPをひょうじするプログレスバー（Progress Bar）
    private JProgressBar[] enemyHpBars = new JProgressBar[5];  // 敵のHPをひょうじするプログレスバー（Progress Bar）
    private JPanel statusPanel; // ステータスをひょうじするパネル（Panel）
    private ImageIcon currentBackgroundImage; // 現在の背景画像を保持するフィールド
    private String currentField = "FOREST"; // 現在のフィールドを保持するフィールド（例: "FOREST", "DUNGEON", "BOSS"）
    private int bossStage = 1; // ボスステージを保持するフィールド（1: ドラゴン, 2: 魔王）

    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // 背景画像のラベル
    private JLabel[] playerImageLabels = new JLabel[4];  // プレイヤー画像用のラベル
    private JLabel[] enemyImageLabels = new JLabel[5];//敵の画像用ラベル
    private JLabel[] playerStatusLabels = new JLabel[4];//プレイヤーの画像の下にステータスを貼るためのラベル
    private JLabel[] enemyStatusLabels = new JLabel[5];//敵の画像の下にステータスを貼るためのラベル

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
    private int retryCount = 1; // リトライ回数を数えるためのフィールド
    private Player coveringPlayer; // かばうを使ったプレイヤーを保持するフィールド

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
        mainPanel.add(new MapPanel(this),"MAP"); // マップ画面を追加

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
            bar.setString("Lv." + member.getLevel() + " HP: " + member.getHp() + "/" + member.getMaxHp());
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
                    bar.setString(" HP: " + currentEnemy.getHp() + "/" + currentEnemy.getMaxHp());
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
    skillButton.setEnabled(false); // ボタンをむこうか（Disable）
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
        party.clear(); // 選択前にパーティーをクリアする

        //選択候補を作成
        java.util.List<Player> availableList = new java.util.ArrayList<>();

        Player hero = new Player("勇者(HERO)", 60, 20, 20,"yuusya_game.png",0, 1, 0, 10);
        hero.learnSkill("斬る(SLASH)", 1.0, "単体攻撃");
        hero.learnSkill("かばう(COVER)", 0.25, "かばう");
        availableList.add(hero);

        Player wizard = new Player("魔法使い(WIZARD)", 45, 5, 50,"mahoutsukai_man.png", 0, 1, 0, 10);
        wizard.learnSkill("炎魔法(FIRE)", 0.5, "全体攻撃");
        availableList.add(wizard);

        Player knight = new Player("騎士(KNIGHT)", 65, 30, 5,"knight.png",0, 1, 0, 100);
        knight.learnSkill("斬る(SLASH)", 1.0, "単体攻撃");
        knight.learnSkill("かばう(COVER)", 0.25, "かばう");
        availableList.add(knight);

        Player thief = new Player("盗賊(THIEF)", 70, 10, 20,"dorobou_hokkamuri.png",0, 1, 0, 100);
        thief.learnSkill("斬る(SLASH)", 1.0, "単体攻撃");
        availableList.add(thief);

        Player summoner = new Player("召喚士(SUMMONER)", 90, 5, 5,"mahoutsukai_necromancer.png",0, 1, 0, 100);
        summoner.learnSkill("召喚(SUMMON)", 1.0, "全体攻撃");
        availableList.add(summoner);

        Player shaman = new Player("祈祷師(SHAMAN)", 50, 5, 45,"oharai_kannushi.png",0, 1, 0, 100);
        shaman.learnSkill("攻撃力UP(ATK BUFF)", 1.5, "単体バフ");
        shaman.learnSkill("魔力UP(MGC BUFF)", 1.5, "単体バフ");
        availableList.add(shaman);

        Player healer = new Player("回復術師(HEALER)", 45, 5, 50,"job_doctor_man.png",0, 1, 0, 100);
        healer.learnSkill("回復(HEAL)", 1.0, "単体回復");
        healer.learnSkill("全体回復(MASS HEAL)", 0.5, "全体回復");
        availableList.add(healer);

        //ウィンドウを作成
        JDialog dialog = new JDialog(this, "キャラクター選択", true);
        dialog.setSize(1220, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10,10));
        dialog.getContentPane().setBackground(new Color(230, 230, 230)); // 背景色を設定

        //表示パーツの準備
        final int[] currentIndex = {0}; //現在表示中のキャラ番号

        JLabel imageLabel = new JLabel("", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(250, 250));

        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("MS ゴシック", Font.BOLD, 18));
        statusArea.setBackground(new Color(255, 255, 255));
        statusArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton prevButton = new TriangleButton(true);
        JButton nextButton = new TriangleButton(false);
        prevButton.setPreferredSize(new Dimension(40, 120));
        nextButton.setPreferredSize(new Dimension(40, 120));

        JButton selectButton = new JButton("選択する(SELECT)");
        JButton startButton = new JButton("出発!!(START)");
        startButton.setEnabled(false); // 最初は無効化しておく
        startButton.setBackground(new Color(255, 215, 0)); // ゴールド色に設定
        startButton.setForeground(Color.WHITE); // 文字色を白に設定
        startButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));

        //チーム4人のアイコン枠
        JLabel[] teamSlots = new JLabel[4];
        JPanel teamPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        teamPanel.add(new JLabel("チーム: "));
        for (int i = 0; i < 4; i++) {
            teamSlots[i] = new JLabel("", JLabel.CENTER);
            teamSlots[i].setPreferredSize(new Dimension(50, 50));
            teamSlots[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            teamPanel.add(teamSlots[i]);
        }

        //画面表示を更新する処理
        Runnable updateView = new Runnable() {
            @Override
            public void run() {
                Player currentPlayer = availableList.get(currentIndex[0]);
                imageLabel.setIcon(currentPlayer.getIcon());
                statusArea.setText("名前: " + currentPlayer.getName() + "\n" +
                                   "体力(HP): " + currentPlayer.getHp() + "\n" +
                                   "攻撃力(ATK): " + currentPlayer.getAtk() + "\n" +
                                   "魔法力(MGC): " + currentPlayer.getMgc() + "\n" +
                                   "特徴(TRAIT)\n" + getCharacterFeatures(currentPlayer.getName()));
            }
        };

        //初期表示
        updateView.run();

        //ボタン処理
        //◀ボタン
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!availableList.isEmpty()) {
                    currentIndex[0] = (currentIndex[0] - 1 + availableList.size()) % availableList.size();
                    updateView.run();
                }
            }
        });

        //▶ボタン
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!availableList.isEmpty()) {
                    currentIndex[0] = (currentIndex[0] + 1) % availableList.size();
                    updateView.run();
                }
            }
        });

        //選択するボタン
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (party.size() < 4 && !availableList.isEmpty()) {
                    //選んだキャラクターをパーティに追加
                    Player selectedPlayer = availableList.get(currentIndex[0]);
                    party.add(selectedPlayer);

                    //チーム枠にアイコンを表示
                    teamSlots[party.size() - 1].setIcon(selectedPlayer.getIcon());

                    //選んだらインデックス処理
                    availableList.remove(currentIndex[0]);

                    //選択後の処理
                    if (party.size() == 4) {
                        selectButton.setEnabled(false); // 選択ボタンを無効化
                        startButton.setEnabled(true);    // 出発ボタンを有効化
                        statusArea.setText("4人パーティが結成されました。\n出発ボタンを押して冒険を始めましょう！");
                        imageLabel.setIcon(null); // キャラクター画像を非表示にする
                    } else {
                        updateView.run();
                    }
                }
            }
        });

        //出発ボタン
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // ダイアログを閉じる
                player = party.get(0); // 最初のキャラクターをプレイヤーとしてセットする
                for (int i = 0; i < 4; i++){
                    playerImageLabels[i].setIcon(party.get(i).getIcon());
                }
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false); // 背景を透明にする

        //左ボタン
        JPanel leftButtonPanel = new JPanel(new GridBagLayout());
        leftButtonPanel.setOpaque(false); // 背景を透明にする
        leftButtonPanel.add(prevButton);
        centerPanel.add(leftButtonPanel, BorderLayout.WEST);

        //中央キャラエリア
        JPanel charInfoPanel = new JPanel(new GridLayout(1,2,10, 0));
        charInfoPanel.setOpaque(false);
        charInfoPanel.add(imageLabel);
        charInfoPanel.add(statusArea);
        centerPanel.add(charInfoPanel, BorderLayout.CENTER);

        //右ボタン
        JPanel rightButtonPanel = new JPanel(new GridBagLayout());
        rightButtonPanel.setOpaque(false); // 背景を透明にする
        rightButtonPanel.add(nextButton);
        centerPanel.add(rightButtonPanel, BorderLayout.EAST);

        //下部分
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false); // 背景を透明にする
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // パディングを追加
        bottomPanel.add(teamPanel, BorderLayout.WEST);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonPanel.setOpaque(false); // 背景を透明にする
        actionButtonPanel.add(selectButton);
        actionButtonPanel.add(startButton);
        bottomPanel.add(actionButtonPanel, BorderLayout.EAST);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);//ダイアログを表示する

    }

    //キャラクターの特徴を返すメソッド
    private String getCharacterFeatures(String characterName) {
        switch (characterName) {
            case "勇者(HERO)":
                return "・バランスの取れたステータス\n・単体攻撃とかばうが可能";
            case "魔法使い(WIZARD)":
                return "・魔法攻撃に特化\n・全体攻撃が得意";
            case "騎士(KNIGHT)":
                return "・高い防御力\n・単体攻撃とかばうが可能";
            case "盗賊(THIEF)":
                return "・素早さが高い\n・単体攻撃が得意";
            case "召喚士(SUMMONER)":
                return "・強力な全体攻撃を持つ\n・耐久力は低め";
            case "祈祷師(SHAMAN)":
                return "・味方の強化が得意\n・バフスキルを持つ";
            case "回復術師(HEALER)":
                return "・味方の回復に特化\n・全体回復も可能";
            default:
                return "";
        }
    }

    //三角形の形をしたボタンを作るメソッド
    class TriangleButton extends JButton {
        private boolean isLeft;
        
        public TriangleButton(boolean isLeft) {
            this.isLeft = isLeft;
            setContentAreaFilled(false); // 背景を塗りつぶさない
            setFocusPainted(false); // フォーカスの描画を無効化
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            //滑らかに描画するためのアンチエイリアスを有効化
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //ボタンの色を設定
            if (getModel().isArmed()) {
                g2d.setColor(new Color(218, 165, 32));
            } else {
                g2d.setColor(new Color(255, 215, 0));
            }
            int w = getWidth();
            int h = getHeight();

            //三角形の頂点座標を計算
            Polygon triangle = new Polygon();
            if (isLeft) {
                //左向きの三角形
                triangle.addPoint(w, 0);
                triangle.addPoint(0, h / 2);
                triangle.addPoint(w, h);
            } else {
                //右向きの三角形
                triangle.addPoint(0, 0);
                triangle.addPoint(w, h / 2);
                triangle.addPoint(0, h);
            }
            g2d.fill(triangle);
            g2d.dispose();
        }

        //三角形の中だけをクリック可能にするための判定
        @Override
        public boolean contains(int x, int y) {
            Polygon triangle = new Polygon();
            if (isLeft) {
                triangle.addPoint(getWidth(), 0);
                triangle.addPoint(0, getHeight() / 2);
                triangle.addPoint(getWidth(), getHeight());
            } else {
                triangle.addPoint(0, 0);
                triangle.addPoint(getWidth(), getHeight() / 2);
                triangle.addPoint(0, getHeight());
            }
            return triangle.contains(x, y);
        }
    }

    //敵のスポーンメソッド
    private void spawnEnemy() {
        //戦いの前に敵パーティをクリア
        enemyParty.clear();
        for (int i = 0; i < 5; i++){
            enemyImageLabels[i].setIcon(null);
            if (enemyHpBars != null){
                enemyHpBars[i].setVisible(false);
            }
        }

        int numberOfEnemies = 1;//出現する敵の数

        //深い森の処理
        if (currentField.equals("FOREST")) {
           if (backgroundLabel != null) {
                //背景画像を変更する
                backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルの森.jpg"));
            }
            numberOfEnemies = (int)(Math.random() * 5) + 1;
            logTextArea.append("スライムが" + numberOfEnemies + "体あらわれた！\n");

            for (int i = 0; i < numberOfEnemies; i++){
                //スライムA、スライムB…と名付ける
                char suffix = (char)('A' + i);
                enemyParty.add(new Enemy("スライム" + suffix, 20, 5, 5,"fantasy_game_character_slime.png", 1));
            }

            //ダンジョンの処理 
        } else if (currentField.equals("DUNGEON")) {
            if (backgroundLabel != null) {
                //背景画像を変更する
                backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのダンジョン.jpg"));
            }
            numberOfEnemies = (int)(Math.random() * 5) + 1;
            logTextArea.append("ゴブリンが" + numberOfEnemies + "体あらわれた！\n");

            for (int i = 0; i < numberOfEnemies; i++){
                char suffix = (char)('A' + i);
                enemyParty.add(new Enemy("ゴブリン" + suffix, 25, 10, 5, "fantasy_goblin.png", 1));
            }

            //ボスエリアの処理
        } else if (currentField.equals("BOSS")) {
            if (backgroundLabel != null) {
                //背景画像を変更する
                backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのボス.jpg"));
            }
            
            if (bossStage == 1) {
                logTextArea.append("伝説の ドラゴン があらわれた！\n");
                enemyParty.add(new Enemy("ドラゴン" ,500, 30, 130, "fantasy_dragon.png", 1));
            } else if (bossStage == 2) {
                logTextArea.append("伝説の 魔王 があらわれた！\n");
                enemyParty.add(new Enemy("魔王" ,800, 50, 200, "fantasy_maou_devil.png", 1));
            }
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

            //足元のテキストを「名前」にする
            playerStatusLabels[i].setText(p.getName());

            if (!p.isAlive() || p.getHp() <= 0) {
                //死んでるキャラクターはグレーアウトして通常の位置に
                playerImageLabels[i].setEnabled(false);
                playerImageLabels[i].setBounds(20 + (i * 140), 100, 130, 400);
                //死んでいるキャラクターのステータスもグレーアウト
                playerStatusLabels[i].setEnabled(false);
                playerStatusLabels[i].setForeground(Color.GRAY);
                playerStatusLabels[i].setBounds(20 + (i * 140), 510, 130, 30);
            } else if (i == currentPlayerIndex){
                //現在ターンが回ってきたキャラは少し上がる
                playerImageLabels[i].setEnabled(true);
                playerImageLabels[i].setBounds(20 + (i * 140), 40, 130, 400);
                //テキストも少し上げ、黄色く光らせる
                playerStatusLabels[i].setEnabled(true);
                playerStatusLabels[i].setBounds(20 + (i * 140), 510, 130, 30);
                playerStatusLabels[i].setForeground(Color.YELLOW);
            } else {
                //待機中の生存キャラは通常位置
                playerImageLabels[i].setEnabled(true);
                playerImageLabels[i].setBounds(20 + (i * 140), 100, 130, 400);
                //待機中の生存キャラは白色
                playerStatusLabels[i].setEnabled(true);
                playerStatusLabels[i].setBounds(20 + (i * 140), 510, 130, 30);
                playerStatusLabels[i].setForeground(Color.WHITE);
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
            case 4 :
                //マップ画面
                cardLayout.show(mainPanel,"MAP");
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
        setSize(1520, 750);
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
            playerImageLabels[i].setBounds(15 + (i * 140), 100, 160, 400);//（i * 140）は140マス間をあけるという意味
            backgroundLabel.add(playerImageLabels[i]);

            //プレイヤーの足元にステータスのラベルを追加する
            playerStatusLabels[i] = new JLabel("",JLabel.CENTER);
            playerStatusLabels[i].setBounds(15 + (i * 140), 510, 160, 30);
            playerStatusLabels[i].setFont(new Font("MS ゴシック", Font.BOLD, 14));
            playerStatusLabels[i].setForeground(Color.WHITE);
            backgroundLabel.add(playerStatusLabels[i]);
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
        
        skillButton = new JButton("技(SKILL)");
        runButton = new JButton("逃げる(RUN)");
        defenseButton = new JButton("防御(DEFENSE)");
        itemButton = new JButton("アイテム(ITEM)");
        dictionaryButton = new JButton("技辞典(SKILL DICTIONARY)");

        bottomPanel.add(scrollPane, BorderLayout.CENTER); // ログテキストエリアをしたがわのしたにはいち 
        bottomPanel.add(statusPanel, BorderLayout.NORTH); // ステータスパネルをしたがわのうえにはいち
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // ぶひん（Parts）をメインウィンドウにはいち
        panel.add(backgroundLabel, BorderLayout.CENTER); // 背景を真ん中に配置
        panel.add(bottomPanel, BorderLayout.SOUTH);       // 操作エリアを下側に配置
        buttonPanel.add(skillButton); // スキルボタンを下の左側に配置
        buttonPanel.add(runButton); // 逃げるボタンを下の左側に配置
        buttonPanel.add(defenseButton); // 防御ボタンを下の左側に配置
        buttonPanel.add(itemButton); // アイテムボタンを下の左側に配置
        buttonPanel.add(dictionaryButton);//技辞典ボタンをアイテムボタンの右に配置する

        // ★逃げるボタンの処理
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.guardFlg = 0; // ガードフラグをリセット
                logTextArea.append( player.getName() + " は逃げ出そうとした！\n");

                // Math.random() は 0.0 以上 1.0 未満のランダムな数字を返す
                // 0.5 未満（50% の確率）なら逃亡成功とする
                if (Math.random() < 0.5) {
                    logTextArea.append("うまくにげきれた！\n");
                    String previousEnemyName = "";//敵の名前を入れる変数
                    
                    //ひとつ前に戦っていた敵に戻る
                    if (enemyCount > 1) {
                       enemyCount--;
                       //戻った後のエネミーカウントで名前を決める
                       if (enemyCount == 1) {
                        previousEnemyName = "スライム";
                       } else if (enemyCount == 2){
                        previousEnemyName = "ゴブリン";
                       }
                        logTextArea.append("もう一度" + previousEnemyName + "と戦闘だ！\n");
                    } else {
                        logTextArea.append("ここより前には戻れない！\n");
                    }

                    spawnEnemy();
                    enemyIcon();
                    updateDisplay();

                } else {
                    // 逃亡失敗の場合
                    logTextArea.append("しかし にげきれなかった！\n");

                    // モンスターのターン（ペナルティとして敵の反撃を受ける）
                    Enemy attacker = null;
                    for (Enemy eInside : enemyParty) {
                        if (eInside.isAlive()) {
                            attacker = eInside;
                            break;
                        }
                    }
                    
                    //生きている敵がいたら攻撃
                    if (attacker != null) {
                        logTextArea.append(attacker.getName() + "が背後から襲い掛かる！\n");
                        int originalAtk = attacker.getAtk();
                        int escapeFailAtk = (int)(originalAtk * 1.5);

                        attacker.setAtk(escapeFailAtk);

                        String monsterResult = attacker.attack(player);
                        logTextArea.append(monsterResult);

                        int damage = (int)(attacker.getAtk());
                        int index = currentPlayerIndex;

                        showPopupText("-" + damage, Color.RED, playerImageLabels[index]);

                        attacker.setAtk(originalAtk);
                    }
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
                    switchNextPlayer();
                    logTextArea.append("次のプレイヤーは" + player.getName() + "だ！\n");
                    logTextArea.append("--------------------------------------------\n");
                }
            }
        });

        // ★ スキルボタンをおしたときのしょりをついか
        skillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.guardFlg = 0; // ガードフラグをリセット
               
                //プレイヤーが覚えている技を選択肢に出す
                java.util.List<Skill> playerSkills = player.getSkills();
                String[] moves = new String[playerSkills.size()];
                for (int i = 0; i < playerSkills.size(); i++) {
                    moves[i] = playerSkills.get(i).getName();
                }

                int moveChoice = JOptionPane.showOptionDialog(
                    BattleGame.this,
                    player.getName() + " はどうする？",
                    "技の選択",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    moves,
                    moves[0]
                );

                //×ボタンを押したらウィンドウごと閉じる
                if (moveChoice == JOptionPane.CLOSED_OPTION) return;

                //選んだ技を取得する
                Skill selectedSkill = playerSkills.get(moveChoice);
                logTextArea.append(player.getName() + " は " + selectedSkill.getName() + " を使った！\n");

                //技の種類によって処理を分ける
                if (selectedSkill.getType().equals("単体攻撃")) {
                    //生きている敵だけをリストに集める
                    Enemy aliveEnemy = null;
                    for (Enemy enemyInside : enemyParty) {
                        if (enemyInside.isAlive()) {
                            aliveEnemy = enemyInside;
                            break;
                        }
                    }
                    if (aliveEnemy != null) {
                        int damage = (int) (player.getAtk() * selectedSkill.getMultiplier());
                        aliveEnemy.hp -= damage;
                        if (aliveEnemy.hp < 0) {
                            aliveEnemy.hp = 0; // HPがマイナスにならないようにする
                        }
                        showPopupText("-" + damage, Color.RED, enemyImageLabels[enemyParty.indexOf(aliveEnemy)]);
                        logTextArea.append(player.getName() + " の攻撃！ " + aliveEnemy.getName() + " に " + damage + " のダメージ！\n");
                        if (!aliveEnemy.isAlive()) {
                            logTextArea.append(aliveEnemy.getName() + " をたおした！\n");
                            int index = enemyParty.indexOf(aliveEnemy);
                            enemyImageLabels[index].setEnabled(false);

                            //現在行動中のプレイヤーに経験値をあげる
                            player.setExp(player.getExp() + aliveEnemy.getRewardExp());

                            //レベルアップ判定
                            if (player.checkLevelUp()) {
                                JOptionPane.showMessageDialog(
                                    BattleGame.this,
                                    player.getName() + "はLv" + player.getLevel() + "に上がった！\n"
                                );
                            }
                            updatePlayerVisuals();
                            updateDisplay();
                        }
                    }

                } else if (selectedSkill.getType().equals("全体攻撃")) {
                    //全体攻撃の場合、すべての生きている敵に攻撃する
                    for (Enemy enemyInside : enemyParty) {
                        if (enemyInside.isAlive()) {
                            int damage = (int) (player.getMgc() * selectedSkill.getMultiplier());
                            enemyInside.hp -= damage;
                            if (enemyInside.hp < 0) {
                                enemyInside.hp = 0;
                            }
                            showPopupText("-" + damage, Color.RED, enemyImageLabels[enemyParty.indexOf(enemyInside)]);
                            logTextArea.append(player.getName() + " の攻撃！ " + enemyInside.getName() + " に " + damage + " のダメージ！\n");
                            if (!enemyInside.isAlive()) {
                                logTextArea.append(enemyInside.getName() + " をたおした！\n");
                                int index = enemyParty.indexOf(enemyInside);
                                enemyImageLabels[index].setEnabled(false);

                                //全体攻撃をしたプレイヤーに経験値をあげる
                                player.setExp(player.getExp() + enemyInside.getRewardExp());
                                if (player.checkLevelUp()) {
                                    JOptionPane.showMessageDialog(
                                        BattleGame.this,
                                        player.getName() + "はLv" + player.getLevel() + "に上がった"
                                    );
                                }
                                updateDisplay();
                            }
                        }
                    }
                } else if (selectedSkill.getType().equals("単体回復")) {
                    //味方全員の選択肢を出す
                    String[] partyNames = new String[party.size()];
                    for (int i = 0; i < party.size(); i++) {
                        partyNames[i] = party.get(i).getName() + " (HP: " + party.get(i).getHp() + "/" + party.get(i).getMaxHp() + ")";
                    }

                    int targetChoice = JOptionPane.showOptionDialog(
                        BattleGame.this,
                        "誰を回復する？",
                        "回復対象の選択",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        partyNames,
                        partyNames[currentPlayerIndex]
                    );

                    if (targetChoice != JOptionPane.CLOSED_OPTION) {
                        Player targetPlayer = party.get(targetChoice);//選んだ味方を取得する

                        if (targetPlayer.isAlive()) {
                            int healAmount = (int) (player.getMaxHp() * selectedSkill.getMultiplier());
                            party.get(targetChoice).hp += healAmount;
                            logTextArea.append(player.getName() + " は " + selectedSkill.getName() + " を使った！\n");
                            if (party.get(targetChoice).hp > party.get(targetChoice).getMaxHp()) {
                                party.get(targetChoice).hp = party.get(targetChoice).getMaxHp(); // 最大HPを超えないようにする
                            }
                            showPopupText("+" + healAmount, Color.GREEN, playerImageLabels[targetChoice]);
                            logTextArea.append(player.getName() + " は " + targetPlayer.getName() + " を " + healAmount + " 回復した！\n");
                        } else {
                            logTextArea.append(targetPlayer.getName() + " はたおれているため回復できない！\n");
                        }
                        
                    }
                } else if (selectedSkill.getType().equals("全体回復")) {
                    //全体回復の場合、すべての生きている味方を回復する
                    for (Player member : party) {
                        if (member.isAlive()) {
                            int healAmount = (int) (player.getMaxHp() * selectedSkill.getMultiplier());
                            member.hp += healAmount;
                            if (member.hp > member.getMaxHp()) {
                                member.hp = member.getMaxHp(); // 最大HPを超えないようにする
                            }
                            showPopupText("+" + healAmount, Color.GREEN, playerImageLabels[party.indexOf(member)]);
                            logTextArea.append( member.getName() + " のHPを " + healAmount + " 回復した！\n");
                        }
                    }
                } else if (selectedSkill.getType().equals("単体バフ")) {
                    //スキル名に魔力が入っているか確認する
                    boolean isMagicBuff = selectedSkill.getName().contains("魔力");

                    //味方全員の選択肢を出す
                    String[] partyNames = new String[party.size()];
                    for (int i = 0; i < party.size(); i++) {
                        if (isMagicBuff) {
                            partyNames[i] = party.get(i).getName() + " (MGC: " + party.get(i).getMgc() + ")";
                        } else {
                            partyNames[i] = party.get(i).getName() + " (ATK: " + party.get(i).getAtk() + ")";
                        }
                    }

                    int targetChoice = JOptionPane.showOptionDialog(
                        BattleGame.this,
                        "誰にバフをかける？",
                        "バフ対象の選択",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        partyNames,
                        partyNames[currentPlayerIndex]
                    );

                    if (targetChoice != JOptionPane.CLOSED_OPTION) {
                        Player targetPlayer = party.get(targetChoice);

                        if (targetPlayer.isAlive()) {
                            if (isMagicBuff) {
                                int beforeMgc = targetPlayer.getMgc();//バフ前の魔力をメモする
                                targetPlayer.mgc = (int) (targetPlayer.getMgc() * selectedSkill.getMultiplier());
                                int buffAmount = targetPlayer.getMgc() - beforeMgc;//バフ後の魔力-バフ前の魔力
                                logTextArea.append(player.getName() + " は " + selectedSkill.getName() + " を使った！\n");
                                logTextArea.append(targetPlayer.getName() + " の魔力が " + targetPlayer.getMgc() + " に上がった！\n");
                                showPopupText("mgc＋" + buffAmount, Color.BLUE, playerImageLabels[targetChoice]);
                            } else {
                                int beforeAtk = targetPlayer.getAtk();//バフ前の攻撃力をメモする
                                targetPlayer.atk = (int) (targetPlayer.getAtk() * selectedSkill.getMultiplier());
                                int buffAmount = targetPlayer.getAtk() - beforeAtk;//バフ後の攻撃力-バフ前の攻撃力
                                logTextArea.append(player.getName() + " は " + selectedSkill.getName() + " を使った！\n");
                                logTextArea.append(targetPlayer.getName() + " の攻撃力が " + targetPlayer.getAtk() + " に上がった！\n");
                                showPopupText("atk＋" + buffAmount, Color.BLUE, playerImageLabels[targetChoice]);
                            }
                        } else {
                            logTextArea.append(targetPlayer.getName() + " はたおれているためバフをかけられない！\n");
                        }
                    }
                } else if (selectedSkill.getType().equals("かばう")) {
                    //クラスのメンバー変数として用意したcoveringPlayerに現在のプレイヤーをセットする
                    coveringPlayer = player;
                    //かばうを使ったログを表示
                    logTextArea.append(player.getName() + " は仲間をかばった！\n");
                    showPopupText("かばう", Color.BLUE, playerImageLabels[currentPlayerIndex]);
                }
                updateDisplay();

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
                    startEnemyTurn();
                }

            }
        });

        //ガードボタンの追加
        defenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.guardFlg = 0; // ガードフラグをリセット
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
                    startEnemyTurn();
                }
                    
            }
        });

        //アイテムボタンの追加
        itemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.guardFlg = 0; // ガードフラグをリセット
                logTextArea.append(player.getName() + " はアイテムをつかった！ HPが20かいふくした！\n");
                showPopupText("+20" , Color.GREEN, playerImageLabels[currentPlayerIndex]);
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
                    startEnemyTurn();
                }

            }
        });

        //技辞典ボタンの追加
        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //技辞典内の解説
                StringBuilder text = new StringBuilder();
                text.append("技辞典(SKILL DICTIONARY)\n");

                text.append("ーーーーーーーーーーーーーーーーーーーーー\n\n");

                text.append("【斬る(SLASH)】\n");
                text.append("・敵一体を攻撃する\n");
                text.append("・攻撃力×1.0ダメージ\n");
                text.append("・Attcks a single enemy\n");
                text.append("・Damage: ATK × 1.0\n\n");

                text.append("【かばう(COVER)】\n");
                text.append("・すべての敵の攻撃を受ける\n");
                text.append("・被ダメージ×0.25\n");
                text.append("・Takes all incoming enemy attacks.\n");
                text.append("・Damage Taken: × 0.25\n\n");

                text.append("【召喚(SUMMON)】\n");
                text.append("・精霊を召喚する\n");
                text.append("・HPを割合で消費\n");
                text.append("・Summons a spirit.\n");
                text.append("・Cost: Consumes a percentage of max HP\n\n");

                text.append("【攻撃力UP(ATK BUFF)】\n");
                text.append("・味方一人の攻撃力を上げる\n");
                text.append("・魔力×1.5\n");
                text.append("・Increases the ATK of one ally.\n");
                text.append("・Effect: MGC × 1.5\n\n");

                text.append("【魔力UP(MGC BUFF)】\n");
                text.append("・味方一人の魔力を上げる\n");
                text.append("・魔力×1.5\n");
                text.append("・Increases the MGC of one ally.\n");
                text.append("・Effect: MGC × 1.5\n\n");

                text.append("【単体回復(HEAL)】\n");
                text.append("・味方一人のHPを回復する\n");
                text.append("・魔力×1.0\n");
                text.append("・Restores HP to one ally.\n");
                text.append("・Heal Amount: MGC × 1.0\n\n");

                text.append("【全体回復(MASS HEAL)】\n");
                text.append("・すべての味方のHPを回復する\n");
                text.append("・魔力×0.5\n");
                text.append("・Restores HP to all allies.\n");
                text.append("・Heal Amount: MGC × 0.5\n\n");

                text.append("ーーーーーーーーーーーーーーーーーーーーー\n");

                JTextArea dictionaryArea = new JTextArea(text.toString());
                dictionaryArea.setEditable(false);
                dictionaryArea.setFont(new Font("MS ゴシック", Font.PLAIN, 14));
                dictionaryArea.setCaretPosition(0); // スクロール位置を先頭に設定

                //スクロールを作成
                JScrollPane dictionaryScroll = new JScrollPane(dictionaryArea);
                dictionaryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                dictionaryScroll.setPreferredSize(new Dimension(450, 300));

                JOptionPane.showMessageDialog(BattleGame.this, dictionaryScroll, "技辞典", JOptionPane.INFORMATION_MESSAGE);
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

        //スタートボタンを押したらマップ画面に移動する
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choicePlayer();
                //4人選択したらバトルを開始する
                if (!party.isEmpty()) {
                    spawnEnemy();
                    enemyIcon();
                    updateDisplay();
                    updatePlayerVisuals();
                    cardLayout.show(mainPanel,"MAP");//マップ画面に切り替え
                }
            }
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

        JButton retryButton = new JButton("不屈の闘志で立ち上がる(RETRY)");
        JButton restartButton = new JButton("新しい旅を始める(RESTART)");
        JButton quitButton = new JButton("諦める(GIVE UP)");

        //不屈の闘志で立ち上がるボタンを押したら、負けたバトル画面に戻る
        retryButton.addActionListener(e -> {
            retryCount--;//リトライ回数を減らす
            if (retryCount <= 0) {
                retryButton.setEnabled(false);
                retryButton.setText("不屈の闘志も尽きた(CAN'T RETRY)");
            }
            //味方全員のHPを全回復する(alive状態に戻す)
            for (Player member : party) {
                member.hp = member.getMaxHp();
            }

            //味方全員のグレーアウトを解除する
            for (int i = 0; i < 4; i++) {
                playerImageLabels[i].setEnabled(true);
            }

            //メンバーのインデックスを最初のプレイヤーに戻す
            currentPlayerIndex = 0;
            player = party.get(0);

            //敵のスポーンをやり直す
            spawnEnemy();
            enemyIcon();
            //バトル画面に再度移動
            cardLayout.show(mainPanel, "BATTLE");
        });

        //新しい旅を始めるボタンを押したら、タイトル画面に戻る
        restartButton.addActionListener(e -> {
            //味方全員のHPを全回復する(alive状態に戻す)
            for (Player member : party) {
                member.hp = member.getMaxHp();
            }
            //メンバーのインデックスを最初のプレイヤーに戻す
            currentPlayerIndex = 0;
            player = party.get(0);

            //敵のスポーンをやり直す
            enemyCount = 1; //敵のカウントをリセット
            spawnEnemy();
            enemyIcon();
            //タイトル画面に戻る
            cardLayout.show(mainPanel, "TITLE");
        });

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

        //まだ冒険を続けるボタンを押したら、タイトル画面に戻る
        continueButton.addActionListener(e -> {     
            //味方全員のHPを全回復する(alive状態に戻す)
            for (Player member : party) {
                member.hp = member.getMaxHp();
            }
            //メンバーのインデックスを最初のプレイヤーに戻す
            currentPlayerIndex = 0;
            player = party.get(0);

            //敵のスポーンをやり直す
            enemyCount = 1; //敵のカウントをリセット
            spawnEnemy();
            enemyIcon();
            //タイトル画面に戻る
            cardLayout.show(mainPanel, "TITLE");
        });

        //もう一回世界を救うボタンを押したら、タイトル画面に戻る
        replayButton.addActionListener(e -> {
            //味方全員のHPを全回復する(alive状態に戻す)
            for (Player member : party) {
                member.hp = member.getMaxHp();
            }
            //メンバーのインデックスを最初のプレイヤーに戻す
            currentPlayerIndex = 0;
            player = party.get(0);

            //敵のスポーンをやり直す
            enemyCount = 1; //敵のカウントをリセット
            retryCount = 1; //リトライ回数をリセット
            spawnEnemy();
            enemyIcon();
            //バトル画面に再度移動
            cardLayout.show(mainPanel, "TITLE");
        });

        //終わるボタンを押したら、ウィンドウごと閉じる
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

    //ポップアップのメソッド(引数四つ)
    private void showPopupText (String text ,Color color ,JLabel targetLabel ,int offsetY) {
        if (targetLabel == null) return;

        String popupName;
        if (color.equals(Color.RED)) {
            popupName = "damage_popup";
        } else {
            popupName = "other_popup";
        }

        JLabel extingPopup = null;
        for (java.awt.Component comp : targetLabel.getComponents()) {
            if (comp instanceof JLabel && popupName.equals(comp.getName())) {
                extingPopup = (JLabel) comp;
                break;
            }
        }

        if (extingPopup != null && color.equals(Color.RED)) {
            String oldText = extingPopup.getText().replace("-","");
            String newText = text.replace("-","");

            int oldDamage = convertStringToInteger(oldText);
            int newDamage = convertStringToInteger(newText);

            extingPopup.setText("-" + (oldDamage + newDamage));
            
            targetLabel.revalidate();
            targetLabel.repaint();
            return;
        }

        //キャラクターのラベルの上に重ねる用のラベルを作る
        JLabel popupLabel = new JLabel(text, JLabel.CENTER);
        popupLabel.setName(popupName);
        popupLabel.setFont(new Font("MS　ゴシック", Font.BOLD, 28));//文字の大きさと太さ
        popupLabel.setForeground(color);

        //ポップアップの種類で表示する位置を変える
        int yPosition;
        if (color.equals(Color.RED)) {
            yPosition = 50;
        } else {
            yPosition = 15;
        }

        //表示する位置
        popupLabel.setBounds(0,yPosition, targetLabel.getWidth(), 40);
        targetLabel.add(popupLabel);

        //画面の再描写
        targetLabel.revalidate();
        targetLabel.repaint();

        //1.5秒(1500ミリ秒)後に自動で消去するタイマー
        javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
            targetLabel.remove(popupLabel);
            targetLabel.revalidate();
            targetLabel.repaint();
            
        });
        timer.setRepeats(false);
        timer.start();
    }

    //二個目のポップアップのメソッド(引数三つ)
    private void showPopupText (String text ,Color color ,JLabel targetLabel) {
        showPopupText(text, color, targetLabel, 0);
    }

    //敵のターンのメソッド
    private void startEnemyTurn() {
        logTextArea.append("敵のターンだ！\n");

        //各プレイヤーが「このターンに何回ポップアップを出したか」を数えるカウンター
        int[] playerPopupCounts = new int[party.size()];

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

                //プレイヤーターゲット変数を先に用意
                Player targetPlayer = null;
                boolean isCovered = false;//かばうが発動したかどうかの目印

                //もしかばっている人がいて、その人が生きている場合
                if (coveringPlayer != null && aliveMembers.contains(coveringPlayer)) {
                    targetPlayer = coveringPlayer;//強制的にターゲットをかばっている人にする
                    isCovered = true;//かばうフラグON
                    logTextArea.append(targetPlayer.getName() + "がかばった！\n");
                } else {
                    //ランダムに生きているプレイヤーを選ぶ
                    int randomIndex = (int)(Math.random() * aliveMembers.size());
                    targetPlayer = aliveMembers.get(randomIndex);
                }

                int pIndex = party.indexOf(targetPlayer);

                //敵の攻撃のコード
                if (isCovered) {
                    //かばう専用の攻撃コード
                    int damage = (int)(enemyInside.getAtk() * 0.25);
                    if (damage < 1) damage = 1;

                    targetPlayer.hp -= damage;//直接かばったプレイヤーのHPを減らす
                    if (targetPlayer.hp < 0)targetPlayer.hp = 0;//0より下にしない

                    logTextArea.append(targetPlayer.getName() + "がかばって" + damage + "ダメージを受けた！\n");
                   
                    int offsetY = playerPopupCounts[pIndex] * 25;
                    showPopupText("-" + damage, Color.RED, playerImageLabels[pIndex], offsetY);

                    playerPopupCounts[pIndex]++;
                } else {
                    //通常の攻撃コード
                    String aliveEnemyResult = enemyInside.attack(targetPlayer);
                    logTextArea.append(aliveEnemyResult);

                    int eDamage = (int)(enemyInside.getAtk());

                    //ガード状態ならポップアップも半減
                    if (targetPlayer.guardFlg == 1) {
                        eDamage = eDamage /2;
                    }

                    int offsetY = playerPopupCounts[pIndex] * 25;
                    showPopupText("-" + eDamage, Color.RED, playerImageLabels[pIndex], offsetY);

                    playerPopupCounts[pIndex]++;
                }

                //一体ごとに画面を更新
                updateDisplay();
                updatePlayerVisuals();
            }
        }
        //敵のターン終了時にかばうを終了する
        coveringPlayer = null;

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

            logTextArea.append("--------------------------------------------\n");
        }
    }   

    //文字を数字に変換するメソッド
    private int convertStringToInteger(String str) {
        int result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= '0' && c <= '9') {
                result = result * 10 + (c - '0');
            }
        }   
        return result;
    }

    //マップ画面のクラス
    class MapPanel extends JPanel {
        public MapPanel(BattleGame game) {
            setBackground(new Color(34, 139, 34)); // 背景色を緑に設定
            //マップでクリックできるようにする
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //クリックしたら移動する
                    int x = e.getX();
                    int y = e.getY();
                    int w = getWidth();
                    int h = getHeight();

                    if (x >= w / 2 - 50 && x <= w / 2 + 50 && y >= h * 9/10 - 50 && y <= h * 9/10) {
                        //街エリアをクリックした場合
                        game.openShopScreen();
                    } else if (x >= w / 10 && x <= w / 10 + 50 && y >= h / 2 - 60 && y <= h / 2 + 60) {
                        //深い森エリアをクリックした場合
                        game.startFieldBattle("FOREST");
                    } else if (x >= w * 9/10 - 50 && x <= w * 9/10 && y >= h / 2 - 60 && y <= h / 2 + 60) {
                        //ダンジョンエリアをクリックした場合
                        game.startFieldBattle("DUNGEON");
                    } else if (x >= w / 2 - 50 && x <= w / 2 + 50 && y >= h / 10 && y <= h / 10 + 50) {
                        //ボスエリアをクリックした場合
                        game.startFieldBattle("BOSS");
                    }
                }   
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // マップの描画処理をここに追加する
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            //道の描画
            g2.setColor(new Color(245, 222, 179)); // 茶色
            g2.fillRect(w / 10, h / 2 - 20, w * 8/10, 40); //横道
            g2.fillRect(w / 2 - 20, h / 10, 40, h * 8/10); //縦道

            //文字・エリア枠の描画
            Font font = new Font("MS ゴシック", Font.BOLD, 22);
            g2.setFont(font);

            //ボスエリア
            g2.setColor(Color.BLACK);
            g2.fillRect(w / 2 - 50, h / 10, 100, 50);
            g2.setColor(Color.RED);
            g2.drawString("ボス", w / 2 - 22, h / 10 + 35);

            //街エリア
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(w / 2 - 50, h * 9/10 - 50, 100, 50);
            g2.setColor(Color.BLUE);
            g2.drawString("街", w / 2 - 11, h * 9/10 - 15);

            //深い森エリア
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(w / 10, h / 2 - 60, 50, 120);
            g2.setColor(Color.WHITE);
            g2.drawString("深", w / 10 + 14, h / 2 - 20);
            g2.drawString("い", w / 10 + 14, h / 2 + 10);
            g2.drawString("森", w / 10 + 14, h / 2 + 40);

            //ダンジョンエリア
            g2.setColor(new Color(139, 69, 19)); // 茶色
            g2.fillRect(w * 9/10 - 50, h / 2 - 60, 50, 120);
            g2.setColor(new Color(144, 238, 144)); // 薄い緑
            g2.drawString("ダ", w * 9/10 - 38, h / 2 - 30);
            g2.drawString("ン", w * 9/10 - 38, h / 2);
            g2.drawString("ジ", w * 9/10 - 38, h / 2 + 30);
            g2.drawString("ョ", w * 9/10 - 38, h / 2 + 60);
            g2.drawString("ン", w * 9/10 - 38, h / 2 + 90);

            //現在地(プレイヤーの位置)の描画
            g2.setColor(Color.RED);
            g2.fillRect(w / 2 - 15, h * 9/10 - 90, 30, 30);
        }
    }

    //ショップ画面のメソッド
    private void openShopScreen() {
        //ショップ画面の作成
        JPanel shopPanel = new JPanel(new BorderLayout());
        shopPanel.setBackground(Color.LIGHT_GRAY);

        JLabel shopLabel = new JLabel("ショップ", JLabel.CENTER);
        shopLabel.setFont(new Font("MS ゴシック", Font.BOLD, 32));
        shopLabel.setForeground(Color.BLACK);
        shopPanel.add(shopLabel, BorderLayout.NORTH);

        JTextArea shopTextArea = new JTextArea();
        shopTextArea.setEditable(false);
        shopTextArea.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        shopTextArea.setText("ここではアイテムを購入できます。\n\n" +
                "1. 回復薬 (HPを20回復) - 100ゴールド\n" +
                "2. 強化薬 (攻撃力を10%アップ) - 200ゴールド\n" +
                "3. 魔力薬 (魔力を10%アップ) - 200ゴールド\n");
        shopPanel.add(shopTextArea, BorderLayout.CENTER);

        JButton backButton = new JButton("戻る");
        backButton.setFont(new Font("Arial", Font.PLAIN, 24));
        backButton.addActionListener(e -> {
            //マップ画面に戻る
            cardLayout.show(mainPanel, "MAP");
            updateDisplay();
        });
        shopPanel.add(backButton, BorderLayout.SOUTH);

        mainPanel.add(shopPanel, "SHOP");
        cardLayout.show(mainPanel, "SHOP");
    }

    //クリックしたマップごとの処理
    public void startFieldBattle(String fieldType) {
        this.currentField = fieldType;

        if (fieldType.equals("BOSS")) {
            //ボス戦の処理
            backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのボス.jpg"));
        } else if (fieldType.equals("FOREST")) {
            //通常戦の処理
            backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルの森.jpg"));
        } else if (fieldType.equals("DUNGEON")) {
            //ダンジョン戦の処理
            backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのダンジョン.jpg"));
        }

        spawnEnemy();
        enemyIcon();
        updateDisplay();
        cardLayout.show(mainPanel, "BATTLE");
    } 
}