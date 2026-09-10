public abstract class Item {
    protected String name; // アイテムの名前（なまえ）

    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 【抽象（ちゅうしょう）メソッド】アイテムの効果（こうか）をここに書（か）く
     * ※具体（ぐたい）的な中身（なかみ）は、子（こ）クラス（薬草（やくそう）や爆弾（ばくだん））でオーバーライドして書（か）きます。
     * @return ログに表示（ひょうじ）するメッセージ（「〇〇を使（つか）った！」など）
     */
    public abstract String use(Player player, Enemy enemy);
}