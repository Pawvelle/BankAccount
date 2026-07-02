package com.bank.service;

import com.bank.model.BankAccount;
import com.bank.model.BankUser;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BankUserManager {
    private List<BankUser> users;
    private static final String DATA_FILE = "data/bank_data.dat";

    public BankUserManager() {
        this.users = new ArrayList<>();
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.users = (List<BankUser>) ois.readObject();
            
            // 恢复计数器，避免新用户/新卡的ID与旧数据冲突
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
            System.out.println("加载本地数据失败：" + e.getMessage());
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
            System.out.println("用户不能为空！");
            return;
        }

        users.add(user);
        saveData();
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
