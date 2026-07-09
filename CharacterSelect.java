import java.awt.*;
import javax.swing.*;

public class CharacterSelect extends JFrame {

    private Player selectPlayer;

    public CharacterSelect() {

        setTitle("Character Select");
        setSize(1200,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel bg = new JLabel(new ImageIcon("characterselectbg.png"));
        bg.setLayout(null);

        JLabel title = new JLabel("SELECT YOUR HERO");
        title.setFont(new Font("Arial",Font.BOLD,40));
        title.setForeground(Color.WHITE);
        title.setBounds(360,20,600,50);
        bg.add(title);

        // ================= HERO 1 =================
        JButton hero1 = new JButton(new ImageIcon("BHOLA.png"));
        hero1.setBounds(40,120,220,320);
        bg.add(hero1);

        JLabel h1 = new JLabel("BHOLA");
        h1.setForeground(Color.YELLOW);
        h1.setBounds(110,450,150,30);
        bg.add(h1);

        // ================= HERO 2 =================
        JButton hero2 = new JButton(new ImageIcon("BIYON.png"));
        hero2.setBounds(310,120,220,320);
        bg.add(hero2);

        JLabel h2 = new JLabel("BIYON");
        h2.setForeground(Color.YELLOW);
        h2.setBounds(390,450,150,30);
        bg.add(h2);

        // ================= HERO 3 =================
        JButton hero3 = new JButton(new ImageIcon("ladyboss.png"));
        hero3.setBounds(580,120,220,320);
        bg.add(hero3);

        JLabel h3 = new JLabel("LADY BOSS");
        h3.setForeground(Color.YELLOW);
        h3.setBounds(640,450,150,30);
        bg.add(h3);

        // ================= HERO 4 =================
        JButton hero4 = new JButton(new ImageIcon("bigboss.png"));
        hero4.setBounds(850,120,220,320);
        bg.add(hero4);

        JLabel h4 = new JLabel("BIG BOSS");
        h4.setForeground(Color.YELLOW);
        h4.setBounds(920,450,150,30);
        bg.add(h4);

       hero1.addActionListener(e -> {
    new BattleGame(new Player(
            "BHOLA",
            150,
            20,
            10,
            10,
            30,
            "BHOLA.png"
    )).setVisible(true);
    dispose();
});

hero2.addActionListener(e -> {
    new BattleGame(new Player(
            "BIYON",
            200,
            25,
            10,
            15,
            30,
            "BIYON.png"
    )).setVisible(true);
    dispose();
});

hero3.addActionListener(e -> {
    new BattleGame(new Player(
            "LadyBoss",
            100,
            15,
            20,
            10,
            30,
            "ladyboss.png"
    )).setVisible(true);
    dispose();
});

hero4.addActionListener(e -> {

    new BattleGame(new Player(
            "BigBoss",
            150,
            40,
            0,
            15,
            30,
            "bigboss.png"
    )).setVisible(true);
    dispose();
});
    }
      }
