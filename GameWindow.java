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
