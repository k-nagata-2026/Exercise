public class Enemy extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Enemy(String name, int hp, int atk, int mgc, String imagePath) {
     super(name, hp, atk, mgc, imagePath); // おやクラスのコンストラクタをよびだす
    }
}