import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GameSaveManager {
    private static final String DB_URL = "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";
    private static final String DB_USER = "raj_db";
    private static final String DB_PASSWORD = "raj_db";

    // १. Game Data Save गर्ने Method
    public static void saveGameData(int playerNo, int level, Timestamp date, int balance) {
        String sql = "INSERT INTO game_save_data (SAVE_ID, PLAYER_NO, PLAYER_LVL, SAVE_DATE, BALANCE) " +
                     "VALUES ((SELECT NVL(MAX(SAVE_ID), 0) + 1 FROM game_save_data), ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerNo);
            pstmt.setInt(2, level);
            pstmt.setTimestamp(3, date);
            pstmt.setInt(4, balance);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("セーブせいこう： (Lv." + level + ") のデータをほぞんしました！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getSaveIdList() {
        List<Integer> idList = new ArrayList<>();
        String sql = "SELECT SAVE_ID FROM game_save_data ORDER BY SAVE_ID ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                idList.add(rs.getInt("SAVE_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idList;
    }

    public static int[] loadGameDataById(int targetId) {
        // [0]: PLAYER_NO, [1]: PLAYER_LVL, [2]: BALANCE
        int[] gameData = new int[3];
        String sql = "SELECT PLAYER_NO, PLAYER_LVL, BALANCE FROM game_save_data WHERE SAVE_ID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, targetId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    gameData[0] = rs.getInt("PLAYER_NO");
                    gameData[1] = rs.getInt("PLAYER_LVL");
                    gameData[2] = rs.getInt("BALANCE");
                    System.out.println("ID:" + targetId + " のデータを読み込みました！");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gameData;
    }
}