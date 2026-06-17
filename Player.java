public class Player extends Character {
    private int level = 1;
    private int mp = 0; // ★ついか：スキルを使うためのマジックポイント
    private final int maxMp = 100;

    public Player(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        super(name, hp, atk, defenc, speed, imagePath);
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getLevel() {
        return this.level;
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        this.mp = Math.min(this.maxMp, Math.max(0, mp));
    }

    public void levelUp() {
        this.level++;
        this.atk += 15; 
        this.hp = this.maxHp; 
        this.mp = 0; // レベルアップでMPはリセット
    }

    // ★ランダム要素とMP増減が入った通常攻撃
    public String attack(Enemy enemy) {
        double chance = Math.random();

        // 10%の確率でミス
        if (chance < 0.10) {
            this.setMp(this.mp + 5); // ミスしても少しMPが貯まる
            return "💨 " + this.name + " の攻撃は外れた（Missed）！ ダメージは 0 だ！ (MP+5)\n";
        }

        int finalDamage = this.atk;
        this.setMp(this.mp + 15); // 通常攻撃が当たるとMPが15貯まる

        // 15%の確率でクリティカル
        if (chance > 0.85) {
            finalDamage = (int)(this.atk * 2.0);
            finalDamage = (int)(finalDamage * (0.9 + Math.random() * 0.2));
            enemy.setHp(enemy.getHp() - finalDamage);
            return "🔥 クリティカルヒット（Critical Hit）！！\n⚔ " + this.name + " は " + enemy.getName() + " に " + finalDamage + " の大ダメージを与えた！ (MP+15)\n";
        } 

        // 通常ヒット
        finalDamage = (int)(this.atk * (0.85 + Math.random() * 0.3));
        enemy.setHp(enemy.getHp() - finalDamage);
        return "⚔ " + this.name + " の攻撃！ " + enemy.getName() + " に " + finalDamage + " のダメージを与えた！ (MP+15)\n";
    }

    // ★ついか：MPを50消費して放つ必殺スキル
    public String useSkill(Enemy enemy) {
        this.setMp(this.mp - 50); // MPを50消費
        int finalDamage = (int)(this.atk * 2.5); // 攻撃力の2.5倍の確定大ダメージ
        finalDamage = (int)(finalDamage * (0.95 + Math.random() * 0.1));
        enemy.setHp(enemy.getHp() - finalDamage);
        return "⚡「くらえぃ！」" + this.name + " の必殺スキルアタック！！\n💥 " + enemy.getName() + " に " + finalDamage + " の超絶ダメージを与えた！\n";
    }
}