import java.awt.*;
import javax.swing.*;

public class HomeScreen extends JFrame {

    public HomeScreen() {
        setTitle("RPG GAME");
        setSize(1420, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon("Homescreen.png");
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(
            1420, 700, Image.SCALE_SMOOTH
        );

        JLabel background = new JLabel(new ImageIcon(resizedImg));
        background.setLayout(null);
        setContentPane(background);

        JButton newGameButton = new JButton("NEW GAME");
        JButton loadGameButton = new JButton("LOAD GAME");
        JButton exitButton = new JButton("EXIT");

        JButton[] buttons = {
            newGameButton, loadGameButton, exitButton
        };

        int y = 380;

        for (JButton button : buttons) {
            button.setBounds(535, y, 350, 60);
            button.setFont(new Font("Times New Roman", Font.BOLD, 26));
            button.setForeground(new Color(255, 215, 0));
            button.setBackground(new Color(25, 25, 25));
            button.setFocusPainted(false);
            background.add(button);
            y += 75;
        }

        // NEW GAME
        newGameButton.addActionListener(e -> {
            try {
                showGameRules(); // पहिले नियम देखाउने
                CharacterSelect cs = new CharacterSelect();
                cs.setVisible(true);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "Character Select Error: " + ex.getMessage()
                );
            }
        });

        // LOAD GAME
        
      loadGameButton.addActionListener(e -> openLoadGameDialog());

      exitButton.addActionListener(e -> System.exit(0));
    }

  // नियम देखाउने मेथड
    private void showGameRules() {
        String rulesHtml = "<html><body style='font-family: Meiryo, SansSerif; width: 340px; font-size: 13px;'>"
                + "<h3 style='color: #2980b9;'>🎮 Controls & Rules / 操作説明</h3>"
                + "<hr>"
                + "<b>[ ↑ ]</b> : Move Up / 上へ移動<br>"
                + "<b>[ ↓ ]</b> : Move Down / 下へ移動<br>"
                + "<b>[ ← ]</b> : Move Left / 左へ移動<br>"
                + "<b>[ →]</b> : Move Right / 右へ移動<br><br>"
                + "<b>[ Space ]</b> : Attack / 攻撃<br>"
                + "<b>[ Enter ]</b> : Safe Shield / セーフシールド<br>"
                + "<b>[ P ]</b> : Use Potion / ポーション使用<br>"
                + "<b>[ I ]</b> : Open Item Box / アイテム使用<br><br>"
                + "<b>[ F ]</b> : Use Skill / スキル使用<br><br>"
                + "<span style='color: #27ae60;'><b>Note:</b> Save before closing! / 終了前に保存をお忘れなく！</span>"
                + "</body></html>";

        JOptionPane.showMessageDialog(
            this,
            rulesHtml,
            "Game Controls / 操作方法",
            JOptionPane.INFORMATION_MESSAGE
        );
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomeScreen().setVisible(true);
        });
    }

    // LOAD र DELETE गर्ने डायलग खोल्ने मेथड
    private void openLoadGameDialog() {
        java.util.List<Integer> idList = GameSaveManager.getSaveIdList();

        if (idList == null || idList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No Save Data Found! / セーブデータがありません。");
            return;
        }

        JComboBox<Integer> saveComboBox = new JComboBox<>(idList.toArray(new Integer[0]));
        saveComboBox.setSelectedItem(idList.get(idList.size() - 1));
        saveComboBox.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel panel = new JPanel(new java.awt.GridLayout(2, 1, 5, 10));
        panel.add(new JLabel("Select Your Save ID / セーブIDを選択:"));
        panel.add(saveComboBox);

        Object[] options = {"▶ LOAD / ロード", "🗑 DELETE / 削除", "Cancel"};

        int choice = JOptionPane.showOptionDialog(
            this,
            panel,
            "LOAD OR DELETE SAVE DATA",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (saveComboBox.getSelectedItem() == null) return;
        int chosenId = (Integer) saveComboBox.getSelectedItem();

        // १. यदि LOAD थिचियो भने
        if (choice == 0) {
            GameSaveManager.SaveData data = GameSaveManager.loadGameDataById(chosenId);

            if (data == null) {
                JOptionPane.showMessageDialog(this, "Failed to Load Save Data!");
                return;
            }

            showGameRules();

            BattleGame game = new BattleGame(data);
            game.setVisible(true);
            dispose();
        } 
        // २. यदि DELETE थिचियो भने Database बाटै डिलिट गर्ने
        else if (choice == 1) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete Save ID: " + chosenId + " from the database?\n" +
                "データベースから「Save ID: " + chosenId + "」を完全に削除しますか？",
                "Confirm Delete / 削除確認",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = GameSaveManager.deleteSaveById(chosenId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "✅ Save ID " + chosenId + " deleted from database!");
                    openLoadGameDialog();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Failed to delete Save ID " + chosenId + "!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}