package com.bank.gui;

import com.bank.model.BankUser;
import com.bank.service.BankUserManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 资产财富排行榜对话框。
 */
public class RankingDialog extends JDialog {
    public RankingDialog(JFrame owner, BankUserManager userManager) {
        super(owner, "资产排行", true);

        List<BankUser> ranking = userManager.getRankingByAssets();

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"排名", "用户名", "用户ID", "总资产", "状态"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (int i = 0; i < ranking.size(); i++) {
            BankUser u = ranking.get(i);
            model.addRow(new Object[]{
                    String.valueOf(i + 1),
                    u.getUsername(),
                    u.getId(),
                    UiUtils.formatMoney(u.calculateTotalWealth()),
                    u.isLocked() ? "已锁定" : "正常"
            });
        }

        JTable table = new JTable(model);
        table.setFont(UiUtils.bodyFont(14));
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UiUtils.DIVIDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(UiUtils.captionFont(12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
        cell.setForeground(UiUtils.TEXT);
        table.setDefaultRenderer(Object.class, cell);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UiUtils.CARD_BG);

        UiUtils.RoundedPanel tableCard = UiUtils.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tableCard.add(scroll, BorderLayout.CENTER);

        JLabel title = new JLabel("资产排行");
        title.setFont(UiUtils.displayFont(26));
        title.setForeground(UiUtils.TEXT);

        JLabel hint = new JLabel(ranking.isEmpty() ? "暂无用户数据" : "按总资产从高到低");
        hint.setFont(UiUtils.bodyFont(14));
        hint.setForeground(UiUtils.TEXT_SECONDARY);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 20, 0));

        UiUtils.RoundedButton close = UiUtils.primaryButton("完成");
        close.setPreferredSize(new Dimension(100, 40));
        close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        bottom.add(close);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.BG);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);
        top.add(hint);

        root.add(top, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(580, 480);
        setLocationRelativeTo(owner);
    }
}
