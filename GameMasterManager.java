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
    
}