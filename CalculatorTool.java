import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CalculatorTool extends JFrame {
    private JTextField inputField;
    private JLabel resultLabel;
    private JPanel buttonPanel;

    public CalculatorTool() {
        initUI();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setLayout(new BorderLayout(5, 5));
        JComponent cp = (JComponent) getContentPane();
        cp.setBackground(new Color(33, 33, 33));
        cp.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel for input and result
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(new Color(33, 33, 33));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        inputField.setHorizontalAlignment(SwingConstants.RIGHT);
        inputField.setColumns(12);
        inputField.setMargin(new Insets(8, 8, 8, 8));
        inputField.setBackground(new Color(45, 45, 45));
        inputField.setForeground(new Color(220, 220, 220));
        inputField.setCaretColor(new Color(220, 220, 220));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(inputField, BorderLayout.NORTH);

        resultLabel = new JLabel("0", SwingConstants.RIGHT);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        resultLabel.setForeground(new Color(0, 230, 118));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        topPanel.add(resultLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Keyboard shortcuts
        InputMap im = inputField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = inputField.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "equals");
        am.put("equals", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String expr = inputField.getText();
                    double result = evaluateExpression(expr);
                    resultLabel.setText(Double.toString(result));
                } catch (Exception ex) {
                    resultLabel.setText("Error");
                }
            }
        });
        im.put(KeyStroke.getKeyStroke('C'), "clear");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        am.put("clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputField.setText("");
                resultLabel.setText("0");
            }
        });

        // Main panel for buttons: operations on left, numbers on right
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(cp.getBackground());

        // Operations panel (stacked vertically)
        JPanel opPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        opPanel.setBackground(cp.getBackground());
        opPanel.setPreferredSize(new Dimension(80, 0));  // widen the operations column
        String[] ops = {"C", "/", "*", "-", "+"};
        for (String op : ops) {
            JButton btn = createButton(op);
            if ("C".equals(op)) btn.setBackground(new Color(255, 69, 58));
            else btn.setBackground(new Color(66, 66, 66));
            btn.setForeground(Color.WHITE);
            opPanel.add(btn);
        }
        mainPanel.add(opPanel, BorderLayout.WEST);

        // Numbers panel (4x3 grid)
        JPanel numPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        numPanel.setBackground(cp.getBackground());
        String[][] nums = {
            {"7", "8", "9"},
            {"4", "5", "6"},
            {"1", "2", "3"},
            {"0", ".", "="}
        };
        for (String[] row : nums) {
            for (String num : row) {
                JButton btn = createButton(num);
                if ("=".equals(num)) btn.setBackground(new Color(0, 200, 83));
                else btn.setBackground(new Color(54, 54, 54));
                btn.setForeground(Color.WHITE);
                numPanel.add(btn);
            }
        }
        mainPanel.add(numPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        setTitle("Calculator Tool");
        pack();
        setSize(350, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.addActionListener(new ButtonClickListener());
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(c.getBackground());
                g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);
                g2d.dispose();
                super.paint(g, c);
            }
        });
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        return btn;
    }

    // Expression evaluator
    private double evaluateExpression(String expr) throws Exception {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                int j = i;
                while (j < expr.length() && (Character.isDigit(expr.charAt(j)) || expr.charAt(j) == '.')) j++;
                tokens.add(expr.substring(i, j));
                i = j;
            } else if ("+-*/".indexOf(c) >= 0) {
                tokens.add(Character.toString(c));
                i++;
            } else {
                throw new Exception("Invalid character");
            }
        }

        Stack<Double> values = new Stack<>();
        Stack<String> ops = new Stack<>();

        for (String token : tokens) {
            if (token.matches("\\d+(\\.\\d+)?")) {
                values.push(Double.parseDouble(token));
            } else {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    computeTop(values, ops);
                }
                ops.push(token);
            }
        }

        while (!ops.isEmpty()) {
            computeTop(values, ops);
        }

        return values.pop();
    }

    private int precedence(String op) {
        if ("*".equals(op) || "/".equals(op)) return 2;
        return 1;
    }

    private void computeTop(Stack<Double> values, Stack<String> ops) {
        double b = values.pop();
        double a = values.pop();
        String op = ops.pop();
        switch (op) {
            case "+": values.push(a + b); break;
            case "-": values.push(a - b); break;
            case "*": values.push(a * b); break;
            case "/": values.push(a / b); break;
        }
    }

    // Button click listener
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = ((JButton) e.getSource()).getActionCommand();
            if (cmd == null) {
                cmd = ((JButton) e.getSource()).getText();
            }
            
            if (cmd.isEmpty()) {
                // Do nothing for empty buttons
            } else if ("C".equals(cmd)) {
                inputField.setText("");
                resultLabel.setText("0");
            } else if ("=".equals(cmd)) {
                try {
                    String expr = inputField.getText();
                    double result = evaluateExpression(expr);
                    inputField.setText(Double.toString(result));
                    resultLabel.setText(Double.toString(result));
                } catch (Exception ex) {
                    inputField.setText("Error");
                    resultLabel.setText("Error");
                }
            } else {
                inputField.setText(inputField.getText() + cmd);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalculatorTool().setVisible(true));
    }
}
