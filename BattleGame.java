import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel; // HPなどをひょうじするラベル（Label）
    private JTextArea logTextArea; // バトルのりれきをひょうじするテキストエリア（Text Area）
    private JButton attackButton;
    private JButton skillButton; // ★ つかい：スキルこうげきボタン（Skill Button）
    private JButton itemButton; // ★ つかい：アイテムボタン（Item Button）
    private JButton runButton; // こうげきコマンドボタン（Command Button）

    // ★ がぞうをひょうじするためのラベル
    private JLabel backgroundLabel; // はいけいがぞうようのラベル
    private JLabel playerImageLabel; // プレイヤーがぞうようのラベル
    private JLabel enemyImageLabel; // てきがぞうようのラベル

    // ★ キャラクターのインスタンスをよういする
    private Player player;
    private Enemy enemy;
    private int enemyCount = 1;
    private int retryCount = 0;

    // ★ アイテムとスキルの管理用（Management）
    private int itemUsesLeft = 3; // アイテムののこりかいすう（3回）
    private int skillCooldown = 0; // スキルのクールダウン（連続で使えないようにする）

    // ★ レベルシステム・アンロック用（Level & Unlock System）
    private static int playerWins = 0; // トータル勝利数（Total Wins）
    private static int currentPlayerLevel = 1; // ★ 変更点：プレイヤーの現在のレベルをここに保存する
    private static boolean isWizardUnlocked = false; // wizardが解放されたか（Unlocked）
    private static boolean isHumanUnlocked = false; // humanが解放されたか（Unlocked）

    public BattleGame() {
        // ウィンドウ（Window）のきほんせってい（Basic Setting）
        setTitle("RPG");
        setSize(1220, 1050);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // がめんのまんなかにひょうじ
        setLayout(new BorderLayout()); // ぜんたいのレイアウト（Layout）をせってい

        // 【うえはんぶん：キャラクターたいじエリア（はいけいのなかにキャラをいれる）】
        backgroundLabel = new JLabel(new ImageIcon("fristbackgroud.png"));
        backgroundLabel.setLayout(null); // ★じゅうよう（Important）：じゆうはいち（Free Layout）にするためにnullにする

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        // ★はいけいラベルをきじゅん（Base）とした、キャラがぞうラベルの「いち（Position）(x, y)」と「サイズ（Size）(はば（Width）,
        // たかさ（Height））」をしてい（Specify）
        playerImageLabel.setBounds(80, 80, 500, 500); // ひだりがわにはいち
        enemyImageLabel.setBounds(700, 80, 500, 500); // みぎがわにはいち

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

        // ★ コマンドボタンをようい（ボタンを4つにふやす）
        attackButton = new JButton("こうげきする (Normal)");
        skillButton = new JButton("スキルこうげき (Skill)");
        itemButton = new JButton("アイテムを使う (Heal: 3)");
        runButton = new JButton("にげる");

        // ボタンをよこならびにするためのパネル（Panel）
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonPanel.add(attackButton);
        buttonPanel.add(skillButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(runButton);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH); // ボタンエリアを下側にはいち

        // ぶひん（Parts）をメインウィンドウにはいち
        add(backgroundLabel, BorderLayout.CENTER); // はいけい（キャラいり）をまんなかにはいち
        add(bottomPanel, BorderLayout.SOUTH); // そうさエリアをしたがわにはいち

        // --- 【ボタンをおしたときのしょり（Action Listener）】 ---

        // 1. こうげきボタン（通常攻撃）
        attackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executePlayerTurn("normal");
            }
        });

        // 2. スキルこうげきボタン
        skillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (skillCooldown > 0) {
                    logTextArea.append("⚠ スキルはまだ使えません！ あと " + skillCooldown + " ターン待ってください。\n");
                    return;
                }
                executePlayerTurn("skill");
            }
        });

        // 3. アイテム（回復）ボタン
        itemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (itemUsesLeft <= 0) {
                    logTextArea.append("⚠ アイテムがなくなりました！\n");
                    return;
                }
                if (player.getHp() >= player.getMaxHp()) {
                    logTextArea.append("⚠ HPはすでに満タンです！\n");
                    return;
                }

                itemUsesLeft--;
                itemButton.setText("アイテムを使う (Heal: " + itemUsesLeft + ")");

                // HPを100回復させる処理（最大HPを超えないように調整）
                int healAmount = 100;
                player.setHp(Math.min(player.getMaxHp(), player.getHp() + healAmount));

                logTextArea.append("💚 アイテムを使用！ " + player.getName() + " のHPが 100 かいふくした！\n");
                updateDisplay();

                // アイテム使用後もエネミーははんげきしてくる
                executeEnemyTurn();
            }
        });

        // 4. にげるボタン
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append(player.getName() + " は逃げ出そうとした！\n");

                // 0.5 未満（50% の確率）なら逃亡成功とする
                if (Math.random() < 0.5) {
                    logTextArea.append("うまくにげきれた！\n");
                    endGame(); // ゲームをしゅうりょうさせる
                } else {
                    logTextArea.append("しかし にげきれなかった！\n");
                    executeEnemyTurn();
                }
            }
        });

        // ★ インスタンスを初期化
        choicePlayer();
        spawnEnemy();

        // ★ 初期ステータスを表示する
        updateDisplay();
        logTextArea.append("野生の " + enemy.getName() + " が現れた！\n");
    }

    // プレイヤーの行動（ターン）をしょりするメソッド
    private void executePlayerTurn(String type) {
        String playerResult = "";

        if (type.equals("normal")) {
            playerResult = player.attack(enemy);
            logTextArea.append(playerResult);
        } else if (type.equals("skill")) {
            logTextArea.append("⚡ " + player.getName() + " のひっさつ必殺スキルアタック！！\n");
            playerResult = player.attack(enemy);
            logTextArea.append(playerResult);
            skillCooldown = 3; // 3ターンのクールダウンを設定
        }

        if (skillCooldown > 0)
            skillCooldown--; // ターン経過でクールダウンを減らす
        updateDisplay();

        // エネミーがたおれたかチェック
        if (!enemy.isAlive()) {
            logTextArea.append("★ " + enemy.getName() + " をたおした！ " + player.getName() + "のしょうり（Victory）！\n");
            enemyImageLabel.setEnabled(false);

            // ★ つかい：しょうりすう（Wins Count）をカウントアップ
            playerWins++;
            logTextArea.append("🏆 トータル勝利数（Total Wins）: " + playerWins + "\n");

            // ★ 変更点：勝利時にプレイヤーがレベルアップし、そのレベルを保存する
            player.levelUp();
            currentPlayerLevel = player.getLevel(); // 現在のレベルを保存！
            logTextArea.append("✨ " + player.getName() + " はレベルアップした！ レベル: " + currentPlayerLevel
                    + " になった！攻撃力がアップし、HPが満タンに回復した！\n");

            // ★ 3回勝利（Level 3相当）で wizard 解放
            if (playerWins >= 3 && !isWizardUnlocked) {
                isWizardUnlocked = true;
                JOptionPane.showMessageDialog(this, "🎉 おめでとう！ wizard（wizard）がアンロックされました！", "キャラクター解放",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            // ★ 6回勝利で human 解放
            if (playerWins >= 6 && !isHumanUnlocked) {
                isHumanUnlocked = true;
                JOptionPane.showMessageDialog(this, "🎉 おめでとう！ human（human）がアンロックされました！", "キャラクター解放",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // 🔄 【ゲームループのしょり】
            if (enemyCount < 4) {
                enemyCount++;
            } else {
                // すべてのエネミーを倒したら、ゲームクリア画面を出してループさせる
                logTextArea.append("🎉 すべてのまものをたいじした！つぎのステージがはじまります！\n");
                
                // ゲームクリアのディスプレイを表示
                showEndScreen(true);
                
                enemyCount = 1;
            }

            // 💡 新しいエネミーが出る前に、キャラクターをえらびなおす画面をだす
            logTextArea.append("⚔ つぎのバトルのキャラクターをせんたくしてください...\n");
            choicePlayer();

            spawnEnemy(); // ★ここで新しいエネミーが自動的にフルHP（Full HP）で現れる！
            enemyImageLabel.setEnabled(true);
            updateDisplay();
            return;
        }

        // エネミーが生きていれば反撃
        executeEnemyTurn();
    }

    // エネミーの行動としょり
    private void executeEnemyTurn() {
        String enemyResult = enemy.attack(player);
        logTextArea.append(enemyResult);
        updateDisplay();

        // プレイヤーがたおれたかチェック
        if (!player.isAlive()) {
            retryCount++;
            if (retryCount == 1) {
                logTextArea.append(" " + player.getName() + " はたおれた… human（human）でリトライ！\n");

                // ★ プレイヤーを human にしてフルHP（Full HP: 300）で復活させる！
                player = new Player("human (human)", 300, 130, 10, 50, "human.png");
                player.setHp(300); // 確実にHPを満タンにする
                
                // ★ リトライ時はレベルをリセットするかそのまま引き継ぐ（ここでは引き継ぎます）
                for(int i=1; i<currentPlayerLevel; i++) {
                    player.levelUp(); 
                }

                // プレイヤーの新しいイメージを 500x500 にスケールしてセット
                ImageIcon pIcon = (ImageIcon) player.getIcon();
                Image pImg = pIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                playerImageLabel.setIcon(new ImageIcon(pImg));

                // 敵を裏ボス（ボス）に切り替え（こちらももちろんフルHP: 1000）
                enemy = new Enemy("ボス", 1000, 20, 10, 50, "dargon.png");
                enemy.setHp(1000);

                // ボスのイメージも 500x500 にスケールしてセット
                ImageIcon eIcon = (ImageIcon) enemy.getIcon();
                Image eImg = eIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                enemyImageLabel.setIcon(new ImageIcon(eImg));

                // 背景もボス戦用に変更
                backgroundLabel.setIcon(new ImageIcon("final backgroud.png"));

                updateDisplay();
                logTextArea.append("突故として、真のボスが現れた！\n");
            } else {
                // 2回目に倒されたら完全ゲームオーバー
                logTextArea.append(" " + player.getName() + " はたおれた… ゲームオーバー（Game Over）\n");
                playerImageLabel.setEnabled(false);
                
                // ゲームオーバーのディスプレイを表示
                showEndScreen(false);
                
                endGame();
            }
            logTextArea.append("--------------------------------------------\n");
            return;
        }
        logTextArea.append("--------------------------------------------\n");
    }

    public static void main(String[] args) {
        BattleGame game = new BattleGame();
        game.setVisible(true); // がめんをひょうじする
    }

    // がめんこうしんしょり
    private void updateDisplay() {
        if (player == null || enemy == null)
            return; // 💡 NullPointerExceptionのあんぜんたいさく

        statusLabel.setText(String.format(
                "【%s (Lv.%d)】 HP: %d/%d  vs  【%s】 HP: %d/%d",
                player.getName(), player.getLevel(), player.getHp(), player.getMaxHp(),
                enemy.getName(), enemy.getHp(), enemy.getMaxHp()));
    }

    private void endGame() {
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        itemButton.setEnabled(false);
        runButton.setEnabled(false); // すべてのボタンを無効化
        logTextArea.append("【ゲームしゅうりょう】ウィンドウをとじてください。\n");
    }

    // ゲームクリア・ゲームオーバーの専用ディスプレイ画面を作るメソッド
    private void showEndScreen(boolean isWin) {
        JDialog endDialog = new JDialog(this, isWin ? "GAME CLEAR" : "GAME OVER", true);
        endDialog.setSize(600, 650);
        endDialog.setLocationRelativeTo(this); // メイン画面の真ん中に表示
        endDialog.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(isWin ? "🎉 GAME CLEAR! 🎉" : "💀 GAME OVER 💀", JLabel.CENTER);
        titleLabel.setFont(new Font("MS ゴシック", Font.BOLD, 30));
        titleLabel.setForeground(isWin ? Color.GREEN : Color.RED);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        endDialog.add(titleLabel, BorderLayout.NORTH);

        String imageName = isWin ? "game clear.png" : "game over .png";
        JLabel imageLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(imageName);
            Image img = icon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel.setText(isWin ? "[GAME CLEAR IMAGE]" : "[GAME OVER IMAGE]");
        }
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        endDialog.add(imageLabel, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                endDialog.dispose(); 
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        btnPanel.add(okButton);
        endDialog.add(btnPanel, BorderLayout.SOUTH);

        endDialog.setVisible(true);
    }

    // キャラクターせんたくメソッド
    private void choicePlayer() {
        String option1 = "ゆうしゃ（Hero）";
        String option2 = isWizardUnlocked ? "wizard（wizard）" : "🔒 wizard (3回勝つと解放)";
        String option3 = isHumanUnlocked ? "human" : "🔒 human (6回勝つと解放)";

        int choice = JOptionPane.showOptionDialog(
                this,
                "しようするキャラクターをせんたくしてください\n（トータル勝利数: " + playerWins + "）",
                "キャラクターせんたく",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] { option1, option2, option3 },
                null);

        if (choice == JOptionPane.CLOSED_OPTION) {
            choice = 0;
        }

        // ★ 変更点：新しくインスタンスを作った後、保存されていたレベル（currentPlayerLevel）までループでレベルを上げる
        if (choice == 0) {
            player = new Player("ゆうしゃ（Hero）", 100, 120, 10, 10, "hero.png");
        } else if (choice == 1) {
            if (!isWizardUnlocked) {
                JOptionPane.showMessageDialog(this, "⚠ このキャラクターはまだロックされています！\nゆうしゃ（Hero）で3回勝利してください。", "ロックされています",
                        JOptionPane.WARNING_MESSAGE);
                choicePlayer(); 
                return; 
            }
            player = new Player("wizard（wizard）", 80, 125, 10, 10, "new image.png");
        } else if (choice == 2) {
            if (!isHumanUnlocked) {
                JOptionPane.showMessageDialog(this, "⚠ このキャラクターはまだロックされています！\n6回勝利してください。", "ロックされています",
                        JOptionPane.WARNING_MESSAGE);
                choicePlayer(); 
                return;
            }
            player = new Player("human (human)", 300, 130, 10, 50, "human.png");
        }

        // 💡 ★ 変更点：選んだキャラクターを、前のバトルで上がったレベル（currentPlayerLevel）と同じレベルまで自動でアップさせる！
        for (int i = 1; i < currentPlayerLevel; i++) {
            player.levelUp();
        }

        // プレイヤーのイメージを 500x500 にスケールしてセット
        ImageIcon playerIcon = (ImageIcon) player.getIcon();
        Image playerImg = playerIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
        playerImageLabel.setIcon(new ImageIcon(playerImg));
    }

    // 新しいエネミーが出現するメソッド
    private void spawnEnemy() {
        if (enemyCount == 1) {
            enemy = new Enemy("スライム", 100, 10, 25, 20, "smile enamy.png");
            logTextArea.append("【だい１せん】スライム があらわれた！\n");
            backgroundLabel.setIcon(new ImageIcon("fristbackgroud.png")); 
        } else if (enemyCount == 2) {
            enemy = new Enemy("dragon", 150, 14, 10, 25, "dargon enamy.png");
            logTextArea.append("【だい２せん】dragon があらわれた！\n");
            backgroundLabel.setIcon(new ImageIcon("second backgroud.png"));
        } else if (enemyCount == 3) {
            enemy = new Enemy("ドラゴン", 200, 20, 10, 30, "Demon enamy.png");
            logTextArea.append("【さいしゅうけっせん】でんせつ（Legend）の ドラゴン があらわれた！\n");
        } else if (enemyCount == 4) {
            enemy = new Enemy("ボス", 1000, 20, 10, 50, "dargon.png");
            logTextArea.append("【うらのボス戦】真の ボス があらわれた！\n");
            backgroundLabel.setIcon(new ImageIcon("final backgroud.png"));
        }

        ImageIcon enemyIcon = (ImageIcon) enemy.getIcon();
        Image enemyImg = enemyIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
        enemyImageLabel.setIcon(new ImageIcon(enemyImg));

        logTextArea.append("--------------------------------------------\n");
    }
}