import javax.swing.ImageIcon;
public class Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
    // フィールド（Field）のていぎ（Define）
    // （こクラスからもアクセス（Access）できるようにprotectedにする）
    protected String name;   // なまえ（Name）
    protected int hp;        // げんざいのHP（Current HP）
    protected int maxHp;     // さいだいHP（Max HP）
    protected int atk;       // こうげきりょく（Attack Power）
    protected ImageIcon icon; // がぞうデータをほじするフィールド
    protected int deff;
    protected int speed; 
    protected int levelup;
    protected int exp = 0;
    protected int expNeeded = 100;
    protected int potion = 30;

    // コンストラクタ（Constructor）
    // （しょきか（Initialize）のためのとくべつなメソッド）
    public Character(String name, int hp, int atk, int deff, int speed, int potion, String imagePath) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.deff = deff;
        this.speed = speed;
        this.levelup = 1;
        this.icon = new ImageIcon(imagePath); // がぞうファイルのよみこみ（Load）

    }
    
    // そとからあんぜん（Safe）にデータをしゅとく（Get）するためのゲッター（Getter）
    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }
    public int getLevel() {
        return levelup;
    }

    public ImageIcon getIcon() {
        return icon;
    }

   
    public void setHp(int hp) {
            this.hp = Math.max(0, hp);
        }
    
    public int getAtk() {
        return atk;
    }

    public void gainExp(int amount) {
        exp += amount;
        while (exp >= expNeeded) {
            exp -= expNeeded;
            levelup++;

            maxHp += 20;
            hp = maxHp;
            atk += 5;
            deff += 3;
            speed += 2;
            
            expNeeded += 10;
            
            System.out.println("LEVEL UP! Lv."
            + levelup);
        }
    }

     // ★ あいてにこうげきするメソッド（Method）
    public String attack(Character target) {
        // あいてのHPをじぶんのこうげきりょくぶんへらす（Decrease）
        target.hp -= Math.max(1, this.atk - target.deff);
        if (target.hp < 0) {
            target.hp = 0; // HPがマイナス（Minus）にならないようにする
        }
        return this.name + " のこうげき！ " + target.getName()
               + " に " + Math.max(1, this.atk - target.deff) + " のダメージ（Damage）！\n";
    }

    // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
    public boolean isAlive() {
        return this.hp > 0;
    }
}