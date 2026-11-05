public class FireOrb extends Item {
    public FireOrb()   {
        super("Fire Orb");
    } 
        @Override
        public String use(Player player, Enemy enemy)  {

            int damage = 40 - enemy.getDeff();

            if (damage < 1) {
                damage = 1;
            }

            enemy.setHp(enemy.getHp() -
 damage);

            return "FIRE ORB!/n"
            + enemy.getName()
            + " took "
            + damage
            + " fire damage!\n";
        }
}
