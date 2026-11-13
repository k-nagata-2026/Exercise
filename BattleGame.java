import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;

public class BattleGame extends JFrame implements KeyListener {
    private JLabel statusLabel;
    private JTextArea logTextArea;


    private int loadedLvlTemp = -1;
    private int loadedGoldTemp = -1;

    private JLabel backgroundLabel;
    private JLabel playerImageLabel;
    private JLabel enemyImageLabel;

    private JLabel playerDamageLabel;
    private JLabel enemyDamageLabel;

    private JProgressBar playerHpBar;
    private JProgressBar playerMpBar;
    private JProgressBar enemyHpBar;

    private JButton bigGameClearButton;
    private JButton bigGameOverButton;

    private JButton attackButton;
    private JButton skillButton;
    private JButton defendButton;
    private JButton healButton;
    private JButton runButton;
    private JButton itemButton;

    private JButton nextStageButton;
    private JButton restartButton;

    private JPanel topShopPanel;
    private JLabel goldDisplayLabel;
    private JButton buyPotionButton;
    private JButton buySwordButton;
    private JLabel goldAnimLabel;

    private Item[] itemBox = new Item[10];

    // --- START SCREEN VARIABLES ---
    private JLabel startScreenLabel;
    private JPanel gamePlayPanel;
    private boolean isGameStarted = false;

    private Player player;
    private Enemy enemy;
    private int enemyCount = 1;
    private int retryCount = 0;
    private static int playerWins = 0;

    private int itemUsesLeft = 3;
    private boolean isPlayerDefending = false;
    private int comboCount = 0;

    private static boolean isWizardUnlocked = false;
    private static boolean isHumanUnlocked = false;

    private static int heroLevel = 1;
    private static int wizardLevel = 1;
    private static int humanLevel = 1;
    private String currentSelectedType = "hero";
    private int currentSaveId = 0; // 0 = new game, 1以上 = ロードしたセーブID

    // --- SHOP LOGIC VARIABLES ---
    private int playerGold = 0;
    private int attackBuffCount = 0;

    private int playerX = 80;
    private final int playerY = 150;
    private int enemyX = 700;
    private final int enemyY = 150;

    private boolean moveLeft = false;
    private boolean moveRight = false;
    private final int SPEED = 14;

    private boolean isAttacking = false;
    private int enemyOriginalX = 700;
    private int shakeFrame = 0;
    private boolean isGameOverState = false;

    private final int WINDOW_WIDTH = 1220;
    private final int WINDOW_HEIGHT = 1050;

