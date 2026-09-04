public abstract class Item {
    private String name;//アイテム名
    private int price;//価格
    private String type;//種類(回復やバフなどで分ける)
    private int value;//効果量
    private String imagePath;//画像

    //コンストラクタ
    public Item (String name, int price , String type, int value, String imagePath) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.value = value;
        this.imagePath = imagePath;
    }

    //ゲッター
    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public String getImagePath() {
        return imagePath;
    }

    //アイテムを使う処理
    public abstract String use(Player targetPlayer);

    //戦闘中に使えるかの判断
    public  boolean canUseInBattle() {
        return true;
    }
}