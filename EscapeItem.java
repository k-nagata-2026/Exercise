public class EscapeItem extends Item {
    public EscapeItem(String name, int price, String type, int value, String imagePath) {
        super(name, price, type, value, imagePath);
    }

    @Override
    public String use(Player targetPlayer) {
        //逃げる処理はBattleGameクラスで行うため、ここではメッセージを返すだけにする
        return targetPlayer.getName() + "は" + getName() + "を使った！戦闘から逃げ出した！";
    }
    
}