public class Enemy extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
     public Enemy(String name, int hp, int atk, int deff,int speed, int potion, String imagePath) {
 super(name, hp, atk, deff, speed, potion, imagePath); // おやクラスのコンストラクタをよびだす
 }
}
