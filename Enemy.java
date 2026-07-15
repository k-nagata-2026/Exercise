public class Enemy extends Character {
    public Enemy(String name, int hp, int attackPower, int defensePower, String imagePath) {
        super(name, hp, attackPower, defensePower, imagePath);
    }

    @Override
    public javax.swing.ImageIcon getIcon() {
        if (super.getIcon() != null) {
            java.awt.Image img = super.getIcon().getImage().getScaledInstance(550, 700, java.awt.Image.SCALE_SMOOTH);
            return new javax.swing.ImageIcon(img);
        }
        return null;
    }
}