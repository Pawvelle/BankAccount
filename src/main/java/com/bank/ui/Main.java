package com.bank.ui;

import com.bank.gui.MainFrame;
import com.bank.gui.UiUtils;

import javax.swing.*;

/**
 * 系统入口：启动 GUI 图形界面。
 */
public class Main {
    public static void main(String[] args) {
        UiUtils.installLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
