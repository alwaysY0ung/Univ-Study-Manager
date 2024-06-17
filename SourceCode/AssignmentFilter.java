import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

public class AssignmentFilter extends JPanel {
    private List<Assignment> assignments;
    private String userId;
    private int year;
    private int sem;
    private String csvFilePath;

    private JTable assignmentTable;
    public JPanel assignmentPanel;
    private DefaultTableModel assignmentTableModel;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> typeComboBox;

    public AssignmentFilter(String userId, String semester) {
        this.userId = userId;
        String[] parts = semester.split("-");
        this.year = Integer.parseInt(parts[0]);
        this.sem = Integer.parseInt(parts[1].substring(0, 1));

        this.csvFilePath = userId + "_" + year + "_" + sem + "_assignment_db.csv";

        setLayout(new BorderLayout());

        assignments = parseCSV(csvFilePath);

        assignmentPanel = new JPanel(new BorderLayout());
        assignmentPanel.setBorder(BorderFactory.createTitledBorder("예정된 과제"));
        String[] assignmentColumns = {"과목", "과제", "마감일"};
        assignmentTableModel = new DefaultTableModel(assignmentColumns, 0);
        assignmentTable = new JTable(assignmentTableModel);
        assignmentTable.setRowSelectionAllowed(false);
        assignmentTable.setShowGrid(false);
        assignmentPanel.add(new JScrollPane(assignmentTable), BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] sortOptions = {"시간순", "과목순"};
        sortComboBox = new JComboBox<>(sortOptions);
        filterPanel.add(new JLabel("정렬: "));
        filterPanel.add(sortComboBox);

        String[] filterOptions = {"미완과제", "완료과제", "전체과제"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterPanel.add(new JLabel("필터: "));
        filterPanel.add(filterComboBox);

        String[] typeOptions = {"전체종류", "시험", "보고서", "퀴즈", "프로젝트"};
        typeComboBox = new JComboBox<>(typeOptions);
        filterPanel.add(typeComboBox);

        assignmentPanel.add(filterPanel, BorderLayout.NORTH);
        add(assignmentPanel, BorderLayout.CENTER);

        sortComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAssignmentTable();
            }
        });

        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAssignmentTable();
            }
        });

        typeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAssignmentTable();
            }
        });

        updateAssignmentTable();
        setSize(600, 400);
        setVisible(true);
    }

    private void updateAssignmentTable() {
        String selectedSortOption = (String) sortComboBox.getSelectedItem();
        String selectedFilterOption = (String) filterComboBox.getSelectedItem();
        String selectedTypeOption = (String) typeComboBox.getSelectedItem();

        List<Assignment> filteredAssignments = new ArrayList<>();

        for (Assignment assignment : assignments) {
            boolean matchesFilter = false;

            if ("미완과제".equals(selectedFilterOption) && !assignment.isCompleted()) {
                matchesFilter = true;
            } else if ("완료과제".equals(selectedFilterOption) && assignment.isCompleted()) {
                matchesFilter = true;
            } else if ("전체과제".equals(selectedFilterOption)) {
                matchesFilter = true;
            }

            if (matchesFilter) {
                if ("전체종류".equals(selectedTypeOption) || selectedTypeOption.equals(assignment.getType())) {
                    filteredAssignments.add(assignment);
                }
            }
        }

        if (selectedSortOption != null) {
            if (selectedSortOption.equals("시간순")) {
                filteredAssignments.sort(Comparator.comparing(Assignment::getDueDate));
            } else if (selectedSortOption.equals("과목순")) {
                filteredAssignments.sort(Comparator.comparing(Assignment::getSubject));
            }
        }

        assignmentTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Assignment assignment : filteredAssignments) {
            try {
                Date dueDate = sdf.parse(assignment.getDueDate());
                assignmentTableModel.addRow(new Object[]{assignment.getSubject(), assignment.getName(), sdf.format(dueDate)});
            } catch (ParseException e) {
                // Handle the exception gracefully
                assignmentTableModel.addRow(new Object[]{assignment.getSubject(), assignment.getName(), assignment.getDueDate()});
            }
        }
    }

    private List<Assignment> parseCSV(String csvFilePath) {
        List<Assignment> assignmentList = new ArrayList<>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(csvFilePath));
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
                        String name = fields[1];
                        String period = fields[2];
                        String subject = fields[3];
                        String type = fields[4];
                        String details = fields.length > 11 ? fields[11] : "";

                        String dueDate = period.isEmpty() ? "2024-06-21" : period;

                        Assignment assignment = new Assignment(completed, name, dueDate, subject, type, details);
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

    private int countOccurrences(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
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
}
