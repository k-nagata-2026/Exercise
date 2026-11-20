import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameMasterManager {
    private static final String DB_URL = "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";
    private static final String DB_USER = "tomura_db";
    private static final String DB_PASSWORD = "tomura_db";

    // キャラクター選択画面用のプレイヤー一覧をDBから取得する
    public static List<Player> loadAllPlayers() {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT PLNO, PLNAME, PLHP, PLPOW, PLMGC, INC_HP, INC_POW, INC_MGC FROM PLAYER ORDER BY PLNO ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int plNo = rs.getInt("PLNO");
                String name = rs.getString("PLNAME");
                int hp = rs.getInt("PLHP");
                int pow = rs.getInt("PLPOW");
                int mgc = rs.getInt("PLMGC");
                int incHp = rs.getInt("INC_HP");
                int incPow = rs.getInt("INC_POW");
                int incMgc = rs.getInt("INC_MGC");

                // 画像ファイル名はPLNOに対応させる
                String imagePath = getPlayerImagePath(plNo);

                // Playerオブジェクトを生成
                Player p = new Player(name, hp, pow, mgc, imagePath, 0, 1, 0, 10);
                // DBの成長率（INC_HP, INC_POW, INC_MGC）をセット
                p.setGrowthRates(incHp, incPow, incMgc);

                // スキルをDB（PLAYER_SKILL ＋ SKILL）から読み込んで覚えさせる
                loadPlayerSkillsFromDB(conn, plNo, p);

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // キャラ番号(PLNO)に対応する画像ファイル名を返す
    private static String getPlayerImagePath(int plNo) {
        switch (plNo) {
            case 1: return "yuusya_game.png";
            case 2: return "mahoutsukai_man.png";
            case 3: return "knight.png";
            case 4: return "dorobou_hokkamuri.png";
            case 5: return "mahoutsukai_necromancer.png";
            case 6: return "oharai_kannushi.png";
            case 7: return "job_doctor_man.png";
            default: return "yuusya_game.png";
        }
    }

    // PLAYER_SKILL と SKILL を合体して、そのキャラの技をDBから覚えさせる
    private static void loadPlayerSkillsFromDB(Connection conn, int plNo, Player p) throws SQLException {
        String sql = "SELECT s.SKNO, s.SKNAME, s.SKTYPE, s.SKCATEGORY, s.SKTARGET, s.SKMULTIPLIER " +
                     "FROM PLAYER_SKILL ps " +
                     "JOIN SKILL s ON ps.SKNO = s.SKNO " +
                     "WHERE ps.PLNO = ? ORDER BY s.SKNO ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int skNo = rs.getInt("SKNO");
                    String skName = rs.getString("SKNAME");
                    String skCategory = rs.getString("SKCATEGORY");
                    String skTarget = rs.getString("SKTARGET");
                    double mult = rs.getDouble("SKMULTIPLIER");

                    // 召喚士の精霊スキル（SKNO 16〜19）の特別設定
                    if (skNo == 16) {
                        Skill nurikabe = new Skill(skName, 1.0, "召喚", 15, 1, "youkai_nurikabe.png", 120, 5, 5);
                        nurikabe.addSpiritSkill(new Skill("かばう", 0.25, "かばう"));
                        nurikabe.addSpiritSkill(new Skill("斬る", 1.0, "単体攻撃"));
                        p.learnSkill(nurikabe);
                    } else if (skNo == 17) {
                        Skill tengu = new Skill(skName, 2.0, "召喚", 30, 4, "youkai_tengu.png", 70, 15, 65);
                        tengu.addSpiritSkill(new Skill("風魔法", 0.5, "魔法"));
                        tengu.addSpiritSkill(new Skill("うちわ", 2.5, "斬る"));
                        p.learnSkill(tengu);
                    } else if (skNo == 18) {
                        Skill kyubi = new Skill(skName, 4.0, "召喚", 50, 7, "youkai_kyubinokitsune.png", 90, 5, 130);
                        kyubi.addSpiritSkill(new Skill("炎魔法", 1.5, "魔法"));
                        kyubi.addSpiritSkill(new Skill("全体回復", 1.5, "全体回復"));
                        p.learnSkill(kyubi);
                    } else if (skNo == 19) {
                        Skill oni = new Skill(skName, 8.0, "召喚", 90, 10, "setsubun_oni_kowai.png", 150, 300, 5);
                        oni.addSpiritSkill(new Skill("金棒", 5.0, "斬る"));
                        oni.addSpiritSkill(new Skill("炎魔法", 0.5, "魔法"));
                        p.learnSkill(oni);
                    } else {
                        // 通常スキル：DBのSKCATEGORYとSKTARGETを、今のバトル処理が判定できる文字列に変換
                        String battleType = convertSkillType(skCategory, skTarget, skName);
                        p.learnSkill(skName, mult, battleType);
                    }
                }
            }
        }
    }

    // DBの SKCATEGORY / SKTARGET を既存のバトル判定用文字列に変換する
    public static String convertSkillType(String category, String target, String skName) {
        if ("DEFENSE".equals(category)) return "かばう";
        if ("COUNTER".equals(category)) return "カウンター";
        if ("ATTACK".equals(category)) {
            return "ALL".equals(target) ? "全体攻撃" : "単体攻撃";
        }
        if ("RECOVERY".equals(category)) {
            return "ALL".equals(target) ? "全体回復" : "単体回復";
        }
        if ("BUFF".equals(category)) {
            return "ALL".equals(target) ? "全体バフ" : "単体バフ";
        }
        if ("DEBUFF".equals(category)) {
            return "ALL".equals(target) ? "全体デバフ" : "単体デバフ";
        }
        return skName;
    }

    // セーブデータロード時に、キャラ名からDBの成長率とスキルを再設定する
    public static void applyMasterDataToPlayer(Player p) {
        String sql = "SELECT PLNO, INC_HP, INC_POW, INC_MGC FROM PLAYER WHERE PLNAME = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int plNo = rs.getInt("PLNO");
                    p.setGrowthRates(rs.getInt("INC_HP"), rs.getInt("INC_POW"), rs.getInt("INC_MGC"));
                    p.getSkills().clear();
                    loadPlayerSkillsFromDB(conn, plNo, p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   
    //DBのENEMYテーブルとENEMY_SKILLテーブルから敵を生成するメソッド
    public static Enemy createEnemyFromDB(int enNo, String suffix) {
        String enemySql = "SELECT ENNAME, ENHP, ENPOW, ENMGC, ENEXP, ENCOIN FROM ENEMY WHERE ENNO = ?";
        String skillSql = "SELECT S.SKNO, S.SKNAME, S.SKCATEGORY, S.SKTARGET, S.SKMULTIPLIER " +
                          "FROM ENEMY_SKILL ES " +
                          "JOIN SKILL S ON ES.SKNO = S.SKNO " +
                          "WHERE ES.ENNO = ? ORDER BY S.SKNO ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(enemySql)) {

            pstmt.setInt(1, enNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbName = rs.getString("ENNAME");
                    int hp = rs.getInt("ENHP");
                    int pow = rs.getInt("ENPOW");
                    int mgc = rs.getInt("ENMGC");
                    int exp = rs.getInt("ENEXP");
                    int coin = rs.getInt("ENCOIN");

                    // DBの英語名から日本語表示名と画像パスをマッピング
                    String displayName = mapEnemyJapaneseName(dbName) + suffix;
                    String imagePath = mapEnemyImagePath(dbName);

                    Enemy enemy = new Enemy(displayName, hp, pow, mgc, imagePath, exp, coin);

                    // 敵のスキルをENEMY_SKILL ＋ SKILLテーブルから読み込む
                    List<Skill> enemySkills = new ArrayList<>();
                    try (PreparedStatement skillStmt = conn.prepareStatement(skillSql)) {
                        skillStmt.setInt(1, enNo);
                        try (ResultSet srs = skillStmt.executeQuery()) {
                            while (srs.next()) {
                                String skName = srs.getString("SKNAME");
                                String skCategory = srs.getString("SKCATEGORY");
                                String skTarget = srs.getString("SKTARGET");
                                double skMult = srs.getDouble("SKMULTIPLIER");

                                // プレイヤー側と共通の変換メソッドでバトル判定用文字列に変換
                                String battleType = convertSkillType(skCategory, skTarget, skName);
                                enemySkills.add(new Skill(skName, skMult, battleType));
                            }
                        }
                    }
                    enemy.setSkills(enemySkills);
                    return enemy;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //英語と日本語を対応させるメソッド
    private static String mapEnemyJapaneseName(String dbName) {
        if (dbName == null) return "魔物";
        // DBにすでに「スライム(SLIME)」のように日本語が含まれていればそのまま使う
        if (dbName.contains("(")) {
            return dbName;
        }
        String upper = dbName.toUpperCase();
        if (upper.contains("SLIME"))       return "スライム";
        if (upper.contains("GOBLIN"))      return "ゴブリン";
        if (upper.contains("ORC"))         return "オーク";
        if (upper.contains("ZOMBIE"))      return "ゾンビ";
        if (upper.contains("GOLEM"))       return "ゴーレム";
        if (upper.contains("DRAGON"))      return "ドラゴン";
        if (upper.contains("DEMON"))       return "魔王";
        if (upper.contains("PHOENIX"))     return "フェニックス";
        if (upper.contains("SHOPMANAGER")) return "店主";
        return dbName;
    }

    //名前に対応した敵の画像を返すメソッド
    private static String mapEnemyImagePath(String dbName) {
        if (dbName == null) return "fantasy_game_character_slime.png";
        String upper = dbName.toUpperCase();
        if (upper.contains("SLIME"))       return "fantasy_game_character_slime.png";
        if (upper.contains("GOBLIN"))      return "fantasy_goblin.png";
        if (upper.contains("ORC"))         return "fantasy_orc.png";
        if (upper.contains("ZOMBIE"))      return "fantasy_zombie_man.png";
        if (upper.contains("GOLEM"))       return "fantasy_golem.png";
        if (upper.contains("DRAGON"))      return "fantasy_dragon.png";
        if (upper.contains("DEMON"))       return "fantasy_maou_devil.png";
        if (upper.contains("PHOENIX"))     return "youkai_suzaku.png";
        if (upper.contains("SHOPMANAGER")) return "job_tenin_man.png";
        return "fantasy_game_character_slime.png";
    }
    
    //DBのITEMテーブルとEQUIPMENTテーブルからショップの商品一覧を取得するメソッド
    public static List<Item> loadShopItems() {
        List<Item> shopItems = new ArrayList<>();

        // 1. ITEMテーブルから非売品(ITPRICE=0)以外を取得
        String itemSql = "SELECT ITNO, ITNAME, ITPRICE FROM ITEM WHERE ITPRICE > 0 ORDER BY ITNO ASC";
        // 2. EQUIPMENTテーブルから非売品(EQPRICE=0)以外を取得
        String eqSql = "SELECT EQNO, EQNAME, EQPRICE FROM EQUIPMENT WHERE EQPRICE > 0 ORDER BY EQNO ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // アイテムの読み込み
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int itNo = rs.getInt("ITNO");
                    String itName = rs.getString("ITNAME");
                    int itPrice = rs.getInt("ITPRICE");
                    Item item = createItemInstance(itNo, itName, itPrice);
                    if (item != null) {
                        shopItems.add(item);
                    }
                }
            }

            // 装備の読み込み
            try (PreparedStatement pstmt = conn.prepareStatement(eqSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int eqNo = rs.getInt("EQNO");
                    String eqName = rs.getString("EQNAME");
                    int eqPrice = rs.getInt("EQPRICE");
                    Item eqItem = createEquipmentInstance(eqNo, eqName, eqPrice);
                    if (eqItem != null) {
                        shopItems.add(eqItem);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shopItems;
    }

    // ITNO から対応する Item オブジェクトを生成する（インベントリロード時にも再利用可能）
    public static Item createItemInstance(int itNo, String itName, int itPrice) {
        switch (itNo) {
            case 1: return new HealItem(1, "Herb", itPrice, "回復", 15, "kanpou.png");
            case 2: return new EscapeItem(2, "Smokebomb", itPrice, "離脱", 50, "ninja_dougu_kemuridama.png");
            case 3: return new HealItem(3, "Panacea", itPrice, "回復", 0, "medical_medicine.png");
            case 4: return new EscapeItem(4, "CamouflageCloth", itPrice, "隠れる", 250, "ninja_kakuremi.png");
            case 5: return new HealItem(5, "PhoenixFeather", itPrice, "蘇生", 999, "hane.png");
            default: return new HealItem(itNo, itName, itPrice, "回復", 15, "kanpou.png");
        }
    }

    // EQNO から対応する EquipmentItem オブジェクトを生成する（インベントリロード時にも再利用可能）
    public static EquipmentItem createEquipmentInstance(int eqNo, String eqName, int eqPrice) {
        switch (eqNo) {
            case 1: return new EquipmentItem(1, "StrongSword", eqPrice, "装備", 30, "game_ken_seiken.png");
            case 2: return new EquipmentItem(2, "StrongCane", eqPrice, "装備", 30, "tsue_sennin.png");
            case 3: return new EquipmentItem(3, "StrongArmor", eqPrice, "装備", 60, "gogatsu_ningyou.png");
            case 4: return new EquipmentItem(4, "WisdomPouch", eqPrice, "装備", 0, "toy_waraibukuro.png");
            case 5: return new EquipmentItem(5, "StrongBoots", eqPrice, "装備", 0, "shoes.png");
            case 6: return new EquipmentItem(6, "GreedPouch", eqPrice, "装備", 0, "money_bag_yen.png");
            case 7: return new EquipmentItem(7, "Book", eqPrice, "装備", 0, "book_yoko.png");
            case 8: return new EquipmentItem(8, "Ohnusa", eqPrice, "装備", 0, "oharai_bou.png");
            case 9: return new EquipmentItem(9, "Apron", eqPrice, "装備", 0, "apron.png");
            default: return new EquipmentItem(eqNo, eqName, eqPrice, "装備", 0, "game_ken_seiken.png");
        }
    }

}