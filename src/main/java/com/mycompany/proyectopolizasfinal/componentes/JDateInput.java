package com.mycompany.proyectopolizasfinal.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.proyectopolizasfinal.util.EstiloUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Componente visual reutilizable para entrada y selección de fechas.
 * Admite variante estándar (flechas) y variante con selector rápido de año.
 */
public class JDateInput extends JPanel {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] DAYS_HEADER = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do"};
    private static final String[] MONTH_NAMES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private final boolean withYearSelect;
    private final JTextField textField;
    private final JButton calendarButton;
    private final List<Consumer<LocalDate>> changeListeners = new ArrayList<>();

    private boolean hasFocus = false;
    private boolean hasError = false;
    private boolean editable = true;

    private final JPanel calendarPanel;
    private JPopupMenu calendarPopup;
    private JDialog calendarDialog;
    private long dialogClosedTime = 0;

    private JLabel monthLabel;
    private JComboBox<Integer> yearComboBox;
    private boolean updatingYearCombo = false;
    private final JButton[] dayButtons = new JButton[42];

    private LocalDate currentDate;
    private YearMonth viewingMonth;

    public JDateInput() {
        this(null, false);
    }

    public JDateInput(boolean withYearSelect) {
        this(null, withYearSelect);
    }

    public JDateInput(LocalDate initialDate) {
        this(initialDate, false);
    }

    public JDateInput(LocalDate initialDate, boolean withYearSelect) {
        this.withYearSelect = withYearSelect;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(180, 34));

        textField = new JTextField(10);
        textField.setToolTipText("Formato: dd/MM/yyyy");
        textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd/MM/yyyy");
        textField.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
        textField.setOpaque(false);

        calendarButton = new JButton(EstiloUI.cargarIcono("calendar.svg", 15, "Label.disabledForeground"));
        calendarButton.setFocusable(false);
        calendarButton.setContentAreaFilled(false);
        calendarButton.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 10));
        calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarButton.setToolTipText(withYearSelect ? "Abrir calendario (con select de año)" : "Abrir calendario");

        calendarPanel = buildCalendarPanel();
        if (!withYearSelect) {
            calendarPopup = new JPopupMenu();
            calendarPopup.setFocusable(false);
            calendarPopup.setOpaque(false);
            calendarPopup.setBackground(new Color(0, 0, 0, 0));
            calendarPopup.setBorder(BorderFactory.createEmptyBorder());
            calendarPopup.add(calendarPanel);
        }

        add(textField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);

        initListeners();
        updateBorder();
        setDate(initialDate);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = editable
                ? EstiloUI.colorOrDefault("TextField.background", new Color(0x0A0A0A))
                : EstiloUI.colorOrDefault("Button.background", new Color(0x262626));
        Color borderCol = hasError
                ? EstiloUI.colorOrDefault("Component.error.borderColor", new Color(0xFF6467))
                : (hasFocus
                        ? EstiloUI.colorOrDefault("TextField.focusedBorderColor", new Color(0x737373))
                        : EstiloUI.colorOrDefault("TextField.borderColor", new Color(255, 255, 255, 38)));
        float strokeWidth = (hasFocus || hasError) ? 1.5f : 1f;
        float offset = strokeWidth / 2f;

        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(
                offset, offset, getWidth() - strokeWidth, getHeight() - strokeWidth, 10f, 10f);
        g2.setColor(bg);
        g2.fill(rect);
        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.draw(rect);
        g2.dispose();
        super.paintComponent(g);
    }

    private void updateBorder() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        repaint();
    }

    private void initListeners() {
        textField.addActionListener(e -> parseFromTextField(true));
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hasFocus = true;
                updateBorder();
            }

            @Override
            public void focusLost(FocusEvent e) {
                hasFocus = false;
                parseFromTextField(false);
                updateBorder();
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                textField.requestFocusInWindow();
            }
        });

        calendarButton.addActionListener(e -> {
            if (withYearSelect) {
                if (System.currentTimeMillis() - dialogClosedTime < 250) {
                    return;
                }
                if (calendarDialog != null && calendarDialog.isVisible()) {
                    calendarDialog.setVisible(false);
                    return;
                }
                viewingMonth = (currentDate != null) ? YearMonth.from(currentDate) : YearMonth.now();
                renderCalendar(viewingMonth);
                showCalendarDialog();
            } else {
                if (calendarPopup.isVisible()) {
                    calendarPopup.setVisible(false);
                    return;
                }
                viewingMonth = (currentDate != null) ? YearMonth.from(currentDate) : YearMonth.now();
                renderCalendar(viewingMonth);
                showCalendarPopup();
            }
        });
    }

    private void showCalendarPopup() {
        int x = getWidth() - calendarPopup.getPreferredSize().width;
        calendarPopup.show(this, x, getHeight() + 4);
    }

    private void showCalendarDialog() {
        if (calendarDialog == null) {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            calendarDialog = new JDialog(parentWindow);
            calendarDialog.setUndecorated(true);
            calendarDialog.setBackground(new Color(0, 0, 0, 0));
            calendarDialog.add(calendarPanel);
            calendarDialog.pack();
            calendarDialog.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    dialogClosedTime = System.currentTimeMillis();
                    calendarDialog.setVisible(false);
                }
            });
        }
        if (isShowing()) {
            Point loc = getLocationOnScreen();
            int x = Math.max(0, loc.x + getWidth() - calendarDialog.getWidth());
            int y = loc.y + getHeight() + 4;
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            if (y + calendarDialog.getHeight() > scr.height) {
                y = loc.y - calendarDialog.getHeight() - 4;
            }
            calendarDialog.setLocation(x, y);
            calendarDialog.setVisible(true);
        }
    }

    private void closeCalendar() {
        if (calendarPopup != null) {
            calendarPopup.setVisible(false);
        }
        if (calendarDialog != null) {
            calendarDialog.setVisible(false);
        }
    }

    private JPanel buildCalendarPanel() {
        JPanel panel = EstiloUI.crearTarjeta(new BorderLayout(5, 8), 10, 10);

        JPanel header = new JPanel(new BorderLayout(4, 0));
        header.setOpaque(false);
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        prevBtn.setMargin(new Insets(2, 8, 2, 8));
        nextBtn.setMargin(new Insets(2, 8, 2, 8));
        prevBtn.setFocusable(false);
        nextBtn.setFocusable(false);
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.putClientProperty(FlatClientProperties.STYLE, "borderWidth: 0;");
        nextBtn.putClientProperty(FlatClientProperties.STYLE, "borderWidth: 0;");

        prevBtn.addActionListener(e -> {
            viewingMonth = viewingMonth.minusMonths(1);
            renderCalendar(viewingMonth);
        });
        nextBtn.addActionListener(e -> {
            viewingMonth = viewingMonth.plusMonths(1);
            renderCalendar(viewingMonth);
        });

        if (withYearSelect) {
            JPanel centerHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            centerHeader.setOpaque(false);
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));

            yearComboBox = new JComboBox<>();
            int currentYear = LocalDate.now().getYear();
            for (int y = 1900; y <= currentYear + 20; y++) {
                yearComboBox.addItem(y);
            }
            yearComboBox.setFocusable(false);
            yearComboBox.addActionListener(e -> {
                if (updatingYearCombo) return;
                Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
                if (selectedYear != null && viewingMonth != null && selectedYear != viewingMonth.getYear()) {
                    viewingMonth = YearMonth.of(selectedYear, viewingMonth.getMonthValue());
                    renderCalendar(viewingMonth);
                }
            });

            centerHeader.add(monthLabel);
            centerHeader.add(yearComboBox);
            header.add(centerHeader, BorderLayout.CENTER);
        } else {
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));
            header.add(monthLabel, BorderLayout.CENTER);
        }

        header.add(prevBtn, BorderLayout.WEST);
        header.add(nextBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel calendarBody = new JPanel(new BorderLayout(0, 4));
        calendarBody.setOpaque(false);

        JPanel daysHeaderPanel = new JPanel(new GridLayout(1, 7, 2, 2));
        daysHeaderPanel.setOpaque(false);
        for (String dayName : DAYS_HEADER) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
            lbl.setForeground(EstiloUI.colorOrDefault("Label.disabledForeground", Color.GRAY));
            daysHeaderPanel.add(lbl);
        }
        calendarBody.add(daysHeaderPanel, BorderLayout.NORTH);

        JPanel daysGrid = new JPanel(new GridLayout(6, 7, 2, 2));
        daysGrid.setOpaque(false);
        for (int i = 0; i < 42; i++) {
            JButton dayBtn = new JButton();
            dayBtn.setMargin(new Insets(1, 1, 1, 1));
            dayBtn.setPreferredSize(new Dimension(32, 24));
            dayBtn.setFocusable(false);
            dayBtn.setBorderPainted(false);
            dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayBtn.addActionListener(ev -> {
                LocalDate selected = (LocalDate) dayBtn.getClientProperty("targetDate");
                if (selected != null) {
                    setDate(selected);
                    closeCalendar();
                }
            });
            dayButtons[i] = dayBtn;
            daysGrid.add(dayBtn);
        }
        calendarBody.add(daysGrid, BorderLayout.CENTER);
        panel.add(calendarBody, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        footer.setOpaque(false);
        JButton todayBtn = new JButton("Hoy");
        todayBtn.setFocusable(false);
        todayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayBtn.putClientProperty(FlatClientProperties.STYLE, "margin: 4,10,4,10; font: -1;");
        todayBtn.addActionListener(e -> {
            setDate(LocalDate.now());
            closeCalendar();
        });

        JButton clearBtn = new JButton("Limpiar");
        clearBtn.setFocusable(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.putClientProperty(FlatClientProperties.STYLE, "margin: 4,10,4,10; font: -1;");
        clearBtn.addActionListener(e -> {
            setDate(null);
            closeCalendar();
        });

        footer.add(todayBtn);
        footer.add(clearBtn);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void renderCalendar(YearMonth ym) {
        if (withYearSelect) {
            monthLabel.setText(MONTH_NAMES[ym.getMonthValue() - 1]);
            if (yearComboBox != null) {
                updatingYearCombo = true;
                yearComboBox.setSelectedItem(ym.getYear());
                updatingYearCombo = false;
            }
        } else {
            monthLabel.setText(MONTH_NAMES[ym.getMonthValue() - 1] + " " + ym.getYear());
        }

        LocalDate firstDay = ym.atDay(1);
        int dayOfWeekOffset = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = ym.lengthOfMonth();
        LocalDate today = LocalDate.now();

        Font plainFont = monthLabel.getFont().deriveFont(Font.PLAIN, 12f);
        Font boldFont = monthLabel.getFont().deriveFont(Font.BOLD, 12f);
        Color fgNormal = EstiloUI.colorOrDefault("Label.foreground", Color.WHITE);
        Color bgSel = EstiloUI.colorOrDefault("Button.default.background", new Color(0x193CB8));
        Color fgSel = EstiloUI.colorOrDefault("Button.default.foreground", Color.WHITE);
        Color fgHoy = EstiloUI.colorOrDefault("SideBar.primary", new Color(0x2B7FFF));

        for (int i = 0; i < 42; i++) {
            JButton btn = dayButtons[i];
            int dayNumber = i - dayOfWeekOffset + 1;

            if (dayNumber >= 1 && dayNumber <= daysInMonth) {
                LocalDate date = ym.atDay(dayNumber);
                btn.setText(String.valueOf(dayNumber));
                btn.putClientProperty("targetDate", date);
                btn.setVisible(true);
                btn.setEnabled(true);

                if (date.equals(currentDate)) {
                    btn.setBackground(bgSel);
                    btn.setForeground(fgSel);
                    btn.setFont(boldFont);
                    btn.setOpaque(true);
                } else if (date.equals(today)) {
                    btn.setBackground(null);
                    btn.setForeground(fgHoy);
                    btn.setFont(boldFont);
                    btn.setOpaque(false);
                } else {
                    btn.setBackground(null);
                    btn.setForeground(fgNormal);
                    btn.setFont(plainFont);
                    btn.setOpaque(false);
                }
            } else {
                btn.setText("");
                btn.putClientProperty("targetDate", null);
                btn.setVisible(false);
            }
        }
    }

    private void parseFromTextField(boolean notifyEvenIfSame) {
        String text = textField.getText().trim();
        if (text.isEmpty()) {
            hasError = false;
            setInternalDate(null, true);
            updateBorder();
            return;
        }

        try {
            LocalDate parsed = LocalDate.parse(text, FORMATTER);
            hasError = false;
            setInternalDate(parsed, true);
        } catch (DateTimeParseException ex) {
            hasError = true;
            setInternalDate(null, false);
        }
        updateBorder();
    }

    private void setInternalDate(LocalDate date, boolean notify) {
        boolean changed = (currentDate == null && date != null)
                || (currentDate != null && !currentDate.equals(date));
        currentDate = date;
        if (changed && notify) {
            for (Consumer<LocalDate> listener : changeListeners) {
                listener.accept(currentDate);
            }
        }
    }

    public LocalDate getDate() {
        return currentDate;
    }

    public void setDate(LocalDate date) {
        setInternalDate(date, true);
        if (date != null) {
            textField.setText(date.format(FORMATTER));
        } else {
            textField.setText("");
        }
        hasError = false;
        updateBorder();
    }

    public boolean isValidDate() {
        String text = textField.getText().trim();
        if (text.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(text, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public void addDateChangeListener(Consumer<LocalDate> listener) {
        changeListeners.add(listener);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        textField.setEditable(editable);
        calendarButton.setEnabled(editable);
        repaint();
    }

    public void setError(boolean error) {
        this.hasError = error;
        updateBorder();
    }

    public boolean isWithYearSelect() {
        return withYearSelect;
    }

    YearMonth getViewingMonth() {
        return viewingMonth;
    }

    void openCalendar() {
        viewingMonth = (currentDate != null) ? YearMonth.from(currentDate) : YearMonth.now();
        renderCalendar(viewingMonth);
        if (isShowing()) {
            if (withYearSelect) {
                showCalendarDialog();
            } else {
                showCalendarPopup();
            }
        }
    }

    void navigateMonth(int deltaMonths) {
        viewingMonth = viewingMonth.plusMonths(deltaMonths);
        renderCalendar(viewingMonth);
    }

    void selectYear(int year) {
        if (withYearSelect && yearComboBox != null) {
            yearComboBox.setSelectedItem(year);
        }
    }

    JButton[] getDayButtons() {
        return dayButtons;
    }

    JComboBox<Integer> getYearComboBox() {
        return yearComboBox;
    }
}
