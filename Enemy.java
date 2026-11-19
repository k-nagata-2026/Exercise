import java.util.ArrayList;
import java.util.List;

public class Enemy extends Character {
    private int rewardExp;
    private int dropCoin;
    private List<Skill> skills = new ArrayList<>(); // DBから読み込む敵のスキル一覧

    // DBのENEMYテーブルの値をそのまま使って生成するコンストラクタ
    public Enemy(String name, int hp, int atk, int mgc, String imagePath, int rewardExp, int dropCoin) {
        super(name, hp, atk, mgc, imagePath, 1);
        this.rewardExp = rewardExp;
        this.dropCoin = dropCoin;
    }

    public int getAtk() {
        return this.atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getRewardExp() {
        return this.rewardExp;
    }

    public void setRewardExp(int rewardExp) {
        this.rewardExp = rewardExp;
    }

    public int getDropCoin() {
        return this.dropCoin;
    }

    public void setDropCoin(int dropCoin) {
        this.dropCoin = dropCoin;
    }

    public List<Skill> getSkills() {
        return this.skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
}