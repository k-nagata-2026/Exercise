import java.awt.*;
import javax.swing.*;

public class CharacterSelect extends JFrame {

    private JButton hero1;
    private JButton hero2;
    private JButton hero3;
    private JButton hero4;

    private JLabel selectedLabel;
    private JLabel hpLabel;
    private JLabel atkLabel;
    private JLabel defLabel;
    private JLabel speedLabel;
    private Player selectedPlayer;
    private JButton startButton;

    public CharacterSelect() {
        System.out.println("CharacterSelect opened");
        setTitle("Character Select");
        setSize(1800,1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel bg = new JLabel(new ImageIcon("characterselect.png"));
        bg.setLayout(null);

        JLabel title = new JLabel("SELECT YOUR HERO");
        title.setFont(new Font("Arial",Font.BOLD,40));
        title.setForeground(Color.black);
        title.setBounds(320,20,600,50);
        bg.add(title);

        selectedLabel = new JLabel("SELECTED : NONE");
        selectedLabel.setFont(new Font("Arial",Font.BOLD,28));
        selectedLabel.setForeground(Color.CYAN);
        selectedLabel.setBounds(380,70,450,35);
        bg.add(selectedLabel);

        hero1 = new JButton(new ImageIcon("BHOLA.png"));
        hero1.setOpaque(false);
        hero1.setContentAreaFilled(false);
        hero1.setBorderPainted(false);
        hero1.setBounds(30,130,220,320);
        hero1.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
        hero1.setContentAreaFilled(false);
        hero1.setFocusPainted(false);
        bg.add(hero1);

        hero2 = new JButton(new ImageIcon("BIYON.png"));
        hero2.setBounds(300,130,220,320);
        hero2.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
        hero2.setContentAreaFilled(false);
        hero2.setFocusPainted(false);
        bg.add(hero2);

        hero3 = new JButton(new ImageIcon("ladyboss.png"));
        hero3.setBounds(570,130,220,320);
        hero3.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
        hero3.setContentAreaFilled(false);
        hero3.setFocusPainted(false);
        bg.add(hero3);

        hero4 = new JButton(new ImageIcon("bigboss.png"));
        hero4.setBounds(840,130,220,320);
        hero4.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
        hero4.setContentAreaFilled(false);
        hero4.setFocusPainted(false);
        bg.add(hero4);

        JLabel h1 = new JLabel("BHOLA");
        h1.setForeground(Color.YELLOW);
        h1.setBounds(100,455,120,30);
        bg.add(h1);

        JLabel h2 = new JLabel("BIYON");
        h2.setForeground(Color.YELLOW);
        h2.setBounds(380,455,120,30);
        bg.add(h2);

        JLabel h3 = new JLabel("LADY BOSS");
        h3.setForeground(Color.YELLOW);
        h3.setBounds(620,455,150,30);
        bg.add(h3);

        JLabel h4 = new JLabel("BIG BOSS");
        h4.setForeground(Color.YELLOW);
        h4.setBounds(900,455,150,30);
        bg.add(h4);

        JPanel stats = new JPanel();
        stats.setLayout(new GridLayout(5,1));
        stats.setBackground(new Color(40,40,40));

        hpLabel = new JLabel("HP : -");
        atkLabel = new JLabel("ATK : -");
        defLabel = new JLabel("DEF : -");
        speedLabel = new JLabel("SPEED : -");

        hpLabel.setForeground(Color.WHITE);
        atkLabel.setForeground(Color.WHITE);
        defLabel.setForeground(Color.WHITE);
        speedLabel.setForeground(Color.WHITE);

        hpLabel.setFont(new Font("Arial",Font.BOLD,18));
        atkLabel.setFont(new Font("Arial",Font.BOLD,18));
        defLabel.setFont(new Font("Arial",Font.BOLD,18));
        speedLabel.setFont(new Font("Arial",Font.BOLD,18));

        stats.add(new JLabel("CHARACTER INFO"));
        stats.add(hpLabel);
        stats.add(atkLabel);
        stats.add(defLabel);
        stats.add(speedLabel);

        stats.setBounds(900,510,240,150);
        bg.add(stats);
        revalidate();
        repaint();
        add(bg);

                hero1.addActionListener(e -> {

    selectedPlayer = new Player(
            "BHOLA",
            150,
            20,
            10,
            10,
            30,
            "BHOLA.png");

    selectHero(hero1,"BHOLA",150,20,10,10);

    startButton.setEnabled(true);

     });

        hero2.addActionListener(e -> {

    selectedPlayer = new Player(
            "BIYON",
            200,
            25,
            10,
            15,
            30,
            "BIYON.png");

    selectHero(hero2,"BIYON",200,25,10,15);

    startButton.setEnabled(true);

        });

        hero3.addActionListener(e -> {

           selectedPlayer = new Player(
                    "LadyBoss",
                    100,
                    15,
                    20,
                    10,
                    30,
                    "ladyboss.png");
 selectHero(hero3,"LADY BOSS",100,15,20,10);
   startButton.setEnabled(true);

        });

        hero4.addActionListener(e -> {

            selectedPlayer = new Player(
                    "BigBoss",
                    150,
                    40,
                    0,
                    15,
                    30,
                    "bigboss.png");
selectHero(hero4,"BIG BOSS",150,40,0,15);

           startButton.setEnabled(true);

        });
        

       startButton = new JButton("START GAME");
startButton.setBounds(450,600,250,50);
startButton.setEnabled(false);
bg.add(startButton);
        startButton.addActionListener(e -> {

    if(selectedPlayer == null){
        JOptionPane.showMessageDialog(this,"Please Select Character");
        return;
    }

    new BattleGame(selectedPlayer);
    dispose();

        });
    }

    private void selectHero(JButton hero,
                        String name,
                        int hp,
                        int atk,
                        int def,
                        int speed) {

    hero1.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    hero2.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    hero3.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    hero4.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));

    hero.setBorder(BorderFactory.createLineBorder(Color.GREEN,5));

    selectedLabel.setText("SELECTED : " + name);

    hpLabel.setText("HP : " + hp);
    atkLabel.setText("ATK : " + atk);
    defLabel.setText("DEF : " + def);
    speedLabel.setText("SPEED : " + speed);
}

private void addHoverEffect(JButton button) {

    button.addMouseListener(new java.awt.event.MouseAdapter() {

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            button.setBorder(BorderFactory.createLineBorder(Color.YELLOW,4));
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            button.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
        }

    });
  
}
}
