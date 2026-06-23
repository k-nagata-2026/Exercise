import javax.swing.ImageIcon;

public class Character {
    // フィールド（Field）のていぎ（Define）
    // （こクラスからもアクセス（Access）できるようにprotectedにする）
    protected String name;   // なまえ（Name）
    protected int hp;        // げんざいのHP（Current HP）
    protected int level = 1;
    protected int mp;
    protected int maxHp;     // さいだいHP（Max HP）
    protected int maxMp;
    protected int atk;       // こうげきりょく（Attack Power）
    protected ImageIcon icon; // がぞうデータをほじするフィールド
    protected int deffence;

    // コンストラクタ（Constructor）
    // （しょきか（Initialize）のためのとくべつなメソッド）
    public Character(String name, int hp, int atk , int deffence, String imagePath) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.maxMp = 50; // しょきMPを50にしてい
        this.mp = 50;
        this.atk = atk;
        this.deffence = deffence;
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
        return level;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    // ★ あいてにこうげきするメソッド（Critical & Miss ついか）
    public String attack(Character target) {
        // ★ 0.0 以上 1.0 未満のランダムな数字を返す
        double rand = Math.random();
        
        // ★ 10% の確率で攻撃がミス（Miss）になる処理
        if (rand < 0.10) {
            return this.name + " のこうげき！ しかし ミス（Miss）した！\n";
        }
        
        int baseDamage = Math.max(1, this.atk - target.deffence);
        String message = "";
        
        // ★ 20% の確率でクリティカル（Critical Hit - 2倍ダメージ）になる処理
        if (rand > 0.80) {
            baseDamage = baseDamage * 2;
            message = "★ 会心の一撃（Critical Hit）！ ";
        }
        
        target.hp -= baseDamage;
        if (target.hp < 0) {
            target.hp = 0; // HPがマイナス（Minus）にならないようにする
        }
        
        return message + this.name + " のこうげき！ " + target.getName()
               + " に " + baseDamage + " のダメージ（Damage）！\n";
    }

    // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
    public boolean isAlive() {
        return this.hp > 0;
    }
}