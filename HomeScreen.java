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
        loadGameButton.addActionListener(e -> {
            java.util.List<Integer> idList =
                GameSaveManager.getSaveIdList();

            if (idList == null || idList.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this, "No Save Data Found!"
                );
                return;
            }

            Object selected = JOptionPane.showInputDialog(
                this,
                "Select Your Save Data:",
                "LOAD GAME",
                JOptionPane.QUESTION_MESSAGE,
                null,
                idList.toArray(),
                idList.get(idList.size() - 1)
            );

            if (selected == null) return;

            int chosenId = (Integer) selected;

            GameSaveManager.SaveData data =
                GameSaveManager.loadGameDataById(chosenId);

            if (data == null) {
                JOptionPane.showMessageDialog(
                    this, "Failed to Load Save Data!"
                );
                return;
            }

            showGameRules(); // पहिले नियम देखाउने

            BattleGame game = new BattleGame(data);
            game.setVisible(true);
            dispose();
        });

        // EXIT
        exitButton.addActionListener(e -> System.exit(0));
    }

    // नियम देखाउने मेथड
    private void showGameRules() {
        String rulesHtml = "<html><body style='font-family: Meiryo, SansSerif; width: 320px; font-size: 13px;'>"
                + "<h3 style='color: #2980b9;'>🎮 Controls & Rules / 操作説明</h3>"
                + "<hr>"
                + "<b>[ W ]</b> : Move Up / 上<br>"
                + "<b>[ S ]</b> : Move Down / 下<br>"
                + "<b>[ A ]</b> : Move Left / 左<br>"
                + "<b>[ D ]</b> : Move Right / 右<br><br>"
                + "<b>[ Space ]</b> : Attack / 攻撃<br>"
                + "<b>[ Enter ]</b> : Safe Zone / セーフゾーン<br><br>"
                + "<span style='color: #27ae60;'><b>Note:</b> Save before closing! / 保存をお忘れなく！</span>"
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
}