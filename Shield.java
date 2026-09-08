public class Shield{
    private String name = "SHIELD BASH";
    private int spCost = 15;        // 消費SP (またはMP)
    private double damageMultiplier = 0.8; // 小ダメージ用の倍率 (通常攻撃の0.8倍など)

    // スキル使用処理
    // 戻り値: スキル発動に成功したかどうか (SPが足りているか)
    public boolean execute(Player player, Enemy enemy) {
        // SP不足チェック
        if (player.getHp() < spCost) {
            System.out.println("SPが足りません！ (必要SP: " + spCost + ")");
            return false;
        }

        // ダメージ計算 (小ダメージ)
        int damage = (int)(player.getAtk() * damageMultiplier);
        
        // 最低でも1ダメージは与える
        if (damage < 1) damage = 1;


        System.out.println(player.getName() + "の " + name + "！");
        System.out.println("敵に " + damage + " の小ダメージを与え、気絶させた！");

        return true;
    }

    // ゲッター類
    public String getName() { return name; }
    public int getSpCost() { return spCost; }
}