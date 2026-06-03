public class Player {
    String name;
    int hp;

    public Player(String name, int hp) {
        this.name = name;
        this.hp = hp;
    }

    public void receiveDamage() {
        this.hp -= 1;
    }
}