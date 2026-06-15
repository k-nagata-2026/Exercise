public class Player extends Character {
  

    // わくぐみのみ（つぎのステップでなかみをかきます）
    public Player(String name, int hp, int atk, String imagePath, int guardFlg) {
        super(name, hp, atk, imagePath); // おやクラスのコンストラクタをよびだす
        this.guardFlg = guardFlg;
    }
    public void guard() {
        guardFlg = 1; // ガードフラグをたてる
    }

}