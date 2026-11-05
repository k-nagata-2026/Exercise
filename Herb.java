public class Herb extends Item {
    private int healAmount = 100; // 回復量

    public Herb() {
        super("薬草（Herb）"); // 親クラス（Item）に名前を教える
    }

    @Override
    public String use(Player player, Enemy enemy) {
        // いまのHPを保存（ほぞん）
        int beforeHp = player.getHp();

        // HPを回復（かいふく）させる
        player.setHp(player.getHp() + healAmount);

        // 最大HPを超えないための処理（しょり）
        if (player.getHp() > player.getMaxHp()) {
            player.setHp(player.getMaxHp());
        }

        int actualHeal = player.getHp() - beforeHp; // 実際に回復した値
        return player.getName() + " は " + name + " を使った！\n" +
                " HPが " + actualHeal + " 回復（かいふく）した！\n";
    }
}
