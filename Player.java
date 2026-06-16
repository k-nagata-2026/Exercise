import javax.swing.ImageIcon;

public class Player {
    private String name;
    private int hp;
    private int maxHp;
    private int attackPower;
    private int defensePower;
    private ImageIcon icon;

    public Player(String name, int hp, int attackPower, int defensePower, String imagePath) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attackPower = attackPower;
        this.defensePower = defensePower;
        this.icon = new ImageIcon(imagePath);
    }

    public String attack(Enemy enemy) {
        int damage = this.attackPower - (enemy.getDefensePower() / 2);
        if (damage < 1) damage = 1; 
        enemy.setHp(enemy.getHp() - damage);
        return String.format("▶ %s attack! %s took %d damage!\n", this.name, enemy.getName(), damage);
    }

    public boolean isAlive() { return this.hp > 0; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = Math.max(0, hp); }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getAttackPower() { return attackPower; }
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }
    public int getDefensePower() { return defensePower; }
    public ImageIcon getIcon() { return icon; }
}