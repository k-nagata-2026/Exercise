public class Enemy extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Enemy(String name, int hp, int atk, String imagePath) {
     super(name, hp, atk, imagePath); // おやクラスのコンストラクタをよびだす
    }
}