import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public class Enemy {
    private String name;
    private int hp;
    private int maxHp;
    private int attackPower;
    private int defensePower;
    private Icon icon; // Uses Icon to support custom procedural drawing

    public Enemy(String name, int hp, int attackPower, int defensePower, String enemyType) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attackPower = attackPower;
        this.defensePower = defensePower;
        this.icon = createEnemyIcon(name); // Generates the enemy image using Java code!
    }

    // This method handles drawing the enemies automatically using vectors and shapes
    private Icon createEnemyIcon(String enemyName) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Enable anti-aliasing for smooth graphic edges
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (enemyName.contains("Knight")) {
                    // --- DRAWING: SHADOW KNIGHT ---
                    // Purple Background Glow Effect
                    g2d.setColor(new Color(75, 0, 130, 80)); 
                    g2d.fillOval(x + 100, y + 150, 300, 400);

                    // Main Body Armor
                    g2d.setColor(new Color(45, 45, 45)); 
                    int[] bodyX = {x + 150, x + 350, x + 300, x + 200};
                    int[] bodyY = {y + 300, y + 300, y + 550, y + 550};
                    g2d.fillPolygon(bodyX, bodyY, 4);

                    // Knight Helmet
                    g2d.setColor(new Color(25, 25, 25)); 
                    g2d.fillOval(x + 200, y + 180, 100, 120);

                    // Red Glowing Eyes
                    g2d.setColor(Color.RED); 
                    g2d.fillOval(x + 220, y + 220, 15, 8);
                    g2d.fillOval(x + 265, y + 220, 15, 8);

                    // Greatsword Weapon
                    g2d.setColor(Color.LIGHT_GRAY); 
                    g2d.fillRect(x + 120, y + 200, 20, 250); 

                } else if (enemyName.contains("Dragon")) {
                    // --- DRAWING: DRAGON ---
                    // Massive Dark Red Wings
                    g2d.setColor(new Color(128, 0, 0)); 
                    g2d.fillOval(x + 50, y + 220, 180, 250);
                    g2d.fillOval(x + 270, y + 220, 180, 250);

                    // Core Dragon Body
                    g2d.setColor(new Color(178, 34, 34)); 
                    g2d.fillOval(x + 150, y + 250, 200, 300);

                    // Dragon Head Triangle
                    g2d.setColor(new Color(139, 0, 0)); 
                    int[] headX = {x + 200, x + 300, x + 250};
                    int[] headY = {y + 250, y + 250, y + 130};
                    g2d.fillPolygon(headX, headY, 3);

                    // Sharp Yellow Eyes
                    g2d.setColor(Color.YELLOW); 
                    g2d.fillOval(x + 225, y + 180, 12, 12);
                    g2d.fillOval(x + 263, y + 180, 12, 12);

                } else {
                    // --- DRAWING: DEFAULT SLIME (LEVEL 1) ---
                    // Blue Gel Slime Body
                    g2d.setColor(new Color(0, 191, 255, 200)); 
                    g2d.fillOval(x + 175, y + 300, 150, 120);
                    
                    // Cute White Outer Eyes
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(x + 210, y + 340, 20, 20);
                    g2d.fillOval(x + 270, y + 340, 20, 20);
                    
                    // Black Inner Pupils
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(x + 218, y + 348, 8, 8);
                    g2d.fillOval(x + 278, y + 348, 8, 8);
                }