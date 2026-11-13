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

    // セーブできる人数（スロット数）の上限
    private static final int MAX_SLOTS = 3;

    // アイテムボックスのスロット数
    public static final int ITEM_SLOTS = 10;

    /**
     * セーブする。
     * - saveId > 0 : そのスロットを更新（続きから遊んでいる人）
     * - saveId == 0: 新しいゲーム
     *      スロットが3つ未満なら新しいスロットを追加、
     *      3つ埋まっていたら一番古い（SAVE_DATEが最小の）スロットを上書き
     * 戻り値: 使われたSAVE_ID（失敗したら -1）
     */
    public static int saveGameData(int saveId, int playerNo, int level, Timestamp date, int balance) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // 1. 続きから遊んでいる場合は、そのスロットを更新
            if (saveId > 0) {
                if (updateSlot(conn, saveId, playerNo, level, date, balance) > 0) {
                    System.out.println("セーブせいこう： ID " + saveId + " を更新しました！");
                    return saveId;
                }
                // 行が見つからない場合は、新規扱いで下に進む
            }

            // 2. 現在のスロット数を数える
            int count = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM game_save_data");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

            if (count < MAX_SLOTS) {
                // 3. 空きがある: 新しいスロットを追加
                int newId = 1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT NVL(MAX(SAVE_ID), 0) + 1 FROM game_save_data");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newId = rs.getInt(1);
                    }
                }

                String insertSql = "INSERT INTO game_save_data (SAVE_ID, PLAYER_NO, PLAYER_LVL, SAVE_DATE, BALANCE) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, newId);
                    ps.setInt(2, playerNo);
                    ps.setInt(3, level);
                    ps.setTimestamp(4, date);
                    ps.setInt(5, balance);
                    ps.executeUpdate();
                }
                System.out.println("セーブせいこう： 新しいID " + newId + " を作成しました！");
                return newId;
            } else {
                // 4. 3つ埋まっている: 一番古いスロットを上書き
                int oldestId = -1;
                String oldestSql = "SELECT SAVE_ID FROM (SELECT SAVE_ID FROM game_save_data "
                        + "ORDER BY SAVE_DATE ASC) WHERE ROWNUM = 1";
                try (PreparedStatement ps = conn.prepareStatement(oldestSql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldestId = rs.getInt(1);
                    }
                }

                if (oldestId > 0 && updateSlot(conn, oldestId, playerNo, level, date, balance) > 0) {
                    System.out.println("セーブせいこう： 一番古い ID " + oldestId + " を上書きしました！");
                    return oldestId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int updateSlot(Connection conn, int saveId, int playerNo, int level,
                                  Timestamp date, int balance) throws SQLException {
        String sql = "UPDATE game_save_data SET PLAYER_NO = ?, PLAYER_LVL = ?, SAVE_DATE = ?, BALANCE = ? "
                + "WHERE SAVE_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerNo);
            ps.setInt(2, level);
            ps.setTimestamp(3, date);
            ps.setInt(4, balance);
            ps.setInt(5, saveId);
            return ps.executeUpdate();
        }
    }

    /**
     * アイテムボックスを game_item_box に保存する。
     * itemNos は 10スロット分の配列で、0 = 空、1以上 = アイテム番号。
     * まずそのSAVE_IDの古いアイテムを全部消してから、入っているスロットだけを書き込む。
     * 戻り値: 成功したら null、失敗したらエラーメッセージ
     */
    public static String saveItemBox(int saveId, int[] itemNos, Timestamp date) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM game_item_box WHERE SAVE_ID = ?")) {
                    del.setInt(1, saveId);
                    del.executeUpdate();
                }

                // BOX_NO は主キー（行ごとに重複しない番号）なので、今ある最大値+1 から順番に振る
                int nextBoxNo = 1;
                try (PreparedStatement mx = conn.prepareStatement(
                        "SELECT NVL(MAX(BOX_NO), 0) + 1 FROM game_item_box");
                     ResultSet rs = mx.executeQuery()) {
                    if (rs.next()) {
                        nextBoxNo = rs.getInt(1);
                    }
                }

                String insertSql = "INSERT INTO game_item_box (BOX_NO, SAVE_ID, ITEM_NO, DATE_) "
                        + "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    int inserted = 0;
                    for (int i = 0; i < itemNos.length; i++) {
                        if (itemNos[i] > 0) {
                            ins.setInt(1, nextBoxNo++);  // BOX_NO: 主キー（行ごとの通し番号）
                            ins.setInt(2, saveId);
                            ins.setInt(3, itemNos[i]);   // ITEM_NO: アイテム番号
                            ins.setTimestamp(4, date);
                            ins.executeUpdate();
                            inserted++;
                        }
                    }
                    // アイテムが1つもない場合は「空でセーブした」印として ITEM_NO = NULL の行を1つ入れる
                    // （ITEM_NO は GAME_ITEM への外部キーなので、0 ではなく NULL にする）
                    if (inserted == 0) {
                        ins.setInt(1, nextBoxNo++);
                        ins.setInt(2, saveId);
                        ins.setNull(3, java.sql.Types.NUMERIC);
                        ins.setTimestamp(4, date);
                        ins.executeUpdate();
                    }
                }
                conn.commit();
                System.out.println("アイテムをほぞんしました！ (セーブID: " + saveId + ")");
                return null;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return "SQLエラー: " + e.getMessage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "接続エラー: " + e.getMessage();
        }
    }

    /**
     * アイテムボックスを読み込む。
     * 戻り値: 10スロット分の配列（0 = 空、1以上 = アイテム番号）
     */
    public static int[] loadItemBox(int saveId) {
        int[] itemNos = new int[ITEM_SLOTS];
        int rowCount = 0;
        String sql = "SELECT ITEM_NO FROM game_item_box WHERE SAVE_ID = ? ORDER BY BOX_NO";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saveId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // 保存された順に、スロット1から詰めて復元する
                int slot = 0;
                while (rs.next()) {
                    rowCount++;
                    int itemNo = rs.getInt("ITEM_NO");
                    if (itemNo > 0 && slot < ITEM_SLOTS) {
                        itemNos[slot++] = itemNo;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        // 行が1つもない = アイテム未保存の古いセーブ → null を返す（呼び出し側は今のアイテムをそのまま使う）
        return rowCount > 0 ? itemNos : null;
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