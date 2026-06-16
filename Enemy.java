public class Enemy extends Character {
    public Enemy(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        super(name, hp, atk, defenc, speed, imagePath); // おやクラスのコンストラクタをよびだす
    }

    // ★ ついか：エネミーのHPをそとからセットするためのメソッド
    public void setHp(int hp) {
        this.hp = hp;
    }

    // ★ ついか：ランダム要素（Miss / Critical）が入った攻撃メソッド
    public String attack(Player player) {
        double chance = Math.random(); // 0.0 から 1.0 のランダムな数字

        // ① 10%の確率で攻撃がミス（外れる）
        if (chance < 0.10) {
            return "💨 " + this.name + " の攻撃をかわした！ ダメージは 0 だ！\n";
        }

        int finalDamage = this.atk;

        // ② 15%の確率でクリティカルヒット（大ダメージ）
        if (chance > 0.85) {
            finalDamage = (int)(this.atk * 2.0); // ダメージ2倍
            finalDamage = (int)(finalDamage * (0.9 + Math.random() * 0.2)); // さらに少しランダム変動
            player.setHp(player.getHp() - finalDamage);
            return "🚨 危ない！ " + this.name + " の痛恨の一撃（Critical Hit）！！\n💥 " + player.getName() + " は " + finalDamage + " の大ダメージを受けた！\n";
        }

        // ③ 通常の攻撃（85%〜115%の間で少しランダムに変動）
        finalDamage = (int)(this.atk * (0.85 + Math.random() * 0.3));
        player.setHp(player.getHp() - finalDamage);
        return "💥 " + this.name + " の反撃！ " + player.getName() + " は " + finalDamage + " のダメージを受けた！\n";
    }
}