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
        skills.add(new Skill(skill, multiplier, type, 0,1,null));
    }

    //召喚士用の技を覚えるメソッド
    public void learnSkill(String skill, double multiplier, String type, int hpCost, int requiredLevel, String imagePath) {
        skills.add(new Skill(skill, multiplier, type, hpCost, requiredLevel,imagePath));
    }

    //技のリストを取得するメソッド
    public List<Skill> getSkills() {
        return this.skills;
    }

    public int getExp() {return this.exp;}
    public void setExp(int exp) {this.exp = exp;}
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

            //レベル１の召喚士の技
            learnSkill("弱い精霊", 1.0, "召喚", 15, 1,"youkai_nurikabe.png");

            //召喚士の場合精霊を開放する
            if (this.name.contains("召喚士(SUMMONER)")) {
                if (this.level == 4) {
                    learnSkill("普通の精霊召喚", 2.0, "召喚", 30, 4, "youkai_tengu.png");
                } else if (this.level == 7) {
                    learnSkill("強い精霊召喚", 4.0, "召喚", 50, 7, "youkai_kyubinokitsune.png");
                } else if (this.level == 10) {
                    learnSkill("超強い精霊", 8.0, "召喚", 90, 10,"setsubun_oni_kowai.png");
                }
            }

            //レベルアップ時に全回復
            this.hp = this.maxHp;
        }
        return leveledUp;

    }

    public void setHp(int hp) {
        this.hp = hp;
    }

}