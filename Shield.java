public class Shield implements Item {
    private String name = "薬草（shield）";
    private int healAmount = 300;

    public Shield() {
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String use(Player player, Enemy enemy) {
        int beforeHp = player.getHp();

        player.setHp(player.getHp() + healAmount);

        if (player.getHp() > player.getMaxHp()) {
            player.setHp(player.getMaxHp());
        }

        int actualHeal = player.getHp() - beforeHp;

        return player.getName() + " は " + getName() + " を使った！\n" +
                " HPが " + actualHeal + " 回復（かいふく）した！\n";
    }
}