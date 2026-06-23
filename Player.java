public class Player extends Character {

    public Player(String name, int hp, int attackPower, int defensePower, String imagePath) {
        super(name, hp, attackPower, defensePower, imagePath);
    }

    public void setHp(int hp) {
        this.hp = Math.min(this.maxHp, Math.max(0, hp));
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        this.mp = Math.min(this.maxMp, Math.max(0, mp));
    }

    // ★ レベルアップ処理のパワーアップ（Level Up Process）
    public void levelUp() {
        this.level++;
        this.maxHp += 20;     // さいだいHP（Max HP）を 20 ふやす
        this.atk += 15;       // こうげきりょく（Attack Power）を 15 ふやす
        this.hp = this.maxHp; // HP全回復（Full HP）
        this.mp = this.maxMp; // MP全回復（Full MP）
    }

    // ★ かいふく（Heal）メソッドをついか（HPを回復する）
    public String heal() {
        int healAmount = 30; // 30ポイント回復する
        this.hp = Math.min(this.maxHp, this.hp + healAmount);
        return this.name + " はポーション（Potion）を使った！ HPが " + healAmount + " 回復した！\n";
    }
}