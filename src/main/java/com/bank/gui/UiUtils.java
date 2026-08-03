package com.bank.gui;

import com.bank.model.BankAccount;
import com.bank.model.CreditAccount;
import com.bank.model.SavingsAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * GUI 通用工具类：Apple 官网风格配色、排版与自定义组件。
 * 全部基于纯 JDK Swing 实现，无需第三方依赖。
 */
public final class UiUtils {

    // ================= Apple 风格调色板 =================
    /** 主色：Apple 蓝 */
    public static final Color ACCENT         = new Color(0x0071E3);
    /** 主色（悬停） */
    public static final Color ACCENT_HOVER   = new Color(0x0077ED);
    /** 主色（按下） */
    public static final Color ACCENT_PRESSED = new Color(0x0066CC);
    /** 主色（极浅底） */
    public static final Color ACCENT_SOFT    = new Color(0xE8F0FE);
    /** 页面背景 */
    public static final Color BG             = new Color(0xF5F5F7);
    /** 卡片 / 导航背景 */
    public static final Color CARD_BG        = new Color(0xFFFFFF);
    /** 正文 */
    public static final Color TEXT           = new Color(0x1D1D1F);
    /** 次要文字（Apple #86868b） */
    public static final Color TEXT_SECONDARY = new Color(0x86868B);
    /** 发丝边框 */
    public static final Color BORDER         = new Color(0xD2D2D7);
    /** 分割线 */
    public static final Color DIVIDER        = new Color(0xE8E8ED);
    /** 成功绿 */
    public static final Color SUCCESS        = new Color(0x34C759);
    /** 危险红 */
    public static final Color DANGER         = new Color(0xFF3B30);
    /** 输入框背景 */
    public static final Color FIELD_BG       = new Color(0xF5F5F7);
    /** 导航半透明感（实色近似） */
    public static final Color NAV_BG         = new Color(0xFAFAFC);

    private static final String FONT_NAME = detectFont();

    private UiUtils() {}

    private static String detectFont() {
        String[] preferred = {
                "SF Pro Display", "SF Pro Text", "PingFang SC",
                "Helvetica Neue", "Helvetica", "Arial"
        };
        try {
            String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String want : preferred) {
                for (String name : available) {
                    if (want.equalsIgnoreCase(name)) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) { /* 环境异常时使用默认字体 */ }
        return "SansSerif";
    }

    // ================= 排版 =================
    public static Font displayFont(int size) { return new Font(FONT_NAME, Font.BOLD, size); }
    public static Font titleFont(int size)   { return new Font(FONT_NAME, Font.BOLD, size); }
    public static Font bodyFont(int size)    { return new Font(FONT_NAME, Font.PLAIN, size); }
    public static Font captionFont(int size) { return new Font(FONT_NAME, Font.PLAIN, size); }
    public static Font labelFont()           { return new Font(FONT_NAME, Font.PLAIN, 12); }

    /** 应用系统外观并统一字体，保证跨平台观感一致。 */
    public static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* 使用默认外观 */ }

        Font ui = bodyFont(14);
        UIManager.put("TextField.font", ui);
        UIManager.put("PasswordField.font", ui);
        UIManager.put("ComboBox.font", ui);
        UIManager.put("TextArea.font", ui);
        UIManager.put("Table.font", ui);
        UIManager.put("TableHeader.font", captionFont(12));
        UIManager.put("OptionPane.messageFont", bodyFont(14));
        UIManager.put("OptionPane.buttonFont", bodyFont(13));
        UIManager.put("Label.font", ui);
        UIManager.put("Button.font", ui);
        UIManager.put("ComboBox.background", FIELD_BG);
        UIManager.put("ComboBox.foreground", TEXT);

