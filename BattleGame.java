import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
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
    private int currentround = 1; // 現在のラウンドを保持するフィールド
    private int maxRounds = 5; // 最大ラウンド数を保持するフィールド
    public static int gold = 0;//所持コインのフィールド
    private static Item[] itembox = new Item[50]; // アイテムリストを保持するフィールド
    private JLabel coinLabel; // 所持コインを表示するラベル
    private JPanel saveSelectPanel; // セーブ選択画面のパネルを保持するフィールド
    private JPanel saveSelectContainer; // セーブ選択画面のコンテナパネルを保持するフィールド
    private static int currentSaveSlot = 1;//セーブスロット番号を保持する変数
    private int battleExpPool = 0;//戦闘中に倒した敵の経験値をためておく変数
    private Player lastAttacker = null;//とどめを刺したキャラクターを記録する変数

    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel;   // 背景画像のラベル
    private JLabel[] playerImageLabels = new JLabel[4];  // プレイヤー画像用のラベル
    private JLabel[] enemyImageLabels = new JLabel[5];//敵の画像用ラベル
    private JLabel[] playerStatusLabels = new JLabel[4];//プレイヤーの画像の下にステータスを貼るためのラベル
    private JLabel[] enemyStatusLabels = new JLabel[5];//敵の画像の下にステータスを貼るためのラベル

    // ★ キャラクターのインスタンスをよういする
    private Player player;
    public static java.util.List<Player> party = new java.util.ArrayList<>();//チームにするためのコード（ArrayListは可変式の配列）
    private Enemy enemy;
    private java.util.List<Enemy> enemyParty = new java.util.ArrayList<>();//敵をチーム（複数体）にするためのコード
    private int enemyCount = 1; // 対峙する敵の数を数えるためのフィールド
    private int guardFlg = 0; // ガードフラグ（0: ガードしていない、1: ガードしている）
    private int currentPlayerIndex = 0; // 今何人目のプレイヤーのターンかを数えるためのフィールド（０から３）
    private CardLayout cardLayout;//画面の切り替えに必要な部品
    private JPanel mainPanel;//画面の切り替えに必要な部品
    private int retryCount = 1; // リトライ回数を数えるためのフィールド
    private Player coveringPlayer; // かばうを使ったプレイヤーを保持するフィールド

    //コンストラクタ(一番最初に実行されるメソッド)
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
        saveSelectPanel = createSaveSelectPanel(); //セーブ選択画面

        //メインパネルに名前を付けて重ねる
        mainPanel.add(titlePanel,"TITLE");
        mainPanel.add(battlePanel,"BATTLE");
        mainPanel.add(gameOverPanel,"GAMEOVER");
        mainPanel.add(gameClearPanel,"GAMECLEAR");
        mainPanel.add(new MapPanel(this),"MAP"); // マップ画面を追加
        mainPanel.add(saveSelectPanel,"SAVE_SELECT"); // セーブ選択画面を追加

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

        //DBから選択候補を取得
        java.util.List<Player> availableList = GameMasterManager.loadAllPlayers();

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

        //選択するボタン
        JButton selectButton = new JButton("選択する(SELECT)");
        selectButton.setEnabled(true);
        selectButton.setBackground(new Color(46,204,113));//緑色
        selectButton.setForeground(Color.WHITE);
        selectButton.setFont(new Font("MS ゴシック",Font.BOLD,16));
        selectButton.setFocusPainted(false);

        //出発ボタン
        JButton startButton = new JButton("出発!!(START)");
        startButton.setEnabled(false); // 最初は無効化しておく
        startButton.setBackground(new Color(255, 215, 0)); // ゴールド色に設定
        startButton.setForeground(Color.WHITE); // 文字色を白に設定
        startButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        selectButton.setFocusPainted(false);

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
                //画面の更新
                ImageIcon rawIcon = currentPlayer.getIcon();
                if (rawIcon != null) {
                    Image img = rawIcon.getImage();
                    Image scaledImg = img.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImg));        
                } else {
                    imageLabel.setIcon(null);
                }

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

                    //選んだら削除
                    availableList.remove(currentIndex[0]);

                    //末尾のキャラクターを削除した場合
                    if (!availableList.isEmpty() && currentIndex[0] >= availableList.size()) {
                        currentIndex[0] = availableList.size() - 1;
                    }

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
                //ボタンを無効化して、二十押し防止
                startButton.setEnabled(false);

                //１．DBにパーティをINSERTする
                boolean success = GameSaveManager.savePartyData(currentSaveSlot,party);
                
                //２．保存に成功したら進む
                if (success) {
                    dialog.dispose(); // ダイアログを閉じる

                    player = party.get(0); // 最初のキャラクターをプレイヤーとしてセットする
                    for (int i = 0; i < 4; i++){
                        playerImageLabels[i].setIcon(party.get(i).getIcon());
                    }

                    //画面を更新してマップ画面へ
                    updateDisplay();
                    gameScreen(4);
                } else {
                    //保存できなかった時
                    startButton.setEnabled(true);
                    JOptionPane.showMessageDialog(dialog,"保存に失敗しました","エラー",JOptionPane.ERROR_MESSAGE);
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

    //敵のスポーンメソッド(敵のステータスもここで決める)
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
                backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルの森.jpg"));
            }
            numberOfEnemies = (int)(Math.random() * 5) + 1;
            logTextArea.append("スライムが" + numberOfEnemies + "体あらわれた！\n");

            for (int i = 0; i < numberOfEnemies; i++){
                String suffix = String.valueOf((char)('A' + i));
                Enemy e = GameMasterManager.createEnemyFromDB(1, suffix); // ENNO=1 (SLIME)
                if (e != null) enemyParty.add(e);
            }

            //ダンジョンの処理 
        } else if (currentField.equals("DUNGEON")) {
            if (backgroundLabel != null) {
                backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのダンジョン.jpg"));
            }
            numberOfEnemies = (int)(Math.random() * 5) + 1;
            logTextArea.append("ゴブリンが" + numberOfEnemies + "体あらわれた！\n");

            for (int i = 0; i < numberOfEnemies; i++){
                String suffix = String.valueOf((char)('A' + i));
                Enemy e = GameMasterManager.createEnemyFromDB(2, suffix); // ENNO=2 (GOBLIN)
                if (e != null) enemyParty.add(e);
            }

            //ボスエリアの処理
        } else if (currentField.equals("BOSS")) {
            if (backgroundLabel != null) {
                backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのボス.jpg"));
            }
            
            if (bossStage == 1) {
                logTextArea.append("伝説の ドラゴン があらわれた！\n");
                Enemy e = GameMasterManager.createEnemyFromDB(6, ""); // ENNO=6 (DRAGON)
                if (e != null) enemyParty.add(e);
            } else if (bossStage == 2) {
                logTextArea.append("伝説の 魔王 があらわれた！\n");
                Enemy e = GameMasterManager.createEnemyFromDB(7, ""); // ENNO=7 (DEMON)
                if (e != null) enemyParty.add(e);
            }
        }

        //これがないと画像が出てこない
        for (int i = 0; i < enemyParty.size(); i++) {
            enemyImageLabels[i].setIcon(enemyParty.get(i).getIcon());
            enemyImageLabels[i].setEnabled(true);

            if (enemyHpBars != null) {
                enemyHpBars[i].setVisible(true);
            }
        }
        enemyIcon();

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
            case 5 :
                //セーブセレクト画面
                cardLayout.show(mainPanel,"SAVE_SELECT");
                break;
            default:
                break;
        }
    }

    //バトル画面の設定(技はここ、敵を倒したときの処理もここ)
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
        
        // ぶひん（Parts）をメインウィンドウに追加
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
               
                // ★ 3列グリッド（召喚士は「召喚」ボタンのみ）のダイアログで技を選ぶ
                Skill selectedSkill = showSkillSelectDialog(player);

                // ×ボタンを押したらウィンドウごと閉じる
                if (selectedSkill == null) return;

                // 召喚以外ならここで技名ログを出す（召喚の場合は精霊を選んだあとに出す）
                if (!selectedSkill.getType().equals("召喚")) {
                    logTextArea.append(player.getName() + " は " + selectedSkill.getName() + " を使った！\n");
                }

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

                            //コインをあげる処理
                            int getCoin = aliveEnemy.getDropCoin();
                            gold += getCoin;
                            logTextArea.append(getCoin + "コインを手に入れた！\n");

                            //現在行動中のプレイヤーに経験値をあげる
                            player.setExp(player.getExp() + aliveEnemy.getRewardExp());

                            //レベルアップ判定
                            // 【全体攻撃】敵を倒したときの処理（コイン獲得の直後）
                            battleExpPool += aliveEnemy.getRewardExp();
                            lastAttacker = player;
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

                                //コインをあげる処理
                                int getCoin = enemyInside.getDropCoin();
                                gold += getCoin;
                                logTextArea.append(getCoin + "コインを手に入れた！\n");

                                //経験値をプールにためて、最後に分ける
                                battleExpPool += enemyInside.getRewardExp();
                                lastAttacker = player;
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
                } else if (selectedSkill.getType().equals("召喚")){
                    // 1. 召喚士が覚えている召喚タイプの技を取得する
                    List<Skill> summonSkills = new ArrayList<>();
                    for (Skill s : player.getSkills()) {
                        if (s.getType().equals("召喚")) {
                            summonSkills.add(s);
                        }
                    }

                    // 2. 「誰を召喚する？」ダイアログ（出せる／出せない表示付き）を開く
                    Skill chosenSummon = showSpiritSelectDialog(player, summonSkills);

                    // 3. ×ボタンで閉じた場合はターンを進めずに中断
                    if (chosenSummon == null) return;

                    // 4. HPを消費する（ダイアログ側でLv・HPチェック済み）
                    player.setSummoned(true);
                    player.hp -= chosenSummon.getHpCost();
                    showPopupText("-" + chosenSummon.getHpCost(), Color.MAGENTA, playerImageLabels[currentPlayerIndex]);
                    logTextArea.append(player.getName() + " は " + chosenSummon.getName() + " を召喚した！\n");

                    // 5. 精霊の画像処理とステータス変身
                    if (chosenSummon.getImagePath() != null) {
                        java.io.File imgFile = new java.io.File(chosenSummon.getImagePath());
                        if (imgFile.exists()) {
                            ImageIcon spiritIcon = new ImageIcon(chosenSummon.getImagePath());
                            Image img = spiritIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                            playerImageLabels[currentPlayerIndex].setIcon(new ImageIcon(img));

                            player.setName(chosenSummon.getName());
                            player.setAtk(chosenSummon.getSpiritAtk());
                            player.setMaxHp(chosenSummon.getSpiritHp());
                            player.setHp(chosenSummon.getSpiritHp());
                            player.setLevel(chosenSummon.getRequiredLevel());

                            if (chosenSummon.getSpiritSkills() != null && !chosenSummon.getSpiritSkills().isEmpty()) {
                                player.setSkills(chosenSummon.getSpiritSkills());
                            }
                        } else {
                            System.out.println("画像ファイルが見つかりません: " + chosenSummon.getImagePath());
                        }
                    }
                }
                updateDisplay();

                // 2. エネミーがたおれたかチェック（Check）
                java.util.List<Enemy> aliveEnemies = new java.util.ArrayList<>();
                for (Enemy enemyInside : enemyParty){
                    if (enemyInside.isAlive()){
                        aliveEnemies.add(enemyInside);
                    }
                }

                //敵が全滅したときの処理
                if(aliveEnemies.isEmpty()){
                    //ボスエリアの判定
                    if (currentField.equals("BOSS")) {
                        if (bossStage == 1) {
                            bossStage = 2;
                            logTextArea.append("ドラゴンを倒した！\n");
                            logTextArea.append("魔王が現れた！\n");
                            spawnEnemy();
                            enemyIcon();
                            updateDisplay();
                            return;
                        } else {
                            //魔王撃破からクリア画面に
                            logTextArea.append("魔王を倒した！\n");
                            distributeBattleExpAndLevelUp();
                            GameSaveManager.savePartyData(currentSaveSlot,party);
                            GameSaveManager.updateGameData(currentSaveSlot,gold, new java.sql.Timestamp(System.currentTimeMillis()));
                            bossStage = 1;
                            gameScreen(2);
                            return;
                        }
                    } else {
                        //森、ダンジョンの判定
                        logTextArea.append("敵の群れを倒した！\n");
                        if (enemyCount < 5) {
                            enemyCount++;
                            spawnEnemy();
                            enemyIcon();
                            updateDisplay();
                            return;
                        } else {
                            logTextArea.append("すべての魔物を倒した\n");
                            distributeBattleExpAndLevelUp();
                            GameSaveManager.savePartyData(currentSaveSlot,party);
                            GameSaveManager.updateGameData(currentSaveSlot,gold, new java.sql.Timestamp(System.currentTimeMillis()));
                            enemyCount = 1;
                            gameScreen(4);
                            return;
                        }
                        
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
                //敵がいるかの確認の処理
                if (enemyParty ==null || enemyParty.isEmpty()) {
                    logTextArea.append("敵がいないためアイテムは使えません！\n");
                    return;
                }

                //ガードフラグをリセット
                player.guardFlg = 0;

                //アイテムを使う処理
                showItemBox();

                updateDisplay();

                //次のターンのキャラクターにチェンジ
                boolean isEnemyTurn = switchNextPlayer();

                //敵のターン
                if (isEnemyTurn) {
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

    //技選択ダイアログ（3列グリッド表示 ＋ 召喚士は「召喚」ボタンのみ表示）全員に適用
    private Skill showSkillSelectDialog(Player currentActor) {
        final Skill[] selectedResult = {null};

        // 召喚士（まだ変身していない状態）なら「召喚」ボタン1つだけ表示
        if (currentActor.getName().contains("召喚士(SUMMONER)") && !currentActor.isSummoned()) {
            JDialog dialog = new JDialog(this, "技の選択", true);
            dialog.setSize(400, 170);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));

            JLabel msgLabel = new JLabel("  " + currentActor.getName() + " はどうする？", JLabel.LEFT);
            msgLabel.setFont(new Font("MS ゴシック", Font.BOLD, 16));
            msgLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
            dialog.add(msgLabel, BorderLayout.NORTH);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            JButton summonBtn = new JButton("召喚");
            summonBtn.setPreferredSize(new Dimension(180, 50));
            summonBtn.setFont(new Font("MS ゴシック", Font.BOLD, 18));
            summonBtn.setBackground(new Color(142, 68, 173));
            summonBtn.setForeground(Color.WHITE);
            summonBtn.setFocusPainted(false);

            summonBtn.addActionListener(e -> {
                // ダミーの「召喚」トリガー用Skillを返して、次の精霊選択へ進む
                selectedResult[0] = new Skill("召喚", 0.0, "召喚");
                dialog.dispose();
            });

            btnPanel.add(summonBtn);
            dialog.add(btnPanel, BorderLayout.CENTER);
            dialog.setVisible(true);
            return selectedResult[0];
        }

        // それ以外のキャラ（勇者・祈祷師・変身後の精霊など）：3列グリッドで並べる
        List<Skill> skills = currentActor.getSkills();
        JDialog dialog = new JDialog(this, "技の選択", true);
        int rows = (int) Math.ceil(skills.size() / 3.0);
        dialog.setSize(820, Math.max(200, 90 + rows * 65));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel msgLabel = new JLabel("  " + currentActor.getName() + " はどうする？", JLabel.LEFT);
        msgLabel.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        msgLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        dialog.add(msgLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        for (Skill sk : skills) {
            JButton btn = new JButton(sk.getName());
            btn.setFont(new Font("MS ゴシック", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                selectedResult[0] = sk;
                dialog.dispose();
            });
            gridPanel.add(btn);
        }

        dialog.add(gridPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
        return selectedResult[0];
    }

    //召喚士の「誰を召喚する？」ダイアログ（出せる／出せないを表示＆判定）
    private Skill showSpiritSelectDialog(Player summoner, List<Skill> summonSkills) {
        final Skill[] selectedSpirit = {null};

        JDialog dialog = new JDialog(this, "精霊召喚", true);
        dialog.setSize(700, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel msgLabel = new JLabel(
            "  誰を召喚する？（現在 Lv." + summoner.getLevel() + " / HP: " + summoner.getHp() + "）",
            JLabel.LEFT
        );
        msgLabel.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        msgLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        dialog.add(msgLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        for (Skill s : summonSkills) {
            int reqLv = s.getRequiredLevel();
            int hpCost = s.getHpCost();

            // 出せる条件：必要レベルに達していて、かつHPが消費量より多いこと
            boolean isLevelOk = summoner.getLevel() >= reqLv;
            boolean isHpOk = summoner.getHp() > hpCost;
            boolean canSummon = isLevelOk && isHpOk;

            String statusLabel;
            if (!isLevelOk) {
                statusLabel = "【出せない：Lv." + reqLv + "必要】";
            } else if (!isHpOk) {
                statusLabel = "【出せない：HP不足】";
            } else {
                statusLabel = "【出せる（召喚可能）】";
            }

            //ここの<html>はJava Swingの機能でレイアウトをきれいにしてくれる
            JButton btn = new JButton("<html><center>" + s.getName() + "<br>" +
                                      "消費HP:" + hpCost + " / 必要Lv." + reqLv + "<br>" +
                                      statusLabel + "</center></html>");
            btn.setFont(new Font("MS ゴシック", Font.BOLD, 13));
            btn.setFocusPainted(false);

            if (canSummon) {
                btn.setBackground(new Color(46, 204, 113)); // 緑色
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    selectedSpirit[0] = s;
                    dialog.dispose();
                });
            } else {
                btn.setEnabled(false); // 出せない精霊はグレーアウトして押せないようにする
            }

            gridPanel.add(btn);
        }

        dialog.add(gridPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
        return selectedSpirit[0];
    }

    //精霊が一回攻撃したら召喚士に戻るメソッド
    public void revertToSummoner(Player player, int index) {
        //名前やステータスを戻す
        player.setName(player.getOriginalName());
        player.setAtk(player.getOriginalAtk());
        player.setMaxHp(player.getOriginalMaxHp());
        player.setSkills(player.getOriginalSkills());
        player.setLevel(player.getOriginalLevel());

        //アイコンを戻す
        if (player.getOriginalImagePath() != null) {
            ImageIcon summonerIcon = new ImageIcon(player.getOriginalImagePath());
            Image img = summonerIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            playerImageLabels[index].setIcon(new ImageIcon(img));
        }

        //更新
        updateDisplay();
    }

    //戦闘終了時に経験値を均等分配し、まとめてレベルアップ判定を行うメソッド
    private void distributeBattleExpAndLevelUp() {
        if (battleExpPool <= 0) return;

        // 生きているパーティメンバーを集める
        List<Player> aliveMembers = new ArrayList<>();
        for (Player member : party) {
            if (member.isAlive()) {
                aliveMembers.add(member);
            }
        }

        if (!aliveMembers.isEmpty()) {
            int shareExp = battleExpPool / aliveMembers.size();     // 1人あたりの均等経験値
            int remainderExp = battleExpPool % aliveMembers.size(); // 割り切れなかった余り

            logTextArea.append("合計 " + battleExpPool + " EXPを獲得！（1人あたり " + shareExp + " EXP）\n");

            StringBuilder levelUpMessages = new StringBuilder();

            for (Player member : aliveMembers) {
                int gain = shareExp;
                // 余りがあり、かつトドメを刺した本人なら余りを加算
                if (remainderExp > 0 && member == lastAttacker) {
                    gain += remainderExp;
                    logTextArea.append(member.getName() + " はトドメボーナスでさらに +" + remainderExp + " EXP獲得！\n");
                }

                member.setExp(member.getExp() + gain);

                // レベルアップ判定（複数レベル一気に上がる場合も考慮して while にする）
                while (member.checkLevelUp()) {
                    levelUpMessages.append(member.getName())
                                   .append(" は Lv.")
                                   .append(member.getLevel())
                                   .append(" に上がった！\n");
                }
            }

            // レベルアップしたキャラがいればまとめて1回ダイアログで表示
            if (levelUpMessages.length() > 0) {
                JOptionPane.showMessageDialog(
                    BattleGame.this,
                    levelUpMessages.toString(),
                    "レベルアップ！",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }

        // 次の戦闘に向けてリセット
        battleExpPool = 0;
        lastAttacker = null;
        updatePlayerVisuals();
        updateDisplay();
    }

    //セーブ選択画面のメソッド
    private JPanel createSaveSelectPanel() {
        saveSelectPanel = new JPanel(new BorderLayout());
        saveSelectPanel.setBackground(new Color(20, 20, 20)); // 背景色を黒に設定

        //上部にラベルを追加
        JLabel label = new JLabel("プレイするデータを選んでください(PLESE SELECT DATA)", JLabel.CENTER);
        label.setFont(new Font("MS ゴシック", Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 上下に余白を追加
        saveSelectPanel.add(label, BorderLayout.NORTH);

        //ボタンを一列に並べるコンテナ(10行1列、間隔10)
        saveSelectContainer = new JPanel(new GridLayout(10, 1, 0, 10));
        saveSelectContainer.setBackground(new Color(20, 20, 20)); // 背景色を黒に設定

        //スクロール可能にする
        JScrollPane scrollPane = new JScrollPane(saveSelectContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // スクロールペインの境界線を削除
        scrollPane.getViewport().setBackground(new Color(20, 20, 20)); // スクロールペインの背景色を黒に設定
        
        saveSelectPanel.add(scrollPane, BorderLayout.CENTER);

        return saveSelectPanel;
    }

    // セーブ選択画面を開くたびにセーブデータを更新するメソッド
    private void updateSaveSelectPanel() {
        saveSelectContainer.removeAll(); // 既存のボタンを削除

        // ★ 1. データ件数に関わらず、最初から2列（横の隙間20px、縦の隙間12px）で表示する
        saveSelectContainer.setLayout(new GridLayout(6, 2, 20, 12));

        // データベースからデータをすべて取得
        List<SaveSlotInfo> slots = GameSaveManager.loadGameData();

        // データの数だけ行を作成する
        for (SaveSlotInfo slot : slots) {
            // ★ 2. セーブデータと削除ボタンの隙間を「0」にしてピッタリくっつける
            JPanel rowPanel = new JPanel(new BorderLayout(0, 0));
            rowPanel.setOpaque(false);
            rowPanel.setPreferredSize(new Dimension(560, 60));

            JButton slotButton = new JButton();
            slotButton.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
            slotButton.setHorizontalAlignment(SwingConstants.LEFT);
            slotButton.setMargin(new Insets(10, 10, 10, 10));

            if (slot.isEmpty()) {
                // 空スロット(新規ゲーム)
                slotButton.setText("ファイル" + slot.getSvNo() + "：ーーNO DATAーー");
                slotButton.setForeground(Color.GRAY);
                slotButton.addActionListener(e -> {
                    createAndStartNewGame();
                });
                rowPanel.add(slotButton, BorderLayout.CENTER);
            } else {
                // データがあるスロット(ロードボタン ＋ 削除ボタン)
                String dateStr = slot.getSvDate().toString().substring(0, 19);
                String buttonText = String.format("ファイル%-2d 所持金 %-6dコイン %s", slot.getSvNo(), slot.getSvCoin(), dateStr);
                slotButton.setText(buttonText);
                slotButton.setForeground(Color.BLACK);
                slotButton.addActionListener(e -> {
                    loadGame(slot.getSvNo());
                });

                // 右側にピッタリくっつけて配置する赤い削除ボタン
                JButton deleteButton = new JButton("削除");
                deleteButton.setPreferredSize(new Dimension(70, 60));
                deleteButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
                deleteButton.setBackground(new Color(231, 76, 60)); // 赤色
                deleteButton.setForeground(Color.WHITE);
                deleteButton.setFocusPainted(false);

                deleteButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "「ファイル" + slot.getSvNo() + "」を本当に削除しますか？\n（この操作は取り消せません）",
                            "セーブデータの削除確認",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean deleted = GameSaveManager.deleteSaveData(slot.getSvNo());
                        if (deleted) {
                            JOptionPane.showMessageDialog(this, "ファイル" + slot.getSvNo() + " を削除しました。");
                            updateSaveSelectPanel(); // 画面のリストを即座に再描画
                        } else {
                            JOptionPane.showMessageDialog(this, "削除に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                rowPanel.add(slotButton, BorderLayout.CENTER);
                rowPanel.add(deleteButton, BorderLayout.EAST);
            }

            saveSelectContainer.add(rowPanel);
        }

        // 新規作成ボタン（10個あるかどうかで表示を変える）
        String newBtnText = (slots.size() >= 10)
                            ? "＋新しく冒険を始める(既存データ上書き)"
                            : "＋新しく冒険を始める(新規データ作成)";
        JButton newGameButton = new JButton(newBtnText);
        newGameButton.setPreferredSize(new Dimension(560, 60));
        newGameButton.setFont(new Font("MS ゴシック", Font.BOLD, 18));
        newGameButton.setBackground(new Color(46, 204, 113)); // 緑色
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);

        newGameButton.addActionListener(e -> {
            createAndStartNewGame();
        });

        saveSelectContainer.add(newGameButton);

        //残りの枠(12枠-セーブデータ)を透明パネルで埋めて、大きさを均等にする
        int totalItems = slots.size() + 1;//セーブデータ+新規ボタン
        for (int i = totalItems; i < 12; i++) {
            JPanel emptySpace = new JPanel();
            emptySpace.setOpaque(false);//透明にする
            saveSelectContainer.add(emptySpace);
        }

        saveSelectContainer.revalidate();
        saveSelectContainer.repaint();
    }

    // 新規セーブデータを作成してゲームを始めるメソッド（最大10個制限付き）
    private void createAndStartNewGame() {
        int initialGold = 0; // 初期所持金
        List<SaveSlotInfo> slots = GameSaveManager.loadGameData();

        // 【パターンA】セーブデータが10個未満の場合：通常通り新規スロットを作成
        if (slots.size() < 10) {
            int newSvNo = GameSaveManager.createNewSaveData(initialGold);
            if (newSvNo != -1) {
                currentSaveSlot = newSvNo;
                gold = initialGold;
                System.out.println("新規データ（スロット " + newSvNo + "）で開始します。");
                choicePlayer();
            } else {
                JOptionPane.showMessageDialog(this, "新規データの作成に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // 【パターンB】セーブデータが10個以上ある場合：上書きするスロットを選択させる
        String[] options = new String[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            SaveSlotInfo s = slots.get(i);
            String dateStr = s.getSvDate().toString().substring(0, 19);
            options[i] = String.format("ファイル%d （所持金: %d / %s）", s.getSvNo(), s.getSvCoin(), dateStr);
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "セーブデータが上限（10個）に達しています。\n上書きして新しく始めるファイルを選んでください：",
                "上書きするセーブデータの選択",
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        // キャンセル（×ボタン含む）された場合は何もしない
        if (selected == null) {
            return;
        }

        // 選ばれた項目に対応するスロット番号を特定
        int targetSvNo = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selected)) {
                targetSvNo = slots.get(i).getSvNo();
                break;
            }
        }

        if (targetSvNo != -1) {
            // 最終確認ダイアログ
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "「ファイル" + targetSvNo + "」のデータを消去して、新しく始めますか？",
                    "上書き確認",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = GameSaveManager.overwriteSaveDataForNewGame(targetSvNo, initialGold);
                if (success) {
                    currentSaveSlot = targetSvNo;
                    gold = initialGold;
                    System.out.println("スロット " + targetSvNo + " を上書きして新規開始します。");
                    choicePlayer();
                } else {
                    JOptionPane.showMessageDialog(this, "データの上書き初期化に失敗しました。", "エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    //ロードする処理
    private void loadGame(int svNo) {
        //選択されたスロット番号を保持する
        currentSaveSlot = svNo;

        //ロードの初期化処理
        System.out.println("ゲームをロードします。スロット番号: " + svNo);

        System.out.println("BattleGame側の現在の所持金: " + gold);

        //データベースからデータを取得する
        GameSaveManager.loadGameDataById(svNo); // ゲームデータをロードする

        //ロード時パーティが空もしくは４人そろっていない場合、初期パーティを作る
        if (party == null || party.size() < 4) {
            System.out.println("新しいデータのためキャラクター選択画面を開きます");
            choicePlayer();//パーティ選択画面に移動
            return;//選択が終わるまでマップ移動を中断するため
        }

        //4人分の画像を画面ラベルにセットする
        player = party.get(0);
        currentPlayerIndex = 0;
        for (int i = 0; i < party.size(); i++) {
            playerImageLabels[i].setIcon(party.get(i).getIcon());
        }

        //エネミーやマップの更新
        spawnEnemy();
        enemyIcon();
        updatePlayerVisuals();
        updateDisplay();

        gameScreen(4);//ロードしたらマップ画面に移動
    }

    //ロードずるときパーティを再構築するメソッド
    public static void setPartyMembers(List<SavePartyMember> dbPartyList) {
        party.clear(); // 既存のパーティをクリア

        //セーブデータにメンバーがいるかチェック
        if (dbPartyList == null || dbPartyList.isEmpty()) {
            System.out.println("セーブデータにパーティが存在しません");
            return;
        }
        
        

        for (SavePartyMember m : dbPartyList) {
            //PLNOからキャラクター名や画像を特定してPlayerオブジェクトを生成
            Player p = createPlayerByPlNo(m.getPlNo());
            if (p != null) {
                p.setLevel(m.getPlLevel());
                p.setHp(m.getPlHp());
                p.setMaxHp(m.getMaxHp());
                p.setAtk(m.getAtk());
                p.setMgc(m.getMgc());
                GameMasterManager.applyMasterDataToPlayer(p);
                party.add(p);
            }
        }

        System.out.println("パーティを読み込みました");
    }

    //PLNO(1~7)から対応するキャラクターを生成して返すメソッド
    public static Player createPlayerByPlNo(int plNo) {
        Player p = null;

        switch(plNo) {
            case 1:
                p = new Player("勇者(HERO)",60,20,20,"yuusya_game.png",0,1,0,10);
                break;
            case 2:
                p = new Player("魔法使い(WIZARD)",45,5,50,"mahoutsukai_man.png",0,1,0,10);
                break;
            case 3:
                p = new Player("騎士(KNIGHT)",65,30,5,"knight.png",0,1,0,10);
                break;
            case 4:
                p = new Player("盗賊(THIEF)",70,10,20,"dorobou_hokkamuri.png",0,1,0,10);
                break;
            case 5:
                p = new Player("召喚士(SUMMONER)",90,5,5,"mahoutsukai_necromancer.png",0,1,0,10);
                break;
            case 6:
                p = new Player("祈祷師(SHAMAN)",50,5,45,"oharai_kannushi.png",0,1,0,10);
                break;
            case 7:
                p = new Player("回復術師(HEALER)",45,5,50,"job_doctor_man.png",0,1,0,10);
                break;
            default:
                return null;
        }

        //スキルメソッドを呼んでセット
        setupSkillsForPlayer(p, p.getName());
        return p;
    }

    //キャラクターごとのスキルを再設定するメソッド
    public static void setupSkillsForPlayer(Player p,String name) {
        if (name.equals("勇者(HERO)")) {
            p.learnSkill("斬る(SLASH)",1.0,"単体攻撃");
            p.learnSkill("かばう(COVER)",0.25,"かばう");
        } else if (name.equals("魔法使い(WIZARD)")) {
            p.learnSkill("炎魔法(FIRE)",1.0,"全体攻撃");
        } else if (name.equals("騎士(KNIGHT)")) {
            p.learnSkill("斬る(SLASH)",1.0,"単体攻撃");
            p.learnSkill("かばう(COVER)",0.25,"かばう");
        } else if (name.equals("盗賊(THIEF)")) {
            p.learnSkill("斬る",1.0,"単体攻撃");
        } else if (name.equals("召喚士(SUMMONER)")) {
            Skill nurikabe = new Skill("弱い精霊",0.0,"召喚",15,1,"youkai_nurikabe.png",120,5,5);
            nurikabe.addSpiritSkill(new Skill("斬る(SLASH)",1.0,"単体攻撃"));
            nurikabe.addSpiritSkill(new Skill("かばう(COVER)",0.25,"かばう"));
            p.learnSkill(nurikabe);
        } else if (name.equals("祈祷師(SHAMAN)")) {
            p.learnSkill("攻撃力UP(ATK BUFF)",1.5,"単体バフ");
            p.learnSkill("魔力UP(MGC BUFF)",1.5,"単体バフ");
        } else if (name.equals("回復術師(HEALER)")) {
            p.learnSkill("回復(HEAL)",1.0,"単体回復");
            p.learnSkill("全体回復(MASS HEAL)",0.5,"全体回復");
        }
    }

    //タイトル画面のメソッド
    private JPanel createTitlePanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(20, 20, 20)); // 背景色を黒に設定

        JLabel titleLabel = new JLabel("ターン制コマンドバトル", JLabel.CENTER);
        titleLabel.setFont(new Font("MS　ゴシック",Font.BOLD,36));
        titleLabel.setForeground(Color.WHITE); // 白文字

        JButton startButton = new JButton("START");
        startButton.setFont(new Font("Arial",Font.PLAIN,24));

        //スタートボタンを押したらセーブデータ画面に移動する
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSaveSelectPanel(); // セーブ選択画面を更新
                cardLayout.show(mainPanel, "SAVE_SELECT");
            }
        });

        JPanel menu = new JPanel(new GridLayout(2,1,0,30));
        menu.setBackground(new Color(20, 20, 20));
        menu.add(titleLabel);
        menu.add(startButton);
        panel.add(menu);
        return panel;
    }

    //ゲームオーバーの画面のメソッド
    private JPanel createGameOverPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);//画面が透けないようにする
        panel.setBackground(new Color(20, 20, 20)); // 背景色を黒に設定

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
        menu.setBackground(new Color(20, 20, 20));
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
        panel.setBackground(new Color(20, 20, 20));

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
        menu.setBackground(new Color(20, 20, 20));
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
            // ↓↓↓ ここから置き換え ↓↓↓
            if (enemyInside.isAlive()) {
                // 生きている味方だけをリストに集める
                java.util.List<Player> aliveMembers = new java.util.ArrayList<>();
                for (Player member : party) {
                    if (member.isAlive() && member.getHp() > 0) {
                        aliveMembers.add(member);
                    }
                }

                // プレイヤーが全滅していたら攻撃をやめる
                if (aliveMembers.isEmpty()) {
                    break;
                }

                // ★ DBから読み込んだ敵スキルからランダムに1つ選択
                Skill chosenSkill = null;
                List<Skill> eSkills = enemyInside.getSkills();
                if (eSkills != null && !eSkills.isEmpty()) {
                    int skIdx = (int)(Math.random() * eSkills.size());
                    chosenSkill = eSkills.get(skIdx);
                }

                String skillName = (chosenSkill != null) ? chosenSkill.getName() : "通常攻撃";
                String skillType = (chosenSkill != null) ? chosenSkill.getType() : "単体攻撃";
                double multiplier = (chosenSkill != null) ? chosenSkill.getMultiplier() : 1.0;

                // ① 全体攻撃スキルの場合
                if (skillType.equals("全体攻撃")) {
                    logTextArea.append(enemyInside.getName() + " の " + skillName + "！\n");
                    for (Player member : aliveMembers) {
                        int pIndex = party.indexOf(member);
                        int damage = (int)(enemyInside.getMgc() * multiplier);
                        if (member.guardFlg == 1) damage /= 2;
                        if (damage < 1) damage = 1;

                        member.hp -= damage;
                        if (member.hp < 0) member.hp = 0;

                        logTextArea.append(member.getName() + " に " + damage + " のダメージ！\n");
                        int offsetY = playerPopupCounts[pIndex] * 25;
                        showPopupText("-" + damage, Color.RED, playerImageLabels[pIndex], offsetY);
                        playerPopupCounts[pIndex]++;
                    }

                // ② 回復スキルの場合
                } else if (skillType.contains("回復")) {
                    int healAmount = (int)(enemyInside.getMaxHp() * multiplier);
                    if (healAmount < 10) healAmount = 10;
                    enemyInside.hp += healAmount;
                    if (enemyInside.hp > enemyInside.getMaxHp()) {
                        enemyInside.hp = enemyInside.getMaxHp();
                    }
                    logTextArea.append(enemyInside.getName() + " の " + skillName + "！ HPが " + healAmount + " 回復した！\n");
                    int eIndex = enemyParty.indexOf(enemyInside);
                    showPopupText("+" + healAmount, Color.GREEN, enemyImageLabels[eIndex]);

                // ③ 単体攻撃（またはその他）の場合
                } else {
                    Player targetPlayer = null;
                    boolean isCovered = false;

                    if (coveringPlayer != null && aliveMembers.contains(coveringPlayer)) {
                        targetPlayer = coveringPlayer;
                        isCovered = true;
                        logTextArea.append(targetPlayer.getName() + "がかばった！\n");
                    } else {
                        int randomIndex = (int)(Math.random() * aliveMembers.size());
                        targetPlayer = aliveMembers.get(randomIndex);
                    }

                    int pIndex = party.indexOf(targetPlayer);
                    int damage = (int)(enemyInside.getAtk() * multiplier);

                    if (isCovered) {
                        damage = (int)(damage * 0.25);
                        if (damage < 1) damage = 1;
                        targetPlayer.hp -= damage;
                        if (targetPlayer.hp < 0) targetPlayer.hp = 0;
                        logTextArea.append(enemyInside.getName() + " の " + skillName + "！ " + targetPlayer.getName() + "がかばって" + damage + "ダメージを受けた！\n");
                    } else {
                        if (targetPlayer.guardFlg == 1) {
                            damage = damage / 2;
                        }
                        if (damage < 1) damage = 1;
                        targetPlayer.hp -= damage;
                        if (targetPlayer.hp < 0) targetPlayer.hp = 0;
                        logTextArea.append(enemyInside.getName() + " の " + skillName + "！ " + targetPlayer.getName() + " に " + damage + " のダメージ！\n");
                    }

                    int offsetY = playerPopupCounts[pIndex] * 25;
                    showPopupText("-" + damage, Color.RED, playerImageLabels[pIndex], offsetY);
                    playerPopupCounts[pIndex]++;
                }

                // 一体ごとに画面を更新
                updateDisplay();
                updatePlayerVisuals();
            }
            // ↑↑↑ ここまで置き換え ↑↑↑
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
        private JButton itemButton;
        private JButton partyButton;
        private JButton saveButton;

        public MapPanel(BattleGame game) {
            setBackground(new Color(34, 139, 34)); // 背景色を緑に設定
            setLayout(null); // レイアウトをnullに設定して自由配置にする

            //1.アイテムボタンの作成(カバンのアイコン)
            itemButton = new JButton();
            ImageIcon origItemIcon = new ImageIcon("持ち物アイコン.png");
            Image scaledItemIcon = origItemIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            itemButton.setIcon(new ImageIcon(scaledItemIcon));
            add(itemButton);

            //ボタンの背景や枠を透明にする
            itemButton.setContentAreaFilled(false);// 背景を透明にする
            itemButton.setBorderPainted(false);// 枠を透明にする
            itemButton.setFocusPainted(false);// フォーカスの点線を消す
            itemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //アイテムボックスを開く処理
                    game.showMapItemBox();
                }
            });

            //2.パーティー情報ボタンの作成
            partyButton = new JButton();
            ImageIcon origPartyIcon = new ImageIcon("チームアイコン.png");
            Image scaledPartyIcon = origPartyIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            partyButton.setIcon(new ImageIcon(scaledPartyIcon));
            add(partyButton);

            //ボタンの背景や枠を透明にする
            partyButton.setContentAreaFilled(false);
            partyButton.setBorderPainted(false);
            partyButton.setFocusPainted(false);
            partyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //パーティー情報を表示する処理
                    game.showMapPartyInfo();
                }
            });

            //セーブボタンを追加する
            saveButton = new JButton();
            ImageIcon origSaveIcon = new ImageIcon("セーブボタン.png");
            Image scaledSaveIcon = origSaveIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            saveButton.setIcon(new ImageIcon(scaledSaveIcon));
            add(saveButton);

            //ボタンの背景や枠を透明にする
            saveButton.setContentAreaFilled(false);
            saveButton.setBorderPainted(false);
            saveButton.setFocusPainted(false);
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //セーブ処理を行う
                    game.saveGame();
                }
            });

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

        //画面の大きさに応じてボタンの位置を調整する処理
        @Override
        public void doLayout() {
            super.doLayout();
            int h = getHeight();

            //底から80ピクセル上に配置するように調整
            if (itemButton != null) {
                itemButton.setBounds(40, h - 80, 60, 60);
            }
            if (partyButton != null) {
                partyButton.setBounds(120, h - 80, 60, 60);
            }
            if (saveButton != null) {
                saveButton.setBounds(200, h - 80, 60, 60);
            }
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

    //マップ用のアイテム確認画面
    public void showMapItemBox() {
        //アイテムボックスの作成
        JDialog itemDialog = new JDialog(this, "アイテムボックス(ITEM BOX)", true);
        itemDialog.setSize(600, 400);
        itemDialog.setLocationRelativeTo(this);//ウィンドウの中央に表示する仕様
        itemDialog.setLayout(null);
        itemDialog.getContentPane().setBackground(Color.WHITE);//背景を白に設定する

        //ラベル
        JLabel label = new JLabel("アイテムボックス(ITEM BOX)", JLabel.CENTER);
        label.setFont(new Font("MS ゴシック", Font.BOLD, 24));
        label.setBounds(0,10,600,30);
        itemDialog.add(label);

        //マス目を作るパネル
        JPanel itemBoxPanel = new JPanel();
        itemBoxPanel.setLayout(new GridLayout(2,5,10,10));//２行５列
        itemBoxPanel.setBounds(20, 65, 540, 200);
        itemBoxPanel.setBackground(Color.WHITE);

        //アイテムボックスの中身をループで表示
        for (int i = 0; i < itembox.length; i++) {
            if (i < itembox.length && itembox[i] != null) {
                Item item = itembox[i];
                itemButton = createItemButton(item.getImagePath(),String.valueOf(item.getPrice()));
                //クリック時は閉じるボタンだけ出す
                itemButton.addActionListener(new ActionListener() {
                    @Override 
                    public void actionPerformed(ActionEvent e) {
                        showMapItemDetailDialog(item);
                    }
                });
                
            } else {
                // 空のスロットを作成
                itemButton = new JButton("");
                itemButton.setBackground(Color.LIGHT_GRAY);
                itemButton.setEnabled(false);
            }
            itemBoxPanel.add(itemButton);
        }
        itemDialog.add(itemBoxPanel);

        //閉じるボタンの作成
        JButton closeButton = new JButton("閉じる(CLOSE)");
        closeButton.setFont(new Font("MSゴシック",Font.BOLD,16));
        closeButton.setBackground(Color.BLUE);//ボタン全体の色
        closeButton.setForeground(Color.WHITE);//ボタンの文字の色
        closeButton.setBounds(460,15,100,30);
        closeButton.addActionListener(e -> itemDialog.dispose());
        itemDialog.add(closeButton);

        //所持コイン表示
        JLabel coinLabel = new JLabel("所持コイン：" + gold + "コイン");
        coinLabel.setFont(new Font("MSゴシック",Font.BOLD,16));
        coinLabel.setBounds(300,325,260,30);
        itemDialog.add(coinLabel);

        itemDialog.setVisible(true);
    }

    //マップ画面でクリックしたときにアイテムの説明を表示するメソッド
    private void showMapItemDetailDialog(Item item) {
        //1.ダイアログの設定
        JDialog dialog = new JDialog((Frame) null, "アイテム確認", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this); // 画面中央に表示
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(Color.WHITE);//背景色を白にする
        
        //2.アイテムの画像
        JLabel imageLabel = new JLabel();
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(item.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }
        imageLabel.setBounds(20, 20, 100, 100);
        dialog.add(imageLabel);

        //3.アイテムの名前
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("MS ゴシック", Font.BOLD, 20));
        nameLabel.setBounds(140, 20, 200, 30);
        dialog.add(nameLabel);

        //4.「効果」見出し
        JLabel effectLabel = new JLabel("効果");
        effectLabel.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        effectLabel.setBounds(140, 60, 200, 30);
        dialog.add(effectLabel);

        //5.アイテムの効果の説明
        String effectText = "";
        if (item instanceof HealItem) {
            effectText = "HPを" + ((HealItem) item).getValue() + "回復する";
        } else if (item instanceof EscapeItem) {
            effectText = "戦闘から逃げる";
        } else if (item instanceof EquipmentItem) {
            effectText = "効果を発動する";
        }
        JLabel descriptionLabel = new JLabel("<html>" + effectText + "</html>");
        descriptionLabel.setFont(new Font("MS ゴシック", Font.PLAIN, 14));
        descriptionLabel.setBounds(140, 90, 220, 100);
        dialog.add(descriptionLabel);
        
        //6.緑色の装備するボタン
        JButton buyButton = new JButton("装備する");
        buyButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        buyButton.setBackground(Color.GREEN);
        buyButton.setForeground(Color.WHITE);
        buyButton.setBounds(140, 200, 100, 30);
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buyItem(item);
                dialog.dispose(); // ダイアログを閉じる
            }
        });
        dialog.add(buyButton);

        //7.青色の閉じるボタン
        JButton closeButton = new JButton("閉じる");
        closeButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        closeButton.setBackground(Color.BLUE);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBounds(250, 200, 100, 30);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // ダイアログを閉じる
            }
        });
        dialog.add(closeButton);

        //8.ダイアログを表示
        dialog.setVisible(true);
    }

    //マップ用のパーティー情報確認画面
    public void showMapPartyInfo() {
        if (party == null || party.isEmpty()) {
            JOptionPane.showMessageDialog(this, "パーティーが存在しません。");
            return;
        }

        //パーティー情報のダイアログを作成
        JDialog partyDialog = new JDialog(this, "パーティー情報(PARTY INFO)", true);
        partyDialog.setSize(1220, 750);
        partyDialog.setLocationRelativeTo(this);
        partyDialog.setLayout(new BorderLayout(10, 10));
        partyDialog.getContentPane().setBackground(new Color(230,230,230));//背景色を薄いグレーに設定

        final int[] currentIndex = {0}; // 現在表示しているメンバーのインデックス

        //キャラクターの画像のラベル
        JLabel imageLabel = new JLabel("",JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(250,250));

        //ステータス表示エリア
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("MS ゴシック", Font.PLAIN, 18));
        infoArea.setBackground(new Color(255,255,255));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //情報を更新するメソッド
        Runnable updateInfo = () -> {
            Player currentMember = party.get(currentIndex[0]);

            //画面の更新
            ImageIcon rawIcon = currentMember.getIcon();
            if (rawIcon != null) {
                Image img = rawIcon.getImage();
                Image scaledImg = img.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));        
            } else {
                imageLabel.setIcon(null);
            }

            //ステータス更新
            StringBuilder infoText = new StringBuilder();
            infoText.append("名前(Name): ").append(currentMember.getName()).append("\n");
            infoText.append("HP: ").append(currentMember.getHp()).append("/").append(currentMember.getMaxHp()).append("\n");
            infoText.append("攻撃力(ATK): ").append(currentMember.getAtk()).append("\n");
            infoText.append("魔力(MGC): ").append(currentMember.getMgc()).append("\n");
            infoText.append("特徴(TRAITS): ").append(getCharacterFeatures(currentMember.getName()));
            infoArea.setText(infoText.toString());
        };

        //アイコンをクリックできるようにする処理
        JLabel[] teamSlots = new JLabel[4];
        JPanel teamPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        teamPanel.setOpaque(false);
        teamPanel.add(new JLabel("チーム："));

        for (int i = 0; i < 4; i++) {
            final int memberIndex = i;//内部イベント処理から外側の変数を参照する場合、定数でないといけない
            teamSlots[i] = new JLabel("", JLabel.CENTER);
            teamSlots[i].setPreferredSize(new Dimension(60,60));//アイコンが入る枠のサイズを決める
            teamSlots[i].setBorder(BorderFactory.createLineBorder(Color.GRAY,2));//枠の周りにグレーの太さ2ピクセルの線を作る

            //パーティメンバーが存在するスロットの処理
            if (i < party.size()) {
                teamSlots[i].setIcon(party.get(i).getIcon());
                teamSlots[i].setCursor(new Cursor(Cursor.HAND_CURSOR));//カーソルを指マークにする

                //アイコンクリック時にそのキャラに切り替える
                teamSlots[i].addMouseListener(new MouseAdapter(){
                    @Override 
                    public void mouseClicked(MouseEvent e){
                        currentIndex[0] = memberIndex;
                        updateInfo.run();
                    }
                });
            }

            teamPanel.add(teamSlots[i]);
        }

        updateInfo.run();//初期表示

        //閉じるボタン
        JButton closeButton = new JButton("閉じる");
        closeButton.setFont(new Font("MSゴシック", Font.BOLD,14));
        closeButton.setBackground(new Color(60,130,220));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                partyDialog.dispose();
            }
        });

        //中央レイアウト
        JPanel charInfoPanel = new JPanel(new GridLayout(1,2,10,0));
        charInfoPanel.setOpaque(false);//後ろの背景の色を透過する
        charInfoPanel.setBorder(BorderFactory.createEmptyBorder(20,20,10,20));
        charInfoPanel.add(imageLabel);
        charInfoPanel.add(infoArea);

        //下部のレイアウト
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        //10ピクセルの枠を作る
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        bottomPanel.add(teamPanel,BorderLayout.WEST);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actionButtonPanel.setOpaque(false);
        actionButtonPanel.add(closeButton);
        bottomPanel.add(actionButtonPanel,BorderLayout.EAST);

        //ダイアログへ配置
        partyDialog.add(charInfoPanel,BorderLayout.CENTER);
        partyDialog.add(bottomPanel,BorderLayout.SOUTH);

        partyDialog.setVisible(true);

    }

    //マップ用のセーブボタン
    public void saveGame() {
        //1.現在の「年、月、日、時、分、秒」のデータを作る
        java.sql.Timestamp currentDate = new java.sql.Timestamp(System.currentTimeMillis());

        //2.選択中のスロットの所持金、日時、パーティを上書き保存する
        boolean partySaved = GameSaveManager.savePartyData(currentSaveSlot, party);
        boolean gameSaved = GameSaveManager.updateGameData(currentSaveSlot, gold, currentDate);

        //3.保存完了のメッセージを表示する
        if (partySaved && gameSaved) {
            JOptionPane.showMessageDialog(this,"ファイル" + currentSaveSlot + "にゲームを上書き保存しました。");
        } else {
            JOptionPane.showMessageDialog(this,"保存に失敗しました","エラー",JOptionPane.ERROR_MESSAGE);
        }
        
    }

    //ショップ画面のメソッド
    private void openShopScreen() {
        //１．ショップパネルの作成
        JPanel shopPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //背景画像の描画
                ImageIcon backgroundIcon = new ImageIcon("ターン制コマンドバトルの街.jpg");
                Image backgroundImage = backgroundIcon.getImage();
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                //背景の上に半透明の黒いオーバーレイを描画
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, 100)); // 半透明の黒
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        //２．タイトル
        JLabel titleLabel = new JLabel("ショップ(SHOP)");
        titleLabel.setFont(new Font("MS ゴシック", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(30,30,220,40);
        shopPanel.add(titleLabel);

        //３．所持金
        coinLabel = new JLabel("所持コイン：" + gold + "コイン");
        coinLabel.setFont(new Font("MSゴシック", Font.BOLD,24));
        coinLabel.setForeground(Color.YELLOW);
        coinLabel.setBounds(260,30,300,40);
        shopPanel.add(coinLabel);

        //４．マップに戻るボタン
        JButton backButton = new JButton("マップに戻る");
        backButton.setFont(new Font("MSゴシック",Font.BOLD,18));
        backButton.setBackground(Color.BLUE);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBounds(1080,30,160,40);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel,"MAP");
                updateDisplay();
            }
        });
        shopPanel.add(backButton);

        // 商品棚の棚板（3段分）
        int[] shelfY = {260, 450, 640};
        for (int y : shelfY) {
            JPanel shelf = new JPanel();
            shelf.setBackground(new Color(120, 60, 20));
            shelf.setBounds(50, y, 880, 12);
            shopPanel.add(shelf);
        }

        //アイテムの生成
        // ★ DBからショップの商品一覧を読み込む
        List<Item> shopItems = GameMasterManager.loadShopItems();

        // 3段×5列（最大15個）で商品ボタンを自動配置する
        int[] itemY = {150, 340, 530}; // 各棚板の少し上に配置
        for (int i = 0; i < shopItems.size() && i < 15; i++) {
            Item currentItem = shopItems.get(i);
            int col = i % 5; // 0〜4列目
            int row = i / 5; // 0〜2段目

            int x = 60 + (col * 170);
            int y = itemY[row];

            JButton itemBtn = createItemButton(currentItem.getImagePath(), String.valueOf(currentItem.getPrice()));
            itemBtn.setBounds(x, y, 140, 100);
            itemBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showItemDetailDialog(currentItem);
                }
            });
            shopPanel.add(itemBtn);
        }

        //店員
        JLabel teninLabel = new JLabel(new ImageIcon("job_tenin_man.png"));
        teninLabel.setBounds(970,100,250,430);
        shopPanel.add(teninLabel);

        //店員のセリフ
        JLabel dialogLabel = new JLabel("いらっしゃいませ！",JLabel.CENTER);
        dialogLabel.setFont(new Font("MSゴシック",Font.BOLD,16));
        dialogLabel.setOpaque(true);
        dialogLabel.setBackground(Color.WHITE);
        dialogLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        dialogLabel.setBounds(1040,550,180,40);
        shopPanel.add(dialogLabel);

        //売るボタン
        JButton sellButton = new JButton("売る(SELL)");
        sellButton.setFont(new Font("MSゴシック",Font.BOLD,18));
        sellButton.setBackground(Color.WHITE);
        sellButton.setBounds(1080,600,140,45);
        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //売る画面に切り替える
                logTextArea.append("売る画面に切り替えます\n");
            }
        });
        shopPanel.add(sellButton);

        mainPanel.add(shopPanel, "SHOP");
        cardLayout.show(mainPanel, "SHOP");
    }

    //ショップ画面でクリックしたときにアイテムの説明を表示するメソッド
    private void showItemDetailDialog(Item item) {
        //1.ダイアログの設定
        JDialog dialog = new JDialog((Frame) null, "アイテム確認", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this); // 画面中央に表示
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(Color.WHITE);//背景色を白にする
        
        //2.アイテムの画像
        JLabel imageLabel = new JLabel();
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            ImageIcon originalIcon = new ImageIcon(item.getImagePath());
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }
        imageLabel.setBounds(20, 20, 100, 100);
        dialog.add(imageLabel);

        //3.アイテムの名前
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("MS ゴシック", Font.BOLD, 20));
        nameLabel.setBounds(140, 20, 200, 30);
        dialog.add(nameLabel);

        //4.「効果」見出し
        JLabel effectLabel = new JLabel("効果");
        effectLabel.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        effectLabel.setBounds(140, 60, 200, 30);
        dialog.add(effectLabel);

        //5.アイテムの効果の説明
        String effectText = "";
        if (item instanceof HealItem) {
            effectText = "HPを" + ((HealItem) item).getValue() + "回復する";
        } else if (item instanceof EscapeItem) {
            effectText = "戦闘から逃げる";
        } else if (item instanceof EquipmentItem) {
            effectText = "効果を発動する";
        }
        JLabel descriptionLabel = new JLabel("<html>" + effectText + "</html>");
        descriptionLabel.setFont(new Font("MS ゴシック", Font.PLAIN, 14));
        descriptionLabel.setBounds(140, 90, 220, 100);
        dialog.add(descriptionLabel);
        
        //6.赤色の買うボタン
        JButton buyButton = new JButton("買う");
        buyButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        buyButton.setBackground(Color.RED);
        buyButton.setForeground(Color.WHITE);
        buyButton.setBounds(140, 200, 100, 30);
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buyItem(item);
                dialog.dispose(); // ダイアログを閉じる
            }
        });
        dialog.add(buyButton);

        //7.青色の買わないボタン
        JButton closeButton = new JButton("買わない");
        closeButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        closeButton.setBackground(Color.BLUE);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBounds(250, 200, 100, 30);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose(); // ダイアログを閉じる
            }
        });
        dialog.add(closeButton);

        //8.ダイアログを表示
        dialog.setVisible(true);
    }

    //アイテムボタンを作るメソッド
    private JButton createItemButton(String imagePath, String price) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());

        //アイテム画像の設定
        ImageIcon rawIcon = new ImageIcon(imagePath);
        Image scaledImage = rawIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);//画像を80x80に縮小
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage), JLabel.CENTER);
        button.add(imageLabel, BorderLayout.CENTER);

        //価格ラベルの設定
        JLabel priceLabel = new JLabel(price + "コイン", JLabel.CENTER);
        priceLabel.setFont(new Font("MSゴシック", Font.BOLD, 14));
        priceLabel.setOpaque(true);
        priceLabel.setBackground(Color.WHITE);
        button.add(priceLabel, BorderLayout.SOUTH);
        
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    //クリックしたマップごとの処理
    public void startFieldBattle(String fieldType) {
        this.currentField = fieldType;

        if (fieldType.equals("BOSS")) {
            //ボス戦の処理
            this.currentround = 1; // ラウンド数をリセット
            backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのボス.jpg"));
        } else if (fieldType.equals("FOREST")) {
            //通常戦の処理
            this.currentround = 1; // ラウンド数をリセット
            backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルの森.jpg"));
        } else if (fieldType.equals("DUNGEON")) {
            //ダンジョン戦の処理
            this.currentround = 1; // ラウンド数をリセット
            backgroundLabel.setIcon(new ImageIcon("ターン制コマンドバトルのダンジョン.jpg"));
        }

        spawnEnemy();
        enemyIcon();
        updateDisplay();
        cardLayout.show(mainPanel, "BATTLE");
    } 

    //ショップでアイテムを買うメソッド
    private void buyItem(Item item) {
        //所持金が足りるかチェック
        if (gold < item.getPrice()) {
            logTextArea.append("所持金が足りません！\n");
            return;
        }

        //アイテムボックスに空きがあるかチェック
        if (addItemToBox(item)) {
            gold -= item.getPrice();
            coinLabel.setText("所持コイン：" + gold + "コイン");
            logTextArea.append(item.getName() + " を購入しました！\n");
            updateDisplay();
        } else {
            logTextArea.append("アイテムボックスがいっぱいです！ " + item.getName() + " は購入できませんでした。\n");
        }

    }

    //アイテムボックスの中身を表示するメソッド
    private void showItemBox() {
        //1.ダイアログの設定
        JDialog dialog = new JDialog((Frame) null, "アイテムボックス", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this); // 画面中央に表示するコード
        dialog.setLayout(null);//ボタンや文字の配置を自由に決めるようにするコード
        dialog.getContentPane().setBackground(Color.WHITE);//背景色を白にするコード

        //2.タイトルラベル
        JLabel titleLabel = new JLabel("アイテムボックス", JLabel.CENTER);
        titleLabel.setFont(new Font("MS ゴシック", Font.BOLD, 24));
        titleLabel.setBounds(0, 10, 600, 30);
        dialog.add(titleLabel);

        //3.アイテムボックスの中身を表示するパネル
        JPanel itemBoxPanel = new JPanel();
        itemBoxPanel.setLayout(new GridLayout(2, 5, 10, 10)); // 2行5列のグリッドレイアウト
        itemBoxPanel.setBounds(20, 65, 540, 200);
        itemBoxPanel.setBackground(Color.WHITE);

        int maxSlots = getMaxBoxSize();
        int rows = (maxSlots > 10) ? 3 : 2;
        itemBoxPanel.setLayout(new GridLayout(rows, 5, 10, 10));

        //4.アイテムボックスの中身をループで表示
        for (int i = 0; i < maxSlots; i++) {
            Item item = itembox[i];
            JButton itemButton;
            if (item != null) {
                itemButton = createItemButton(item.getImagePath(), String.valueOf(item.getPrice()));
                int index = i; // ローカル変数にコピーして、ActionListener内で使用
                itemButton.addActionListener(e -> {
                    showUseItemDialog(item, index, dialog); // アイテム使用ダイアログを表示
                });
            } else {
                itemButton = new JButton("");
                itemButton.setFont(new Font("MSゴシック", Font.PLAIN, 14));
                itemButton.setBackground(Color.LIGHT_GRAY);
            }
            itemBoxPanel.add(itemButton);
        }

        dialog.add(itemBoxPanel);

        //5.閉じるボタン
        JButton closeButton = new JButton("閉じる");
        closeButton.setFont(new Font("MSゴシック", Font.BOLD, 16));
        closeButton.setBackground(Color.BLUE);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBounds(460, 15, 100, 30);
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.add(closeButton);

        //6.所持コインを表示
        JLabel coinLabel = new JLabel("所持コイン：" + gold + "コイン");
        coinLabel.setFont(new Font("MSゴシック", Font.BOLD, 16));
        coinLabel.setBounds(300, 325, 260, 30);
        dialog.add(coinLabel);

        //7.ダイアログを表示
        dialog.setVisible(true);
    }

    //戦闘中にアイテムを使用するか確認するダイアログを表示するメソッド
    private void showUseItemDialog(Item item, int itemIndex, JDialog parentDialog) {
        //1.ダイアログの設定
        JDialog dialog = new JDialog((Frame) null, "アイテム使用確認", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this); // 画面中央に表示するコード
        dialog.setLayout(null);//ボタンや文字の配置を自由に決めるようにするコード
        dialog.getContentPane().setBackground(Color.WHITE);//背景色を白にするコード

        //2.アイテムの名前と効果を表示するラベル
        JLabel itemNameLabel = new JLabel(item.getName(), JLabel.CENTER);
        itemNameLabel.setFont(new Font("MS ゴシック", Font.BOLD, 20));
        itemNameLabel.setBounds(0, 20, 400, 30);
        dialog.add(itemNameLabel);

        String effectText = "";
        if (item instanceof HealItem) {
            effectText = "HPを" + ((HealItem) item).getValue() + "回復する";
        } else if (item instanceof EscapeItem) {
            effectText = "戦闘から逃げる";
        } else if (item instanceof EquipmentItem) {
            effectText = "効果を発動する";
        }
        JLabel effectLabel = new JLabel(effectText, JLabel.CENTER);
        effectLabel.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        effectLabel.setBounds(0, 60, 400, 30);
        dialog.add(effectLabel);

        //3.使用ボタン
        JButton useButton = new JButton("使う");
        useButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        useButton.setBackground(Color.RED);
        useButton.setForeground(Color.WHITE);
        useButton.setBounds(80, 120, 100, 30);
        useButton.addActionListener(e -> {
            dialog.dispose(); // ダイアログを閉じる
            parentDialog.dispose(); // アイテムボックスダイアログも閉じる
            useItemDuringBattle(item, itemIndex); // 戦闘中にアイテムを使用する処理
            
        });
        dialog.add(useButton);

        //4.キャンセルボタン
        JButton cancelButton = new JButton("使わない");
        cancelButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        cancelButton.setBackground(Color.BLUE);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBounds(220, 120, 100, 30);
        cancelButton.addActionListener(e -> {
            dialog.dispose(); // ダイアログを閉じる
        });
        dialog.add(cancelButton);

        //5.ダイアログを表示
        dialog.setVisible(true);
    }

    //戦闘中にアイテムの処理のメソッド
    private void useItemDuringBattle(Item item, int itemIndex) {
        //現在のターンのプレイヤーの情報を取得
        Player targetPlayer = party.get(currentPlayerIndex);

        //1.回復アイテム
        if (item instanceof HealItem) {
            HealItem healItem = (HealItem) item;
            int healAmount = healItem.getValue();
            targetPlayer.hp += healAmount;
            if (targetPlayer.hp > targetPlayer.getMaxHp()) {
                targetPlayer.hp = targetPlayer.getMaxHp();
            }
            logTextArea.append(targetPlayer.getName() + " は " + healAmount + " 回復した！\n");
        } else if (item instanceof EscapeItem) {
            //2.逃げるアイテム
            EscapeItem escapeItem = (EscapeItem) item;
            int escapeChance = escapeItem.getEscapeChance();
            int randomValue = (int)(Math.random() * 100);
            if (randomValue < escapeChance) {
                logTextArea.append(targetPlayer.getName() + " は戦闘から逃げることに成功した！\n");
                // 戦闘から逃げる処理を追加
                cardLayout.show(mainPanel, "MAP");
                return;
            } else {
                logTextArea.append(targetPlayer.getName() + " は戦闘から逃げることに失敗した…\n");
            }
        } else if (item instanceof EquipmentItem) {
            //3.装備アイテム
            EquipmentItem equipmentItem = (EquipmentItem) item;
            // 装備の効果を発動する処理を追加
            logTextArea.append(targetPlayer.getName() + " は " + equipmentItem.getName() + " を装備した！\n");
        }

        // 使用したアイテムをアイテムボックスから削除
        itembox[itemIndex] = null; //アイテムボックスのスロットを空にする
        GameSaveManager.updateInventorySlot(currentSaveSlot, itemIndex + 1, null);
        updateDisplay();

        //ターンの処理
        int OldPlayerIndex = currentPlayerIndex;
        switchNextPlayer();

        if (!enemyParty.isEmpty() && currentPlayerIndex <= OldPlayerIndex) {
            // プレイヤーが交代しなかった場合、敵のターンを開始
            startEnemyTurn();
        }
    }

    //goldのsetterメソッド
    public static void setGold(int gold) {
        BattleGame.gold = gold;
    }

    //アイテムボックスのセッターメソッド
    public static void setItemBox(Item[] loadedBox) {
        itembox = loadedBox;
    }

    private int getMaxBoxSize() {
        // 将来 WisdomPouch(EQNO=4) を装備しているキャラがいれば 50 を返す
        return 10;
    }

    private boolean addItemToBox(Item item) {
        int maxSlots = getMaxBoxSize();
        for (int i = 0; i < maxSlots; i++) {
            if (itembox[i] == null) {
                itembox[i] = item;
                // DBの INVENTORY テーブルの該当枠 (i + 1) を即座に UPDATE
                GameSaveManager.updateInventorySlot(currentSaveSlot, i + 1, item);
                return true;
            }
        }
        logTextArea.append("アイテムボックスがいっぱいです！ " + item.getName() + " は拾えませんでした。\n");
        return false;
    }

}