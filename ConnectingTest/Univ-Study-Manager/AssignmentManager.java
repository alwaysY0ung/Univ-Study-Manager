import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentManager extends JPanel implements ActionListener {
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private Map<String, List<AssignmentButton>> assignmentButtonMap;
    private JDialog assignmentDialog;
    private JTextArea descriptionArea;
    private Map<AssignmentButton, String> originalDescriptionMap; // 원본 과제 설명 맵

    private static final String[] SUBJECTS = {"객체", "컴아텍", "오소프", "공수Ⅱ", "데구"};
    private static final String CSV_FILE_PATH = "assignment_db.csv";

    public AssignmentManager() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel subjectPanel = new JPanel(new GridLayout(1, SUBJECTS.length, 10, 10));
        for (String subject : SUBJECTS) {
            JLabel subjectLabel = new JLabel(subject, SwingConstants.CENTER);
            subjectPanel.add(subjectLabel);
        }
        add(subjectPanel, BorderLayout.NORTH);

        assignmentButtonMap = new HashMap<>();
        originalDescriptionMap = new HashMap<>();
        loadAssignments(); // CSV 파일에서 과제 정보를 불러옴

        JPanel assignmentPanel = new JPanel(new GridLayout(0, SUBJECTS.length, 0, 0));
        for (String subject : SUBJECTS) {
            JPanel subjectPanel2 = new JPanel();
            subjectPanel2.setLayout(new BoxLayout(subjectPanel2, BoxLayout.Y_AXIS));
            List<AssignmentButton> buttons = assignmentButtonMap.get(subject);
            if (buttons != null) {
                for (AssignmentButton button : buttons) {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.setMaximumSize(button.getPreferredSize());
                    subjectPanel2.add(button);
                }
            }
            assignmentPanel.add(subjectPanel2);
        }

        scrollPane = new JScrollPane(assignmentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    // CSV 파일에서 과제 정보를 불러오는 메서드
    private void loadAssignments() {
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
                        String description = fields.length > 11 ? fields[11] : "";

                        if (isValidSubject(subject)) {
                            AssignmentButton button = addAssignmentButton(subject, assignmentName, period, description, completed);
                            originalDescriptionMap.put(button, description); // 원본 과제 설명 저장
                        }
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

    // CSV 라인을 파싱하여 필드 배열로 반환하는 메서드
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());

        return fields.toArray(new String[0]);
    }

    // 유효한 과목인지 확인하는 메서드
    private boolean isValidSubject(String subject) {
        for (String validSubject : SUBJECTS) {
            if (validSubject.equals(subject)) {
                return true;
            }
        }
        return false;
    }

    // 문자열을 sanitize(정제)하여 CSV 주입을 방지하고 유효한 CSV 형식을 보장하는 메서드
    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        // 모든 이중 인용 부호를 두 개의 이중 인용 부호로 바꿈 (CSV 이스케이핑)
        value = value.replace("\"", "\"\"");
        // 값에 쉼표나 줄바꿈 문자가 포함된 경우 전체를 이중 인용 부호로 묶음
        if (value.contains(",") || value.contains("\n")) {
            value = "\"" + value + "\"";
        }
        return value;
    }

    // 과제 버튼을 생성하고 맵에 추가하는 메서드
    private AssignmentButton addAssignmentButton(String subject, String name, String period, String description, boolean completed) {
        AssignmentButton button = new AssignmentButton(subject, name, period, description, completed);
        button.addActionListener(this);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(button.getPreferredSize());
        List<AssignmentButton> buttons = assignmentButtonMap.computeIfAbsent(subject, k -> new ArrayList<>());
        buttons.add(button);
        return button;
    }

    // 과제 정보를 표시하는 팝업 다이얼로그를 보여주는 메서드
    private void showAssignmentDialog(AssignmentButton button) {
        assignmentDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), button.getAssignmentName() + " 과제 정보", true);
        assignmentDialog.setSize(400, 300);
        assignmentDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("과제명: " + button.getAssignmentName());
        JLabel periodLabel = new JLabel("마감일: " + button.getAssignmentPeriod());
        JLabel completedLabel = new JLabel("완료 여부: " + (button.isCompleted() ? "완료" : "미완료"));
        JPanel labelPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        labelPanel.add(nameLabel);
        labelPanel.add(periodLabel);
        labelPanel.add(completedLabel);
        panel.add(labelPanel, BorderLayout.NORTH);

        descriptionArea = new JTextArea(button.getAssignmentDescription());
        descriptionArea.setLineWrap(true); // 자동 줄 바꿈 설정
        descriptionArea.setWrapStyleWord(true); // 단어 단위로 줄 바꿈 설정
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton saveButton = new JButton("저장");
        saveButton.addActionListener(new SaveButtonListener(button));
        panel.add(saveButton, BorderLayout.SOUTH);

        assignmentDialog.add(panel);
        assignmentDialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof AssignmentButton) {
            AssignmentButton button = (AssignmentButton) e.getSource();
            showAssignmentDialog(button);
        }
    }

    // 저장 버튼 리스너 클래스
    private class SaveButtonListener implements ActionListener {
        private AssignmentButton button;

        public SaveButtonListener(AssignmentButton button) {
            this.button = button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String newDescription = descriptionArea.getText();
            String originalDescription = originalDescriptionMap.get(button);
            if (!newDescription.equals(originalDescription)) {
                button.setAssignmentDescription(newDescription);
            }
            saveAssignmentsToFile(); // 수정된 과제 정보를 CSV 파일에 저장
            assignmentDialog.dispose();
        }
    }

    // 과제 정보를 CSV 파일에 저장하는 메서드
    private void saveAssignmentsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            writer.write("완료,과제명,마감일,관련수업,과제종류,성적비율,환산점수,과제만점,내 점수,관련파일,관련URL,정보\n");

            for (List<AssignmentButton> buttons : assignmentButtonMap.values()) {
                for (AssignmentButton button : buttons) {
                    String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                            button.isCompleted() ? "Y" : "N",
                            button.getAssignmentName(),
                            button.getAssignmentPeriod(),
                            button.getSubject(),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            sanitize(button.getAssignmentDescription())
                    );
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 과제 버튼 클래스
    private static class AssignmentButton extends JButton {
        private String subject;
        private String assignmentName;
        private String assignmentPeriod;
        private String assignmentDescription;
        private boolean completed;

        public AssignmentButton(String subject, String assignmentName, String assignmentPeriod, String assignmentDescription, boolean completed) {
            this.subject = subject;
            this.assignmentName = assignmentName;
            this.assignmentPeriod = assignmentPeriod;
            this.assignmentDescription = assignmentDescription;
            this.completed = completed;
            setPreferredSize(new Dimension(150, 50));

            if (completed) {
                setBackground(Color.LIGHT_GRAY); // 완료된 과제는 회색 박스로 표시
            } else {
                setBackground(Color.YELLOW); // 미완료 과제는 노란색 박스로 표시
            }

            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(3, 3, 3, 3);

            JLabel nameLabel = new JLabel(assignmentName);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(nameLabel, gbc);

            JLabel periodLabel = new JLabel(assignmentPeriod);
            periodLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(periodLabel, gbc);
        }

        public String getSubject() {
            return subject;
        }

        public String getAssignmentName() {
            return assignmentName;
        }

        public String getAssignmentPeriod() {
            return assignmentPeriod;
        }

        public String getAssignmentDescription() {
            return assignmentDescription;
        }

        public void setAssignmentDescription(String assignmentDescription) {
            this.assignmentDescription = assignmentDescription;
        }

        public boolean isCompleted() {
            return completed;
        }
    }

    // 메인 메서드
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("과제 관리 페이지");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);

            AssignmentManager assignmentManagerUI = new AssignmentManager();
            frame.add(assignmentManagerUI);

            frame.setVisible(true);
        });
    }
}
