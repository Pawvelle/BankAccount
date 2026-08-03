package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankException;
import com.bank.model.BankAccount;
import com.bank.model.BankUser;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BankUserManager {
    private List<BankUser> users;
    private static final String DATA_FILE = "data/bank_data.dat";
    private String loadErrorMessage;

    public BankUserManager() {
        this.users = new ArrayList<>();
        loadData();
    }

    /**
     * 如果本地数据加载失败（兼容性问题或文件损坏），返回错误描述。
     * GUI 可在启动时检查并提示用户。
     */
    public String getLoadErrorMessage() {
        return loadErrorMessage;
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.users = (List<BankUser>) ois.readObject();

            int maxUserId = 100000;
            int maxAccountId = 2026000;
            for (BankUser user : users) {
                try {
                    int uid = Integer.parseInt(user.getId().replace("USER_", ""));
                    if (uid > maxUserId) maxUserId = uid;
                } catch (Exception ignored) {}

                for (BankAccount acc : user.getMyAccounts()) {
                    try {
                        int aid = Integer.parseInt(acc.getAccountNumber().replace("HUE_", ""));
                        if (aid > maxAccountId) maxAccountId = aid;
                    } catch (Exception ignored) {}
                }
            }
            BankUser.updateUserCounter(maxUserId);
            BankAccount.updateAccountCounter(maxAccountId);

            System.out.println("成功从本地加载数据，当前用户数：" + users.size());
        } catch (Exception e) {
            // 记录到字段，GUI 启动时可读出此消息
            this.loadErrorMessage = "本地数据格式更新或文件损坏（" + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "），已初始化为空数据。\n"
                    + "如你确定是版本升级导致，请联系开发者手动迁移 data/bank_data.dat。";
            System.out.println(loadErrorMessage);
            this.users = new ArrayList<>();
        }
    }

    public void saveData() {
        File file = new File(DATA_FILE);
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("保存数据失败：" + e.getMessage());
        }
    }

    public void addUser(BankUser user) {
        if (user == null) {
            throw new BankException("用户不能为空！");
        }

        users.add(user);
        saveData();
    }

    public BankUser findUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new AccountNotFoundException("请输入有效的用户ID！");
        }
        for (BankUser user : users) {
            if (user.getId().equalsIgnoreCase(id.trim())) {
                return user;
            }
        }
        throw new AccountNotFoundException("未找到ID为 [" + id + "] 的用户。");
    }

    public BankUser authenticate(String id, String password) {
        BankUser user = findUserById(id);
        user.verifyPassword(password);
        return user;
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
