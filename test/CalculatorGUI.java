import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//
public class CalculatorGUI extends JFrame implements ActionListener {
    // 계산기의 구성 요소들을 선언합니다.
    private JTextField textField;
    private JButton[] numberButtons;
    private JButton addButton, subtractButton, multiplyButton, divideButton, decimalButton, equalsButton, clearButton, sqrtButton, percentButton, undoButton;
    private JTextArea historyArea;
    private double num1 = 0, num2 = 0, result = 0;
    private char operator;

    public CalculatorGUI() {
        // 계산기 프레임의 기본 설정을 합니다.
        setTitle("자율계산기");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setResizable(true);
        setLocationRelativeTo(null);

        // 메인 패널을 생성하고 배경색을 설정합니다.
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

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

        // 계산기 패널과 계산 기록 패널을 분할하는 JSplitPane을 생성합니다.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, calculatorPanel, createHistoryPanel());
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 메인 패널을 프레임에 추가합니다.
        add(mainPanel);
        setVisible(true);
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
            num2 = Double.parseDouble(textField.getText());
            calculateResult();
        } else if (source == addButton || source == subtractButton || source == multiplyButton || source == divideButton || source == sqrtButton || source == percentButton) {
            num1 = Double.parseDouble(textField.getText());
            operator = buttonText.charAt(0);
            textField.setText("");
        } else if (source == undoButton) {
            String currentText = textField.getText();
            if (currentText.length() > 0) {
                textField.setText(currentText.substring(0, currentText.length() - 1));
            }
        } else {
            String currentText = textField.getText();
            textField.setText(currentText + buttonText);
        }
    }

    // 계산 결과를 계산하는 메서드입니다.
    private void calculateResult() {
        switch (operator) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case 'x':
                result = num1 * num2;
                break;
            case '/':
                if (num2 != 0)
                    result = num1 / num2;
                else
                    textField.setText("Error");
                break;
            case '√':
                result = Math.sqrt(num1);
                break;
            case '%':
                result = num1 % num2;
                break;
        }
        textField.setText(String.valueOf(result));
        historyArea.append(num1 + " " + operator + " " + num2 + " = " + result + "\n\n");
        num1 = result;
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

    // 메인 메서드입니다.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalculatorGUI());
    }
}