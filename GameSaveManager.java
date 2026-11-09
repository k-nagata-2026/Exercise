import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//メインクラス(publicをつける)
public class GameSaveManager {
    private static final String DB_URL = "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";
    private static final String DB_USER = "tomura_db";
    private static final String DB_PASSWORD = "tomura_db";
    public static void saveGameData(int gold, Timestamp date) {
        String sql = "INSERT INTO save_data (SVNO, SVCOIN, SVDATE) VALUES ((SELECT NVL(MAX(SVNO), 0) + 1 FROM save_data), ?, ?)";
        //game_data_seq.NEXTVALはせんせいがよういしたシーケンスというものをつかって、IDをれんばんでほぞんするきのうです

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gold);
            pstmt.setTimestamp(2, date);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("セーブせいこう：" + " (Gold: " + gold + ", Date: " + date + ") のデータをほぞんしました！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

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
}