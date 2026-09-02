public class Enemy extends Character {
    private int rewardExp;
    private int dropCoin;
    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Enemy(String name, int hp, int atk, int mgc, String imagePath, int level, int dropCoin) {
        super(name, hp, atk, mgc, imagePath, level); // おやクラスのコンストラクタをよびだす
        this.dropCoin = dropCoin * this.level; // ドロップするコインの量を設定
        //最大Lv10
        if (this.level > 10) {
            this.level = 10;
        }

        //敵の種類によって落とす経験値を変える
        int baseExp = 5;
        if (name.contains("ゴブリン")) {
            baseExp = 10;
        } else if (name.contains("ドラゴン")) {
            baseExp = 100;
        }

        this.rewardExp = baseExp * this.level;
    }

    public int getAtk() {
        return this.atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getRewardExp() {
        return this.rewardExp;
    }

    public int getDropCoin() {
        return this.dropCoin;
    }
}