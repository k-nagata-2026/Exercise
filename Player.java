public class Player extends Character {

    // わくぐみのみ（つぎのステップでなかみをかきます）
     public Player(String name, int hp, int atk, int deff, int speed, int potion, String imagePath) {
        super(name, hp, atk, deff, speed, potion, imagePath); // おやクラスのコンストラクタをよびだす
    }

    public void gainExp(int amount) {
        exp += amount;
        if (exp >= expNeeded) {
            levelup++;
            exp -= expNeeded;
            expNeeded += 100;
            setMaxHp(getMaxHp() + 20);
            setHp(getMaxHp());
            setAtk(getAtk() + 5);

    javax.swing.JOptionPane.showMessageDialog(null,
           "LEVEL UP!\nLv." + levelup + " になった! "
           );        

        }
    }
    public int getLevel() {
        return levelup;
    }
    public int getExp() {
        return exp;
    }
}
