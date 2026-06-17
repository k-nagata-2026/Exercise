import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattleGame extends JFrame {
    private JLabel statusLabel; 
    private JTextArea logTextArea; 
    private JButton attackButton;
    private JButton skillButton; 
    private JButton itemButton; 
    private JButton defenseButton; 
    private JButton runButton; 

    private JLabel backgroundLabel; 
    private JLabel playerImageLabel; 
    private JLabel enemyImageLabel; 

    private Player player;
    private Enemy enemy;
    private int enemyCount = 1;
    private int retryCount = 0;

    private int itemUsesLeft = 3; 
    private boolean isPlayerDefending = false; 

    private static int playerWins = 0; 
    private static int currentPlayerLevel = 1; // ★ यो लेभल रिस्टार्ट गर्दा पनि जोगिइरहन्छ
    private static boolean isWizardUnlocked = false; 
    private static boolean isHumanUnlocked = false; 

    public BattleGame() {
        setTitle("RPG - 無限レベル＆再挑戦モード");
        setSize(1220, 1050);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout()); 

        backgroundLabel = new JLabel(new ImageIcon("fristbackgroud.png"));
        backgroundLabel.setLayout(null); 

        playerImageLabel = new JLabel("", JLabel.CENTER);
        enemyImageLabel = new JLabel("", JLabel.CENTER);

        playerImageLabel.setBounds(80, 80, 500, 500); 
        enemyImageLabel.setBounds(700, 80, 500, 500); 

        backgroundLabel.add(playerImageLabel);
        backgroundLabel.add(enemyImageLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("ここにステータスがひょうじされます", JLabel.CENTER);
        statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 14));

        logTextArea = new JTextArea(8, 30);
        logTextArea.setEditable(false); 
        JScrollPane scrollPane = new JScrollPane(logTextArea); 

        attackButton = new JButton("こうげきする (Normal)");
        skillButton = new JButton("必殺スキル (Cost: 50 MP)");
        itemButton = new JButton("アイテムを使う (Heal: 3)");
        defenseButton = new JButton("ぼうぎょ (Guard)"); 
        runButton = new JButton("にげる");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        buttonPanel.add(attackButton);
        buttonPanel.add(skillButton);
        buttonPanel.add(itemButton);
        buttonPanel.add(defenseButton); 
        buttonPanel.add(runButton);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH); 

        add(backgroundLabel, BorderLayout.CENTER); 
        add(bottomPanel, BorderLayout.SOUTH); 

        // 1. こうげきボタン
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
                if (player.getMp() < 50) {
                    logTextArea.append("⚠ MPが足りません！ スキルには 50 MP 必要です。\n");
                    return;
                }
                executePlayerTurn("skill");
            }
        });

        // 3. アイテムボタン
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

                int healAmount = 100;
                player.setHp(Math.min(player.getMaxHp(), player.getHp() + healAmount));

                logTextArea.append("💚 アイテムを使用！ " + player.getName() + " のHPが 100 かいふくした！\n");
                updateDisplay();
                executeEnemyTurn();
            }
        });

        // 4. ぼうぎょボタン
        defenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPlayerDefending = true;
                player.setMp(player.getMp() + 20); 
                logTextArea.append("🛡 " + player.getName() + " はしっかりと身を護っている！ (MP+20)\n");
                updateDisplay();
                executeEnemyTurn();
            }
        });

        // 5. にげるボタン
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.append(player.getName() + " は逃げ出そうとした！\n");
                if (Math.random() < 0.4) { 
                    logTextArea.append("うまくにげきれた！\n");
                    endGame(); 
                } else {
                    logTextArea.append("しかし にげきれなかった！敵の不意打ち！\n");
                    executeEnemyTurn();
                }
            }
        });

        // 最初のかいし画面
        initGameSession();
    }

    // ★ついか：ゲームを新しく、または同じレベルで開始するときの初期化
    private void initGameSession() {
        attackButton.setEnabled(true);
        skillButton.setEnabled(true);
        itemButton.setEnabled(true);
        defenseButton.setEnabled(true);
        runButton.setEnabled(true);
        playerImageLabel.setEnabled(true);
        enemyImageLabel.setEnabled(true);
        
        itemUsesLeft = 3;
        itemButton.setText("アイテムを使う (Heal: 3)");
        retryCount = 0;
        isPlayerDefending = false;

        choicePlayer();
        spawnEnemy();
        updateDisplay();
        logTextArea.append("\n⚔--- 新しい冒険が始まった！現在のレベル: " + currentPlayerLevel + " ---\n");
    }

    private void executePlayerTurn(String type) {
        String playerResult = "";

        if (type.equals("normal")) {
            playerResult = player.attack(enemy);
            logTextArea.append(playerResult);
        } else if (type.equals("skill")) {
            playerResult = player.useSkill(enemy); 
            logTextArea.append(playerResult);
        }

        updateDisplay();

        if (!enemy.isAlive()) {
            logTextArea.append("★ " + enemy.getName() + " をたおした！ " + player.getName() + "のしょうり！\n");
            enemyImageLabel.setEnabled(false);

            playerWins++;
            logTextArea.append("🏆 トータル勝利数: " + playerWins + "\n");

            player.levelUp();
            currentPlayerLevel = player.getLevel(); 
            logTextArea.append("✨ " + player.getName() + " はレベルアップ！ レベル: " + currentPlayerLevel + " になった！\n");

            if (playerWins >= 3 && !isWizardUnlocked) {
                isWizardUnlocked = true;
                JOptionPane.showMessageDialog(this, "🎉 wizardがアンロックされました！", "キャラクター解放", JOptionPane.INFORMATION_MESSAGE);
            }
            if (playerWins >= 6 && !isHumanUnlocked) {
                isHumanUnlocked = true;
                JOptionPane.showMessageDialog(this, "🎉 humanがアンロックされました！", "キャラクター解放", JOptionPane.INFORMATION_MESSAGE);
            }

            if (enemyCount < 4) {
                enemyCount++;
            } else {
                logTextArea.append("🎉 すべての強敵を撃破した！ステージ周回！\n");
                showEndScreen(true);
                enemyCount = 1;
                initGameSession(); // クリア時も自動で次へ
                return;
            }

            logTextArea.append("⚔ 次のバトルのキャラクターをせんたくしてください...\n");
            choicePlayer();
            spawnEnemy(); 
            enemyImageLabel.setEnabled(true);
            updateDisplay();
            return;
        }

        executeEnemyTurn();
    }

    private void executeEnemyTurn() {
        String enemyResult = enemy.attack(player, isPlayerDefending);
        logTextArea.append(enemyResult);
        
        isPlayerDefending = false; 
        updateDisplay();

        if (!player.isAlive()) {
            retryCount++;
            if (retryCount == 1) {
                logTextArea.append(" " + player.getName() + " は倒れた… humanで最終リトライ！\n");

                player = new Player("human (human)", 300, 130, 10, 50, "human.png");
                player.setHp(300); 
                
                for(int i = 1; i < currentPlayerLevel; i++) {
                    player.levelUp(); 
                }

                ImageIcon pIcon = (ImageIcon) player.getIcon();
                Image pImg = pIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                playerImageLabel.setIcon(new ImageIcon(pImg));

                // 裏ボスもレベルに応じてHP強化
                int bossHp = 1000 + (currentPlayerLevel * 50);
                enemy = new Enemy("ボス", bossHp, 25, 10, 50, "dargon.png"); 
                enemy.setHp(bossHp);

                ImageIcon eIcon = (ImageIcon) enemy.getIcon();
                Image eImg = eIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                enemyImageLabel.setIcon(new ImageIcon(eImg));

                backgroundLabel.setIcon(new ImageIcon("final backgroud.png"));
                updateDisplay();
                logTextArea.append("絶望の底から、真の裏ボスが姿を現した！\n");
            } else {
                logTextArea.append(" " + player.getName() + " は完全に力尽きた… ゲームオーバー\n");
                playerImageLabel.setEnabled(false);
                endGame();
                showEndScreen(false); // ★ ここでカスタム選択ダイアログが出る
            }
            logTextArea.append("--------------------------------------------\n");
            return;
        }
        logTextArea.append("--------------------------------------------\n");
    }

    public static void main(String[] args) {
        BattleGame game = new BattleGame();
        game.setVisible(true); 
    }

    private void updateDisplay() {
        if (player == null || enemy == null) return; 

        statusLabel.setText(String.format(
                "【%s (Lv.%d)】 HP: %d/%d  MP: %d/100  vs  【%s】 HP: %d/%d",
                player.getName(), player.getLevel(), player.getHp(), player.getMaxHp(), player.getMp(),
                enemy.getName(), enemy.getHp(), enemy.getMaxHp()));
    }

    private void endGame() {
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        itemButton.setEnabled(false);
        defenseButton.setEnabled(false); 
        runButton.setEnabled(false); 
    }

    // ★ 大幅修正：Restart と Back ボタンを実装した終了画面
    private void showEndScreen(boolean isWin) {
        JDialog endDialog = new JDialog(this, isWin ? "STAGE CLEAR" : "GAME OVER", true);
        endDialog.setSize(600, 700);
        endDialog.setLocationRelativeTo(this); 
        endDialog.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(isWin ? "🎉 STAGE CLEAR! 🎉" : "💀 GAME OVER 💀", JLabel.CENTER);
        titleLabel.setFont(new Font("MS ゴシック", Font.BOLD, 30));
        titleLabel.setForeground(isWin ? Color.GREEN : Color.RED);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        endDialog.add(titleLabel, BorderLayout.NORTH);

        String imageName = isWin ? "gameclear.png" : "gameover.png";
        JLabel imageLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(imageName);
            Image img = icon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel.setText(isWin ? "[GAME CLEAR]" : "[GAME OVER]");
        }
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        endDialog.add(imageLabel, BorderLayout.CENTER);

        // ボタンエリア
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        if (!isWin) {
            // ① Restart ボタン（現在のレベルのままステージ1からやり直す）
            JButton restartButton = new JButton("もう一度戦う (Restart)");
            restartButton.setFont(new Font("MS ゴシック", Font.BOLD, 14));
            restartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    endDialog.dispose();
                    enemyCount = 1; // ステージ1に戻る
                    initGameSession(); // 同じレベルでリスタート
                }
            });

            // ② Back ボタン（最初のキャラ選択画面に戻る）
            JButton backButton = new JButton("最初に戻る (Back)");
            backButton.setFont(new Font("MS ゴシック", Font.BOLD, 14));
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    endDialog.dispose();
                    enemyCount = 1;
                    initGameSession(); // キャラクターを選び直す画面を呼び出す
                }
            });

            btnPanel.add(restartButton);
            btnPanel.add(backButton);
        } else {
            // クリア時のOKボタン
            JButton okButton = new JButton("OK");
            okButton.setFont(new Font("MS ゴシック", Font.BOLD, 16));
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    endDialog.dispose();
                }
            });
            btnPanel.add(okButton);
        }

        endDialog.add(btnPanel, BorderLayout.SOUTH);
        endDialog.setVisible(true);
    }

    private void choicePlayer() {
        String option1 = "ゆうしゃ（Hero）";
        String option2 = isWizardUnlocked ? "wizard（wizard）" : "🔒 wizard (3回勝つと解放)";
        String option3 = isHumanUnlocked ? "human" : "🔒 human (6回勝つと解放)";

        int choice = JOptionPane.showOptionDialog(
                this,
                "しようするキャラクターをせんたくしてください\n（トータル勝利数: " + playerWins + " / 現在のベースLv: " + currentPlayerLevel + "）",
                "キャラクターせんたく",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] { option1, option2, option3 },
                null);

        if (choice == JOptionPane.CLOSED_OPTION) choice = 0;

        if (choice == 0) {
            player = new Player("ゆうしゃ（Hero）", 100, 120, 10, 10, "hero.png");
        } else if (choice == 1) {
            if (!isWizardUnlocked) {
                JOptionPane.showMessageDialog(this, "🔒 ロックされています！", "ロック", JOptionPane.WARNING_MESSAGE);
                choicePlayer(); 
                return; 
            }
            player = new Player("wizard（wizard）", 80, 125, 10, 10, "new image.png");
        } else if (choice == 2) {
            if (!isHumanUnlocked) {
                JOptionPane.showMessageDialog(this, "🔒 ロックされています！", "ロック", JOptionPane.WARNING_MESSAGE);
                choicePlayer(); 
                return;
            }
            player = new Player("human (human)", 300, 130, 10, 50, "human.png");
        }

        for (int i = 1; i < currentPlayerLevel; i++) {
            player.levelUp();
        }

        ImageIcon playerIcon = (ImageIcon) player.getIcon();
        Image playerImg = playerIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
        playerImageLabel.setIcon(new ImageIcon(playerImg));
    }

    // ★ 大幅修正：プレイヤーのレベル（currentPlayerLevel）に応じてエネミーのHPが自動で高くなるロジック
    private void spawnEnemy() {
        // レベルに応じたHPボーナス（レベルが高ければ高いほど敵のHPが跳ね上がる）
        int hpBonus = (currentPlayerLevel - 1) * 30; 

        if (enemyCount == 1) {
            int enemyHp = 100 + hpBonus;
            enemy = new Enemy("スライム", enemyHp, 12, 25, 20, "smile enamy.png"); 
            logTextArea.append("【だい１せん】スライム があらわれた！ (敵HP: " + enemyHp + ")\n");
            backgroundLabel.setIcon(new ImageIcon("fristbackgroud.png")); 
        } else if (enemyCount == 2) {
            int enemyHp = 150 + hpBonus;
            enemy = new Enemy("dragon", enemyHp, 16, 10, 25, "dargon enamy.png"); 
            logTextArea.append("【だい２せん】dragon があらわれた！ (敵HP: " + enemyHp + ")\n");
            backgroundLabel.setIcon(new ImageIcon("second backgroud.png"));
        } else if (enemyCount == 3) {
            int enemyHp = 200 + hpBonus;
            enemy = new Enemy("ドラゴン", enemyHp, 22, 10, 30, "Demon enamy.png"); 
            logTextArea.append("【さいしゅうけっせん】でんせつ（Legend）の ドラゴン があらわれた！ (敵HP: " + enemyHp + ")\n");
        } else if (enemyCount == 4) {
            int enemyHp = 1000 + (hpBonus * 2); // ボスはボーナスも2倍！
            enemy = new Enemy("ボス", enemyHp, 25, 10, 50, "dargon.png"); 
            logTextArea.append("【うらのボス戦】真の ボス があらわれた！ (敵HP: " + enemyHp + ")\n");
            backgroundLabel.setIcon(new ImageIcon("final backgroud.png"));
        }

        ImageIcon enemyIcon = (ImageIcon) enemy.getIcon();
        Image enemyImg = enemyIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
        enemyImageLabel.setIcon(new ImageIcon(enemyImg));

        logTextArea.append("--------------------------------------------\n");
    }
}