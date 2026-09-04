public class EquipmentItem extends Item {
    public EquipmentItem(String name, int price, String type, int value, String imagePath) {
        super(name, price, type, value, imagePath);
    }

    @Override
    public String use(Player targetPlayer) {
        //装備する処理はBattleGameクラスで行うため、ここではメッセージを返すだけにする
        return targetPlayer.getName() + "は" + getName() + "を使った！装備した！";
    }

    //戦闘中は使えない
    @Override
    public boolean canUseInBattle() {
        return false;
    }
    
}
