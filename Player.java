import java.util.ArrayList;
import java.util.List;

public class Player extends Character {
    //プレイヤーが覚えている技のリスト
    private List<Skill> skills;
    public boolean isCovered = false;//かばう
    private int exp;
    private int maxExp;

    //コンストラクタ
    public Player(String name, int hp, int atk, int mgc, String imagePath, int guardFlg, int level, int exp, int maxExp) {
        super(name, hp, atk, mgc, imagePath, level); // おやクラスのコンストラクタをよびだす
        this.guardFlg = guardFlg;
        this.skills = new ArrayList<>();
        this.exp = exp;
        this.maxExp = maxExp;
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

    public int getExp() {return this.exp;}
    public void setExp(int exp) {exp = this.exp;}
    public int getMaxExp() {return this.maxExp;}
    public int getLevel() {return this.level;}

    //レベルアップの処理
    public boolean checkLevelUp() {
        boolean leveledUp = false;//レベルアップしたかどうかのメモ
        while (this.exp >= this.maxExp) {
            //すでにLv10ならレベルを上げずにループから抜ける
            if (this.level >= 10) {
                this.exp = 0;
                break;
            }

            //経験値を消費してレベルを１上げる
            this.exp -= this.maxExp;
            this.level++;
            leveledUp = true;

            //次に必要な経験値量を1.5倍にする
            this.maxExp = (int)(this.maxExp * 1.5);

            //キャラクターごとの上昇量を決める
            int hpUp = 0;
            int atkUp = 0;
            int mgcUp = 0;

            if(this.name.contains("勇者(HERO)")) {
                hpUp = 5; atkUp = 5; mgcUp = 5;
            } else if (this.name.contains("魔法使い(WIZARD)")) {
                hpUp = 5; mgcUp = 10;
            } else if (this.name.contains("騎士(KNIGHT)")) {
                hpUp = 10; atkUp = 5;
            } else if (this.name.contains("盗賊(THIEF)")) {
                hpUp = 5;
            } else if (this.name.contains("召喚士(SUMMONER)")) {
                hpUp = 10;
            } else if (this.name.contains("祈祷師(SHAMAN)")) {
                hpUp = 5; mgcUp = 10;
            } else if (this.name.contains("回復術師(HEALER)")) {
                hpUp = 5; mgcUp = 10;
            }

            //ステータスにプラスする
            this.maxHp += hpUp;
            this.atk += atkUp;
            this.mgc += mgcUp;
        }
        return leveledUp;
    }

}