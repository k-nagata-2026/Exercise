import java.util.ArrayList;
import java.util.List;

public class Skill {
    private String name;//技の名前
    private double multiplier;//技の倍率
    private String type;//技の種類(単体か全体か、回復かバフか)
    private int hpCost;//消費するHP
    private int requiredLevel;
    private String imagePath;
    private int spiritHp;
    private int spiritAtk;
    private int spiritMgc;
    private List<Skill> spiritSkills = new ArrayList<>();//精霊たちの技専用リスト

    //召喚士用のコンストラクタ
    public Skill(String name, double multiplier, String type, int hpCost, int requiredLevel, String imagePath, int spiritHp, int spiritAtk, int spiritMgc) {
        this.name = name;
        this.multiplier = multiplier;
        this.type = type;
        this.hpCost = hpCost;
        this.requiredLevel = requiredLevel;
        this.imagePath = imagePath;
        this.spiritHp = spiritHp;
        this.spiritAtk = spiritAtk;
        this.spiritMgc = spiritMgc;
    }

    //普通のコンストラクタ
    public Skill(String name, double multiplier, String type) {
        this(name, multiplier, type, 0,1,null,0,0, 0);
    }

    //ゲッター
    public String getName() {
        return name;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getType() {
        return type;
    }

    public int getHpCost() {
        return hpCost;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getSpiritHp() {
        return spiritHp;
    }

    public int getSpiritAtk() {
        return spiritAtk;
    }

    public int getSpiritMgc() {
        return spiritMgc;
    }

    public void addSpiritSkill(Skill skill) {
        this.spiritSkills.add(skill);
    }

    public List<Skill> getSpiritSkills() {
        return spiritSkills;
    }
}
