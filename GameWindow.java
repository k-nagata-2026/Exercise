import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 800;
    private static final int ICON_SIZE = 180;

    Player player;
    Monster monster;
    JLabel playerIconLabel = new JLabel();
    JLabel playerInfoLabel = new JLabel();
    JLabel monsterIconLabel = new JLabel();
    JLabel monsterInfoLabel = new JLabel();
    JLabel killCountLabel = new JLabel();
    JTextArea logArea = new JTextArea();
    JButton attackButton = new JButton("攻撃する");

    public GameWindow() {
        setTitle("Java Bronze Battle v1.0");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        player = new Player("勇者", 100, 20, loadIcon("Human.png", ICON_SIZE, ICON_SIZE));
        monster = new Monster("スライム", 50, 10, loadIcon("Slime.png", ICON_SIZE, ICON_SIZE));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 14));
        statusPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, ICON_SIZE + 40));
        playerIconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        monsterIconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        playerInfoLabel.setFont(new Font("MS ゴシック", Font.BOLD, 24));
        monsterInfoLabel.setFont(new Font("MS ゴシック", Font.BOLD, 24));
        killCountLabel.setFont(new Font("MS ゴシック", Font.PLAIN, 24));

        statusPanel.add(playerIconLabel);
        statusPanel.add(playerInfoLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(monsterIconLabel);
        statusPanel.add(monsterInfoLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(killCountLabel);

        updateStatus();

        logArea.setEditable(false);
        logArea.setFont(new Font("MS ゴシック", Font.PLAIN, 16));
        add(statusPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(attackButton, BorderLayout.SOUTH);
        attackButton.addActionListener(e -> battleTurn());
    }

    private void battleTurn() {
        int pDmg = player.attack();
        monster.receiveDamage(pDmg);
        logArea.append(player.name + "の攻撃！ " + monster.name + "に" + pDmg + "のダメージ！\n");
        if (monster.hp <= 0) {
            logArea.append(monster.name + "を倒した！\n");
            player.killCount++;
            if (player.killCount >= 10) {
                logArea.append("★★★ 10体撃破！ゲームクリア！ ★★★\n");
                attackButton.setEnabled(false);
            } else {
                monster = new Monster("スライム", 50, 10, loadIcon("Slime.png", ICON_SIZE, ICON_SIZE));
                logArea.append("新しい" + monster.name + "が現れた！\n");
            }
        } else {
            int mDmg = monster.attack();
            player.receiveDamage(mDmg);
            logArea.append(monster.name + "の反撃！ " + player.name + "は" + mDmg + "のダメージ！\n");
            if (player.hp <= 0) {
                logArea.append(player.name + "は力尽きた... GAME OVER\n");
                attackButton.setEnabled(false);
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        playerIconLabel.setIcon(player.icon);
        playerInfoLabel.setText(player.name + " HP: " + player.hp);
        monsterIconLabel.setIcon(monster.icon);
        monsterInfoLabel.setText(monster.name + " HP: " + monster.hp);
        killCountLabel.setText("討伐数: " + player.killCount + "/10");
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image image = icon.getImage();
        Image resized = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }

    public static void main(String[] args) {
        new GameWindow().setVisible(true);
    }
}