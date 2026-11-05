import java.sql.Timestamp;
public class SaveSlotInfo {
    private int svNo;
    private int svCoin;
    private Timestamp svDate;

    public SaveSlotInfo(int svNo, int svCoin, Timestamp svDate) {
        this.svNo = svNo;
        this.svCoin = svCoin;
        this.svDate = svDate;
    }

    public int getSvNo() {
        return svNo;
    }

    public int getSvCoin() {
        return svCoin;
    }

    public Timestamp getSvDate() {
        return svDate;
    }

    //日付がnullなら空スロットとして扱う
    public boolean isEmpty() {
        return svDate == null;
    }
}