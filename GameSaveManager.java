import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class GameSaveManager {
    private static final String DB_URL = "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";
    private static final String DB_USER = "raj_db";
    private static final String DB_PASSWORD = "raj_db";
    public static void saveGameData(int playerNO, int level, Timestamp date, int BALANCE) {
        String sql = "INSERT INTO game_save_data (SAVE_ID, PLAYER_NO, PLAYER_LVL, SAVE_DATE, BALANCE) VALUES ((SELECT NVL(MAX(SAVE_ID), 0) + 1 FROM game_save_data), ?, ?, ?,?)";
//game_data_seq.NEXTVALはせんせいがよういしたシーケンスというものをつかって、IDをれんばんでほぞんするきのうです

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playerNO);
            pstmt.setInt(2, level);
            pstmt.setTimestamp(3, date);
            pstmt.setInt(4, BALANCE);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("セーブせいこう：" + " (Lv." + level + ") のデータをほぞんしました！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
