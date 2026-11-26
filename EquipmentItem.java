public class EquipmentItem extends Item {
    private int bonusAtk = 0;
    private int bonusMgc = 0;
    private int bonusHp = 0;

    public EquipmentItem(int dbNo, String name, int price, String type, int value, String imagePath) {
        super(dbNo, name, price, type, value, imagePath);
        initEquipmentStats(name, value);
    }

    // 装備名（またはDBのvalue）から上昇ステータスを自動設定する
    private void initEquipmentStats(String name, int value) {
        if (name == null) return;

        if (name.contains("StrongSword") || name.contains("強い剣")) {
            this.bonusAtk = 30;
        } else if (name.contains("StrongCane") || name.contains("強い杖")) {
            this.bonusMgc = 30;
        } else if (name.contains("StrongArmor") || name.contains("強い鎧")) {
            this.bonusHp = 60;
        } else if (name.contains("Book") || name.contains("本")) {
            this.bonusMgc = 15;
            this.bonusHp = 15;
        } else if (name.contains("Apron") || name.contains("エプロン")) {
            this.bonusAtk = 15;
            this.bonusMgc = 15;
            this.bonusHp = 20;
        } else if (name.contains("剣")) {
            // 将来「普通の剣」などを追加したときはDBのvalue分だけ攻撃力アップ
            this.bonusAtk = value;
        }
    }

    public int getBonusAtk() { return bonusAtk; }
    public int getBonusMgc() { return bonusMgc; }
    public int getBonusHp() { return bonusHp; }

    // --- 特殊能力の判定メソッド ---
    public boolean isStrongBoots() {
        return getName() != null && (getName().contains("StrongBoots") || getName().contains("強い靴"));
    }

    public boolean isGreedPouch() {
        return getName() != null && (getName().contains("GreedPouch") || getName().contains("強欲の袋"));
    }

    public boolean isWisdomPouch() {
        return getName() != null && (getName().contains("WisdomPouch") || getName().contains("知恵の袋") || getName().contains("ウィズダムポーチ"));
    }

    public boolean isOhnusa() {
        return getName() != null && (getName().contains("Ohnusa") || getName().contains("大幣"));
    }

    public boolean isApron() {
        return getName() != null && (getName().contains("Apron") || getName().contains("エプロン"));
    }

    @Override
    public String use(Player targetPlayer) {
        return targetPlayer.getName() + "は" + getName() + "を装備した！";
    }

    //戦闘中は使えない判定
    @Override
    public boolean canUseInBattle() {
        return false;
    }
}