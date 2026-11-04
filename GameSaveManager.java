import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//補助クラス(publicはつけない)管理が楽だかららしい
class SaveSlotInfo {
    private int svno;
    private int svcoin;
    private Timestamp svdate;

    public SaveSlotInfo(int svno, int svcoin, Timestamp svdate) {
        this.svno = svno;
        this.svcoin = svcoin;
        this.svdate = svdate;
    }

    public int getSvno() {
        return svno;
    }

    public int getSvcoin() {
        return svcoin;
    }

    public Timestamp getSvdate() {
        return svdate;
    }

}

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
}