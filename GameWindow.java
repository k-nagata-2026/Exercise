import javax.swing.*;
import java.awt.*;
public class GameWindow extends JFrame {
    Player player  = new Player("勇者", 5);
    Player monster = new Player("スライム", 3);
    JLabel statusLabel = new JLabel();
    JTextArea logArea  = new JTextArea();
    JButton attackButton = new JButton("攻撃する");
    public GameWindow() {
        setTitle("シンプルバトル");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        updateStatus();
        logArea.setEditable(false);
        add(statusLabel,              BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(attackButton,             BorderLayout.SOUTH);
        attackButton.addActionListener(e -> battleTurn());
    }
    private void battleTurn() {
        monster.receiveDamage();
        logArea.append(player.name + "の攻撃！ " + monster.name + "のHPが1減った！\n");
        if (monster.hp <= 0) {
            logArea.append(monster.name + "を倒した！ゲームクリア！\n");
            attackButton.setEnabled(false);
            return;
        }
        player.receiveDamage();
        logArea.append(monster.name + "の反撃！ " + player.name + "のHPが1減った！\n");
        if (player.hp <= 0) {
            logArea.append(player.name + "は力尽きた... GAME OVER\n");
            attackButton.setEnabled(false);
        }
        updateStatus();
    }
    private void updateStatus() {
        statusLabel.setText(
            " " + player.name  + " HP:" + player.hp  +
            "  |  " + monster.name + " HP:" + monster.hp
        );
    }
    public static void main(String[] args) {
        new GameWindow().setVisible(true);
    }
}
