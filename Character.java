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
    protected int defense;   //ぼうぎょりょく（Defense Power）

    // コンストラクタ（Constructor）
    // （しょきか（Initialize）のためのとくべつなメソッド）
    public Character(String name, int hp, int atk, String imagePath, int defense) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.icon = new ImageIcon(imagePath); // がぞうファイルのよみこみ（Load）
        this.defense = defense;
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

    public ImageIcon getIcon() {
        return icon;
    }

    public int getDefense() {
        return defense;
    }

    // ★ あいてにこうげきするメソッド（Method）
    public String attack(Character target) {
        // あいてのHPをじぶんのこうげきりょくぶんへらす（Decrease）
        target.hp -= (this.atk - target.defense);
        if (target.hp < 0) {
            target.hp = 0; // HPがマイナス（Minus）にならないようにする
        }
        return this.name + " のこうげき！ " + target.getName()
               + " に " + (this.atk - target.defense) + " のダメージ（Damage）！\n";
    }

    // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
    public boolean isAlive() {
        return this.hp > 0;
    }


}