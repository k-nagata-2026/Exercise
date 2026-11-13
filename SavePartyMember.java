public class SavePartyMember {
    public int ptNO;
    public int svNo;
    public int plNO;
    public int Level;
    public int CurrentHp;
    public int MaxHp;
    public int MaxPow;
    public int MaxMgc;
    public int Eq1;
    public int Eq2;

    public SavePartyMember(int ptNO, int svNo, int plNO, int plLevel, int plCurrentHp, int plMaxHp, int plMaxPow, int plMaxMgc, int plEq1, int plEq2) {
        this.ptNO = ptNO;
        this.svNo = svNo;
        this.plNO = plNO;
        this.Level = plLevel;
        this.CurrentHp = plCurrentHp;
        this.MaxHp = plMaxHp;
        this.MaxPow = plMaxPow;
        this.MaxMgc = plMaxMgc;
        this.Eq1 = plEq1;
        this.Eq2 = plEq2;
    }

    //ゲッターメソッド
    public int getPlNo() {return plNO;}
    public int getPlLevel() {return Level;}
    public int getPlHp() {return CurrentHp;}
    public int getMaxHp() {return MaxHp;}
    public int getAtk() {return MaxPow;}
    public int getMgc() {return MaxMgc;}
    
}
