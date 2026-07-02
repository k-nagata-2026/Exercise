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
    public void setLevel(int level) {
    this.levelup = level;
}

public void setExp(int exp) {
    this.exp = exp;
}

public String usePotion() {
    if (potion > 0) {
        potion--;
        hp += 30;

        if (hp > maxHp) {
            hp = maxHp;
        }

        return getName() + " used a Potion! HP +30";
    } else {
        return "No Potion left!";
    } 
    }

public String skillAttack(Enemy enemy) {

    int damage = (getAtk() * 2) - enemy.getDeff();

    if (damage < 1) {
        damage = 1;
    }

    enemy.setHp(enemy.getHp() - damage);

    return getName() + " used SKILL!\n"
         + enemy.getName() + " took " + damage + " damage!\n";
}
}
