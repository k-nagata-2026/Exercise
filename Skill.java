public class Skill {
    private String name;//技の名前
    private double multiplier;//技の倍率
    private String type;//技の種類(単体か全体か、回復かバフか)
    private int hpCost;//消費するHP
    private int requiredLevel;
    private String imagePath;

    //召喚士用のコンストラクタ
    public Skill(String name, double multiplier, String type, int hpCost, int requiredLevel, String imagePath) {
        this.name = name;
        this.multiplier = multiplier;
        this.type = type;
        this.hpCost = hpCost;
        this.requiredLevel = requiredLevel;
        this.imagePath = imagePath;
    }

    //普通のコンストラクタ
    public Skill(String name, double multiplier, String type) {
        this(name, multiplier, type, 0,1,null);
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
}
