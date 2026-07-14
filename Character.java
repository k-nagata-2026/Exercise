import javax.swing.ImageIcon;
public class Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
     // フィールド（Field）のていぎ（Define）
    // （こクラスからもアクセス（Access）できるようにprotectedにする）
    protected String name;   //名前
    protected int hp;        //現在のHP
    protected int maxHp;     //最大HP
    protected int atk;       //攻撃力
    protected int mgc;       //魔力
    protected ImageIcon icon; //画像データを保持するフィールド
    protected int guardFlg = 0; // ガードフラグ（0: ガードしていない、1: ガードしている）
    protected int level;

    // コンストラクタ（Constructor）
    // （しょきか（Initialize）のためのとくべつなメソッド）
    public Character(String name, int hp, int atk, int mgc, String imagePath, int level) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.mgc = mgc;
        this.icon = new ImageIcon(imagePath); // がぞうファイルのよみこみ（Load）
        this.guardFlg = 0; // ガードフラグをリセット
        this.level = level;
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
    
    public int getAtk() {
        return atk;
    }

    public int getMgc() {
        return mgc;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public int level() {
        return level;
    }

    // ★ あいてにこうげきするメソッド（Method）
    public String attack(Character target) {

        if (target instanceof Player && target.guardFlg == 1) { // あいてがガードしているばあい
            //ダメージを半減する計算
            int halfDamage = this.atk / 2;
            target.hp -= halfDamage;
            if (target.hp < 0) {
                target.hp = 0; // HPがマイナス（Minus）にならないようにする
            }
            return this.name + " のこうげき！ " + target.getName()
                   + " はガードしている！ ダメージを" + halfDamage + "におさえた！\n";
        }else {
            // あいてのHPをじぶんのこうげきりょくぶんへらす（Decrease）
            target.hp -= this.atk;
            if (target.hp < 0) {
                target.hp = 0; // HPがマイナス（Minus）にならないようにする
            }
            return this.name + " のこうげき！ " + target.getName()
               + " に " +  this.atk + " のダメージ（Damage）！\n";
        }
    }

    //倍率をかけて攻撃するメソッド（Method）
    public String skill(Character target, double multiplier) {
        int temporaryAtk = (int)(this.atk * multiplier);
        if (target instanceof Player && target.guardFlg == 1) { // あいてがガードしているばあい
            //ダメージを半減する計算
            int halfDamage = temporaryAtk / 2;
            target.hp -= halfDamage;
            if (target.hp < 0) {
                target.hp = 0; // HPがマイナス（Minus）にならないようにする
            }
            return this.name + " のこうげき！ " + target.getName()
                   + " はガードしている！ ダメージを" + halfDamage + "におさえた！\n";
        } else {
            // あいてのHPをじぶんのこうげきりょくぶんへらす（Decrease）
            target.hp -= temporaryAtk;
            if (target.hp < 0) {
                target.hp = 0; // HPがマイナス（Minus）にならないようにする
            }
            return this.name + " のこうげき！ " + target.getName()
               + " に " +  temporaryAtk + " のダメージ（Damage）！\n";
        }
    }

    // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
    public boolean isAlive() {
        return this.hp > 0;
    }


}