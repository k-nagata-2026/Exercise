public class Player extends Character {
    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Player(String name, int hp, int atk, String imagePath, int defense) {
        super(name, hp, atk, imagePath, defense); // おやクラスのコンストラクタをよびだす
    }
}