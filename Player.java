public class Player extends Character {
    // ★ ついか：プレイヤーのレベル管理用
    private int level = 1;

    public Player(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        super(name, hp, atk, defenc, speed, imagePath); // おやクラスのコンストラクタをよびだす
    }

    // ★ ついか：HPをそとからセットするためのメソッド
    public void setHp(int hp) {
        this.hp = hp;
    }

    // ★ ついか：現在のレベルを取得するメソッド
    public int getLevel() {
        return this.level;
    }

    // ★ ついか：レベルアップのしょり（ATKやMaxHPをふやす）
    public void levelUp() {
        this.level++;
        // レベルアップごとに攻撃力を 15 ふやし、HPを最大まで回復する
        this.atk += 15; 
        this.hp = this.maxHp; 
    }

    // ★ ついか：ランダム要素（Miss / Critical）が入った攻撃メソッド
    public String attack(Enemy enemy) {
        double chance = Math.random(); // 0.0 から 1.0 のランダムな数字

        // ① 10%の確率で攻撃がミス（外れる）
        if (chance < 0.10) {
            return "💨 " + this.name + " の攻撃は外れた（Missed）！ ダメージは 0 だ！\n";
        }

        int finalDamage = this.atk;

        // ② 15%の確率でクリティカルヒット（大ダメージ）
        if (chance > 0.85) {
            finalDamage = (int)(this.atk * 2.0); // ダメージ2倍
            finalDamage = (int)(finalDamage * (0.9 + Math.random() * 0.2)); // さらに少しランダム変動
            enemy.setHp(enemy.getHp() - finalDamage);
            return "🔥 クリティカルヒット（Critical Hit）！！\n⚔ " + this.name + " は " + enemy.getName() + " に " + finalDamage + " の大ダメージを与えた！\n";
        }

        // ③ 通常の攻撃（85%〜115%の間で少しランダムに変動）
        finalDamage = (int)(this.atk * (0.85 + Math.random() * 0.3));
        enemy.setHp(enemy.getHp() - finalDamage);
        return "⚔ " + this.name + " の攻撃！ " + enemy.getName() + " に " + finalDamage + " のダメージを与えた！\n";
    }
}