import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GameSaveManager {
    private static final String DB_URL = "jdbc:oracle:thin:@//3.115.96.237:1521/fds1";
    private static final String DB_USER = "sagar_db";
    private static final String DB_PASS = "sagar_db";

    public static Connection getConnection() {
        try {
            //Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            System.out.println("Database Connection Error: " + e.getMessage());
            return null;
        }
    }

    // 既存のセーブスロット一覧を取得するメソッド
    public static List<Integer> getExistingSaveSlots() {
        List<Integer> slots = new ArrayList<>();
        String sql = "SELECT save_id FROM rpg_game_save_data ORDER BY save_id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                slots.add(rs.getInt("save_id"));
            }
        } catch (Exception e) {
            System.out.println("セーブスロット一覧取得エラー: " + e.getMessage());
        }
        return slots;
    }

    // ★ セーブ日時（SYSTIMESTAMP）を自動記録するように修正した保存メソッド
    public static void saveGameDataToSlot(int slotId, int playerNo, int stage, int hp, int level, int enemyHp, Item[] itemBox) {
        Connection conn = getConnection();
        if (conn == null) {
            System.out.println("⚠️ データベース接続エラーのため、セーブできませんでした。");
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM rpg_game_save_data WHERE save_id = ?";
        
        // UPDATE時に saved_at = SYSTIMESTAMP を更新
        String updateSql = "UPDATE rpg_game_save_data SET player_no = ?, stage = ?, hp = ?, level_no = ?, enemy_hp = ?, "
                         + "item_slot_1 = ?, item_slot_2 = ?, item_slot_3 = ?, saved_at = SYSTIMESTAMP WHERE save_id = ?";
        
        // INSERT時に saved_id は自動生成 (IDENTITY) に任せるか、あるいは slotId を入れる
        String insertSql = "INSERT INTO rpg_game_save_data (player_no, stage, hp, level_no, enemy_hp, item_slot_1, item_slot_2, item_slot_3, saved_at) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";

        try {
            conn.setAutoCommit(false); // 手動コミットで確実にOracleへ書き込む

            boolean exists = false;
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
                pstmtCheck.setInt(1, slotId);
                ResultSet rs = pstmtCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }

            String i1 = (itemBox != null && itemBox.length > 0 && itemBox[0] != null) ? itemBox[0].getName() : null;
            String i2 = (itemBox != null && itemBox.length > 1 && itemBox[1] != null) ? itemBox[1].getName() : null;
            String i3 = (itemBox != null && itemBox.length > 2 && itemBox[2] != null) ? itemBox[2].getName() : null;

            if (exists) {
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateSql)) {
                    pstmtUpdate.setInt(1, playerNo);
                    pstmtUpdate.setInt(2, stage);
                    pstmtUpdate.setInt(3, hp);
                    pstmtUpdate.setInt(4, level);
                    pstmtUpdate.setInt(5, enemyHp);
                    pstmtUpdate.setString(6, i1);
                    pstmtUpdate.setString(7, i2);
                    pstmtUpdate.setString(8, i3);
                    pstmtUpdate.setInt(9, slotId);
                    pstmtUpdate.executeUpdate();
                }
            } else {
                try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql)) {
                    pstmtInsert.setInt(1, playerNo);
                    pstmtInsert.setInt(2, stage);
                    pstmtInsert.setInt(3, hp);
                    pstmtInsert.setInt(4, level);
                    pstmtInsert.setInt(5, enemyHp);
                    pstmtInsert.setString(6, i1);
                    pstmtInsert.setString(7, i2);
                    pstmtInsert.setString(8, i3);
                    pstmtInsert.executeUpdate();
                }
            }
            
            // データベースへ変更を確実にコミット！
            conn.commit();
            System.out.println("💾 スロット " + slotId + " にセーブ日時付きで正常に保存しました！");

        } catch (Exception e) {
            System.out.println("ゲームセーブ時のエラー: " + e.getMessage());
            try { conn.rollback(); } catch (Exception ignored) {}
        } finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }

    // ロード用メソッド
    public static boolean loadGameDataById(int slotId) {
        String sql = "SELECT player_no, stage, hp, level_no, enemy_hp, item_slot_1, item_slot_2, item_slot_3 "
                   + "FROM rpg_game_save_data WHERE save_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, slotId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int playerNo = rs.getInt("player_no");
                    int stage = rs.getInt("stage");
                    int hp = rs.getInt("hp");
                    int level = rs.getInt("level_no");
                    int enemyHp = rs.getInt("enemy_hp");
                    String item1 = rs.getString("item_slot_1");
                    String item2 = rs.getString("item_slot_2");
                    String item3 = rs.getString("item_slot_3");

                    BattleGame.loadGameVariables(playerNo, stage, hp, level, enemyHp, item1, item2, item3);
                    System.out.println("📂 スロット " + slotId + " のデータをロードしました。");
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("ゲームロード時のエラー: " + e.getMessage());
        }
        return false;
    }
}
