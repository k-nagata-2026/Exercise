public abstract class Item {
    private int dbNo; //ITEMテーブルのITNO または EQUIPMENTテーブルのEQNO
    private String name;//アイテム名
    private int price;//価格
    private String type;//種類(回復やバフなどで分ける)
    private int value;//効果量
    private String imagePath;//画像

    // ★ DB番号付きコンストラクタ
    public Item(int dbNo, String name, int price, String type, int value, String imagePath) {
        this.dbNo = dbNo;
        this.name = name;
        this.price = price;
        this.type = type;
        this.value = value;
        this.imagePath = imagePath;
    }

    public int getDbNo() {
        return dbNo;
    }

    public void setDbNo(int dbNo) {
        this.dbNo = dbNo;
    }

    //ゲッター
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getType() { return type; }
    public int getValue() { return value; }
    public String getImagePath() { return imagePath; }

    //アイテムを使う処理
    public abstract String use(Player targetPlayer);

    //戦闘中に使えるかの判断
    public boolean canUseInBattle() {
        return true;
    }
}