import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Character {
    protected String name;
    protected int hp;
    protected int atk;
    protected int defenc;
    protected int speed;
    protected Icon icon;
    protected String imagePath; 

    // スーパークラスのコンストラクタ
    public Character(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
        this.defenc = defenc;
        this.speed = speed;
        this.imagePath = imagePath;
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

    public int getAtk() {
        return this.atk;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public Icon getIcon() {
        return this.icon;
    }

    // キャラクターが生存しているか確認するメソッド
    public boolean isAlive() {
        return this.hp > 0;
    }
}