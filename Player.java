public class Player extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
     public Player(String name, int hp, int atk, int deff, int speed, int potion, String imagePath) {
        super(name, hp, atk, deff, speed, potion, imagePath); // おやクラスのコンストラクタをよびだす
    }
}
