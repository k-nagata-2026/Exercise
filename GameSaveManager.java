import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//メインクラス(publicをつける)
public class GameSaveManager {
    private static final String DB_URL = "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";
    private static final String DB_USER = "tomura_db";
    private static final String DB_PASSWORD = "tomura_db";

    //セーブデータを取得するメソッド
    public static List<SaveSlotInfo> loadGameData() {
        List<SaveSlotInfo> saveSlots = new ArrayList<>();
        String sql = "SELECT SVNO, SVCOIN, SVDATE FROM save_data ORDER BY SVNO ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int svno = rs.getInt("SVNO");
                int svcoin = rs.getInt("SVCOIN");
                Timestamp svdate = rs.getTimestamp("SVDATE");
                saveSlots.add(new SaveSlotInfo(svno, svcoin, svdate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return saveSlots;
    }

    //所持金とパーティー情報を取得するメソッド
    public static void loadGameDataById(int svNo) {
        //save_dataテーブルからお金と日付を取得する
        String sql = "SELECT SVNO, SVCOIN, SVDATE FROM save_data WHERE SVNO = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, svNo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int svcoin = rs.getInt("SVCOIN");
                Timestamp svdate = rs.getTimestamp("SVDATE");
                BattleGame.setGold(svcoin); // BattleGameクラスに所持金を渡す
                System.out.println("ロードせいこう：" + " (SVNO: " + svNo + ", Gold: " + svcoin + ", Date: " + svdate + ")");
            } else {
                System.out.println("指定されたセーブデータは存在しません。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //current_partyテーブルからパーティー情報を取得する
        List<SavePartyMember> partyMembers = new ArrayList<>();
        String partySql = "SELECT PTNO, SVNO, PLNO, PL_LEVEL, PL_CURRENT_HP, PL_MAX_HP, PL_MAX_POW, PL_MAX_MGC, PL_EQ1, PL_EQ2 "
                        + "FROM current_party WHERE SVNO = ? ORDER BY PTNO ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(partySql)) {

            pstmt.setInt(1, svNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SavePartyMember member = new SavePartyMember(
                        rs.getInt("PTNO"),
                        rs.getInt("SVNO"),
                        rs.getInt("PLNO"),
                        rs.getInt("PL_LEVEL"),
                        rs.getInt("PL_CURRENT_HP"),
                        rs.getInt("PL_MAX_HP"),
                        rs.getInt("PL_MAX_POW"),
                        rs.getInt("PL_MAX_MGC"),
                        rs.getInt("PL_EQ1"),
                        rs.getInt("PL_EQ2")
                    );
                    partyMembers.add(member);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        BattleGame.setPartyMembers(partyMembers);
    }

    //カレントパーティテーブルにパーティを保存するメソッド(保存結果によって次の画面へ進むかどうかを切り替えたいからboolean)
    public static boolean savePartyData (int svNo, List<Player> partyList) {
        // 既存のパーティレコードの PTNO 一覧を取得するSQL
        String selectPtSql = "SELECT PTNO FROM current_party WHERE SVNO = ? ORDER BY PTNO ASC";

        // 既存レコードを上書き更新するSQL
        String updateSql = "UPDATE current_party "
                         + "SET PLNO = ?, PL_LEVEL = ?, PL_CURRENT_HP = ?, PL_MAX_HP = ?, PL_MAX_POW = ?, PL_MAX_MGC = ?, PL_EQ1 = 0, PL_EQ2 = 0 "
                         + "WHERE PTNO = ? AND SVNO = ?";

        // 新規作成直後など、まだレコードが存在しない場合のみ使うINSERT
        String maxPtSql = "SELECT NVL(MAX(PTNO), 0) FROM current_party";
        String insertSql = "INSERT INTO current_party "
                         + "(PTNO, SVNO, PLNO, PL_LEVEL, PL_CURRENT_HP, PL_MAX_HP, PL_MAX_POW, PL_MAX_MGC, PL_EQ1, PL_EQ2) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // トランザクション開始

            // 1. すでにこのスロット(svNo)に登録されている PTNO を取得
            List<Integer> existingPtNos = new ArrayList<>();
            try (PreparedStatement selectPstmt = conn.prepareStatement(selectPtSql)) {
                selectPstmt.setInt(1, svNo);
                try (ResultSet rs = selectPstmt.executeQuery()) {
                    while (rs.next()) {
                        existingPtNos.add(rs.getInt("PTNO"));
                    }
                }
            }

            if (existingPtNos.size() == partyList.size()) {
                // 2-A. すでに4人分のレコードがある場合は UPDATE で上書き！
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    for (int i = 0; i < partyList.size(); i++) {
                        Player p = partyList.get(i);
                        int ptNo = existingPtNos.get(i); // 既存の PTNO をそのまま使う
                        int plNo = getPlNoFromName(p.getName());

                        updatePstmt.setInt(1, plNo);
                        updatePstmt.setInt(2, p.getLevel());
                        updatePstmt.setInt(3, p.getHp());
                        updatePstmt.setInt(4, p.getMaxHp());
                        updatePstmt.setInt(5, p.getAtk());
                        updatePstmt.setInt(6, p.getMgc());
                        updatePstmt.setInt(7, ptNo);
                        updatePstmt.setInt(8, svNo);

                        updatePstmt.addBatch();
                    }
                    updatePstmt.executeBatch();
                }
                System.out.println("スロット " + svNo + " のパーティを上書き保存(UPDATE)しました。");
            } else {
                // 2-B. 新規データでまだレコードがない場合は、現在の最大 PTNO の連番で INSERT
                int currentMaxPtNo = 0;
                try (PreparedStatement maxPstmt = conn.prepareStatement(maxPtSql);
                     ResultSet rs = maxPstmt.executeQuery()) {
                    if (rs.next()) {
                        currentMaxPtNo = rs.getInt(1);
                    }
                }

                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < partyList.size(); i++) {
                        Player p = partyList.get(i);
                        int ptNo = currentMaxPtNo + i + 1;
                        int plNo = getPlNoFromName(p.getName());

                        insertPstmt.setInt(1, ptNo);
                        insertPstmt.setInt(2, svNo);
                        insertPstmt.setInt(3, plNo);
                        insertPstmt.setInt(4, p.getLevel());
                        insertPstmt.setInt(5, p.getHp());
                        insertPstmt.setInt(6, p.getMaxHp());
                        insertPstmt.setInt(7, p.getAtk());
                        insertPstmt.setInt(8, p.getMgc());

                        insertPstmt.addBatch();
                    }
                    insertPstmt.executeBatch();
                }
                System.out.println("スロット " + svNo + " のパーティを新規保存(INSERT)しました。");
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    } 
    
    //キャラ名からPLNOを割り出すメソッド
    private static int getPlNoFromName(String name) {
        if (name.contains("勇者")) return 1;
        if (name.contains("魔法使い")) return 2;
        if (name.contains("騎士")) return 3;
        if (name.contains("盗賊")) return 4;
        if (name.contains("召喚士")) return 5;
        if (name.contains("祈祷師")) return 6;
        if (name.contains("回復術師")) return 7;
        return 1;
    }

    //新しいセーブデータを作成し、そのSVNOを返すメソッド
    public static int createNewSaveData(int initialGold) {
        String maxSql = "SELECT NVL(MAX(SVNO),0) + 1 FROM SAVE_DATA";
        String insertSql = "INSERT INTO SAVE_DATA (SVNO,SVCOIN,SVDATE) VALUES (?,?,?)";
        int newSvNo = -1;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 次の SVNO を取得
            try (PreparedStatement maxPstmt = conn.prepareStatement(maxSql);
                 ResultSet rs = maxPstmt.executeQuery()) {
                if (rs.next()) {
                    newSvNo = rs.getInt(1);
                }
            }

            // 2. 新しいスロットを DB に INSERT
            if (newSvNo != -1) {
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    insertPstmt.setInt(1, newSvNo);
                    insertPstmt.setInt(2, initialGold);
                    insertPstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    insertPstmt.executeUpdate();
                    System.out.println("新規セーブスロット（SVNO: " + newSvNo + "）を作成しました！");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return newSvNo;
    }

    // 指定したセーブスロット(svNo)の所持金と保存日時を上書き更新(UPDATE)するメソッド
    public static boolean updateGameData(int svNo, int gold, Timestamp date) {
        String sql = "UPDATE save_data SET SVCOIN = ?, SVDATE = ? WHERE SVNO = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gold);
            pstmt.setTimestamp(2, date);
            pstmt.setInt(3, svNo);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("セーブ上書き成功： (SVNO: " + svNo + ", Gold: " + gold + ", Date: " + date + ")");
                return true;
            } else {
                System.out.println("更新対象のセーブデータ(SVNO: " + svNo + ")が見つかりませんでした。");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}