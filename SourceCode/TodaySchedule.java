import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;

public class TodaySchedule extends JPanel {
    private JTable todoTable;
    private JTable scheduleTable;
    private JTable assignmentTable;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> typeComboBox;
    private JLabel imageLabel;
    private String defaultImageFileName = "3학기_시간표.jpg"; // 기본 이미지 파일명
    private static final int IMAGE_MARGIN = 20; // 이미지 여백
    private String userId;
    private int year;
    private int sem;

    private Map<String, DefaultTableModel> modelMapAssignment = new HashMap<>();
    private Map<String, DefaultTableModel> modelMapClass = new HashMap<>();

    public TodaySchedule(String userId, String semester) {
        this.userId = userId;
        String[] parts = semester.split("-");
        this.year = Integer.parseInt(parts[0]);
        this.sem = Integer.parseInt(parts[1].substring(0, 1));

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
                if (column == 0) {
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

        // 최근 수업
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBorder(BorderFactory.createTitledBorder("최근 수업"));
        String[] scheduleColumns = {"주차", "강의명", "예고된 강의 내용"};

        // 오늘 날짜와 1주 전후 날짜 구하기
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minus(1, ChronoUnit.WEEKS);
        LocalDate oneWeekLater = today.plus(1, ChronoUnit.WEEKS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Object[]> scheduleDataList = new ArrayList<>();
        for (String className : modelMapClass.keySet()) {
            DefaultTableModel model = modelMapClass.get(className);
            for (int i = 0; i < model.getRowCount(); i++) {
                String week = (String) model.getValueAt(i, 0);
                String lecturePlan = (String) model.getValueAt(i, 1);
                String lectureDate = (String) model.getValueAt(i, 8);

                // 강의 날짜가 1주 전후 범위 내에 있는지 확인
                LocalDate parsedLectureDate = LocalDate.parse(lectureDate, formatter);
                if (!parsedLectureDate.isBefore(oneWeekAgo) && !parsedLectureDate.isAfter(oneWeekLater)) {
                    Object[] rowData = {week, className, lecturePlan};
                    scheduleDataList.add(rowData);
                }
            }
        }

        Object[][] scheduleData = scheduleDataList.toArray(new Object[0][]);

        DefaultTableModel scheduleTableModel = new DefaultTableModel(scheduleData, scheduleColumns);
        scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setRowSelectionAllowed(false);
        scheduleTable.setShowGrid(false);
        schedulePanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // 예정된 과제
        AssignmentFilter a = new AssignmentFilter(userId, semester);
        JPanel assignmentPanel = a.assignmentPanel;


        // -------------시간표 이미지 띄우고 변경하기
        JPanel courseTimetablePanel = new JPanel(new BorderLayout());
        courseTimetablePanel.setBorder(BorderFactory.createTitledBorder("시간표 이미지"));
        courseTimetablePanel.setPreferredSize(new Dimension(300, 300)); // 패널 크기 설정

        // 이미지 라벨 생성 및 기본 이미지 설정
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        setDefaultImage();

        // 이미지 변경 버튼 생성 및 이벤트 핸들러 설정
        JButton changeImageButton = new JButton("이미지 변경");
        changeImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeImage();
            }
        });

        // 레이아웃 설정
        courseTimetablePanel.add(imageLabel, BorderLayout.CENTER);
        courseTimetablePanel.add(changeImageButton, BorderLayout.SOUTH);

        // 창 분할
        // 전체 패널의 크기
        Dimension panelSize = new Dimension(900, 700);
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());

        JSplitPane leftVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel todoButtonPanel = new JPanel(new BorderLayout());
        todoButtonPanel.add(todoPanel, BorderLayout.CENTER);
        leftVerticalSplitPane.setTopComponent(todoButtonPanel);
        leftVerticalSplitPane.setBottomComponent(schedulePanel);
        leftVerticalSplitPane.setResizeWeight(0.5);
        leftPanel.add(leftVerticalSplitPane, BorderLayout.CENTER);

        JSplitPane rightVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightVerticalSplitPane.setTopComponent(assignmentPanel);
        rightVerticalSplitPane.setBottomComponent(courseTimetablePanel);
        rightVerticalSplitPane.setResizeWeight(0.5);
        rightPanel.add(rightVerticalSplitPane, BorderLayout.CENTER);

        horizontalSplitPane.setLeftComponent(leftPanel);
        horizontalSplitPane.setRightComponent(rightPanel);
        horizontalSplitPane.setResizeWeight(0.5);

        // 좌우 패널의 크기를 동일하게 맞추기 위해 divider 위치 설정
        int dividerLocation = panelSize.width / 2; // 좌우 패널을 정확히 반씩 나누기
        horizontalSplitPane.setDividerLocation(dividerLocation);

        // 전체 패널 크기 설정
        setPreferredSize(panelSize);
        add(horizontalSplitPane, BorderLayout.CENTER);
    }

    private void loadCsvDataAssignment() {
        String csvFileAssignment = userId + "_" + year + "_" + sem + "_assignment_db.csv";
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
        String csvFileClass = userId + "_" + year + "_" + sem + "_class_db.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFileClass, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            String[] columnNamesClass = {"주차", "예고된 강의 내용", "복습", "과제/시험", "음성기록", "관련파일", "To-Do", "완료여부", "수업일"};

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

    // 기본 이미지 설정 메서드
    private void setDefaultImage() {
        try {
            BufferedImage defaultImage = ImageIO.read(new File(defaultImageFileName));
            Image scaledImage = defaultImage.getScaledInstance(
                    350 - IMAGE_MARGIN, // 이미지 크기 조정
                    350 - IMAGE_MARGIN, // 이미지 크기 조정
                    Image.SCALE_SMOOTH
            );
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "기본 이미지 파일을 찾을 수 없습니다.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 이미지 변경 메서드
    private void changeImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage newImage = ImageIO.read(selectedFile);
                Image scaledImage = newImage.getScaledInstance(
                        350 - IMAGE_MARGIN, // 이미지 크기 조정
                        350 - IMAGE_MARGIN, // 이미지 크기 조정
                        Image.SCALE_SMOOTH
                );
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
