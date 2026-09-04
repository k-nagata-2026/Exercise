public class HealItem extends Item {
    public HealItem(String name, int price, String type, int value, String imagePath) {
        super(name, price, type, value, imagePath);
    }

    @Override
    public String use(Player targetPlayer) {
        int oldHp = targetPlayer.getHp();
        //value分回復する
        int newHp = Math.min(targetPlayer.getHp() + getValue(), targetPlayer.getMaxHp());
        targetPlayer.setHp(newHp);
        return targetPlayer.getName() + "は" + getName() + "を使った！HPが" + oldHp + "から" + newHp + "に回復した！";
    }

    @Override
    public boolean canUseInBattle() {
        return true;
    }
    
}
