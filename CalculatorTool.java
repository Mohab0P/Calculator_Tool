import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
        add(display, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        setTitle("Calculator Tool");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private class ButtonClickListener implements ActionListener {
        private ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = ((JButton)e.getSource()).getText();
            if ("=".equals(cmd)) {
                try {
                    String expr = display.getText();
                    Object result = engine.eval(expr);
                    display.setText(result.toString());
                } catch (ScriptException ex) {
                    display.setText("Error");
                }
            } else {
                display.setText(display.getText() + cmd);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CalculatorTool().setVisible(true);
        });
    }
}