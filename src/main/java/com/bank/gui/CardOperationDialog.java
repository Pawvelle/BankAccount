package com.bank.gui;

import com.bank.exception.BankException;
import com.bank.model.BankAccount;
import com.bank.model.BankUser;
import com.bank.model.CreditAccount;
import com.bank.model.SavingsAccount;
import com.bank.service.BankUserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 银行卡操作对话框。
 */
public class CardOperationDialog extends JDialog {
    private final BankUserManager userManager;
    private final BankAccount account;
    private final JLabel balanceValueLabel = new JLabel();
    private final JLabel statusValueLabel = new JLabel();

    public CardOperationDialog(JFrame owner, BankUserManager userManager, BankUser user, BankAccount account) {
        super(owner, "卡片操作", true);
        this.userManager = userManager;
        this.account = account;

        JPanel root = new JPanel();
        root.setBackground(UiUtils.BG);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(32, 36, 28, 36));

        JLabel title = new JLabel(UiUtils.cardTypeName(account));
        title.setFont(UiUtils.displayFont(26));
        title.setForeground(UiUtils.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);

        JLabel sub = new JLabel(account.getAccountNumber());
        sub.setFont(UiUtils.bodyFont(14));
        sub.setForeground(UiUtils.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(6, 0, 24, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(sub);

        UiUtils.RoundedPanel info = UiUtils.card();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        balanceValueLabel.setText(UiUtils.formatMoney(account.getBalance()));
        info.add(balanceRow());
        info.add(divider());
        statusValueLabel.setText(account.isLocked() ? "已锁定" : "正常");
        info.add(statusRow());
        if (account instanceof SavingsAccount) {
            info.add(divider());
            info.add(UiUtils.infoRow("年利率",
                    String.format("%.2f%%", ((SavingsAccount) account).getInterestRate() * 100)));
        } else if (account instanceof CreditAccount) {
            CreditAccount c = (CreditAccount) account;
            info.add(divider());
            info.add(UiUtils.infoRow("信用额度", UiUtils.formatMoney(c.getCreditLimit())));
            info.add(divider());
            info.add(UiUtils.infoRow("已用额度", UiUtils.formatMoney(c.getUsedCredit())));
        }
        root.add(info);
        root.add(Box.createVerticalStrut(24));

        JPanel buttons = new JPanel(new GridLayout(0, 2, 12, 12));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        addAction(buttons, "存款", true, this::doDeposit);
        addAction(buttons, "取款", false, this::doWithdraw);
        addAction(buttons, "修改密码", false, this::changePassword);
        addAction(buttons, "详细信息", false, this::showDetails);
        if (account instanceof SavingsAccount) {
            addAction(buttons, "结息", false, this::applyInterest);
        } else if (account instanceof CreditAccount) {
            addAction(buttons, "信用卡管理", true, this::openCreditFeatures);
        }
        // 「重置卡密码」始终显示：卡片锁定时是解锁入口，未锁定时也是紧急重置入口
        addAction(buttons, account.isLocked() ? "重置卡密码（已锁定）" : "重置卡密码",
                true, this::resetLockedCardPassword);
        root.add(buttons);
        root.add(Box.createVerticalStrut(20));

        UiUtils.RoundedButton close = UiUtils.quietButton("关闭");
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.setPreferredSize(new Dimension(120, 36));
        close.setMaximumSize(new Dimension(120, 36));
        close.addActionListener(e -> dispose());
        root.add(close);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setLocationRelativeTo(owner);
    }

    private JPanel balanceRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel k = new JLabel("余额");
        k.setFont(UiUtils.bodyFont(14));
        k.setForeground(UiUtils.TEXT_SECONDARY);
        balanceValueLabel.setFont(UiUtils.titleFont(18));
        balanceValueLabel.setForeground(UiUtils.TEXT);
        balanceValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(k, BorderLayout.WEST);
        row.add(balanceValueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel statusRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel k = new JLabel("状态");
        k.setFont(UiUtils.bodyFont(14));
        k.setForeground(UiUtils.TEXT_SECONDARY);
        statusValueLabel.setFont(UiUtils.bodyFont(14));
        statusValueLabel.setForeground(UiUtils.TEXT);
        statusValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(k, BorderLayout.WEST);
        row.add(statusValueLabel, BorderLayout.EAST);
        return row;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UiUtils.DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void addAction(JPanel buttons, String text, boolean primary, Runnable action) {
        UiUtils.RoundedButton b = primary ? UiUtils.primaryButton(text) : UiUtils.secondaryButton(text);
        b.setPreferredSize(new Dimension(0, 44));
        b.addActionListener(e -> action.run());
        buttons.add(b);
    }

    private String promptSecret(String message) {
        JPasswordField pf = new JPasswordField();
        pf.setColumns(14);
        UiUtils.styleField(pf);
        int r = JOptionPane.showConfirmDialog(this, new Object[]{message, pf}, "安全验证",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(pf.getPassword());
    }

    private Double promptAmount(String message) {
        String s = JOptionPane.showInputDialog(this, message);
        if (s == null) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字金额。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void refreshBalance() {
        balanceValueLabel.setText(UiUtils.formatMoney(account.getBalance()));
        statusValueLabel.setText(account.isLocked() ? "已锁定" : "正常");
    }

    private void doDeposit() {
        String pwd = promptSecret("请输入银行卡密码");
        if (pwd == null) {
            return;
        }
        Double amount = promptAmount("请输入存款金额");
        if (amount == null) {
            return;
        }
        try {
            account.deposit(pwd, amount);
            userManager.saveData();
            refreshBalance();
            notifyLegacyIfNeeded();
            JOptionPane.showMessageDialog(this,
                    "存款成功！\n存款金额：" + UiUtils.formatMoney(amount)
                            + "\n当前余额：" + UiUtils.formatMoney(account.getBalance()),
                    "操作成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "操作失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doWithdraw() {
        String pwd = promptSecret("请输入银行卡密码");
        if (pwd == null) {
            return;
        }
        Double amount = promptAmount("请输入取款金额");
        if (amount == null) {
            return;
        }
        try {
            account.withdraw(pwd, amount);
            userManager.saveData();
            refreshBalance();
            notifyLegacyIfNeeded();
            JOptionPane.showMessageDialog(this,
                    "取款成功！\n取款金额：" + UiUtils.formatMoney(amount)
                            + "\n当前余额：" + UiUtils.formatMoney(account.getBalance()),
                    "操作成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "操作失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 旧版数据迁移提示：本次会话中如果刚把卡片密码从明文升级为 hash 存储，
     * 弹窗提醒用户尽快修改成新密码。
     */
    private void notifyLegacyIfNeeded() {
        if (account.isLegacyPasswordUpgraded()) {
            JOptionPane.showMessageDialog(this,
                    "检测到这张卡的数据来自旧版本。\n" +
                            "为保障资金安全，请尽快通过「修改密码」或「重置卡密码」设置一个新的6位数字密码。",
                    "安全提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void changePassword() {
        String oldPwd = promptSecret("请输入原密码 (6位数字)");
        if (oldPwd == null) {
            return;
        }
        String newPwd = promptSecret("请输入新密码 (6位数字)");
        if (newPwd == null) {
            return;
        }
        String confirm = promptSecret("请再次输入新密码");
        if (confirm == null) {
            return;
        }
        try {
            if (account.setNewAccountPassword(oldPwd, newPwd, confirm)) {
                userManager.saveData();
                refreshBalance();
                JOptionPane.showMessageDialog(this, "银行卡密码修改成功。", "操作成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "修改失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("卡号：").append(account.getAccountNumber()).append("\n");
        sb.append("持卡人ID：").append(account.getAccountHolder()).append("\n");
        sb.append("类型：").append(UiUtils.cardTypeName(account)).append("\n");
        sb.append("状态：").append(account.isLocked() ? "已锁定" : "正常").append("\n");
        sb.append("余额：").append(UiUtils.formatMoney(account.getBalance())).append("\n");
        if (account instanceof SavingsAccount) {
            sb.append("年利率：").append(String.format("%.2f%%", ((SavingsAccount) account).getInterestRate() * 100)).append("\n");
        } else if (account instanceof CreditAccount) {
            CreditAccount c = (CreditAccount) account;
            sb.append("信用额度：").append(UiUtils.formatMoney(c.getCreditLimit())).append("\n");
            sb.append("已用额度：").append(UiUtils.formatMoney(c.getUsedCredit())).append("\n");
            double available = c.getBalance() + c.getCreditLimit() - c.getUsedCredit();
            sb.append("可用额度：").append(UiUtils.formatMoney(available)).append("\n");
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(UiUtils.bodyFont(14));
        area.setBackground(UiUtils.BG);
        JOptionPane.showMessageDialog(this, area, "银行卡详细信息", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyInterest() {
        if (!(account instanceof SavingsAccount)) {
            return;
        }
        SavingsAccount s = (SavingsAccount) account;
        s.applyInterest();
        userManager.saveData();
        refreshBalance();
        JOptionPane.showMessageDialog(this,
                "结息处理完成！\n当前余额：" + UiUtils.formatMoney(s.getBalance()),
                "操作成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openCreditFeatures() {
        if (!(account instanceof CreditAccount)) {
            return;
        }
        CreditFeaturesDialog dialog = new CreditFeaturesDialog(this, userManager, (CreditAccount) account);
        dialog.setVisible(true);
        refreshBalance();
    }

    /**
     * 卡片锁定后强制重置密码：输入新密码 + 确认即可解锁。
     */
    private void resetLockedCardPassword() {
        JPasswordField newPwd = new JPasswordField();
        JPasswordField confirmPwd = new JPasswordField();
        UiUtils.styleField(newPwd);
        UiUtils.styleField(confirmPwd);
        int r = JOptionPane.showConfirmDialog(this,
                new Object[]{"请输入新密码 (6位数字)：", newPwd, "再次输入新密码：", confirmPwd},
                "重置卡密码",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            account.forceResetPassword(new String(newPwd.getPassword()), new String(confirmPwd.getPassword()));
            userManager.saveData();
            refreshBalance();
            JOptionPane.showMessageDialog(this,
                    "卡片密码已重置，锁定状态已解除。\n卡号：" + account.getAccountNumber(),
                    "重置成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, "重置失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
