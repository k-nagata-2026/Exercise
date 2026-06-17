public class Enemy extends Character {
    public Enemy(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        super(name, hp, atk, defenc, speed, imagePath);
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    // ★プレイヤーの防御状態（isPlayerDefending）を判定する新しいアタックメソッド
    public String attack(Player player, boolean isPlayerDefending) {
        double chance = Math.random();

        // 10%の確率で敵の攻撃がミス
        if (chance < 0.10) {
            return "💨 " + this.name + " の攻撃を素早くかわした！ ダメージは 0 だ！\n";
        }

        int finalDamage = this.atk;

        // ----- 👿 敵の特殊AI・スキル（ゲームが超難関に！） -----
        
        // 【ステージ1：スライム】30%の確率で回復して粘る
        if (this.name.equals("スライム") && chance > 0.70) {
            this.hp += 35;
            return "💚 スライムは どろどろに分裂して体力を 35 回復した！なかなか倒れない！\n";
        } 
        // 【ステージ2 & 3：ドラゴン系】30%の確率でガード不可の強力な火炎放射
        else if ((this.name.equals("dragon") || this.name.equals("ドラゴン")) && chance > 0.70) {
            finalDamage = (int)(this.atk * 2.3);
            if (isPlayerDefending) {
                finalDamage = (int)(finalDamage * 0.5); // 防御してても半分は喰らう
                player.setHp(player.getHp() - finalDamage);
                return "🔥 悪魔の吐息！ " + this.name + " の火炎放射！！\n🛡️ ガードを突き破り、" + player.getName() + " は " + finalDamage + " の熱気ダメージを受けた！\n";
            }
            player.setHp(player.getHp() - finalDamage);
            return "🔥 ゴォォォ！ " + this.name + " の直撃火炎放射！！！\n🚨 " + player.getName() + " は " + finalDamage + " の圧倒的大ダメージを受けた！\n";
        }
        // 【ファイナルステージ：真のボス】HPが半分(500)以下になると攻撃力が永続2.5倍（狂暴化）
        else if (this.name.equals("ボス") && this.hp <= 500) {
            finalDamage = (int)(this.atk * 2.5);
            if (isPlayerDefending) finalDamage = (int)(finalDamage * 0.3); // 防御で軽減可能
            player.setHp(player.getHp() - finalDamage);
            return "😡 真のボスは血に飢えている（狂暴化・Rage Mode）！！\n☠️ 凄まじい一撃！ " + player.getName() + " は " + finalDamage + " の致命的なダメージを受けた！\n";
        }

        // ----- 通常の計算（クリティカル or 通常ヒット） -----
        if (chance > 0.85) {
            finalDamage = (int)(this.atk * 1.7);
        } else {
            finalDamage = (int)(this.atk * (0.85 + Math.random() * 0.3));
        }

        // 🛡️ プレイヤーが通常防御していた場合のダメージ70%カット処理
        if (isPlayerDefending) {
            finalDamage = (int)(finalDamage * 0.3);
            player.setHp(player.getHp() - finalDamage);
            return "🛡️ キィィン！ " + player.getName() + " は鉄壁の構えで防御した！\n💥 ダメージを大幅に抑え、わずか " + finalDamage + " のダメージを受けた！\n";
        }

        player.setHp(player.getHp() - finalDamage);
        return "💥 " + this.name + " の手痛い反撃！ " + player.getName() + " は " + finalDamage + " のダメージを受けた！\n";
    }
}