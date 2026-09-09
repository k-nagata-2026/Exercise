public class ThunderStick extends Item {
    public ThunderStick() {
        super("Thunder Stick");
    }
   @Override
   public String use(Player player,Enemy enemy){
         
         int damage = 50 - enemy.getDeff();
                 if (damage < 1) {
                    damage = 1;
                 }
                 enemy.setHp(enemy.getHp() - damage);

                 return "THUNDER STICK!\n"
                        + enemy.getName()
                        + "took"
                        + damage
                        + "lightning damage!/n";
   }
}