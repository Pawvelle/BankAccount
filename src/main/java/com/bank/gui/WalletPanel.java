package com.bank.gui;

import com.bank.model.BankAccount;
import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 卡包管理：精简工具栏 + 干净表格。
 */
public class WalletPanel extends JPanel {
    private final MainFrame frame;
    private final BankUserManager userManager;
    private BankUser user;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"", "卡号", "类型", "余额", "状态"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public WalletPanel(MainFrame frame, BankUserManager userManager) {
        this.frame = frame;
        this.userManager = userManager;
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);

        table.setFont(UiUtils.bodyFont(14));
        table.setRowHeight(52);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UiUtils.DIVIDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(UiUtils.captionFont(12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
        left.setForeground(UiUtils.TEXT);
        DefaultTableCellRenderer secondary = new DefaultTableCellRenderer();
        secondary.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        secondary.setForeground(UiUtils.TEXT_SECONDARY);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 16));
        right.setForeground(UiUtils.TEXT);

        table.setDefaultRenderer(Object.class, left);
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(0).setMaxWidth(56);
        table.getColumnModel().getColumn(0).setCellRenderer(secondary);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setCellRenderer(secondary);
        table.getColumnModel().getColumn(3).setCellRenderer(right);
        table.getColumnModel().getColumn(4).setCellRenderer(secondary);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openSelectedCard();
                }
            }
        });
    }

    public void refresh(BankUser user) {
        this.user = user;
        model.setRowCount(0);
        List<BankAccount> accounts = user.getMyAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount acc = accounts.get(i);
            model.addRow(new Object[]{
                    String.valueOf(i + 1),
                    acc.getAccountNumber(),
                    UiUtils.cardTypeName(acc),
                    UiUtils.formatMoney(acc.getBalance()),
                    acc.isLocked() ? "已锁定" : "正常"
            });
        }
        removeAll();
        add(buildNav(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent buildNav() {
        UiUtils.RoundedButton back = UiUtils.ghostButton("← 用户中心");
        back.setPreferredSize(new Dimension(120, 32));
        back.addActionListener(e -> frame.showUserCenter(user));

        JLabel brand = new JLabel("卡包");
        brand.setFont(UiUtils.titleFont(15));
        brand.setForeground(UiUtils.TEXT);

        return UiUtils.navBar(back, brand);
    }

    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(36, 72, 36, 72));

        JPanel title = UiUtils.pageTitle(
                "我的卡包",
                user.getUsername() + "  ·  双击卡片即可操作");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(title);
        body.add(Box.createVerticalStrut(28));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        UiUtils.RoundedButton apply = UiUtils.primaryButton("申请新卡");
        apply.setPreferredSize(new Dimension(140, 42));
        apply.addActionListener(e -> {
            ApplyCardDialog dialog = new ApplyCardDialog(frame, userManager, user);
            dialog.setVisible(true);
            refresh(user);
        });
        actions.add(apply);

        UiUtils.RoundedButton use = UiUtils.secondaryButton("使用选中卡");
        use.setPreferredSize(new Dimension(140, 42));
        use.addActionListener(e -> openSelectedCard());
        actions.add(use);

        body.add(actions);
        body.add(Box.createVerticalStrut(24));

        if (user.getMyAccounts().isEmpty()) {
            UiUtils.RoundedPanel empty = UiUtils.card();
            empty.setLayout(new GridBagLayout());
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
            empty.setPreferredSize(new Dimension(0, 220));

            JPanel msg = new JPanel();
            msg.setOpaque(false);
            msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
            JLabel t = new JLabel("还没有银行卡");
            t.setFont(UiUtils.titleFont(20));
            t.setForeground(UiUtils.TEXT);
            t.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel s = new JLabel("申请一张新卡，开始管理你的资产");
            s.setFont(UiUtils.bodyFont(14));
            s.setForeground(UiUtils.TEXT_SECONDARY);
            s.setAlignmentX(Component.CENTER_ALIGNMENT);
            s.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            msg.add(t);
            msg.add(s);
            empty.add(msg);
            body.add(empty);
            return body;
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UiUtils.CARD_BG);
        scroll.setOpaque(false);

        UiUtils.RoundedPanel tableCard = UiUtils.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tableCard.add(scroll, BorderLayout.CENTER);
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));

        body.add(tableCard);
        return body;
    }

    private void openSelectedCard() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一张银行卡。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BankAccount acc = user.getMyAccounts().get(row);
        CardOperationDialog dialog = new CardOperationDialog(frame, userManager, user, acc);
        dialog.setVisible(true);
        refresh(user);
    }
}
