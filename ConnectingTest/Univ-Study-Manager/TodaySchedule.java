import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TodaySchedule extends JPanel {
    private JTable todoTable;
    private JTable scheduleTable;
    private JTable assignmentTable;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> typeComboBox;

    String csvFileClass = "class_db.csv";
    String assignmentCsvFile = "assignment_db.csv"; // 추가: assignment_db.csv 파일 경로
    String[] columnNamesClass = {"주차", "예고된 강의 내용", "복습", "과제/시험", "음성기록", "관련파일", "To-Do", "완료여부", "수업일"};
    Map<String, DefaultTableModel> modelMapClass = new HashMap<>(); // 관련수업별 모델을 저장하는 맵

    String csvFileAssignment = "assignment_db.csv";
    String classCsvFile = "class_db.csv"; // 추가: class_db.csv 파일 경로
    Map<String, DefaultTableModel> modelMapAssignment = new HashMap<>();
    String[] columnNamesAssignment = {"완료", "과제명", "마감일", "관련수업", "과제종류", "성적비율", "환산점수", "과제만점", "내 점수", "관련파일", "관련URL", "정보"};

    private void loadCsvDataAssignment() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFileAssignment, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            String[] columnNames = {"완료", "과제명", "마감일", "관련수업", "과제종류", "성적비율", "환산점수", "과제만점", "내 점수", "관련파일", "관련URL", "정보"};

            br.readLine();

            while ((line = br.readLine()) != null) {
                sb.append(line);
                int quoteCount = countOccurrences(sb.toString(), '\"');

                if (quoteCount % 2 == 0) {
                    String csvLine = sb.toString();
                    String[] data = parseCSVLine(csvLine);

                    if (data.length < 4) {
                        sb.setLength(0);
                        continue;
                    }

                    String relatedClass = data[3];
                    DefaultTableModel model = modelMapAssignment.get(relatedClass);

                    if (model == null) {
                        model = new DefaultTableModel(columnNames, 0) {
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == 0) {
                                    return Boolean.class;
                                }
                                return String.class;
                            }
                        };
                        modelMapAssignment.put(relatedClass, model);
                    }

                    Object[] rowData = new Object[columnNames.length];
                    rowData[0] = data[0].equals("Y");
                    for (int i = 1; i < columnNames.length && i < data.length; i++) {
                        rowData[i] = data[i];
                    }
                    model.addRow(rowData);
                    sb.setLength(0);
                } else {
                    sb.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCsvDataClass() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFileClass, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;

            br.readLine(); // 헤더 건너뛰기

            while ((line = br.readLine()) != null) {
                sb.append(line);
                int quoteCount = countOccurrences(sb.toString(), '\"');

                if (quoteCount % 2 == 0) {
                    String csvLine = sb.toString();
                    String[] data = parseCSVLine(csvLine);

                    if (data.length < 4) {
                        sb.setLength(0);
                        continue;
                    }

                    String className = data[0];
                    DefaultTableModel model = modelMapClass.get(className);

                    if (model == null) {
                        model = new DefaultTableModel(columnNamesClass, 0) {
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == 7 || columnIndex == 2) {
                                    return Boolean.class;
                                }
                                return String.class;
                            }
                        };
                        modelMapClass.put(className, model);
                    }

                    Object[] rowData = new Object[columnNamesClass.length];
                    rowData[0] = data[1];
                    rowData[1] = data[2];
                    rowData[2] = data[3].equals("Y");
                    rowData[3] = ""; // 과제/시험 열은 빈칸으로 시작, 나중에 할당
                    rowData[4] = data[5];
                    rowData[5] = data[6];
                    rowData[6] = data[7];
                    rowData[7] = data[8].equals("Y");
                    rowData[8] = data[9];

                    model.addRow(rowData);
                    sb.setLength(0);
                } else {
                    sb.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    /* parseCSVLine 메서드는 주어진 CSV 행을 parse하여 문자열 배열로 반환하는 메서드로
     * 따옴표로 묶인 필드를 처리한다
     */
    private String[] parseCSVLine(String csvLine) {
        boolean inQuotes = false; // 따옴표 안에 있는지 여부
        StringBuilder sb = new StringBuilder();
        java.util.List<String> fields = new ArrayList<>();

        for (int i = 0; i < csvLine.length(); i++) {
            char ch = csvLine.charAt(i);

            if (ch == '\"') { // 따옴표인 경우
                inQuotes = !inQuotes; // 따옴표 상태 전환
            } else if (ch == ',' && !inQuotes) { // 쉼표이고 따옴표 밖에 있는 경우
                fields.add(sb.toString().trim()); // 현재 필드를 리스트에 추가
                sb.setLength(0); // Clear the buffer
            } else {
                sb.append(ch); // 필드에 문자 추가
            }
        }
        fields.add(sb.toString().trim()); // Add the last field

        return fields.toArray(new String[0]); // 리스트를 문자열 배열로 반환하여 변환
    }

    public TodaySchedule() {
        setLayout(new BorderLayout());

        // CSV 데이터 로드
        loadCsvDataAssignment();
        loadCsvDataClass();

        // TO-DO Deadline
        JPanel todoPanel = new JPanel(new BorderLayout());
        todoPanel.setBorder(BorderFactory.createTitledBorder("TO-DO Deadline"));
        String[] todoColumns = {"과제명", "완료여부", "마감일"};

        // modelMapAssignment에서 "과제명", "완료여부", "마감일" 데이터 추출
        List<Object[]> todoDataList = new ArrayList<>();
        for (DefaultTableModel model : modelMapAssignment.values()) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String assignmentName = (String) model.getValueAt(i, 1);
                boolean isCompleted = (boolean) model.getValueAt(i, 0);
                String deadline = (String) model.getValueAt(i, 2);
                
                Object[] rowData = {assignmentName, isCompleted, deadline};
                todoDataList.add(rowData);
            }
        }

        // todoData 배열 정의
        Object[][] todoData = todoDataList.toArray(new Object[0][]);

        DefaultTableModel todoTableModel = new DefaultTableModel(todoData, todoColumns);
        todoTable = new JTable(todoTableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };

        todoTable.setRowSelectionAllowed(false);
        todoTable.setShowGrid(false);

        todoPanel.add(new JScrollPane(todoTable), BorderLayout.CENTER);

        todoTable.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                int col = todoTable.getSelectedColumn();
                if (col == 1) {
                    todoTable.repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        // 미완료 과제 빨간색으로 강조
        todoTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // 체크박스가 체크되었는지 확인
                boolean isChecked = (boolean) table.getValueAt(row, 1);

                // 체크박스 체크 여부에 따른 수행
                if (column == 0 ) {
                    System.out.println(c.getForeground().toString());
                    if (isChecked) {
                        // 체크박스가 체크되었을 경우, 글자 색상을 기본 색상으로 변경
                        c.setForeground(table.getForeground());
                    } else {
                        // 체크박스가 체크되지 않은 경우, 첫 번째 요소와 세 번째 요소의 글자 색상을 빨간색으로 변경
                            c.setForeground(Color.RED);
                    }
                }
                return c;
            }
        });


        // 오늘 수업
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBorder(BorderFactory.createTitledBorder("오늘 수업"));
        String[] scheduleColumns = {"과목명", "진도 내용", "과제"};
        Object[][] scheduleData = {
                {"객체지향프로그래밍", "Java 이벤트 프로그래밍", "-"},
                {"데이터구조", "Queue의 활용", "-"}
        };
        DefaultTableModel scheduleTableModel = new DefaultTableModel(scheduleData, scheduleColumns);
        scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setRowSelectionAllowed(false);
        scheduleTable.setShowGrid(false);
        schedulePanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // 시간표 팝업 창
        JButton timeTableButton = new JButton("시간표");
        timeTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 시간표 팝업 창 구현
                JDialog timeTableDialog = new JDialog();
                timeTableDialog.setTitle("시간표");
                timeTableDialog.setSize(400, 300);
                timeTableDialog.setLocationRelativeTo(null);
                // 시간표 DB에서 정보를 가져와 표 생성
                // ...
                timeTableDialog.setVisible(true);
            }
        });

        // 예정된 과제
        AssignmentFilter a = new AssignmentFilter();
        JPanel assignmentPanel = a.assignmentPanel;        

        // 예정된 TO-DO
        JPanel upcomingPanel = new JPanel(new BorderLayout());
        upcomingPanel.setBorder(BorderFactory.createTitledBorder("예정된 TO-DO"));
        // 예정된 TO-DO 목록을 DB에서 가져와 표시
        // ...

        // 창 분할
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());

        JSplitPane leftVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel todoButtonPanel = new JPanel(new BorderLayout());
        todoButtonPanel.add(todoPanel, BorderLayout.CENTER);
        todoButtonPanel.add(timeTableButton, BorderLayout.SOUTH);
        leftVerticalSplitPane.setTopComponent(todoButtonPanel);
        leftVerticalSplitPane.setBottomComponent(schedulePanel);
        leftVerticalSplitPane.setResizeWeight(0.5);
        leftPanel.add(leftVerticalSplitPane, BorderLayout.CENTER);

        JSplitPane rightVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightVerticalSplitPane.setTopComponent(assignmentPanel);
        rightVerticalSplitPane.setBottomComponent(upcomingPanel);
        rightVerticalSplitPane.setResizeWeight(0.5);
        rightPanel.add(rightVerticalSplitPane, BorderLayout.CENTER);

        horizontalSplitPane.setLeftComponent(leftPanel);
        horizontalSplitPane.setRightComponent(rightPanel);
        horizontalSplitPane.setResizeWeight(0.5);
        add(horizontalSplitPane, BorderLayout.CENTER);
    }

    
}
