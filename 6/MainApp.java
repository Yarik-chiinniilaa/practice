import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface DataSourceInfo {
    String description() default "";
}

interface MyDataObserver {
    void update(List<Double> data);
}

class DataRepository {
    private final List<Double> values = new ArrayList<>();
    private final List<MyDataObserver> observers = new ArrayList<>();
    // ОБМЕЖЕННЯ: Максимальна кількість стовпців
    public static final int MAX_COLUMNS = 15;

    public void addObserver(MyDataObserver o) {
        observers.add(o);
    }

    public int getSize() {
        return values.size();
    }

    public void addValue(double val) {
        // Перевірка на ліміт кількості
        if (values.size() >= MAX_COLUMNS) {
            return; 
        }
        values.add(val);
        notifyObservers();
    }

    public void updateAt(int index, double val) {
        if (index >= 0 && index < values.size()) {
            values.set(index, val);
            notifyObservers();
        }
    }

    private void notifyObservers() {
        List<Double> copy = new ArrayList<>(values);
        for (MyDataObserver o : observers) {
            o.update(copy);
        }
    }
}

class ModernChart extends JPanel implements MyDataObserver {
    private List<Double> data = new ArrayList<>();
    private boolean isDark = false;

    public ModernChart() {
        updateAppearance();
    }

    public void setDarkMode(boolean dark) {
        this.isDark = dark;
        updateAppearance();
        repaint();
    }

    private void updateAppearance() {
        Color bgColor = isDark ? new Color(30, 30, 30) : Color.WHITE;
        Color borderColor = isDark ? new Color(70, 70, 70) : new Color(210, 210, 210);
        Color textColor = isDark ? new Color(220, 220, 220) : Color.BLACK;

        setBackground(bgColor);
        setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            new TitledBorder(new LineBorder(borderColor), "Моніторинг даних (Max: " + DataRepository.MAX_COLUMNS + ")", 
                TitledBorder.LEFT, TitledBorder.TOP, null, textColor)
        ));
    }

    @Override
    public void update(List<Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = getInsets();
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;

        double max = Collections.max(data);
        if (max <= 0) max = 1;
        
        int barWidth = (w - 40) / data.size();

        for (int i = 0; i < data.size(); i++) {
            int barHeight = (int) ((data.get(i) / max) * (h - 70));
            int x = insets.left + 20 + i * barWidth;
            int y = insets.top + h - 40 - barHeight;

            RoundRectangle2D bar = new RoundRectangle2D.Double(x + 5, y, barWidth - 10, barHeight, 12, 12);

            g2.setColor(isDark ? new Color(60, 130, 190) : new Color(100, 180, 240));
            g2.fill(bar);

            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(isDark ? new Color(100, 160, 210) : new Color(50, 120, 180));
            g2.draw(bar);

            g2.setColor(isDark ? Color.WHITE : Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(String.format("%.0f", data.get(i)), x + 7, y - 8);
            
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString("ID:" + i, x + 7, insets.top + h - 15);
        }
    }
}

public class MainApp extends JFrame {
    private final DataRepository repository = new DataRepository();
    private boolean isDark = false;
    private final Random random = new Random();
    
    private final ModernChart chart = new ModernChart();
    private final JPanel controls = new JPanel();
    private final JTextField inputId = new JTextField("0", 3);
    private final JTextField inputVal = new JTextField("100", 5);
    private final JButton updateBtn = new JButton("Оновити");
    private final JButton addRandomBtn = new JButton("Додати рандом");
    private final JButton themeBtn = new JButton("Темний режим");

    public MainApp() {
        setTitle("Dashboard :: Limits Enabled");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 550);
        setLayout(new BorderLayout());

        repository.addObserver(chart);

        themeBtn.addActionListener(e -> {
            isDark = !isDark;
            applyTheme();
        });

        updateBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(inputId.getText());
                double val = Double.parseDouble(inputVal.getText());
                
                // Обмеження на значення (0 - 500)
                if (val < 0 || val > 500) {
                    JOptionPane.showMessageDialog(this, "Значення має бути від 0 до 500!");
                    return;
                }
                repository.updateAt(id, val);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка формату!");
            }
        });

        addRandomBtn.addActionListener(e -> {
            if (repository.getSize() < DataRepository.MAX_COLUMNS) {
                double randomVal = 10 + (190 * random.nextDouble());
                repository.addValue(Math.round(randomVal));
                
                // Деактивуємо кнопку, якщо ліміт вичерпано
                if (repository.getSize() >= DataRepository.MAX_COLUMNS) {
                    addRandomBtn.setEnabled(false);
                    addRandomBtn.setText("Ліміт досягнуто");
                }
            }
        });

        controls.add(new JLabel("ID:")); 
        controls.add(inputId);
        controls.add(new JLabel("Значення:")); 
        controls.add(inputVal);
        controls.add(updateBtn);
        controls.add(addRandomBtn);
        controls.add(themeBtn);

        add(chart, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        repository.addValue(40.0);
        repository.addValue(85.0);
        repository.addValue(120.0);

        applyTheme();
        setLocationRelativeTo(null);
    }

    private void applyTheme() {
        Color bgMain = isDark ? new Color(25, 25, 25) : new Color(240, 240, 240);
        Color fgText = isDark ? new Color(220, 220, 220) : Color.BLACK;
        Color bgBtn = isDark ? new Color(50, 50, 50) : new Color(225, 225, 225);
        Color bgInput = isDark ? new Color(40, 40, 40) : Color.WHITE;
        Color borderCol = isDark ? new Color(80, 80, 80) : Color.GRAY;

        chart.setDarkMode(isDark);
        controls.setBackground(bgMain);
        getContentPane().setBackground(bgMain);

        controls.setBorder(new TitledBorder(new LineBorder(borderCol), "Керування", 
            TitledBorder.LEFT, TitledBorder.TOP, null, fgText));

        JButton[] buttons = {updateBtn, addRandomBtn, themeBtn};
        for (JButton b : buttons) {
            b.setOpaque(true);
            b.setBackground(bgBtn);
            b.setForeground(fgText);
            b.setBorder(BorderFactory.createLineBorder(borderCol));
        }

        JTextField[] fields = {inputId, inputVal};
        for (JTextField f : fields) {
            f.setBackground(bgInput);
            f.setForeground(fgText);
            f.setCaretColor(fgText);
            f.setBorder(BorderFactory.createLineBorder(borderCol));
        }

        for (Component c : controls.getComponents()) {
            if (c instanceof JLabel) c.setForeground(fgText);
        }

        themeBtn.setText(isDark ? "Світлий режим" : "Темний режим");
        repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}