public class Player extends Character {
    private int level;

    public Player(String name, int hp, int attackPower, int defensePower, String imagePath) {
        super(name, hp, attackPower, defensePower, imagePath);
        this.level = 1;
    }

    public int getLevel() {
        return this.level;
    }

    public String levelUp() {
        this.level++;
        this.maxHp += 20;
        this.hp = this.maxHp;
        // Raato line hatauna super class ko variable call gareko:
        return this.name + " はレベルアップした！ レベル " + this.level + " になった！\n"
             + "最大HPが 20、攻撃力が 5、防御力が 3 上がった！\n";
    }

    public String heal() {
        int healAmount = 30;
        this.hp = Math.min(this.maxHp, this.hp + healAmount);
        return this.name + " はポーション（Potion）を使った！ HPが " + healAmount + " 回復した！\n";
    }

    @Override
    public javax.swing.ImageIcon getIcon() {
        if (super.getIcon() != null) {
            java.awt.Image img = super.getIcon().getImage().getScaledInstance(550, 700, java.awt.Image.SCALE_SMOOTH);
            return new javax.swing.ImageIcon(img);
        }
        return null;
    }

    public void setHp (int hp) {
        this.hp = hp;
    }
}