package com.bank.gui;

import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 用户中心：大标题 + 统计数字 + 精简操作，留白优先。
 */
public class UserCenterPanel extends JPanel {
    private final MainFrame frame;
    private final BankUserManager userManager;
    private BankUser user;

    public UserCenterPanel(MainFrame frame, BankUserManager userManager) {
        this.frame = frame;
        this.userManager = userManager;
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
    }

    public void refresh(BankUser user) {
        this.user = user;
        removeAll();
        add(buildNav(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent buildNav() {
        JLabel brand = new JLabel("Bank Account");
        brand.setFont(UiUtils.titleFont(15));
        brand.setForeground(UiUtils.TEXT);

        UiUtils.RoundedButton logout = UiUtils.quietButton("退出");
        logout.setPreferredSize(new Dimension(72, 32));
        logout.addActionListener(e -> frame.showLogin());

        return UiUtils.navBar(brand, logout);
    }

    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(40, 72, 40, 72));

        JPanel title = UiUtils.pageTitle(
                "你好，" + user.getUsername(),
                "用户 ID  " + user.getId());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(title);
        body.add(Box.createVerticalStrut(36));

        // 统计：无描边白底，数字为主
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        stats.add(buildStat("总资产", UiUtils.formatMoney(user.calculateTotalWealth()), UiUtils.TEXT));
        stats.add(buildStat("银行卡", String.valueOf(user.getMyAccounts().size()) + " 张", UiUtils.TEXT));
        stats.add(buildStat("状态", user.isLocked() ? "已锁定" : "正常",
                user.isLocked() ? UiUtils.DANGER : UiUtils.SUCCESS));
        body.add(stats);
        body.add(Box.createVerticalStrut(28));

        // 主操作：一主一次
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        UiUtils.RoundedButton walletBtn = UiUtils.primaryButton("卡包管理");
        walletBtn.setPreferredSize(new Dimension(168, 44));
        walletBtn.addActionListener(e -> frame.showWallet(user));
        actions.add(walletBtn);

        UiUtils.RoundedButton editBtn = UiUtils.secondaryButton("个人资料");
        editBtn.setPreferredSize(new Dimension(140, 44));
        editBtn.addActionListener(e -> {
            new EditProfileDialog(frame, userManager, user).setVisible(true);
            refresh(user);
        });
        actions.add(editBtn);

        UiUtils.RoundedButton rankBtn = UiUtils.ghostButton("资产排行");
        rankBtn.setPreferredSize(new Dimension(100, 44));
        rankBtn.addActionListener(e -> new RankingDialog(frame, userManager).setVisible(true));
        actions.add(rankBtn);

        body.add(actions);
        body.add(Box.createVerticalStrut(36));

        body.add(buildInfoCard());
        return body;
    }

    private JPanel buildStat(String caption, String value, Color accent) {
        UiUtils.RoundedPanel card = UiUtils.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel cap = new JLabel(caption);
        cap.setFont(UiUtils.captionFont(13));
        cap.setForeground(UiUtils.TEXT_SECONDARY);
        col.add(cap);
        col.add(Box.createVerticalStrut(10));

        JLabel val = new JLabel(value);
        val.setFont(UiUtils.displayFont(26));
        val.setForeground(accent);
        col.add(val);

        card.add(col, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildInfoCard() {
        UiUtils.RoundedPanel card = UiUtils.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JLabel title = new JLabel("基本资料");
        title.setFont(UiUtils.titleFont(17));
        title.setForeground(UiUtils.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        String[][] rows = {
                {"用户名", user.getUsername()},
                {"生日", user.getBirthday()},
                {"手机", user.getPhone()},
                {"邮箱", user.getEmail()},
                {"状态", user.isLocked() ? "已锁定" : "正常"}
        };
        for (int i = 0; i < rows.length; i++) {
            JPanel row = UiUtils.infoRow(rows[i][0], rows[i][1]);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            card.add(row);
            if (i < rows.length - 1) {
                JSeparator sep = new JSeparator();
                sep.setForeground(UiUtils.DIVIDER);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(sep);
            }
        }

        return card;
    }
}
