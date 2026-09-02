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
        Image resizedImg = img.getScaledInstance(1420, 700, Image.SCALE_SMOOTH);

        JLabel background = new JLabel(new ImageIcon(resizedImg));
        background.setLayout(null);
        setContentPane(background);

        JButton startButton = new JButton("START GAME");
        startButton.setBounds(535, 420, 350, 60);
        startButton.setFont(new Font("Times New Roman", Font.BOLD, 26));
        startButton.setForeground(new Color(255, 215, 0));
        startButton.setBackground(new Color(25, 25, 25));
        startButton.setFocusPainted(false);

       JButton exitButton = new JButton("EXIT");
        exitButton.setBounds(535, 500, 350, 60);
       exitButton.setFont(new Font("Times New Roman", Font.BOLD, 26));
       exitButton.setForeground(new Color(255, 215, 0));
       exitButton.setBackground(new Color(25, 25, 25));
       exitButton.setFocusPainted(false);

        background.add(startButton);
        background.add(exitButton);

        startButton.addActionListener(e -> {

    JOptionPane.showMessageDialog(this, "Character Select Open");

    new CharacterSelect().setVisible(true);

    dispose();

});

     exitButton.addActionListener(e -> {
         System.exit(0);
       });
 }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomeScreen().setVisible(true);
        });
    }
}