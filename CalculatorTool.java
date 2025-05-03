import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;
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
        // Use system look and feel for native UI appearance
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored) {}

        // Layout and container styling
        setLayout(new BorderLayout(5, 5));
        JComponent cp = (JComponent) getContentPane();
        cp.setBackground(new Color(240, 240, 240));
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with input field and result label
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(cp.getBackground());

        // Editable input field where user types or clicks numbers/operators
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 24));
        inputField.setHorizontalAlignment(SwingConstants.RIGHT);
        inputField.setColumns(12);
        inputField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        topPanel.add(inputField, BorderLayout.NORTH);

        // Result label showing evaluation output
        resultLabel = new JLabel("0", SwingConstants.RIGHT);
        resultLabel.setFont(new Font("Monospaced", Font.BOLD, 32));
        resultLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(resultLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Enter/Esc key bindings on the input field
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

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 4, 5, 5));
        buttonPanel.setBackground(Color.DARK_GRAY);

        String[] buttons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.addActionListener(new ButtonClickListener());
            buttonPanel.add(btn);
        }

        // Style buttons: padding and colors
        // Configure buttonPanel after creation
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                String t = btn.getText();
                if ("+-*/=".contains(t)) {
                    btn.setBackground(new Color(255, 165, 0));
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(Color.BLACK);
                }
            }
        }

        add(buttonPanel, BorderLayout.CENTER);

        setTitle("Calculator Tool");
        pack();
        setSize(300, getHeight());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // Evaluate a simple expression containing +, -, *, /
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

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = ((JButton) e.getSource()).getText();
            if ("=".equals(cmd)) {
                try {
                    String expr = inputField.getText();
                    double result = evaluateExpression(expr);
                    resultLabel.setText(Double.toString(result));
                } catch (Exception ex) {
                    resultLabel.setText("Error");
                }
            } else {
                // append to input field
                inputField.setText(inputField.getText() + cmd);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CalculatorTool().setVisible(true);
        });
    }
}