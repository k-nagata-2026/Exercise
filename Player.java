import java.util.ArrayList;
import java.util.List;

public class Player extends Character {
    //プレイヤーが覚えている技のリスト
    private List<Skill> skills;

    //コンストラクタ
    public Player(String name, int hp, int atk, int mgc, String imagePath, int guardFlg) {
        super(name, hp, atk, mgc, imagePath); // おやクラスのコンストラクタをよびだす
        this.guardFlg = guardFlg;
        this.skills = new ArrayList<>();
    }

    public void guard() {
        guardFlg = 1; // ガードフラグをたてる
    }

    //技をおぼえるメソッド
    public void learnSkill(String skill, double multiplier, String type) {
        skills.add(new Skill(skill, multiplier, type));
    }

    //技のリストを取得するメソッド
    public List<Skill> getSkills() {
        return this.skills;
    }

}