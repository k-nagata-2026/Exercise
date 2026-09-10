public class Bomb extends Item {
    private int damage = 40; // 爆弾の威力（ばくだんのいりょく）

    public Bomb() {
        super("爆弾（ばくだん）");
    }

    @Override
    public String use(Player player, Enemy enemy) {
        // 敵のHPを減らす
        enemy.setHp(enemy.getHp() - damage);

        // 敵のHPがマイナスにならないための処理（しょり）
        if (enemy.getHp() < 0) {
            enemy.setHp(0);
        }

        return player.getName() + " は " + name + " を投げた！\n" +
                " " + enemy.getName() + " に " + damage + " のダメージ！\n";
    }
}

