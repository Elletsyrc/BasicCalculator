package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class CalculatorController {

    @FXML private TextField display;
    @FXML private Label radLabel;

    private boolean isRadians = true;
    private double memory = 0;
    private boolean startNewNumber = false;

    @FXML
    public void initialize() {
        // Allow toggling between RAD and DEG
        radLabel.setOnMouseClicked((MouseEvent e) -> {
            isRadians = !isRadians;
            radLabel.setText(isRadians ? "RAD" : "DEG");
        });
    }

    // Handles numbers 0-9 and dot
    @FXML
    private void onNumberClick(ActionEvent event) {
        if (startNewNumber) {
            display.clear();
            startNewNumber = false;
        }
        String value = ((Button) event.getSource()).getText();
        display.setText(display.getText().equals("0") ? value : display.getText() + value);
    }

    // Handles standard operators (+, -, *, /)
    @FXML
    private void onOperatorClick(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        // Replace visual × and ÷ with code friendly * and /
        if (op.equals("×")) op = "*";
        if (op.equals("÷")) op = "/";
        
        display.setText(display.getText() + " " + op + " ");
        startNewNumber = false;
    }

    // Handles scientific functions (sin, cos, log, etc.)
    @FXML
    private void onFunctionClick(ActionEvent event) {
        String func = ((Button) event.getSource()).getText();
        try {
            double value = Double.parseDouble(display.getText());
            double result = 0;

            switch (func) {
                case "sin" -> result = isRadians ? Math.sin(value) : Math.sin(Math.toRadians(value));
                case "cos" -> result = isRadians ? Math.cos(value) : Math.cos(Math.toRadians(value));
                case "tan" -> result = isRadians ? Math.tan(value) : Math.tan(Math.toRadians(value));
                case "log" -> result = Math.log10(value);
                case "ln"  -> result = Math.log(value);
                case "√"   -> result = Math.sqrt(value);
                case "x²"  -> result = Math.pow(value, 2);
                case "1/x" -> result = 1 / value;
                case "x!"  -> result = factorial(value);
                case "±"   -> result = value * -1;
                case "%"   -> result = value / 100;
            }
            display.setText(formatResult(result));
            startNewNumber = true;
        } catch (NumberFormatException e) {
            display.setText("Error");
        }
    }

    // Handles constants like pi and e
    @FXML
    private void onConstantClick(ActionEvent event) {
        String text = ((Button) event.getSource()).getText();
        double value = text.equals("π") ? Math.PI : Math.E;
        display.setText(String.valueOf(value));
        startNewNumber = true;
    }

    // Handles Memory Operations
    @FXML
    private void onMemoryClick(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        double currentVal = 0;
        try {
            if (!display.getText().isEmpty()) currentVal = Double.parseDouble(display.getText());
        } catch (Exception ignored) {}

        switch (op) {
            case "MC" -> memory = 0;
            case "MR" -> {
                display.setText(formatResult(memory));
                startNewNumber = true;
            }
            case "M+" -> memory += currentVal;
            case "M-" -> memory -= currentVal;
        }
    }

    @FXML
    private void onClear() {
        display.setText("0");
        startNewNumber = true;
    }

    @FXML
    private void onDelete() {
        String current = display.getText();
        if (current.length() > 0 && !current.equals("0")) {
            display.setText(current.substring(0, current.length() - 1));
            if (display.getText().isEmpty()) display.setText("0");
        }
    }

    @FXML
    private void onEquals() {
        try {
            String expression = display.getText();
            // Simple robust evaluation logic could be added here.
            // For brevity, we will handle a simple 2-operand case or use a helper.
            // In a full robust app, integrate a Shunting-yard algorithm parser.
            // Here is a basic evaluator for continuous operations:
            double result = eval(expression);
            display.setText(formatResult(result));
            startNewNumber = true;
        } catch (Exception e) {
            display.setText("Error");
        }
    }

    // Helper for factorial
    private double factorial(double n) {
        if (n < 0) return Double.NaN;
        if (n == 0) return 1;
        double res = 1;
        for (int i = 2; i <= n; i++) res *= i;
        return res;
    }

    // Format to remove unnecessary .0
    private String formatResult(double d) {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }

    // Simple parser for +, -, *, / expressions
    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}