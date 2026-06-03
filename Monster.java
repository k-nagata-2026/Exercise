public class Monster {
    String name;
    int hp;
    int atk;

    public Monster(String name, int hp, int atk) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
    }
    public int attack() {
        return this.atk;
    }
    public void receiveDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0) this.hp = 0;
    }
}
