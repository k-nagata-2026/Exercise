public class Enemy extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Enemy(String name, int hp, int atk, int mgc, String imagePath,String backgroundImagePath) {
        super(name, hp, atk, mgc, imagePath, backgroundImagePath); // おやクラスのコンストラクタをよびだす
        this.backgroundImagePath = backgroundImagePath; // 背景画像の設定
    }

    //背景画像のゲッター
    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }
}