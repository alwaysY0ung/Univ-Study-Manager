/*--------------------------------------------------------*/
//계산기 패널 코드와 메인 코드를 분리하였으며,
//본 깃허브 파일에서는 계산기 패널 코드 이후에 주석으로 구분하여 메인에서 호출하는 코드를 담았습니다!
/*---------------------------------------------------------*/
//먼저 계산기 패널입니다------------------------------------
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class UserCalculator extends JPanel implements ActionListener {
    // 계산기의 구성 요소들을 선언합니다.
    private JTextField textField;
    private JButton[] numberButtons;
    private JButton addButton, subtractButton, multiplyButton, divideButton, decimalButton, equalsButton, clearButton, sqrtButton, percentButton, undoButton;
    private JTextArea historyArea;
    private JSplitPane splitPane;

    public UserCalculator() {
        // 계산기 패널의 기본 설정을 합니다.
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // 계산기 패널을 생성합니다.
        JPanel calculatorPanel = createCalculatorPanel();

        // 계산기 패널과 계산 기록 패널을 분할하는 JSplitPane을 생성합니다.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, calculatorPanel, createHistoryPanel());
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
    }

    // 계산기 패널을 생성하는 메서드입니다.
    private JPanel createCalculatorPanel() {
        // 계산기 패널을 생성하고 배경색과 여백을 설정합니다.
        JPanel calculatorPanel = new JPanel(new BorderLayout());
        calculatorPanel.setBackground(Color.BLACK);
        calculatorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 계산 결과를 표시할 텍스트 필드를 생성하고 설정합니다.
        textField = new JTextField();
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setEditable(false);
        textField.setFont(new Font("Arial", Font.PLAIN, 24));
        textField.setPreferredSize(new Dimension(200, 50));
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        textField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        calculatorPanel.add(textField, BorderLayout.NORTH);

        // 버튼 패널을 생성하고 배경색과 여백을 설정합니다.
        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // 숫자 버튼을 생성합니다.
        numberButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            numberButtons[i] = createButton(String.valueOf(i), Color.WHITE, Color.BLACK);
        }

        // 연산자 및 기능 버튼을 생성합니다.
        Color lightGray = new Color(200, 200, 200);
        addButton = createButton("+", lightGray, Color.BLACK);
        subtractButton = createButton("-", lightGray, Color.BLACK);
        multiplyButton = createButton("x", lightGray, Color.BLACK);
        divideButton = createButton("/", lightGray, Color.BLACK);
        decimalButton = createButton(".", lightGray, Color.BLACK);
        equalsButton = createButton("=", lightGray, Color.BLACK);
        clearButton = createButton("AC", lightGray, Color.BLACK);
        sqrtButton = createButton("√", lightGray, Color.BLACK);
        percentButton = createButton("%", lightGray, Color.BLACK);
        undoButton = createButton("<-", lightGray, Color.BLACK);

        // 버튼을 버튼 패널에 추가합니다.
        buttonPanel.add(clearButton);
        buttonPanel.add(addButton);
        buttonPanel.add(subtractButton);
        buttonPanel.add(multiplyButton);
        buttonPanel.add(numberButtons[7]);
        buttonPanel.add(numberButtons[8]);
        buttonPanel.add(numberButtons[9]);
        buttonPanel.add(divideButton);
        buttonPanel.add(numberButtons[4]);
        buttonPanel.add(numberButtons[5]);
        buttonPanel.add(numberButtons[6]);
        buttonPanel.add(sqrtButton);
        buttonPanel.add(numberButtons[1]);
        buttonPanel.add(numberButtons[2]);
        buttonPanel.add(numberButtons[3]);
        buttonPanel.add(percentButton);
        buttonPanel.add(numberButtons[0]);
        buttonPanel.add(decimalButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(equalsButton);

        // 버튼 패널을 계산기 패널에 추가합니다.
        calculatorPanel.add(buttonPanel, BorderLayout.CENTER);

        return calculatorPanel;
    }

    // 계산 기록 패널을 생성하는 메서드입니다.
    private JScrollPane createHistoryPanel() {
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Arial", Font.PLAIN, 12));
        historyArea.setBackground(Color.WHITE);
        historyArea.setForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setPreferredSize(new Dimension(200, 400));
        return scrollPane;
    }

    // 버튼 클릭 이벤트를 처리하는 메서드입니다.
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String buttonText = ((JButton) source).getText();

        if (source == clearButton) {
            textField.setText("");
        } else if (source == equalsButton) {
            String expression = textField.getText();
            try {
                double result = evaluateExpression(expression);
                textField.setText(String.valueOf(result));
                historyArea.append(expression + " = " + result + "\n\n");
            } catch (IllegalArgumentException ex) {
                textField.setText("Invalid Expression");
            } catch (UnsupportedOperationException ex) {
                textField.setText("Cannot divide by zero");
            }
        } else if (source == undoButton) {
            String currentText = textField.getText();
            if (currentText.length() > 0) {
                textField.setText(currentText.substring(0, currentText.length() - 1));
            }
        } else if (source == sqrtButton) {
            String currentText = textField.getText();
            if (currentText.length() > 0) {
                try {
                    double number = Double.parseDouble(currentText);
                    if (number < 0) {
                        textField.setText("Invalid Input");
                    } else {
                        double result = Math.sqrt(number);
                        String formattedResult = String.format("%.10f", result);
                        textField.setText(formattedResult);
                        historyArea.append("√" + number + " = " + formattedResult + "\n\n");
                    }
                } catch (NumberFormatException ex) {
                    textField.setText("Invalid Input");
                }
            }
        } else {
            String currentText = textField.getText();
            textField.setText(currentText + buttonText);
        }
    }

    // 계산 수식을 평가하는 메서드입니다.
    private double evaluateExpression(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        boolean isNegative = false;

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (Character.isDigit(ch)) {
                StringBuilder numberBuilder = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    numberBuilder.append(expression.charAt(i));
                    i++;
                }
                i--;
                double number = Double.parseDouble(numberBuilder.toString());
                if (isNegative) {
                    number = -number;
                    isNegative = false;
                }
                numbers.push(number);
            } else if (ch == '(') {
                operators.push(ch);
            } else if (ch == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    double result = applyOperator(operators.pop(), numbers.pop(), numbers.pop());
                    numbers.push(result);
                }
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop();
                }
            } else if (ch == '+' || ch == '-' || ch == 'x' || ch == '/' || ch == '%') {
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    double result = applyOperator(operators.pop(), numbers.pop(), numbers.pop());
                    numbers.push(result);
                }
                operators.push(ch);
                if (ch == '-' && (i == 0 || (i > 0 && (expression.charAt(i - 1) == '(' || expression.charAt(i - 1) == '+' || expression.charAt(i - 1) == '-' || expression.charAt(i - 1) == 'x' || expression.charAt(i - 1) == '/' || expression.charAt(i - 1) == '%')))) {
                    isNegative = true;
                }
            }
        }

        while (!operators.isEmpty()) {
            double result = applyOperator(operators.pop(), numbers.pop(), numbers.pop());
            numbers.push(result);
        }

        if (numbers.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return numbers.pop();
    }

    // 연산자의 우선순위를 판단하는 메서드입니다.
    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == 'x' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    // 연산자를 적용하여 계산 결과를 반환하는 메서드입니다.
    private double applyOperator(char operator, double b, double a) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case 'x':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new UnsupportedOperationException("Cannot divide by zero");
                }
                return a / b;
            case '%':
                return a % b;
        }
        return 0;
    }

    // 버튼을 생성하는 메서드입니다.
    private JButton createButton(String label, Color backgroundColor, Color foregroundColor) {
        JButton button = new JButton(label);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.addActionListener(this);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        return button;
    }
}