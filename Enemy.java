public class Enemy extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Enemy(String name, int hp, int atk, int mgc, String imagePath) {
        super(name, hp, atk, mgc, imagePath); // おやクラスのコンストラクタをよびだす
    }

    public int getAtk() {
        return this.atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }
}