        UIManager.put("Table.background", CARD_BG);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", DIVIDER);
        UIManager.put("Table.selectionBackground", ACCENT_SOFT);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", BG);
        UIManager.put("TableHeader.foreground", TEXT_SECONDARY);

        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", CARD_BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("ScrollBar.width", 10);
    }

    public static String formatMoney(double amount) {
        return String.format("%,.2f", amount) + " 元";
    }

    public static String cardTypeName(BankAccount acc) {
        if (acc instanceof SavingsAccount) {
            return "储蓄卡";
        }
        if (acc instanceof CreditAccount) {
            return "信用卡";
        }
        return "未知类型";
    }

    /** 统一修饰输入框：浅底、无描边、聚焦时蓝色细环。 */
    public static void styleField(JComponent field) {
        field.setFont(bodyFont(15));
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT);
        field.setOpaque(true);
        applyFocusBorder(field, false);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                applyFocusBorder(field, true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                applyFocusBorder(field, false);
            }
        });

        if (field instanceof JTextComponent) {
            ((JTextComponent) field).setCaretColor(ACCENT);
        }
    }

    private static void applyFocusBorder(JComponent field, boolean focused) {
        Color ring = focused ? ACCENT : new Color(0, 0, 0, 0);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ring, focused ? 2 : 1, true),
                BorderFactory.createEmptyBorder(focused ? 10 : 11, focused ? 13 : 14, focused ? 10 : 11, focused ? 13 : 14)));
    }

    /** 纵向表单项：上方小标签 + 下方输入框。 */
    public static JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(labelFont());
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(l);
        row.add(Box.createVerticalStrut(6));

        styleField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.add(field);
        return row;
    }

    /** 顶部导航栏：白底 + 底部分割线，左右分区。 */
    public static JPanel navBar(JComponent left, JComponent right) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(NAV_BG);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
                BorderFactory.createEmptyBorder(18, 48, 18, 40)));
        if (left != null) {
            nav.add(left, BorderLayout.WEST);
        }
        if (right != null) {
            nav.add(right, BorderLayout.EAST);
        }
        return nav;
    }

    /** 页面大标题块。 */
    public static JPanel pageTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(displayFont(32));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);

        if (subtitle != null && !subtitle.isEmpty()) {
            p.add(Box.createVerticalStrut(6));
            JLabel s = new JLabel(subtitle);
            s.setFont(bodyFont(15));
            s.setForeground(TEXT_SECONDARY);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(s);
        }
        return p;
    }

    /** 资料键值行。 */
    public static JPanel infoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel k = new JLabel(key);
        k.setFont(bodyFont(14));
        k.setForeground(TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(bodyFont(14));
        v.setForeground(TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    // ================= 按钮工厂 =================
    /** 主按钮：Apple 蓝实心胶囊 */
    public static RoundedButton primaryButton(String text) {
        RoundedButton b = new RoundedButton(text, ACCENT, ACCENT_HOVER, ACCENT_PRESSED, null, Color.WHITE, 980);
        b.setFont(bodyFont(15));
        return b;
    }

    /** 次按钮：透明底 + 蓝色描边胶囊 */
    public static RoundedButton secondaryButton(String text) {
        RoundedButton b = new RoundedButton(text, new Color(0, 0, 0, 0),
                tint(ACCENT, 0.06f), tint(ACCENT, 0.12f), ACCENT, ACCENT, 980);
        b.setFont(bodyFont(15));
        return b;
    }

    /** 幽灵 / 链接按钮：蓝色文字 */
    public static RoundedButton ghostButton(String text) {
        RoundedButton b = new RoundedButton(text, new Color(0, 0, 0, 0),
                tint(ACCENT, 0.06f), tint(ACCENT, 0.12f), null, ACCENT, 12);
        b.setFont(bodyFont(14));
        return b;
    }

    /** 静默文字按钮（退出等）：灰色 */
    public static RoundedButton quietButton(String text) {
        RoundedButton b = new RoundedButton(text, new Color(0, 0, 0, 0),
                tint(TEXT_SECONDARY, 0.08f), tint(TEXT_SECONDARY, 0.14f), null, TEXT_SECONDARY, 12);
        b.setFont(bodyFont(14));
        return b;
    }

    /** 无边白卡片（Apple 常见：白块浮在灰底上，无描边） */
    public static RoundedPanel card() {
        return new RoundedPanel(18, CARD_BG, null, false);
    }

    /** 轻阴影卡片（仅用于登录等焦点区域） */
    public static RoundedPanel elevatedCard() {
        return new RoundedPanel(22, CARD_BG, null, true);
    }

    private static Color tint(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(alpha * 255));
    }

    private static Color shift(Color c, float f) {
        return new Color(Math.min(255, Math.round(c.getRed() * f)),
                Math.min(255, Math.round(c.getGreen() * f)),
                Math.min(255, Math.round(c.getBlue() * f)));
    }

    // ================= 氛围背景（登录页） =================
    public static class AtmospherePanel extends JPanel {
        public AtmospherePanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();

            // 柔和径向灰白氛围，避免纯平色
            GradientPaint gp = new GradientPaint(0, 0, new Color(0xFAFAFC), 0, h, BG);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // 顶部轻微冷色光晕
            RadialGradientPaint glow = new RadialGradientPaint(
                    w * 0.5f, -h * 0.05f, Math.max(w, h) * 0.7f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(232, 240, 254, 160), new Color(232, 240, 254, 0)});
            g2.setPaint(glow);
            g2.fillRect(0, 0, w, h / 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= 圆角按钮 =================
    public static class RoundedButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color stroke;
        private final int radius;
        private boolean hovered;

        public RoundedButton(String text, Color bg, Color hover, Color pressed, Color stroke, Color fg, int radius) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = hover;
            this.pressedBg = pressed;
            this.stroke = stroke;
            this.radius = radius;
            setForeground(fg);
            setFont(bodyFont(14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 20, 8, 20));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public RoundedButton(String text, Color bg, Color fg) {
            this(text, bg, shift(bg, 1.04f), shift(bg, 0.90f), null, fg, 980);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = hovered ? hoverBg : normalBg;
            if (getModel().isPressed()) {
                bg = pressedBg;
            }
            int r = Math.min(radius, Math.min(getWidth(), getHeight()));
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1f, getHeight() - 1f, r, r));
            if (stroke != null) {
                g2.setColor(stroke);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2f, getHeight() - 2f, r, r));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= 圆角面板 =================
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final Color stroke;
        private final boolean shadow;
        private static final int SHADOW_PAD = 8;

        public RoundedPanel(int radius, Color fill, Color stroke) {
            this(radius, fill, stroke, false);
        }

        public RoundedPanel(int radius, Color fill, Color stroke, boolean shadow) {
            this.radius = radius;
            this.fill = fill;
            this.stroke = stroke;
            this.shadow = shadow;
            setOpaque(false);
            if (shadow) {
                setBorder(new EmptyBorder(SHADOW_PAD, SHADOW_PAD, SHADOW_PAD + 4, SHADOW_PAD));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int p = shadow ? SHADOW_PAD : 0;
            int rw = w - 1 - p * 2;
            int rh = h - 1 - p * 2 - (shadow ? 4 : 0);

            if (shadow) {
                for (int i = 3; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 6 + i * 3));
                    g2.fillRoundRect(p - i + 1, p + i + 1, rw + (i - 1) * 2, rh + i, radius + 2, radius + 2);
                }
            }
            g2.setColor(fill);
            g2.fillRoundRect(p, p, rw, rh, radius, radius);
            if (stroke != null) {
                g2.setColor(stroke);
                g2.drawRoundRect(p, p, rw, rh, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

}
