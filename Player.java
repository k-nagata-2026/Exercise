public class Player extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
     public Player(String name, int hp, int atk, int deff, int speed, String imagePath) {
        super(name, hp, atk, deff, speed, imagePath); // おやクラスのコンストラクタをよびだす
    }
}
