import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Character {
    protected String name;
    protected int hp;
    protected int atk;
    protected int defenc;
    protected int speed;
    protected Icon icon;

    // スーパークラスのコンストラクタ
    public Character(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
        this.defenc = defenc;
        this.speed = speed;
        this.icon = new ImageIcon(imagePath);
    }

    // ----- 共通のゲッターとセッター (Getters & Setters) -----
    public String getName() {
        return this.name;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public Icon getIcon() {
        return this.icon;
    }

    // キャラクターが生存しているか確認するメソッド
    public boolean isAlive() {
        return this.hp > 0;
    }
}
