import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonPanel extends JPanel {
    private String userId;
    private String semester;

    public ButtonPanel(String userId, String semester) {
        this.userId = userId;
        this.semester = semester;

        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1)); // 세로로 3개의 버튼을 배치

        JButton button1 = new JButton("과목 정렬 팝업");
        JButton button2 = new JButton("기간 정렬 팝업");
        JButton button3 = new JButton("달력 팝업");

        button1.addActionListener(new ButtonClickListener());
        button2.addActionListener(new ButtonClickListener());
        button3.addActionListener(new ButtonClickListener());

        buttonPanel.add(button1);
        buttonPanel.add(button2);
        buttonPanel.add(button3);

        NungyeolPanel nungyeolPanel = new NungyeolPanel();

        add(buttonPanel, BorderLayout.CENTER);
        add(nungyeolPanel, BorderLayout.SOUTH);
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            if (source.getText().equals("과목 정렬 팝업")) {
                JFrame popupFrame = new JFrame("과목 정렬 팝업");
                popupFrame.add(new AssignmentManager(userId, semester));
                popupFrame.setSize(900, 700);
                popupFrame.setVisible(true);
                popupFrame.setLocationRelativeTo(null);
            } else if (source.getText().equals("기간 정렬 팝업")) {
                JFrame popupFrame = new UpcomingAssignment(userId, semester);
                popupFrame.setVisible(true);
            } else if (source.getText().equals("달력 팝업")) {
                JFrame popupFrame = new JFrame("달력 팝업");
                popupFrame.add(new CalendarAssignment(userId, semester));
                popupFrame.setSize(750, 1000); // 사이즈 조정
                popupFrame.setVisible(true);
                popupFrame.setLocationRelativeTo(null);
            } else {
                JOptionPane.showMessageDialog(null, source.getText() + " clicked!");
            }
        }
    }
}
