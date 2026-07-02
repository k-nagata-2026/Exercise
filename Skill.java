public class Skill {
    private String name;//技の名前
    private double multiplier;//技の倍率
    private String type;//技の種類(単体か全体か、回復かバフか)

    //コンストラクタ
    public Skill(String name, double multiplier, String type) {
        this.name = name;
        this.multiplier = multiplier;
        this.type = type;
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
}
