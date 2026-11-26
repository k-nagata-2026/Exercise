import java.util.ArrayList;
import java.util.List;

public class Player extends Character {
    private List<Skill> skills;//プレイヤーが覚えている技のリスト
    public boolean isCovered = false;//かばう
    private int exp;
    private int maxExp;
    private int incHp = 0;
    private int incPow = 0;
    private int incMgc = 0;
    //精霊用のフィールド
    private boolean isSummoned = false;
    private String originalName;
    private int originalLevel;
    private int originalMaxHp;
    private int originalHp;
    private int originalAtk;
    private int originalMgc;
    private EquipmentItem equip1 = null;
    private EquipmentItem equip2 = null;
    private List<Skill> originalSkills;
    private String originalImagePath;

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
        skills.add(new Skill(skill, multiplier, type, 0,1,null,0,0,0));
    }

    //召喚士用の技を覚えるメソッド
    public void learnSkill(String skill, double multiplier, String type, int hpCost, int requiredLevel, String imagePath, int spiritHp, int spiritAtk, int spiritMgc) {
        skills.add(new Skill(skill, multiplier, type, hpCost, requiredLevel,imagePath,spiritHp,spiritAtk,spiritMgc));
    }

    //作成済みのスキルオブジェクトを直接追加するメソッド
    public void learnSkill(Skill skill) {
        skills.add(skill);
    }

    //召喚時に技リストを丸ごと上書きするメソッド
    public void setSkills(List<Skill> skills) {
        this.skills = new ArrayList<>(skills);
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

            //精霊状態は召喚士のレベルも上げる
            if(this.isSummoned) {
                this.originalLevel++;
            }

            //次に必要な経験値量を1.5倍にする
            this.maxExp = (int)(this.maxExp * 1.5);

            //DBから読み込んだ数値をステータスにプラスする
            this.maxHp += this.incHp;
            this.atk += this.incPow;
            this.mgc += this.incMgc;

            //召喚士の場合精霊を開放する
            if (this.name.contains("召喚士(SUMMONER)")) {
                if (this.level == 4) {
                    Skill tengu = new Skill("普通の精霊召喚", 2.0, "召喚", 30, 4, "youkai_tengu.png", 70, 15, 65);
                    tengu.addSpiritSkill(new Skill("風魔法", 0.5, "魔法"));
                    tengu.addSpiritSkill(new Skill("うちわ",2.5, "斬る"));
                    learnSkill(tengu);
                } else if (this.level == 7) {
                    Skill kyubi = new Skill("強い精霊召喚", 4.0, "召喚", 50, 7, "youkai_kyubinokitsune.png", 90, 5, 130);
                    kyubi.addSpiritSkill(new Skill("炎魔法",1.5,"魔法"));
                    kyubi.addSpiritSkill(new Skill("全体回復",1.5,"全体回復"));
                    learnSkill(kyubi);
                } else if (this.level == 10) {
                    Skill oni = new Skill("超強い精霊", 8.0, "召喚", 90, 10,"setsubun_oni_kowai.png", 150, 300, 5);
                    oni.addSpiritSkill(new Skill("金棒", 5.0, "斬る"));
                    oni.addSpiritSkill(new Skill("炎魔法",0.5,"魔法"));
                    learnSkill(oni);
                }

            }

            //レベルアップ時に全回復
            this.hp = this.maxHp;
        }
        return leveledUp;

    }

    //精霊に変身(召喚)するときにステータスを覚えたり渡したりするメソッド
    public void toransformToSpirit(Skill spiritSkill) {
        //変身する前に召喚士のステータスを覚える
        if(!this.isSummoned) {
            this.originalName = this.name;
            this.originalLevel = this.level;
            this.originalMaxHp = this.maxHp;
            this.originalAtk = this.atk;
            this.originalMgc = this.mgc;
            this.originalSkills = new ArrayList<>(this.skills);
            this.isSummoned = true;
        }

        //精霊のステータスに上書きする
        this.name = spiritSkill.getName();
        this.atk = spiritSkill.getSpiritAtk();
        this.maxHp = spiritSkill.getSpiritHp();
        this.mgc = spiritSkill.getSpiritMgc();
        this.hp = spiritSkill.getSpiritHp();
        this.level = spiritSkill.getRequiredLevel();
        //召喚した精霊がスキルを持っているかの確認
        if(spiritSkill.getSpiritSkills() != null) {
            //コピーを作って上書きする
            this.skills = new ArrayList<>(spiritSkill.getSpiritSkills());
        }

    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public void setMgc(int mgc) {
        this.mgc = mgc;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setGrowthRates(int incHp, int incPow, int incMgc) {
        this.incHp = incHp;
        this.incPow = incPow;
        this.incMgc = incMgc;
    }

    public boolean isSummoned() {
        return isSummoned;
    }

    public void setSummoned(boolean isSummoned) {
        this.isSummoned = isSummoned;
    }

    public String getOriginalName() {
        return originalName;
    }

    public int getOriginalLevel() {
        return originalLevel;
    }

    public void setOriginalLevel(int originalLevel) {
        this.level = originalLevel;
    }

    public int getOriginalMaxHp() {
        return originalMaxHp;
    }

    public void setOriginalMaxHp(int originalMaxHp) {
        this.level = originalMaxHp;
    }

    public int getOriginalAtk() {
        return originalAtk;
    }

    public void setOriginalAtk(int originalAtk) {
        this.level = originalAtk;
    }
    
    public int getOriginalMgc() {
        return originalMgc;
    }

    public void setOriginalMgc(int originalMgc) {
        this.level = originalMgc;
    }
    
    public List<Skill> getOriginalSkills() {
        return originalSkills;
    }

    public String getOriginalImagePath() {
        return originalImagePath;
    }

    public void setOriginalImagePatn(String originalImagePath) {
        this.originalImagePath = originalImagePath;
    }

    // --- 装備関連のメソッド ---
    public EquipmentItem getEquip1() {
        return equip1;
    }

    public EquipmentItem getEquip2() {
        return equip2;
    }

    // ロード時など、ステータス再計算なしで装備だけをセットするメソッド
    public void setEquip1WithoutEffect(EquipmentItem equip1) {
        this.equip1 = equip1;
    }

    public void setEquip2WithoutEffect(EquipmentItem equip2) {
        this.equip2 = equip2;
    }

    // 装備1に装着、もしくは解除する
    public EquipmentItem equipSlot1(EquipmentItem newEquip) {
        EquipmentItem oldEquip = this.equip1;
        removeEquipStats(oldEquip);
        this.equip1 = newEquip;
        applyEquipStats(newEquip);
        return oldEquip;
    }

    // 装備2に装着、もしくは解除する
    public EquipmentItem equipSlot2(EquipmentItem newEquip) {
        EquipmentItem oldEquip = this.equip2;
        removeEquipStats(oldEquip);
        this.equip2 = newEquip;
        applyEquipStats(newEquip);
        return oldEquip;
    }

    //装備したときステータスを上げる
    private void applyEquipStats(EquipmentItem eq) {
        if (eq == null) return;
        this.atk += eq.getBonusAtk();
        this.mgc += eq.getBonusMgc();
        this.maxHp += eq.getBonusHp();
        this.hp += eq.getBonusHp();
    }

    //装備を外したときステータスを下げる
    private void removeEquipStats(EquipmentItem eq) {
        if (eq == null) return;
        this.atk -= eq.getBonusAtk();
        this.mgc -= eq.getBonusMgc();
        this.maxHp -= eq.getBonusHp();
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp; // 最大HPが減った分、現在HPがはみ出さないように調整
        }
    }

    //ーーー特殊能力付きの装備たちの処理ーーー

    // 強い靴（回避率+20%）を装備しているか（2つ装備なら+40%）戦闘の処理で回避できるようにする
    public double getEvasionBonus() {
        double bonus = 0.0;
        if (equip1 != null && equip1.isStrongBoots()) bonus += 0.20;
        if (equip2 != null && equip2.isStrongBoots()) bonus += 0.20;
        return bonus;
    }

    // 大幣（バフ・デバフを1ターンで2回できる）を装備しているかの判定
    public boolean hasOhnusa() {
        return (equip1 != null && equip1.isOhnusa()) || (equip2 != null && equip2.isOhnusa());
    }

    // 店長のエプロン（敵の落とすお金2倍）を装備しているかの判定
    public boolean hasApron() {
        return (equip1 != null && equip1.isApron()) || (equip2 != null && equip2.isApron());
    }

    // 強欲の袋の装備数（1つにつきお金+5）
    public int getGreedPouchCount() {
        int count = 0;
        if (equip1 != null && equip1.isGreedPouch()) count++;
        if (equip2 != null && equip2.isGreedPouch()) count++;
        return count;
    }

    // 知恵の袋の装備数（1つにつきアイテム枠+5）
    public int getWisdomPouchCount() {
        int count = 0;
        if (equip1 != null && equip1.getDbNo() == 4) count++;
        if (equip2 != null && equip2.getDbNo() == 4) count++;
        return count;
    }

}