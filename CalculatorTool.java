import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CalculatorTool extends JFrame {
    private JTextField display;
    private JPanel buttonPanel;

    public CalculatorTool() {
        initUI();
    }

    private void initUI() {
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.PLAIN, 24));
        display.setHorizontalAlignment(SwingConstants.RIGHT);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 4, 5, 5));

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

        setLayout(new BorderLayout(5, 5));
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(display, BorderLayout.CENTER);
        JButton resetBtn = new JButton("C");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 20));
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setText("");
            }
        });
        topPanel.add(resetBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        setTitle("Calculator Tool");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private class ButtonClickListener implements ActionListener {
        // removed ScriptEngine; using custom evaluator

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = ((JButton)e.getSource()).getText();
            if ("=".equals(cmd)) {
                try {
                    String expr = display.getText();
                    double result = evaluateExpression(expr);
                    display.setText(Double.toString(result));
                } catch (Exception ex) {
                    display.setText("Error");
                }
            } else {
                display.setText(display.getText() + cmd);
            }
        }
    }


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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CalculatorTool().setVisible(true);
        });
    }
}