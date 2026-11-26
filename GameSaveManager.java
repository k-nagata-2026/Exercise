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

        //INVENTORYテーブルから所持アイテム・装備を読み込む
        Item[] loadedBox = new Item[50];
        String invSql = "SELECT inv.BXNO, inv.TYPE, inv.ITNO, inv.EQNO, "
                      + "it.ITNAME, it.ITPRICE, eq.EQNAME, eq.EQPRICE "
                      + "FROM INVENTORY inv "
                      + "LEFT JOIN ITEM it ON inv.ITNO = it.ITNO "
                      + "LEFT JOIN EQUIPMENT eq ON inv.EQNO = eq.EQNO "
                      + "WHERE inv.SVNO = ? ORDER BY inv.BXNO ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(invSql)) {

            pstmt.setInt(1, svNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int bxNo = rs.getInt("BXNO");
                    String type = rs.getString("TYPE");
                    if (bxNo >= 1 && bxNo <= 50 && type != null) {
                        if ("IT".equals(type)) {
                            int itNo = rs.getInt("ITNO");
                            String itName = rs.getString("ITNAME");
                            int itPrice = rs.getInt("ITPRICE");
                            loadedBox[bxNo - 1] = GameMasterManager.createItemInstance(itNo, itName, itPrice);
                        } else if ("EQ".equals(type)) {
                            int eqNo = rs.getInt("EQNO");
                            String eqName = rs.getString("EQNAME");
                            int eqPrice = rs.getInt("EQPRICE");
                            loadedBox[bxNo - 1] = GameMasterManager.createEquipmentInstance(eqNo, eqName, eqPrice);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        BattleGame.setItemBox(loadedBox);
    }

    //カレントパーティテーブルにパーティを保存するメソッド(保存結果によって次の画面へ進むかどうかを切り替えたいからboolean)
    public static boolean savePartyData (int svNo, List<Player> partyList) {
        // 既存のパーティレコードの PTNO 一覧を取得するSQL
        String selectPtSql = "SELECT PTNO FROM current_party WHERE SVNO = ? ORDER BY PTNO ASC";

        // 既存レコードを上書き更新するSQL
        String updateSql = "UPDATE current_party "
                         + "SET PLNO = ?, PL_LEVEL = ?, PL_CURRENT_HP = ?, PL_MAX_HP = ?, PL_MAX_POW = ?, PL_MAX_MGC = ?, PL_EQ1 = ?, PL_EQ2 = ? "
                         + "WHERE PTNO = ? AND SVNO = ?";

        // 新規作成直後など、まだレコードが存在しない場合のみ使うINSERT
        String maxPtSql = "SELECT NVL(MAX(PTNO), 0) FROM current_party";
        String insertSql = "INSERT INTO current_party "
                         + "(PTNO, SVNO, PLNO, PL_LEVEL, PL_CURRENT_HP, PL_MAX_HP, PL_MAX_POW, PL_MAX_MGC, PL_EQ1, PL_EQ2) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                        int eq1No = (p.getEquip1() != null) ? p.getEquip1().getDbNo() : 0;
                        int eq2No = (p.getEquip2() != null) ? p.getEquip2().getDbNo() : 0;

                        updatePstmt.setInt(1, plNo);
                        updatePstmt.setInt(2, p.getLevel());
                        updatePstmt.setInt(3, p.getHp());
                        updatePstmt.setInt(4, p.getMaxHp());
                        updatePstmt.setInt(5, p.getAtk());
                        updatePstmt.setInt(6, p.getMgc());
                        updatePstmt.setInt(7, eq1No);
                        updatePstmt.setInt(8, eq2No);
                        updatePstmt.setInt(9, ptNo);
                        updatePstmt.setInt(10, svNo);

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
                        int eq1No = (p.getEquip1() != null) ? p.getEquip1().getDbNo() : 0;
                        int eq2No = (p.getEquip2() != null) ? p.getEquip2().getDbNo() : 0;

                        insertPstmt.setInt(1, ptNo);
                        insertPstmt.setInt(2, svNo);
                        insertPstmt.setInt(3, plNo);
                        insertPstmt.setInt(4, p.getLevel());
                        insertPstmt.setInt(5, p.getHp());
                        insertPstmt.setInt(6, p.getMaxHp());
                        insertPstmt.setInt(7, p.getAtk());
                        insertPstmt.setInt(8, p.getMgc());
                        insertPstmt.setInt(9, eq1No);
                        insertPstmt.setInt(10, eq2No);

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
                    initInventorySlots(conn, newSvNo);//INVENTORY テーブルに BXNO 1〜50 の空枠を作成
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

    // 既存のセーブスロットを新規ゲーム用にリセット（上書き初期化）するメソッド
    public static boolean overwriteSaveDataForNewGame(int svNo, int initialGold) {
        //選んだセーブスロットのコインと日時をリセットする
        String updateSql = "UPDATE save_data SET SVCOIN = ?, SVDATE = ? WHERE SVNO = ?";
        //選んだセーブスロットのパーティを削除する
        String deletePartySql = "DELETE FROM current_party WHERE SVNO = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // トランザクション開始

            // 1. save_data の所持金と日時を初期化
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, initialGold);
                updatePstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                updatePstmt.setInt(3, svNo);
                int updated = updatePstmt.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. 古いパーティデータを削除（この後のキャラ選択の「出発!!」で新しい4人が INSERT される）
            try (PreparedStatement deletePstmt = conn.prepareStatement(deletePartySql)) {
                deletePstmt.setInt(1, svNo);
                deletePstmt.executeUpdate();
            }
            try (PreparedStatement delInvPstmt = conn.prepareStatement("DELETE FROM INVENTORY WHERE SVNO = ?")) {
                delInvPstmt.setInt(1, svNo);
                delInvPstmt.executeUpdate();
            }
            initInventorySlots(conn, svNo);//空のアイテムスロットを作る

            conn.commit();
            System.out.println("スロット " + svNo + " を新規データで上書き初期化しました！");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 指定されたセーブスロット(svNo)のデータを完全に削除するメソッド(自分を楽にするために作った)
    public static boolean deleteSaveData(int svNo) {
        String deleteInvSql = "DELETE FROM INVENTORY WHERE SVNO = ?";
        String deletePartySql = "DELETE FROM current_party WHERE SVNO = ?";
        String deleteSaveSql = "DELETE FROM save_data WHERE SVNO = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // トランザクション開始

            // 1. 先に子テーブル（INVENTORY）を削除 
            try (PreparedStatement invPstmt = conn.prepareStatement(deleteInvSql)) {
                invPstmt.setInt(1, svNo);
                invPstmt.executeUpdate();
            }

            // 2. 次に子テーブル（パーティ情報）を削除
            try (PreparedStatement partyPstmt = conn.prepareStatement(deletePartySql)) {
                partyPstmt.setInt(1, svNo);
                partyPstmt.executeUpdate();
            }

            // 3. 最後に親テーブル（セーブデータ本体）を削除
            try (PreparedStatement savePstmt = conn.prepareStatement(deleteSaveSql)) {
                savePstmt.setInt(1, svNo);
                int deleted = savePstmt.executeUpdate();
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            System.out.println("スロット " + svNo + " のセーブデータを削除しました。");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 指定スロットの INVENTORY テーブルに BXNO 1〜15 の空スロットを作成する補助メソッド
    private static void initInventorySlots(Connection conn, int svNo) throws SQLException {
        String invSql = "INSERT INTO INVENTORY (SVNO, BXNO, TYPE, ITNO, EQNO) VALUES (?, ?, NULL, NULL, NULL)";
        try (PreparedStatement pstmt = conn.prepareStatement(invSql)) {
            for (int bxNo = 1; bxNo <= 10; bxNo++) {//最初は10枠
                pstmt.setInt(1, svNo);
                pstmt.setInt(2, bxNo);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    //1枠分のインベントリを即時 UPDATE するメソッド（購入・拾得・使用時に呼ぶ）
    public static void updateInventorySlot(int svNo, int bxNo, Item item) {
        String sql = "UPDATE INVENTORY SET TYPE = ?, ITNO = ?, EQNO = ? WHERE SVNO = ? AND BXNO = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (item == null) {
                pstmt.setNull(1, Types.VARCHAR);
                pstmt.setNull(2, Types.INTEGER);
                pstmt.setNull(3, Types.INTEGER);
            } else if (item instanceof EquipmentItem) {
                pstmt.setString(1, "EQ");
                pstmt.setNull(2, Types.INTEGER);
                pstmt.setInt(3, item.getDbNo());
            } else {
                pstmt.setString(1, "IT");
                pstmt.setInt(2, item.getDbNo());
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, svNo);
            pstmt.setInt(5, bxNo);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //アイテムボックス全体を一括で INVENTORY に保存するメソッド（セーブボタン用）
    public static boolean saveAllInventory(int svNo, Item[] itembox) {
        String sql = "UPDATE INVENTORY SET TYPE = ?, ITNO = ?, EQNO = ? WHERE SVNO = ? AND BXNO = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < itembox.length; i++) {
                Item item = itembox[i];
                if (item == null) {
                    pstmt.setNull(1, Types.VARCHAR);
                    pstmt.setNull(2, Types.INTEGER);
                    pstmt.setNull(3, Types.INTEGER);
                } else if (item instanceof EquipmentItem) {
                    pstmt.setString(1, "EQ");
                    pstmt.setNull(2, Types.INTEGER);
                    pstmt.setInt(3, item.getDbNo());
                } else {
                    pstmt.setString(1, "IT");
                    pstmt.setInt(2, item.getDbNo());
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setInt(4, svNo);
                pstmt.setInt(5, i + 1); // BXNO は 1〜50
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //ウィズダムポーチ装備時にDBの INVENTORY に指定範囲の枠（例: 11〜15）を追加する
    public static void expandInventorySlots(int svNo, int oldMax, int newMax) {
        String invSql = "INSERT INTO INVENTORY (SVNO, BXNO, TYPE, ITNO, EQNO) VALUES (?, ?, NULL, NULL, NULL)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(invSql)) {
            for (int bxNo = oldMax + 1; bxNo <= newMax; bxNo++) {
                pstmt.setInt(1, svNo);
                pstmt.setInt(2, bxNo);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //ウィズダムポーチ解除時にDBの INVENTORY から減った分の枠（例: 11〜15）を削除する
    public static void shrinkInventorySlots(int svNo, int newMax, int oldMax) {
        String delSql = "DELETE FROM INVENTORY WHERE SVNO = ? AND BXNO > ? AND BXNO <= ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(delSql)) {
            pstmt.setInt(1, svNo);
            pstmt.setInt(2, newMax);
            pstmt.setInt(3, oldMax);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}