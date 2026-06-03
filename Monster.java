import javax.swing.Icon;

public class Monster {
    String name;
    int hp;
    int atk;
    Icon icon;

    public Monster(String name, int hp, int atk, Icon icon) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
        this.icon = icon;
    }

    public int attack() {
        return this.atk;
    }

    public void receiveDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0)
            this.hp = 0;
    }
}
