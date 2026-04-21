import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BankUserManager {
    private final List<BankUser> users;

    public BankUserManager() {
        this.users = new ArrayList<>();
    }

    public void addUser(BankUser user) {
        if (user == null) {
            System.out.println("用户不能为空！");
            return;
        }

        users.add(user);
    }
    public List<BankUser> getAllUsers() {
        return new ArrayList<>(users);
    }

    public List<BankUser> getRankingByAssets() {
        List<BankUser> ranking = new ArrayList<>(users);
        ranking.sort(Comparator.comparingDouble(BankUser::calculateTotalWealth).reversed());
        return ranking;
    }
}
