
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class GameSaveManager {

    private static final String DB_URL =
        "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";

    private static final String DB_USER = "sandip_db";
    private static final String DB_PASSWORD = "badshah_db";

    public static void saveGameData(
        int playerId,
        int playerLevel,
        String slotName,
        int usedPotion,
        int usedSkill,
        int enemyDefeated) {

        System.out.println("SAVE METHOD CALLED!");

        DriverManager.setLoginTimeout(5);

        String sql =
            "INSERT INTO SAVESLOT " +
            "(SAVEID, PLAYERID, PLAYERLEVEL, SLOTNAME, " +
            "SAVED_AT, USEDPOTION, USEDSKILL, ENEMYDEFEATED) " +
            "SELECT NVL(MAX(SAVEID), 0) + 1, ?, ?, ?, " +
            "SYSDATE, ?, ?, ? FROM SAVESLOT";

        try (
            Connection conn = DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD
            );

            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            System.out.println("DATABASE CONNECTED!");

            pstmt.setInt(1, playerId);
            pstmt.setInt(2, playerLevel);
            pstmt.setString(3, slotName);
            pstmt.setInt(4, usedPotion);
            pstmt.setInt(5, usedSkill);
            pstmt.setInt(6, enemyDefeated);

            int result = pstmt.executeUpdate();

            System.out.println("Rows inserted: " + result);

            if (result > 0) {
                JOptionPane.showMessageDialog(
                    null, "Game Saved Successfully!"
                );
            } else {
                JOptionPane.showMessageDialog(
                    null, "Game Save Failed!"
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                null,
                "DATABASE ERROR:\n" + e.getMessage()
            );
        }
    }
    public static List<Integer> getSaveIdList() {
    List<Integer> idList = new ArrayList<>();

    String sql = "SELECT SAVEID FROM SAVESLOT ORDER BY SAVEID ASC";

    try (
        Connection conn = DriverManager.getConnection(
            DB_URL, DB_USER, DB_PASSWORD
        );
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()
    ) {
        while (rs.next()) {
            idList.add(rs.getInt("SAVEID"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return idList;
}
    public static class SaveData {
    public int playerId;
    public int playerLevel;
    public String slotName;
    public int usedPotion;
    public int usedSkill;
    public int enemyDefeated;
}
public static SaveData loadGameDataById(int targetId) {

    String sql =
        "SELECT PLAYERID, PLAYERLEVEL, SLOTNAME, " +
        "USEDPOTION, USEDSKILL, ENEMYDEFEATED " +
        "FROM SAVESLOT WHERE SAVEID = ?";

    try (
        Connection conn = DriverManager.getConnection(
            DB_URL, DB_USER, DB_PASSWORD
        );
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
        pstmt.setInt(1, targetId);

        try (ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                SaveData data = new SaveData();

                data.playerId = rs.getInt("PLAYERID");
                data.playerLevel = rs.getInt("PLAYERLEVEL");
                data.slotName = rs.getString("SLOTNAME");
                data.usedPotion = rs.getInt("USEDPOTION");
                data.usedSkill = rs.getInt("USEDSKILL");
                data.enemyDefeated = rs.getInt("ENEMYDEFEATED");

                return data;
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}
}

