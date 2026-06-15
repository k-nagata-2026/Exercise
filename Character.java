import javax.swing.ImageIcon;
public class Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
     // フィールド（Field）のていぎ（Define）
    // （こクラスからもアクセス（Access）できるようにprotectedにする）
    protected String name;   // なまえ（Name）
    protected int hp;        // げんざいのHP（Current HP）
    protected int maxHp;     // さいだいHP（Max HP）
    protected int atk;       // こうげきりょく（Attack Power）
    protected int mgc;       // まほうりょく（Magic Power）
    protected ImageIcon icon; // がぞうデータをほじするフィールド
    protected int defense;   //ぼうぎょりょく（Defense Power）
    protected int guardFlg = 0; // ガードフラグ（0: ガードしていない、1: ガードしている）

    // コンストラクタ（Constructor）
    // （しょきか（Initialize）のためのとくべつなメソッド）
    public Character(String name, int hp, int atk, int mgc, String imagePath) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.mgc = mgc;
        this.icon = new ImageIcon(imagePath); // がぞうファイルのよみこみ（Load）
        this.guardFlg = 0; // ガードフラグをリセット
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

    // ★ あいてにこうげきするメソッド（Method）
    public String attack(Character target) {

        if (target instanceof Player && target.guardFlg == 1) { // あいてがガードしているばあい
            target.guardFlg = 0; // ガードフラグをリセット
            return this.name + " のこうげき！ " + target.getName()
                   + " はガードしている！ ダメージをうけない！\n";
        }
        else {
        // あいてのHPをじぶんのこうげきりょくぶんへらす（Decrease）
        target.hp -= this.atk;
        if (target.hp < 0) {
            target.hp = 0; // HPがマイナス（Minus）にならないようにする
        }
        return this.name + " のこうげき！ " + target.getName()
               + " に " +  this.atk + " のダメージ（Damage）！\n";
    }
    }

    // ★ せいぞんはんてい（Alive Check）メソッド（HPが0よりおおきければ true）
    public boolean isAlive() {
        return this.hp > 0;
    }


}