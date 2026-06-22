public class Enemy extends Character {
    private int maxHp; // エネミーの最大HPを保持する変数

    public Enemy(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        super(name, hp, atk, defenc, speed, imagePath);
        this.maxHp = hp; // 初期HPを最大HPとして設定
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    // BattleGame.javaのライン289のエラーを解決するためのゲッターメソッド
    public int getMaxHp() {
        return this.maxHp; 
    }

    // エネミーの攻撃メソッド（プレイヤーオブジェクトと防御状態を引数にとる）
    public String attack(Player player, boolean isPlayerDefending) {
        double chance = Math.random();

        // 10%の確率で攻撃がミスする
        if (chance < 0.10) {
            return "💨 " + this.name + " の攻撃をかわした！ ダメージは 0 だ！\n";
        }

        int finalDamage = this.atk;

        // ----- 各エネミーの特殊スキル能力 -----
        if (this.name.equals("スライム") && chance > 0.70) {
            this.hp += 35;
            return "💚 スライムは体力を 35 回復した！\n";
        } 
        else if ((this.name.equals("dragon") || this.name.equals("ドラゴン")) && chance > 0.70) {
            finalDamage = (int)(this.atk * 2.3);
            if (isPlayerDefending) {
                finalDamage = (int)(finalDamage * 0.5); 
                player.setHp(player.getHp() - finalDamage);
                return "🔥 " + this.name + " の火炎放射！！ ガードを突き破られ、 " + player.getName() + " は " + finalDamage + " のダメージを受けた！\n";
            }
            player.setHp(player.getHp() - finalDamage);
            return "🔥 " + this.name + " の直撃火炎放射！！！ " + player.getName() + " は " + finalDamage + " の大ダメージを受けた！\n";
        }
        else if (this.name.equals("ボス") && this.hp <= 500) {
            // ボスの体力が500以下でレイジモード（怒り状態）が発動
            finalDamage = (int)(this.atk * 2.5);
            if (isPlayerDefending) finalDamage = (int)(finalDamage * 0.3);
            player.setHp(player.getHp() - finalDamage);
            return "😡 真のボスは怒り狂っている（Rage Mode）！！ " + player.getName() + " は " + finalDamage + " の致命的なダメージを受けた！\n";
        }

        // クリティカル判定（30%の確率で大ダメージ）
        if (chance > 0.70) {
            finalDamage = (int)(this.atk * 1.7);
        } else {
            finalDamage = (int)(this.atk * (0.85 + Math.random() * 0.3));
        }

        // プレイヤーが「ぼうぎょ」している場合のダメージ軽減処理
        if (isPlayerDefending) {
            finalDamage = (int)(finalDamage * 0.3);
            player.setHp(player.getHp() - finalDamage);
            return "🛡️ " + player.getName() + " は攻撃をガードした！わずか " + finalDamage + " のダメージ！\n";
        }

        player.setHp(player.getHp() - finalDamage);
        return "💥 " + this.name + " の反撃！ " + player.getName() + " は " + finalDamage + " のダメージを受けた！\n";
    }
}