import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CalendarAssignment extends JPanel implements ActionListener {
    private Calendar cal;
    private int year, month, date;
    private JPanel pane = new JPanel();
    private JButton btn1 = new JButton("◀");  // 이전버튼
    private JButton btn2 = new JButton("▶");  // 다음버튼
    private JLabel yearlb = new JLabel("년");
    private JLabel monthlb = new JLabel("월");
    private JComboBox<Integer> yearCombo = new JComboBox<>();
    private DefaultComboBoxModel<Integer> yearModel = new DefaultComboBoxModel<>();
    private JComboBox<Integer> monthCombo = new JComboBox<>();
    private DefaultComboBoxModel<Integer> monthModel = new DefaultComboBoxModel<>();
    private JPanel pane2 = new JPanel(new BorderLayout());
    private JPanel title = new JPanel(new GridLayout(1, 7));
    private String titleStr[] = {"일", "월", "화", "수", "목", "금", "토"};
    private JPanel datePane = new JPanel(new GridLayout(0, 7));
    private List<Assignment> assignments;
    private static final String CSV_FILE_PATH = "C:\\Users\\SM-PC\\Documents\\workspace\\IntelliJ\\Java\\Java_2024\\src\\Project\\assignment_db.csv";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private JDialog assignmentDialog;
    private JTextArea descriptionArea;



    public CalendarAssignment() {

        cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        date = cal.get(Calendar.DATE);
        assignments = parseCSV(CSV_FILE_PATH);

        for (int i = year - 100; i <= year + 50; i++) {
            yearModel.addElement(i);
        }
        yearCombo.setModel(yearModel);
        yearCombo.setSelectedItem(year);

        for (int i = 1; i <= 12; i++) {
            monthModel.addElement(i);
        }
        monthCombo.setModel(monthModel);
        monthCombo.setSelectedItem(month);

        for (int i = 0; i < titleStr.length; i++) {
            JLabel lbl = new JLabel(titleStr[i], JLabel.CENTER);
            if (i == 0) {
                lbl.setForeground(Color.red);
            } else if (i == 6) {
                lbl.setForeground(Color.blue);
            }
            title.add(lbl);
        }
        day(year, month);

        pane.add(btn1);
        pane.add(yearCombo);
        pane.add(yearlb);
        pane.add(monthCombo);
        pane.add(monthlb);
        pane.add(btn2);
        pane.setBackground(Color.lightGray);
        add(BorderLayout.NORTH, pane);
        pane2.add(title, BorderLayout.NORTH);
        pane2.add(datePane, BorderLayout.CENTER);
        add(BorderLayout.CENTER, pane2);

        setVisible(true);
        setSize(900, 700);

        btn1.addActionListener(this);
        btn2.addActionListener(this);
        yearCombo.addActionListener(this);
        monthCombo.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        Object eventObj = e.getSource();
        if (eventObj instanceof JComboBox) {
            datePane.setVisible(false);
            datePane.removeAll();
            day((Integer) yearCombo.getSelectedItem(), (Integer) monthCombo.getSelectedItem());
            datePane.setVisible(true);
        } else if (eventObj instanceof JButton) {
            JButton eventBtn = (JButton) eventObj;
            int yy = (Integer) yearCombo.getSelectedItem();
            int mm = (Integer) monthCombo.getSelectedItem();
            if (eventBtn.equals(btn1)) { // 이전달
                if (mm == 1) {
                    yy--;
                    mm = 12;
                } else {
                    mm--;
                }
            } else if (eventBtn.equals(btn2)) { // 다음달
                if (mm == 12) {
                    yy++;
                    mm = 1;
                } else {
                    mm++;
                }
            }
            yearCombo.setSelectedItem(yy);
            monthCombo.setSelectedItem(mm);
        }
    }
    // 문자열에서 특정 문자의 출현 횟수를 세는 메서드
    private int countOccurrences(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    private List<Assignment> parseCSV(String csvFilePath) {
        List<Assignment> assignmentList = new ArrayList<>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(CSV_FILE_PATH));
            StringBuilder sb = new StringBuilder();
            String line;
            br.readLine(); // 첫 번째 줄 (헤더) 건너뛰기

            while ((line = br.readLine()) != null) {
                sb.append(line);
                int quoteCount = countOccurrences(sb.toString(), '\"');
                if (quoteCount % 2 == 0) {
                    String csvLine = sb.toString();
                    String[] fields = parseCSVLine(csvLine);
                    if (fields.length >= 4) {
                        boolean completed = fields[0].equals("Y");
                        String assignmentName = fields[1];
                        String period = fields[2];
                        String subject = fields[3];
                        String type = fields[4];
                        String description = fields.length > 11 ? fields[11] : "";

                        String dueDate = period.isEmpty() ? "2024-06-21" : period;

                        Assignment assignment = new Assignment(completed, assignmentName, dueDate, subject, type, description);
                        assignmentList.add(assignment);
                    }
                    sb.setLength(0);
                } else {
                    sb.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return assignmentList;
    }

    private String[] parseCSVLine(String csvLine) {
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        List<String> fields = new ArrayList<>();

        for (int i = 0; i < csvLine.length(); i++) {
            char ch = csvLine.charAt(i);

            if (ch == '\"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        fields.add(sb.toString().trim());

        return fields.toArray(new String[0]);
    }

    public void day(int year, int month) {
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, 1);
        int week = date.get(Calendar.DAY_OF_WEEK);
        int lastDay = date.getActualMaximum(Calendar.DAY_OF_MONTH);

        datePane.removeAll();

        for (int space = 1; space < week; space++) {
            datePane.add(new JLabel("\t"));
        }

        for (int day = 1; day <= lastDay; day++) {
            JPanel dateSubPane = new JPanel(new GridLayout(10, 1));
            Border border = BorderFactory.createLineBorder(Color.lightGray);
            dateSubPane.setBorder(border);

            // 패널 사이에 간격을 추가
            Border margin = new EmptyBorder(1, 1, 1, 1);
            dateSubPane.setBorder(BorderFactory.createCompoundBorder(border, margin));

            JLabel lbl = new JLabel(String.valueOf(day), JLabel.CENTER);
            JLabel lbl2 = new JLabel("", JLabel.CENTER);
            date.set(year, month - 1, day);
            int Week = date.get(Calendar.DAY_OF_WEEK);
            if (Week == Calendar.SUNDAY) {
                lbl.setForeground(Color.red);
            } else if (Week == Calendar.SATURDAY) {
                lbl.setForeground(Color.BLUE);
            }
            dateSubPane.add(lbl);
            dateSubPane.add(lbl2);
            List<Assignment> dayAssignments = getAssignmentsForDate(year, month, day);
            if (!dayAssignments.isEmpty()) {
                for (Assignment assignment : dayAssignments) {
                    JButton assignmentButton = new JButton(assignment.getName());
                    assignmentButton.setBorder(BorderFactory.createLineBorder(Color.gray));
                    Font font = assignmentButton.getFont();
                    assignmentButton.setFont(font.deriveFont(font.getSize()-2.0f));
                    assignmentButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showAssignmentDetails(assignment);
                        }
                    });
                    dateSubPane.add(assignmentButton);
                }
            }

            datePane.add(dateSubPane);

        }

        datePane.revalidate();
        datePane.repaint();
    }

    private List<Assignment> getAssignmentsForDate(int year, int month, int day) {
        List<Assignment> dayAssignments = new ArrayList<>();
        for (Assignment assignment : assignments) {
            try {
                Date dueDate = dateFormat.parse(assignment.getDueDate()); // 파싱
                Calendar cal = Calendar.getInstance();
                cal.setTime(dueDate);

                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.DAY_OF_MONTH) == day) {
                    dayAssignments.add(assignment);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dayAssignments;
    }

    private void showAssignmentDetails(Assignment assignment) {
        JFrame parentFrame = new JFrame();
        assignmentDialog = new JDialog(parentFrame, assignment.getName() + " 과제 정보", true);
        assignmentDialog.setSize(400, 300);
        assignmentDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("과제명: " + assignment.getName());
        JLabel periodLabel = new JLabel("마감일: " + assignment.getDueDate());
        JLabel completedLabel = new JLabel("완료 여부: " + (assignment.getCompleted() ? "완료" : "미완료"));
        JPanel labelPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        labelPanel.add(nameLabel);
        labelPanel.add(periodLabel);
        labelPanel.add(completedLabel);
        panel.add(labelPanel, BorderLayout.NORTH);

        descriptionArea = new JTextArea(assignment.getDetails());
        descriptionArea.setLineWrap(true); // 자동 줄 바꿈 설정
        descriptionArea.setWrapStyleWord(true); // 단어 단위로 줄 바꿈 설정
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        assignmentDialog.add(panel);
        assignmentDialog.setVisible(true);

    }
}
