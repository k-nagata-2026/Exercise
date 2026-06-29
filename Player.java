public class Player extends Character {
    private int maxHp;
    private int mp;
    private int level;

    public Player(String name, int hp, int atk, int defenc, int speed, String imagePath) {
        super(name, hp, atk, defenc, speed, imagePath);
        this.maxHp = hp; 
        this.mp = 0;     
        this.level = 1;  
    }

    // ----- BattleGameに必要なゲッターメソッド -----
    public int getMaxHp() {
        return this.maxHp;
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        if (mp > 100) this.mp = 100;
        else if (mp < 0) this.mp = 0;
        else this.mp = mp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // レベルアップ時のステータス上昇
    public void levelUp() {
        this.level++;
        this.maxHp += 20; 
        this.atk += 5;    
        this.setHp(this.maxHp); 
    }

    // 必殺スキル攻撃のロジック (Cost: 50 MP)
    public String useSkill(Enemy enemy) {
        if (this.mp < 50) {
            return "⚠ MPが足りません！\n";
        }
        this.mp -= 50; 
        int damage = (int)(this.atk * 2.5); 
        enemy.setHp(enemy.getHp() - damage);
        return "✨ " + this.name + " の必殺スキル発動！！！ " + enemy.getName() + " に " + damage + " の大ダメージを与えた！\n";
    }

    // 通常攻撃のロジック (ヒット時にMPが15回復)
    public String attack(Enemy enemy) {
        int damage = (int)(this.atk * (0.9 + Math.random() * 0.2));
        enemy.setHp(enemy.getHp() - damage);
        setMp(this.mp + 15);
        return "⚔ " + this.name + " の攻撃！ " + enemy.getName() + " に " + damage + " のダメージを与えた！ (MP+15)\n";
    }
}