    private Clip bgmClip;





public BattleGame() {
        // 1. Start Screen Label Setup
        startScreenLabel = new JLabel("クリックしてスタート / ロード", SwingConstants.CENTER);

        // 2. ★ Start Screen Click Listener (DB Data Load & Display Refresh) ★
        startScreenLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    List<Integer> idList = GameSaveManager.getSaveIdList();

                    List<Object> optionsList = new ArrayList<>();
                    optionsList.add("0: 新しいゲームで始める (New Game)");

                    if (idList != null && !idList.isEmpty()) {
                        for (int id : idList) {
                            optionsList.add(id); // 
                        }
                    }

                    Object[] idOptions = optionsList.toArray();

                    Object loadSelected = JOptionPane.showInputDialog(
                            BattleGame.this,
                            "ゲームの始め方を選択してください（セーブID選択）：",
                            "ゲーム開始 / ロード (Start & Load)",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            idOptions,
                            idOptions[idOptions.length - 1]
                    );

                    if (loadSelected == null) return; 

                    int savedLvl = 1;


                    if (loadSelected instanceof Integer) {
                        int chosenId = (Integer) loadSelected;
                        int[] loadedData = GameSaveManager.loadGameDataById(chosenId);

                        if (loadedData != null && loadedData[0] > 0) {
                            int plyrNo = loadedData[0];       // 1: hero, 2: wizard, 3: human
                            savedLvl = loadedData[1];         // PLAYER_LVL
                            int savedBalance = loadedData[2];   // BALANCE

                            // Gold Assign
                            playerGold = savedBalance;

                            if (plyrNo == 1) {
                                heroLevel = savedLvl;
                                currentSelectedType = "hero";
                            } else if (plyrNo == 2) {
                                wizardLevel = savedLvl;
                                currentSelectedType = "wizard";
                            } else {
                                humanLevel = savedLvl;
                                currentSelectedType = "human";
                            }

                            JOptionPane.showMessageDialog(BattleGame.this, 
                                "セーブID " + chosenId + " のデータをロードしました！\n" +
                                "Level: " + savedLvl + " | Gold: " + savedBalance + "G から再開します。");
                        }
                    } else {
                        // New Game (Lv.1)
                        savedLvl = 1;
                        playerGold = 0;
                        JOptionPane.showMessageDialog(BattleGame.this, "新しいゲームで開始します！");
                    }

                    int maxHp = 100 + (savedLvl - 1) * 20;
                    int atk = 20 + (savedLvl - 1) * 5;
                    int def = 5 + (savedLvl - 1) * 2;

                    // Player Object initialize गर्ने
                    player = new Player("勇者", maxHp, atk, def, 10, "img/hero.png");
                    player.setLevel(savedLvl);
                    player.setHp(maxHp);

                    isGameStarted = true;
                    
                    if (startScreenLabel != null) {
                        startScreenLabel.setVisible(false);
                    }
                    if (gamePlayPanel != null) {
                        gamePlayPanel.setVisible(true);
                    }

                    updateGameScreenUI();
                    
                    revalidate();
                    repaint();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(BattleGame.this, "ロードエラー: " + ex.getMessage());
                }
            }
        });





        // 3. Frame Configuration & Items setup
        setTitle("本格RPG - ハイブリッド・アンロックバトルシステム + Visual FX");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        itemBox[0] = new Bomb();
        itemBox[1] = new Herb();
        itemBox[2] = new mp();
        itemBox[3] = new Shield();

        // ==========================================
        // 1. START SCREEN process (Rule -> DB Load Dialog -> Screen Switch)
        // ==========================================
        JPanel mainStartPanel = new JPanel(new GridBagLayout());
        mainStartPanel.setBackground(Color.decode("#1a1c23"));

        startScreenLabel = new JLabel();
        startScreenLabel.setLayout(null);

        int imgWidth = 570;
        int imgHeight = 970;

        try {
            ImageIcon startIcon = new ImageIcon("start.png");
            Image scaledStart = startIcon.getImage().getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
            startScreenLabel.setIcon(new ImageIcon(scaledStart));
            startScreenLabel.setPreferredSize(new Dimension(imgWidth, imgHeight));
            startScreenLabel.setSize(imgWidth, imgHeight);
        } catch (Exception e) {
            System.out.println("start.png not found");
        }

        // ⭕️ START ボタン領域用 (透明 JButton)
        JButton invisibleStartBtn = new JButton();
        invisibleStartBtn.setBounds(40, 740, 490, 80);
        invisibleStartBtn.setOpaque(false);
        invisibleStartBtn.setContentAreaFilled(false);
        invisibleStartBtn.setBorderPainted(false);
        invisibleStartBtn.setFocusPainted(false);

        invisibleStartBtn.addActionListener(e -> {
            System.out.println("[DEBUG] START BUTTON CLICKED!");
            isGameStarted = true;
            try {
                // 1. ルール説明を表示
                showRulesDialog();

                // 2. Oracle DB बाट Save ID तान्ने
                List<Integer> idList = GameSaveManager.getSaveIdList();
                List<Object> optionsList = new ArrayList<>();
                optionsList.add("0: 新しいゲームで始める (New Game)");

                if (idList != null && !idList.isEmpty()) {
                    for (int id : idList) {
                        optionsList.add(id);
                    }
                }

                Object[] idOptions = optionsList.toArray();

                // 3. Save ID Select Dialog
                Object loadSelected = JOptionPane.showInputDialog(
                        BattleGame.this,
                        "ゲームの始め方を選択してください（セーブID選択）：",
                        "ゲーム開始 / ロード (Start & Load)",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        idOptions,
                        idOptions[idOptions.length - 1]
                );

                if (loadSelected == null) {
                    isGameStarted = false;
                    return;
                }

                if (loadSelected instanceof Integer) {
                    // --- 続きから始める (Load Game) ---
                    int chosenId = (Integer) loadSelected;
                    currentSaveId = chosenId;
                    int[] loadedData = GameSaveManager.loadGameDataById(chosenId);

                    if (loadedData != null && loadedData[0] > 0) {
                        int plyrNo = loadedData[0];
                        int savedLvl = loadedData[1];
                        int savedBalance = loadedData[2];

                        playerGold = savedBalance;

                        if (plyrNo == 1) {
                            heroLevel = savedLvl;
                            currentSelectedType = "hero";
                        } else if (plyrNo == 2) {
                            wizardLevel = savedLvl;
                            isWizardUnlocked = true;
                            currentSelectedType = "wizard";
                        } else {
                            humanLevel = savedLvl;
                            isWizardUnlocked = true;
                            isHumanUnlocked = true;
                            currentSelectedType = "human";
                        }

                        // セーブされたキャラクター（hero / wizard / human）でプレイヤーを作る
                        createLoadedPlayer();

                        initGameSession(false);

                        // セーブされたアイテムを復元する
                        restoreItemsFromSave(chosenId);

                        JOptionPane.showMessageDialog(BattleGame.this, 
                            "セーブID " + chosenId + " のデータをロードしました！\n" +
                            "Level: " + savedLvl + " | Gold: " + savedBalance + "G から再開します。");
                    }
                } else {
                    // --- 新しく始める (New Game -> Character Select) ---
                    currentSaveId = 0;
                    choicePlayer();
                    initGameSession(false);
                }

            } catch (Exception ex) {
                System.out.println("Init error: " + ex.getMessage());
                ex.printStackTrace();
            }

            // 確実な画面遷移処理
            LayoutManager layout = getContentPane().getLayout();
            if (layout instanceof CardLayout) {
                CardLayout cl = (CardLayout) layout;
                cl.show(getContentPane(), "GAMEPLAY");
            }
            revalidate();
            repaint();
            BattleGame.this.requestFocusInWindow();
        });

        // ⭕️ EXIT ボタン領域用 (透明 JButton)
        JButton invisibleExitBtn = new JButton();
        invisibleExitBtn.setBounds(40, 850, 490, 80);
        invisibleExitBtn.setOpaque(false);
        invisibleExitBtn.setContentAreaFilled(false);
        invisibleExitBtn.setBorderPainted(false);
        invisibleExitBtn.setFocusPainted(false);

        invisibleExitBtn.addActionListener(e -> {
            System.out.println("[DEBUG] EXIT BUTTON CLICKED!");
            System.exit(0);
        });

        // 1. ラベルにボタンを追加
        startScreenLabel.add(invisibleStartBtn);
        startScreenLabel.add(invisibleExitBtn);

        // 2. メインパネルにラベルを追加
        mainStartPanel.add(startScreenLabel);

        // 3. 全体ウィンドウ (CardLayout) に START 画面を追加登録
        getContentPane().add(mainStartPanel, "START");



        

        // ==========================================
        // २. GAMEPLAY PANEL
        // ==========================================
        gamePlayPanel = new JPanel(new BorderLayout());

        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(null);

        setBackgroundImage("fristbackgroud.png");

        // BUTTON PANEL SETTING
        topShopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        topShopPanel.setBackground(new Color(0, 0, 0, 150));
        topShopPanel.setBounds(0, 0, WINDOW_WIDTH, 50);

        goldDisplayLabel = new JLabel("💰 Gold: 0 G");
        goldDisplayLabel.setFont(new Font("MS ゴシック", Font.BOLD, 18));
        goldDisplayLabel.setForeground(Color.YELLOW);

        buyPotionButton = new JButton("💚 ポーション (+1回) : 50G");
        buyPotionButton.setBackground(Color.decode("#228B22"));
        buyPotionButton.setForeground(Color.WHITE);
        buyPotionButton.setFocusable(false);
        buyPotionButton.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        buySwordButton = new JButton("⚔️ 伝説の剣 (ATK +15) : 100G");
        buySwordButton.setBackground(Color.decode("#B22222"));
        buySwordButton.setForeground(Color.WHITE);
        buySwordButton.setFocusable(false);
        buySwordButton.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        buyPotionButton.addActionListener(e -> {
            if (playerGold >= 50) {
                playerGold -= 50;
                itemUsesLeft++;
                healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")");
                playSE("hp.wav");
                logTextArea.append("🛒 [SHOP] ポーションを購入しました！(残り: " + itemUsesLeft + "回)\n");
                updateDisplay();
            } else {
                logTextArea.append("❌ [SHOP] ゴールドが足りません！\n");
            }
        });

        buySwordButton.addActionListener(e -> {
            if (playerGold >= 100) {
                playerGold -= 100;
                attackBuffCount++;
                playSE("hp.wav");
                logTextArea.append("🛒 [SHOP] 伝説の剣を購入！攻撃力 +15 (合計バフ: +" + (attackBuffCount * 15) + ")\n");
                updateDisplay();
            } else {
                logTextArea.append("❌ [SHOP] ゴールドが足りません！\n");
            }
        });

        topShopPanel.add(goldDisplayLabel);
        topShopPanel.add(buyPotionButton);
        topShopPanel.add(buySwordButton);

        backgroundLabel.add(topShopPanel);

        // 💰 GOLD COLLECT ANIMATION LABEL
        goldAnimLabel = new JLabel("", JLabel.CENTER);
        goldAnimLabel.setFont(new Font("Arial", Font.BOLD, 36));
        goldAnimLabel.setForeground(Color.YELLOW);
        goldAnimLabel.setBounds((WINDOW_WIDTH - 300) / 2, 70, 300, 50);
        backgroundLabel.add(goldAnimLabel);

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        playerImageLabel.setBounds(playerX, playerY, 500, 500);
        enemyImageLabel.setBounds(enemyX, enemyY, 500, 500);

        playerDamageLabel = new JLabel("", JLabel.CENTER);
        playerDamageLabel.setFont(new Font("Arial", Font.BOLD, 42));
        playerDamageLabel.setForeground(Color.RED);
        playerDamageLabel.setBounds(playerX + 150, playerY - 40, 200, 50);

        enemyDamageLabel = new JLabel("", JLabel.CENTER);
        enemyDamageLabel.setFont(new Font("Arial", Font.BOLD, 42));
        enemyDamageLabel.setForeground(Color.ORANGE);
        enemyDamageLabel.setBounds(enemyX + 150, enemyY - 40, 200, 50);

        backgroundLabel.add(playerDamageLabel);
        backgroundLabel.add(enemyDamageLabel);

        try {
            ImageIcon clearIcon = new ImageIcon("game clear.png");
            switchBGM("game win.wav");
            Image scaledClear = clearIcon.getImage().getScaledInstance(800, 350, Image.SCALE_SMOOTH);
            bigGameClearButton = new JButton(new ImageIcon(scaledClear));
        } catch (Exception e) {
            bigGameClearButton = new JButton("🎉 STAGE CLEAR");
        }
        bigGameClearButton.setBounds((WINDOW_WIDTH - 800) / 2, 300, 800, 350);
        bigGameClearButton.setBorderPainted(false);
        bigGameClearButton.setContentAreaFilled(false);
        bigGameClearButton.setFocusable(false);
        bigGameClearButton.setVisible(false);

        try {
            ImageIcon overIcon = new ImageIcon("game over.png");
            switchBGM("game lose.wav");
            Image scaledOver = overIcon.getImage().getScaledInstance(800, 350, Image.SCALE_SMOOTH);
            bigGameOverButton = new JButton(new ImageIcon(scaledOver));
        } catch (Exception e) {
            bigGameOverButton = new JButton("💀 GAME OVER");
        }
        bigGameOverButton.setBounds((WINDOW_WIDTH - 800) / 2, 300, 800, 350);
        bigGameOverButton.setBorderPainted(false);
        bigGameOverButton.setContentAreaFilled(false);
        bigGameOverButton.setFocusable(false);
        bigGameOverButton.setVisible(false);

        backgroundLabel.add(bigGameClearButton);
        bigGameOverButton.addActionListener(e -> resetToTitle());
        backgroundLabel.add(bigGameOverButton);
        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("【操作】 SPACE : 攻撃 | S : スキル | D : 防御 | H : 回復 | I : アイテム| R : 逃げる", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JPanel barsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        barsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        playerHpBar = new JProgressBar(0, 100);
        playerHpBar.setStringPainted(true);
        playerHpBar.setForeground(new Color(34, 139, 34));
        playerHpBar.setBackground(Color.RED);

        playerMpBar = new JProgressBar(0, 100);
        playerMpBar.setStringPainted(true);
        playerMpBar.setForeground(new Color(0, 191, 255));
        playerMpBar.setBackground(Color.DARK_GRAY);

        enemyHpBar = new JProgressBar(0, 100);
        enemyHpBar.setStringPainted(true);
        enemyHpBar.setForeground(new Color(148, 0, 211));
        enemyHpBar.setBackground(Color.RED);

        barsPanel.add(new JLabel("プレイヤー HP:", JLabel.RIGHT));
        barsPanel.add(playerHpBar);
        barsPanel.add(new JLabel("プレイヤー MP:", JLabel.RIGHT));
        barsPanel.add(playerMpBar);
        barsPanel.add(new JLabel("敵 HP:", JLabel.RIGHT));
        barsPanel.add(enemyHpBar);

        logTextArea = new JTextArea(10, 30);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("MS ゴシック", Font.PLAIN, 15));
        logTextArea.setBackground(Color.BLACK);
        logTextArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 10, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        attackButton = new JButton("⚔️ 攻撃");
        attackButton.setBackground(Color.decode("#A30000"));
        attackButton.setForeground(Color.WHITE);

        skillButton = new JButton("✨ スキル (MP 40)");
        skillButton.setBackground(Color.decode("#8A2BE2"));
        skillButton.setForeground(Color.WHITE);

        defendButton = new JButton("🛡️ 防御");
        defendButton.setBackground(Color.decode("#004080"));
        defendButton.setForeground(Color.WHITE);

        healButton = new JButton("💚 回復 (残り: " + itemUsesLeft + ")");
        healButton.setBackground(Color.decode("#22d222"));
        healButton.setForeground(Color.WHITE);

        itemButton = new JButton("Item");

        runButton = new JButton("🏃 逃げる");
        runButton.setBackground(Color.decode("#5A5A5A"));
        runButton.setForeground(Color.WHITE);

        nextStageButton = new JButton("⏭️ 次へ");
        nextStageButton.setBackground(Color.decode("#4d2395"));
        nextStageButton.setForeground(Color.WHITE);
        nextStageButton.setVisible(false);

        restartButton = new JButton("🔄 キャラ選択へ");
        restartButton.setBackground(Color.decode("#3f2f4f"));
        restartButton.setForeground(Color.WHITE);
        restartButton.setVisible(false);

        JButton saveButton = new JButton("💾 セーブ");
        saveButton.setBackground(Color.decode("#4f2f36"));
        saveButton.setForeground(Color.GREEN);
        saveButton.setVisible(true);


        JButton[] allButtons = {attackButton, skillButton, defendButton, healButton, runButton, nextStageButton, restartButton};
        Font btnFont = new Font("MS ゴシック", Font.BOLD, 16); 

        for (JButton btn : allButtons) {
            btn.setFont(btnFont);
            btn.setFocusPainted(false);
            btn.setFocusable(false);
        }

        attackButton.addActionListener(e -> handleAttackAction());
        skillButton.addActionListener(e -> handleSkillAction());
        defendButton.addActionListener(e -> handleDefendAction());
        healButton.addActionListener(e -> useHealItem());
        runButton.addActionListener(e -> triggerRunDialog());
        nextStageButton.addActionListener(e -> goToNextStage());
        restartButton.addActionListener(e -> resetToTitle());
        saveButton.addActionListener(e -> saveButton());

        bigGameClearButton.addActionListener(e -> {
            if (enemyCount >= 4)
                resetToTitle();
            else
                goToNextStage();
        });

        itemButton.addActionListener(e -> {

            // 1. ダイアログに表示（ひょうじ）するための「選択肢（せんたくし）リスト（文字列（もじれつ））」を作る（つくる）
            String[] choices = new String[10];
            for (int i = 0; i < itemBox.length; i++) {
                if (itemBox[i] != null) {
                    choices[i] = "スロット " + (i + 1) + " : " + itemBox[i].getName();
                } else {
                    choices[i] = "スロット " + (i + 1) + " : (からっぽ)";
                }
            }

            // 2. Swingの便利（べんり）なインプットダイアログを表示（ひょうじ）（プルダウン形式（けいしき））
            Object selected = JOptionPane.showInputDialog(
                    BattleGame.this,
                    "使用するアイテムを選択してください",
                    "アイテムボックス (最大10個)",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    choices, // 10個（こ）のスロットの文字（もじ）配列（はいれつ）
                    choices[0] // 最初（さいしょ）から選択（せんたく）されている項目（こうもく）
            );

            // キャンセルされた（×ボタンやCancel）場合（ばあい）は処理（しょり）を中断（ちゅうだん）する
            if (selected == null) {
                return;
            }

            // 3. 選択（せんたく）された項目（こうもく）が「何（なん）番（ばん）目（め）のスロットか」を特定（とくてい）する
            int selectedIndex = -1;
            for (int i = 0; i < choices.length; i++) {
                if (choices[i].equals(selected)) {
                    selectedIndex = i;
                    break;
                }
            }

            // 4. そのスロットの中身（なかみ）が「からっぽ（null）」じゃないかチェック
            Item selectedItem = itemBox[selectedIndex];
            if (selectedItem == null) {
                logTextArea.append("そのスロットはからっぽです！\n");
                return;
            }

            // 5. アイテムを使用（しよう）し、使（つか）ったスロットをnullにして消（け）す
            String resultLog = selectedItem.use(player, enemy);
            itemBox[selectedIndex] = null; // ★ 使（つか）ったら消（け）える！
            logTextArea.append(resultLog + "\n");

            // 6. 敵（てき）が倒（たお）れたかチェック
            if (enemy.getHp() <= 0) {
                logTextArea.append(enemy.getName() + " を倒した！\n");
                // ★ 10%の確率（かくりつ）でアイテムドロップ
                if (Math.random() < 0.1) {
                    addItemToBox(new Herb());
                    logTextArea.append("★ " + enemy.getName() + " が薬草を落とした！\n");
                }
                spawnEnemy();
            }
            updateDisplay();
        });
        buttonPanel.add(attackButton);
        buttonPanel.add(skillButton);
        buttonPanel.add(defendButton);
        buttonPanel.add(healButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(runButton);
        buttonPanel.add(nextStageButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(saveButton);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(barsPanel, BorderLayout.CENTER);

        JPanel logAndButtonPanel = new JPanel(new BorderLayout());
        logAndButtonPanel.add(scrollPane, BorderLayout.CENTER);
        logAndButtonPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.add(logAndButtonPanel, BorderLayout.SOUTH);

        gamePlayPanel.add(backgroundLabel, BorderLayout.CENTER);
        gamePlayPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainStartPanel, "START_SCREEN");
        add(gamePlayPanel, "GAMEPLAY");

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        Timer gameLoop = new Timer(16, e -> {
            if (isGameStarted) {
                updateMovement();
                updateAnimation();
            }
        });
        gameLoop.start();

        playBGM("backgroundmusic.wav");
    }

    private void playBGM(String fileName) {
        try {
            File soundFile = new File(fileName);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioStream);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        } catch (Exception e) {
            System.out.println("Error playing BGM: " + e.getMessage());
        }
    }

    private void playSE(String fileName) {
        try {
            File soundFile = new File(fileName);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip seClip = AudioSystem.getClip();
                seClip.open(audioStream);
                seClip.start();
            }
        } catch (Exception e) {
            System.out.println("Error playing SE: " + e.getMessage());
        }
    }

    private void showDamageText(JLabel label, String text) {
        label.setText(text);
        Timer timer = new Timer(800, e -> label.setText(""));
        timer.setRepeats(false);
        timer.start();
    }

    private void setBackgroundImage(String fileName) {
        try {
            ImageIcon bgIcon = new ImageIcon(fileName);
            Image scaledBg = bgIcon.getImage().getScaledInstance(WINDOW_WIDTH, WINDOW_HEIGHT - 250, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(scaledBg));
        } catch (Exception e) {
            System.out.println("Background image load failed: " + fileName);
        }
    }

    private void showRulesDialog() {
        String rulesJa = "【ゲームのルールと操作方法】\n\n"
                + "1. ターン制のバトルシステムです。\n"
                + "2. 攻撃(SPACE)を続けるとコンボが繋がり、大ダメージ！\n"
                + "3. スキル(S)はMPを40消費して2.5倍の大ダメージ！\n"
                + "4. 防御(D)をすると敵のダメージを減らし、MPが回復します。\n"
                + "5. 回復(H)はHPを100回復しますが、回数制限(3回)があります。\n"
                + "6. キャラクターのレベルが上がると能力が強化され、新しい英雄がアンロックされます！\n"
                + "※ 敵もプレイヤーのレベルに合わせて強力になり、HP減少で暴走(RAGE)します！";

        String rulesEn = "【Game Rules & Controls】\n\n"
                + "1. It is a turn-based battle system.\n"
                + "2. Attack (SPACE) continuously to build up combos and deal massive damage!\n"
                + "3. Skill (S) consumes 40 MP to deal 2.5x massive damage!\n"
                + "4. Defense (D) reduces incoming enemy damage and restores MP.\n"
                + "5. Heal (H) restores 100 HP, but it has a limited number of uses (3 times).\n"
                + "6. Leveling up your character enhances stats and unlocks new heroes!\n"
                + "* Note: Enemies also scale with player level and enter RAGE mode when HP drops low!";

        // Custom Panel setup
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));

        // Text Area
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(rulesJa, 10, 35);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        panel.add(new javax.swing.JScrollPane(textArea), java.awt.BorderLayout.CENTER);

        // Language Toggle Checkbox
        javax.swing.JCheckBox langCheckBox = new javax.swing.JCheckBox("English で表示 / Show in English");
        langCheckBox.addActionListener(e -> {
            if (langCheckBox.isSelected()) {
                textArea.setText(rulesEn);
            } else {
                textArea.setText(rulesJa);
            }
        });
        panel.add(langCheckBox, java.awt.BorderLayout.SOUTH);

        // Show Dialog Box
        JOptionPane.showMessageDialog(this, panel, "📢 遊び方ガイド / How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateMovement() {
        if (isGameOverState)
            return;
        if (moveLeft && playerX > 10)
            playerX -= SPEED;
        if (moveRight && playerX < 600)
            playerX += SPEED;
        playerImageLabel.setLocation(playerX, playerY);
    }

    private void updateAnimation() {
        if (shakeFrame > 0) {
            shakeFrame--;
            if (shakeFrame % 2 == 0) {
                enemyImageLabel.setLocation(enemyOriginalX + 15, enemyY);
            } else {
                enemyImageLabel.setLocation(enemyOriginalX - 15, enemyY);
            }
            if (shakeFrame == 0)
                enemyImageLabel.setLocation(enemyOriginalX, enemyY);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isGameStarted)
            return;

        if (isGameOverState) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && nextStageButton.isVisible())
                goToNextStage();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && restartButton.isVisible())
                resetToTitle();
            return;
        }

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)
            moveLeft = true;
        if (key == KeyEvent.VK_RIGHT)
            moveRight = true;

        if (key == KeyEvent.VK_SPACE)
            handleAttackAction();
        if (key == KeyEvent.VK_S)
            handleSkillAction();
        if (key == KeyEvent.VK_D)
            handleDefendAction();
        if (key == KeyEvent.VK_H)
            useHealItem();
        if (key == KeyEvent.VK_R)
            triggerRunDialog();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)
            moveLeft = false;
        if (key == KeyEvent.VK_RIGHT)
            moveRight = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void handleAttackAction() {
        if (isGameOverState || isAttacking)
            return;
        animateAttackAndExecute(false);
    }

    private void handleSkillAction() {
        if (isGameOverState || isAttacking)
            return;
        if (player.getMp() < 40) {
            logTextArea.append("⚠ MPが足りません！ (必要MP: 40)\n");
            return;
        }
        player.setMp(player.getMp() - 40);
        animateAttackAndExecute(true);
    }

    private void animateAttackAndExecute(boolean isSkill) {
        isAttacking = true;
        int originalX = playerX;

        Timer forwardTimer = new Timer(10, null);
        forwardTimer.addActionListener(e -> {
            playerX += 25;
            if (playerX >= originalX + 150) {
                ((Timer) e.getSource()).stop();

                executePlayerTurn(isSkill);

                Timer backwardTimer = new Timer(10, null);
                backwardTimer.addActionListener(ev -> {
                    playerX -= 15;
                    if (playerX <= originalX) {
                        playerX = originalX;
                        ((Timer) ev.getSource()).stop();
                        isAttacking = false;
                    }
                });
                backwardTimer.start();
            }
        });
        forwardTimer.start();
    }

    private void handleDefendAction() {
        if (isGameOverState)
            return;
        isPlayerDefending = true;
        int mpRecovery = player.getName().contains("wizard") && player.getLevel() >= 5 ? 35 : 20;
        player.setMp(Math.min(100, player.getMp() + mpRecovery));
        logTextArea.append("🛡️ " + player.getName() + " は身を護っている！ (MP+" + mpRecovery + ")\n");
        comboCount = 0;
        updateDisplay();
        executeEnemyTurn();
    }

    private void checkUnlockConditions() {
        if (heroLevel >= 3 && !isWizardUnlocked) {
            isWizardUnlocked = true;
            triggerFlashEffect(Color.CYAN);
            logTextArea.append("\n👑 【UNLOCK】 ゆうしゃのレベルが3に達したため『wizard』が解放されました！\n\n");
        }
        if (wizardLevel >= 4 && !isHumanUnlocked) {
            isHumanUnlocked = true;
            triggerFlashEffect(new Color(255, 215, 0));
            logTextArea.append("\n👑 【UNLOCK】 wizardのレベルが4に達したため『human』が解放されました！\n\n");
        }
    }

    private void triggerRunDialog() {
        if (isGameOverState)
            return;
        logTextArea.append("🏃 " + player.getName() + " は戦場から離脱した！\n");
        showGameClearOrEndScreen(false, "🏃 戦場から離脱しました！タイトルに戻って仕切り直しましょう。");
    }

    private void executePlayerTurn(boolean isSkill) {
        shakeFrame = 12;
        playSE("pouch.wav");

        if (player.getName().contains("wizard")) {
            int passiveMp = player.getLevel() >= 3 ? 10 : 5;
            player.setMp(Math.min(100, player.getMp() + passiveMp));
        }

        double critChance = 0.15;
        if (player.getName().contains("human")) {
            critChance = player.getLevel() >= 5 ? 0.40 : 0.30;
        }

        boolean isCritical = Math.random() < critChance && !isSkill;

        int baseDamage = (int) (player.getAtk() * (0.9 + Math.random() * 0.2));
        if (isSkill)
            baseDamage *= 2.5;

        baseDamage += (attackBuffCount * 15);

        int finalDamage = isCritical ? baseDamage * 2 : baseDamage;

        enemy.setHp(Math.max(0, enemy.getHp() - finalDamage));
        if (!isSkill)
            player.setMp(Math.min(100, player.getMp() + 15));

        showDamageText(enemyDamageLabel, "-" + finalDamage);

        if (isSkill) {
            triggerFlashEffect(Color.MAGENTA);
            logTextArea.append("✨【SKILL】 " + player.getName() + " の大魔法が炸裂！ " + enemy.getName() + " に " + finalDamage
                    + " のダメージ！\n");
        } else if (isCritical) {
            triggerFlashEffect(Color.YELLOW);
            logTextArea.append("💥 【CRITICAL】 " + player.getName() + " の会心の一撃！ " + enemy.getName() + " に " + finalDamage
                    + " のダメージ！\n");
            comboCount++;
        } else {
            logTextArea.append(
                    "⚔️ " + player.getName() + " の攻撃！ " + enemy.getName() + " に " + finalDamage + " のダメージを与えた！\n");
            comboCount++;
        }
        updateDisplay();

        if (!enemy.isAlive()) {
            logTextArea.append("\n★━━━━━━━━━━━━━━━━━━━━★\n");
            logTextArea.append("★ " + enemy.getName() + " を完全に見事に倒した！\n");
            logTextArea.append("★━━━━━━━━━━━━━━━━━━━━★\n\n");
            playerWins++;

            int droppedGold = 50 + (enemyCount * 30);
            playerGold += droppedGold;

            goldAnimLabel.setText("💰 +" + droppedGold + " Gold!");
            Timer goldFadeTimer = new Timer(1500, ev -> {
                goldAnimLabel.setText("");
                updateDisplay();
            });
            goldFadeTimer.setRepeats(false);
            goldFadeTimer.start();

            logTextArea.append("💰 " + droppedGold + " ゴールドを獲得した！\n");

            player.levelUp();

            if (currentSelectedType.equals("hero"))
                heroLevel = player.getLevel();
            else if (currentSelectedType.equals("wizard"))
                wizardLevel = player.getLevel();
            else if (currentSelectedType.equals("human"))
                humanLevel = player.getLevel();

            logTextArea.append("✨✨【LEVEL UP】✨✨\n" + player.getName() + " は レベル: " + player.getLevel() + " に上がった！\n");
            logTextArea.append("👉 (HP と 攻撃力 が大幅にアップしました！)\n\n");

            checkUnlockConditions();
            showGameClearOrEndScreen(true, "🎉 STAGE CLEAR! 次のステージへ進むか、上のショップで強化しましょう！");
            return;
        }

        Timer delayTimer = new Timer(400, ev -> executeEnemyTurn());
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private void useHealItem() {
        if (isGameOverState)
            return;
        if (itemUsesLeft <= 0) {
            logTextArea.append("⚠ アイテムがなくなりました！\n");
            return;
        }
        if (player.getHp() >= player.getMaxHp()) {
            logTextArea.append("⚠ HPはすでに満タンです！\n");
            return;
        }

        playSE("hp.wav");
        itemUsesLeft--;
        healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")");

        int previousHp = player.getHp();
        player.setHp(Math.min(player.getMaxHp(), player.getHp() + 100));
        int healedAmount = player.getHp() - previousHp;

        logTextArea.append("💚 アイテムを使用！ " + player.getName() + " のHPが " + healedAmount + " 回復した！\n");

        playerDamageLabel.setForeground(new Color(34, 139, 34));
        showDamageText(playerDamageLabel, "+" + healedAmount);

        triggerFlashEffect(Color.GREEN);
        comboCount = 0;
        updateDisplay();

        Timer delayEnemyTimer = new Timer(1000, ev -> {
            executeEnemyTurn();
        });
        delayEnemyTimer.setRepeats(false);
        delayEnemyTimer.start();
    }

    private void executeEnemyTurn() {
        if (!enemy.isAlive() || isGameOverState)
            return;

        double enemyHpPercent = (double) enemy.getHp() / enemy.getMaxHp();
        boolean isEnemyEnraged = enemyHpPercent <= 0.40;

        int originalAtk = enemy.getAtk();

        if (isEnemyEnraged) {
            triggerFlashEffect(Color.RED);
            logTextArea.append("🔥 【RAGE MODE】 " + enemy.getName() + " の攻撃力が1.5倍に暴走中！\n");
            enemy.setAtk((int) (originalAtk * 1.5));
        }

        boolean passiveGuard = player.getName().contains("ゆうしゃ") && player.getHp() <= (player.getMaxHp() * 0.3);
        boolean playerGuarding = isPlayerDefending || passiveGuard;

        boolean shieldBreakerAttack = Math.random() < 0.35 && isEnemyEnraged;
        boolean lifeStealAttack = Math.random() < 0.30 && !shieldBreakerAttack;

        if (shieldBreakerAttack) {
            logTextArea.append("💥 【SHIELD BREAKER】 " + enemy.getName() + " がガードを貫通する一撃を放った！\n");
            playerGuarding = false;
        }

        int oldHp = player.getHp();
        String enemyResult = enemy.attack(player, playerGuarding);
        int damageTaken = oldHp - player.getHp();

        if (lifeStealAttack && damageTaken > 0) {
            int healAmount = (int) (damageTaken * 0.4);
            enemy.setHp(Math.min(enemy.getMaxHp(), enemy.getHp() + healAmount));
            logTextArea.append("🧛 【LIFE STEAL】 " + enemy.getName() + " はプレイヤーのHPを吸収した！ (敵 HP +" + healAmount + ")\n");
            enemyDamageLabel.setForeground(new Color(34, 139, 34));
            showDamageText(enemyDamageLabel, "+" + healAmount);
        }

        if (damageTaken > 0) {
            playerDamageLabel.setForeground(Color.RED);
            showDamageText(playerDamageLabel, "-" + damageTaken);
        }

        logTextArea.append(enemyResult);

        enemy.setAtk(originalAtk);

        if (!isPlayerDefending)
            comboCount = 0;
        isPlayerDefending = false;
        updateDisplay();

        if (!player.isAlive()) {
            retryCount++;
            if (retryCount == 1) {
                logTextArea.append("💀 覚醒：最後の希望として human が参戦する！\n");
                int calculatedHp = 300 + (humanLevel - 1) * 35;
                int calculatedAtk = 130 + (humanLevel - 1) * 15;
                player = new Player("human (human)", calculatedHp, calculatedAtk, 10, 50, "human.png");
                player.setLevel(humanLevel);

                enemy = new Enemy("裏ボス (BOSS - TRUE FORM)", 2500, 60, 10, 50, "dragon.png");
                playerX = 80;
                updateDisplay();
            } else {
                showGameClearOrEndScreen(false, "💀 GAME OVER... 敗北しました。タイトルに戻ってレベルを確認しましょう。");
            }
             try {
            ImageIcon playerIcon = new ImageIcon(player.getImagePath());
            playerImageLabel.setIcon(new ImageIcon(playerIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.out.println("Player image load failed!");
        }
        }
    }
     
   


    private void showGameClearOrEndScreen(boolean isWin, String message) {
        isGameOverState = true;
        logTextArea
                .setText("\n==================================\n" + message + "\n==================================\n");

        attackButton.setVisible(false);
        skillButton.setVisible(false);
        defendButton.setVisible(false);
        healButton.setVisible(false);
        runButton.setVisible(false);

        if (isWin) {
            triggerFlashEffect(Color.YELLOW);
            bigGameClearButton.setVisible(true);
            if (enemyCount < 4) {
                nextStageButton.setVisible(true);
            } else {
                restartButton.setVisible(true);
            }
        } else {
            triggerFlashEffect(Color.BLACK);
            bigGameOverButton.setVisible(true);
            restartButton.setVisible(true);
        }
    }

    private void goToNextStage() {
        enemyCount++;
        isGameOverState = false;
        nextStageButton.setVisible(false);
        bigGameClearButton.setVisible(false);

        attackButton.setVisible(true);
        skillButton.setVisible(true);
        defendButton.setVisible(true);
        healButton.setVisible(true);
        runButton.setVisible(true);

        playerX = 80;
        spawnEnemy();
        updateDisplay();
        requestFocusInWindow();
    }

    private void resetToTitle() {
        enemyCount = 1;
        currentSaveId = 0;
        isGameOverState = false;
        playerGold = 0;
        attackBuffCount = 0;

        attackButton.setVisible(true);
        skillButton.setVisible(true);
        defendButton.setVisible(true);
        healButton.setVisible(true);
        runButton.setVisible(true);

        restartButton.setVisible(false);
        bigGameClearButton.setVisible(false);
        bigGameOverButton.setVisible(false);

        initGameSession(true);

        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "GAMEPLAY");
        this.requestFocusInWindow();
    }

 private void saveButton() {
    try {
        int playerNo;
        int level;

        if ("hero".equals(currentSelectedType)) {
            playerNo = 1;
            level = heroLevel;
        } else if ("wizard".equals(currentSelectedType)) {
            playerNo = 2;
            level = wizardLevel;
        } else {
            playerNo = 3;
            level = humanLevel;
        }

        java.sql.Timestamp currentDate = new java.sql.Timestamp(System.currentTimeMillis());

        int savedId = GameSaveManager.saveGameData(currentSaveId, playerNo, level, currentDate, playerGold);

        if (savedId > 0) {
            currentSaveId = savedId;
            String itemError = GameSaveManager.saveItemBox(savedId, itemBoxToNumbers(), currentDate);
            if (itemError == null) {
                JOptionPane.showMessageDialog(this, "ほぞんしました (セーブID: " + savedId + ")\nアイテムも保存しました。");
            } else {
                JOptionPane.showMessageDialog(this,
                        "ほぞんしました (セーブID: " + savedId + ")\nただし、アイテムの保存に失敗しました。\n" + itemError);
            }
        } else {
            JOptionPane.showMessageDialog(this, "セーブに失敗しました");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "セーブエラー: " + ex.getMessage());
    }
}


    private void triggerFlashEffect(Color color) {
        JPanel flashPanel = new JPanel();
        flashPanel.setBackground(color);
        flashPanel.setBounds(0, 0, getWidth(), getHeight());
        backgroundLabel.add(flashPanel);
        backgroundLabel.repaint();
        Timer timer = new Timer(150, e -> {
            backgroundLabel.remove(flashPanel);
            backgroundLabel.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void initGameSession(boolean needToChooseCharacter) {
        playerX = 80;
        itemUsesLeft = 3;
        retryCount = 0;
        isPlayerDefending = false;
        comboCount = 0;
        healButton.setText("💚 回復 (残り: " + itemUsesLeft + ")");

        if (needToChooseCharacter)
            choicePlayer();
        spawnEnemy();
        updateDisplay();
        logTextArea.setText("");
        logTextArea.append("\n⚔--- ステージ " + enemyCount + " 開始！ ---\n");
    }

    private void updateDisplay() {
        if (player == null || enemy == null)
            return;

        playerHpBar.setMaximum(player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerHpBar.setString(player.getHp() + " / " + player.getMaxHp());

        playerMpBar.setMaximum(100);
        playerMpBar.setValue(player.getMp());
        playerMpBar.setString(player.getMp() + " / 100");

        enemyHpBar.setMaximum(enemy.getMaxHp());
        enemyHpBar.setValue(enemy.getHp());
        enemyHpBar.setString(enemy.getHp() + " / " + enemy.getMaxHp());

        statusLabel.setText(String.format(
                "【%s (Lv.%d)】[%d Combo]  |  敵: 【%s】",
                player.getName(), player.getLevel(), comboCount, enemy.getName()));

        if (goldDisplayLabel != null) {
            goldDisplayLabel.setText("💰 Gold: " + playerGold + " G ");
        }
    }

    private void choicePlayer() {
        String option1 = "ゆうしゃ (Lv." + heroLevel + ") [開放済み]";
        String option2 = isWizardUnlocked ? "wizard (Lv." + wizardLevel + ") [開放済み]" : "🔒 wizard (ゆうしゃLv.3で開放)";
        String option3 = isHumanUnlocked ? "human (Lv." + humanLevel + ") [開放済み]" : "🔒 human (wizardLv.4で開放)";

        while (true) {
            int choice = JOptionPane.showOptionDialog(this, "使用するキャラクターを選択してください", "キャラクター選択",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[] { option1, option2, option3 }, null);

            if (choice == JOptionPane.CLOSED_OPTION || choice == 0) {
                int calculatedHp = 100 + (heroLevel - 1) * 20;
                int calculatedAtk = 120 + (heroLevel - 1) * 10;
                player = new Player("ゆうしゃ（Hero）", calculatedHp, calculatedAtk, 10, 10, "hero.png");
                player.setLevel(heroLevel);
                currentSelectedType = "hero";
                break;
            } else if (choice == 1) {
                if (isWizardUnlocked) {
                    int calculatedHp = 80 + (wizardLevel - 1) * 15;
                    int calculatedAtk = 125 + (wizardLevel - 1) * 12;
                    player = new Player("wizard（wizard）", calculatedHp, calculatedAtk, 10, 10, "wizard player.png");
                    player.setLevel(wizardLevel);
                    currentSelectedType = "wizard";
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "❌ wizardはまだロックされています！\nゆうしゃのレベルを3以上にしてください。", "ロック中",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (choice == 2) {
                if (isHumanUnlocked) {
                    int calculatedHp = 300 + (humanLevel - 1) * 35;
                    int calculatedAtk = 130 + (humanLevel - 1) * 15;
                    player = new Player("human (human)", calculatedHp, calculatedAtk, 10, 50, "human.png");
                    player.setLevel(humanLevel);
                    currentSelectedType = "human";
                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "❌ humanはまだロックされています！\nwizardのレベルを4以上にしてください。", "ロック中",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        try {
            ImageIcon playerIcon = new ImageIcon(player.getImagePath());
            playerImageLabel
                    .setIcon(new ImageIcon(playerIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.out.println("Player image load failed!");
        }
    }
      private void switchBGM(String fileName) {
      if (bgmClip != null && bgmClip.isRunning()) {
        bgmClip.stop();
        bgmClip.close();
    }
    playBGM(fileName);
}


    private void spawnEnemy() {
        // 敵のレベルは「今使っているキャラ」のレベルだけで決める
        int currentMaxLevel;
        if (currentSelectedType.equals("wizard")) {
            currentMaxLevel = wizardLevel;
        } else if (currentSelectedType.equals("human")) {
            currentMaxLevel = humanLevel;
        } else {
            currentMaxLevel = heroLevel;
        }
        double levelMultiplier = 1.0 + (currentMaxLevel - 1) * 0.15;

        if (enemyCount == 1) {
            int hp = (int) (150 * levelMultiplier);
            int atk = (int) (18 * levelMultiplier);
            enemy = new Enemy("スライム (Lv." + currentMaxLevel + ")", hp, atk, 25, 20, "smile enamy.png");
            setBackgroundImage("fristbackgroud.png");
            switchBGM("backgroundmusic.wav");

        } else if (enemyCount == 2) {
            int hp = (int) (300 * levelMultiplier);
            int atk = (int) (28 * levelMultiplier);
            enemy = new Enemy("ゴブリン (Lv." + currentMaxLevel + ")", hp, atk, 25, 20, "dargon enamy.png");
            setBackgroundImage("second backgroud.png");
            switchBGM("background music2.wav");
        } else if (enemyCount == 3) {
            int hp = (int) (500 * levelMultiplier);
            int atk = (int) (42 * levelMultiplier);
            enemy = new Enemy("オークキング (Lv." + currentMaxLevel + ")", hp, atk, 25, 20, "Demon enamy.png");
            setBackgroundImage("backgroundthree.png");
        } else {
            int hp = (int) (1000 * levelMultiplier);
            int atk = (int) (65 * levelMultiplier);
            enemy = new Enemy("魔王 (BOSS - FINAL FORM)", hp, atk, 25, 20, "dargon.png");
            setBackgroundImage("final backgroud.png");
            switchBGM("backgroud music 3.wav");
        }

        try {
            ImageIcon enemyIcon = new ImageIcon(enemy.getImagePath());
            enemyImageLabel
                    .setIcon(new ImageIcon(enemyIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.out.println("Enemy image load failed!");
        }
    }

    // ロードしたセーブのキャラクター（currentSelectedType）でプレイヤーを作る
    private void createLoadedPlayer() {
        if (currentSelectedType.equals("wizard")) {
            int calculatedHp = 80 + (wizardLevel - 1) * 15;
            int calculatedAtk = 125 + (wizardLevel - 1) * 12;
            player = new Player("wizard（wizard）", calculatedHp, calculatedAtk, 10, 10, "wizard player.png");
            player.setLevel(wizardLevel);
        } else if (currentSelectedType.equals("human")) {
            int calculatedHp = 300 + (humanLevel - 1) * 35;
            int calculatedAtk = 130 + (humanLevel - 1) * 15;
            player = new Player("human (human)", calculatedHp, calculatedAtk, 10, 50, "human.png");
            player.setLevel(humanLevel);
        } else {
            int calculatedHp = 100 + (heroLevel - 1) * 20;
            int calculatedAtk = 120 + (heroLevel - 1) * 10;
            player = new Player("ゆうしゃ（Hero）", calculatedHp, calculatedAtk, 10, 10, "hero.png");
            player.setLevel(heroLevel);
        }

        try {
            ImageIcon playerIcon = new ImageIcon(player.getImagePath());
            playerImageLabel
                    .setIcon(new ImageIcon(playerIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            System.out.println("Player image load failed!");
        }
    }

    // アイテム → アイテム番号（DBの ITEM_NO）。1: Bomb, 2: Herb, 3: mp, 4: Shield, 0: なし
    private int itemToNo(Item item) {
        if (item == null) {
            return 0;
        }
        switch (item.getClass().getSimpleName()) {
            case "Bomb":
                return 1;
            case "Herb":
                return 2;
            case "mp":
                return 3;
            case "Shield":
                return 4;
            default:
                return 0;
        }
    }

    // アイテム番号 → アイテム
    private Item noToItem(int itemNo) {
        switch (itemNo) {
            case 1:
                return new Bomb();
            case 2:
                return new Herb();
            case 3:
                return new mp();
            case 4:
                return new Shield();
            default:
                return null;
        }
    }

    // アイテムボックス（10スロット）を番号の配列にする
    private int[] itemBoxToNumbers() {
        int[] nos = new int[itemBox.length];
        for (int i = 0; i < itemBox.length; i++) {
            nos[i] = itemToNo(itemBox[i]);
        }
        return nos;
    }

    // ロードしたセーブのアイテムボックスを復元する
    private void restoreItemsFromSave(int saveId) {
        int[] nos = GameSaveManager.loadItemBox(saveId);
        if (nos == null) {
            return; // アイテム未保存の古いセーブ: 最初のアイテムをそのまま使う
        }
        for (int i = 0; i < itemBox.length; i++) {
            itemBox[i] = (i < nos.length) ? noToItem(nos[i]) : null;
        }
        logTextArea.append("💾 アイテムを復元しました！\n");
    }

    private void addItemToBox(Item item) {
        for (int i = 0; i < itemBox.length; i++) {
            if (itemBox[i] == null) { // 空（あ）きスロットを発見（はっけん）！
                itemBox[i] = item; // アイテムをセット
                logTextArea.append("アイテムボックスのスロット " + (i + 1) + " に " + item.getName() + " を追加しました。\n");
                return; // 1つ入れたら終了（しゅうりょう）
            }
        }
        // ここまで来たら全スロットが埋まっている
        logTextArea.append("アイテムボックスがいっぱいです！ " + item.getName() + " は拾えませんでした。\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BattleGame game = new BattleGame();
            game.setVisible(true);
        });
    }
   private void updateGameScreenUI() {
    if (this.loadedLvlTemp > 0 && this.player != null) {
        this.player.setLevel(this.loadedLvlTemp);
        int calcHp = 100 + (this.loadedLvlTemp - 1) * 20;
        this.player.setMaxHp(calcHp);
        this.player.setHp(calcHp);
    }
    if (this.loadedGoldTemp >= 0) {
        this.playerGold = this.loadedGoldTemp;
    }
    if (statusLabel != null && this.player != null) {
        statusLabel.setText("Player Level: " + this.player.getLevel() + " (" + currentSelectedType + ")");
    }
    if (goldDisplayLabel != null) {
        goldDisplayLabel.setText("Gold: " + this.playerGold + "G");
    }
    if (playerHpBar != null && this.player != null) {
        playerHpBar.setMaximum(this.player.getMaxHp());
        playerHpBar.setValue(this.player.getHp());
        playerHpBar.setString(this.player.getHp() + " / " + this.player.getMaxHp());
    }
}

}