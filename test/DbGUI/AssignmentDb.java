import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AssignmentDb extends JFrame {
    private JTabbedPane tabbedPane; // 탭 패널을 저장하는 변수
    String csvFile = "assignment_db.csv";
    // 관련수업별 모델을 저장하는 맵
    Map<String, DefaultTableModel> modelMap = new HashMap<>();

    public AssignmentDb() {
        String[] columnNames = {"완료", "과제명", "마감일", "관련수업", "과제종류", "성적비율", "환산점수", "과제만점", "내 점수", "관련파일", "관련URL", "정보"};

        loadCsvData();

        tabbedPane = new JTabbedPane(); // 탭 패널 생성

        // model Map의 각 entry에 대해 JTable 생성 및 구성
        for (Map.Entry<String, DefaultTableModel> entry : modelMap.entrySet()) {
            DefaultTableModel model = entry.getValue();
            JTable table = new JTable(model) {
                @Override
                public TableCellEditor getCellEditor(int row, int column) {
                    if (column == 4) { // 과제종류 열
                        JComboBox<String> comboBox = new JComboBox<>(new String[]{"보고서", "프로젝트", "출석", "시험", "퀴즈"});
                        return new DefaultCellEditor(comboBox);
                    }
                    return super.getCellEditor(row, column);
                }

                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    if (column == 4) { // 과제종류 열
                        return new CustomTableCellRenderer();
                    }
                    return super.getCellRenderer(row, column);
                }
            };

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            sorter.setComparator(2, new Comparator<String>() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                @Override
                public int compare(String date1, String date2) {
                    if (date1.isEmpty() || date2.isEmpty()) {
                        return date1.compareTo(date2);
                    }
                    LocalDate localDate1 = LocalDate.parse(date1, formatter);
                    LocalDate localDate2 = LocalDate.parse(date2, formatter);
                    return localDate1.compareTo(localDate2);
                }
            });
            table.setRowSorter(sorter);

            table.getColumnModel().getColumn(10).setCellRenderer(new HyperlinkRenderer()); // URL 열에 하이퍼링크 renderer 설정
            table.getColumnModel().getColumn(11).setCellRenderer(new InfoRenderer()); // 정보 열에 정보 renderer 설정

            table.setDefaultRenderer(String.class, new DueDateRenderer());

            table.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    if (column == 5 || column == 7 || column == 8) { // 성적비율, 과제만점, 내 점수 열이 변경된 경우
                        updateConvertedScore(table, row);
                    }

                    saveChangesToCsv();
                }
            });

            table.addMouseListener(new MouseAdapter() { // 마우스 이벤트 Listener 추가
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // 클릭한 열 인덱스
                    int row = e.getY() / table.getRowHeight(); // 클릭한 행 인덱스

                    // 유효한 행과 열 범위 내에 있는 경우
                    if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                        Object value = table.getValueAt(row, column); // 앞서 선택한(클릭한) 셀의 값
                        if (column == 9) { // 관련파일 열인 경우
                            if (value instanceof String && !((String) value).isEmpty()) {
                                int response = JOptionPane.showOptionDialog(null,
                                        "파일을 열겠습니까? 파일을 교체하겠습니까?",
                                        "파일 열기/교체",
                                        JOptionPane.YES_NO_CANCEL_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        new String[]{"파일 열기", "파일 교체", "취소"},
                                        "파일 열기");

                                if (response == 0) {
                                    openFile((String) value);
                                } else if (response == 1) {
                                    selectFile(table, row, column);
                                }
                            } else {
                                selectFile(table, row, column);
                            }
                        } else if (value instanceof String && column == 10) { // (URL 열 && 값이 문자열)인 경우
                            String url = (String) value;
                            openWebpage(url);
                        } else if (value instanceof String && column == 11) { // (정보 열 && 값이 문자열)인 경우
                            String info = (String) value;
                            showInfoPopup(info, table, row, column); // 정보 팝업
                        } else if (value instanceof String && column == 1) { // (과제명 열 && 값이 문자열)인 경우
                            String assignmentName = (String) value;
                            showAssignmentPopup(assignmentName, table, row, column); // 과제명 팝업 (수정 가능)
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table); // table을 Scroll 패널에 추가
            tabbedPane.addTab(entry.getKey(), scrollPane); // tab에 Scroll 패널에 추가
        }

        JButton addButton = new JButton("새 과제 추가");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewRow();
            }
        });

        JButton deleteButton = new JButton("선택 과제 삭제");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedRow();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
        add(tabbedPane, BorderLayout.CENTER); // 프레임에 tab 패널 추가

        setTitle("과제 & 시험 DB"); // 프레임 제목: 과제 & 시험 DB
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 프레임 종료시, 프로그램 종료
        pack();
        setLocationRelativeTo(null); // 프레임을 화면 중앙에 배치
    }

    private void addNewRow() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            String tabTitle = tabbedPane.getTitleAt(selectedIndex);
            DefaultTableModel model = modelMap.get(tabTitle);
            if (model != null) {
                Object[] newRow = new Object[model.getColumnCount()];
                newRow[3] = tabTitle; // Set the "관련수업" field to the tab title
                model.addRow(newRow);
                saveChangesToCsv();
            }
        }
    }

    private void deleteSelectedRow() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedIndex);
            JTable table = (JTable) scrollPane.getViewport().getView();
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.removeRow(table.convertRowIndexToModel(selectedRow));
                saveChangesToCsv();
            }
        }
    }

    private void loadCsvData() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
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
                    DefaultTableModel model = modelMap.get(relatedClass);

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
                        modelMap.put(relatedClass, model);
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

    private void saveChangesToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            writer.write("완료,과제명,마감일,관련수업,과제종류,성적비율,환산점수,과제만점,내 점수,관련파일,관련URL,정보\n");

            for (Map.Entry<String, DefaultTableModel> entry : modelMap.entrySet()) {
                DefaultTableModel model = entry.getValue();
                for (int row = 0; row < model.getRowCount(); row++) {
                    StringBuilder sb = new StringBuilder();
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        if (col > 0) {
                            sb.append(',');
                        }
                        Object value = model.getValueAt(row, col);
                        if (value != null) {
                            String valueStr = value.toString();
                            if (value instanceof Boolean) {
                                valueStr = (Boolean) value ? "Y" : "N";
                            }
                            if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                                valueStr = "\"" + valueStr.replace("\"", "\"\"") + "\"";
                            }
                            sb.append(valueStr);
                        }
                    }
                    writer.write(sb.toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConvertedScore(JTable table, int row) {
        try {
            String gradeRatioStr = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "0";
            String maxScoreStr = table.getValueAt(row, 7) != null ? table.getValueAt(row, 7).toString() : "1";
            String myScoreStr = table.getValueAt(row, 8) != null ? table.getValueAt(row, 8).toString() : "0";

            double gradeRatio = Double.parseDouble(gradeRatioStr.isEmpty() ? "0" : gradeRatioStr);
            double maxScore = Double.parseDouble(maxScoreStr.isEmpty() ? "1" : maxScoreStr);
            double myScore = Double.parseDouble(myScoreStr.isEmpty() ? "0" : myScoreStr);

            double convertedScore = (gradeRatio * myScore) / maxScore;
            table.setValueAt(String.format("%.2f", convertedScore), row, 6);
        } catch (NumberFormatException e) {
            table.setValueAt("0.00", row, 6);
        }
    }

    /* countOccurrences 메서드는 주어진 문자열에서 특정 문자의 출현 횟수를 계산하는 메서드 */
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
        java.util.List<String> fields = new java.util.ArrayList<>();

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


    /* openWebpage 메서드는 URL을 기본 웹 브라우저에서 여는 메서드 */
    private void openWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url)); // URL 객체 생성 및 기본 웹 브라우저에서 열기
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFile(String filePath) {
        try {
            Desktop.getDesktop().open(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectFile(JTable table, int row, int column) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            table.setValueAt(selectedFile.getAbsolutePath(), row, column);
            saveChangesToCsv();
        }
    }

    /* showInfoPopup 메서드는 정보 문자열을 팝업 창에 표시하는 메서드 */
    private void showInfoPopup(String initialInfo, JTable table, int row, int column) {
        JTextArea textArea = new JTextArea(initialInfo); // 정보 문자열로 텍스트 area 생성
        textArea.setLineWrap(true); // Enable line wrapping
        textArea.setWrapStyleWord(true); // Wrap at word boundaries

        JScrollPane scrollPane = new JScrollPane(textArea); // 텍스트 영역을 스크롤 패널에 추가
        scrollPane.setPreferredSize(new Dimension(400, 300));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "정보", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String updatedInfo = textArea.getText();
            updateTableAndSave(table, row, column, updatedInfo); // 테이블 업데이트 및 CSV 파일 저장
        }
    }

    private void updateTableAndSave(JTable table, int row, int column, String updatedInfo) {
        table.setValueAt(updatedInfo, row, column);
        saveChangesToCsv(); // 변경 내용을 CSV 파일에 저장
    }

    /* showAssignmentPopup 매서드는 과제명을 팝업 창에 표시하는 메서드 */
    private void showAssignmentPopup(String initialAssignmentName, JTable table, int row, int column) {
        JTextArea textArea = new JTextArea(initialAssignmentName);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "과제명", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String updatedAssignmentName = textArea.getText();
            table.setValueAt(updatedAssignmentName, row, column);
            saveChangesToCsv(); // 변경 내용을 CSV 파일에 저장
        }
    }

    /* HyperlinkRenderer 클래스는 JTable의 cell renderer로 사용되며, 해당 셀의 내용을 하이퍼링크처럼 렌더링 */
    private class HyperlinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setForeground(Color.BLUE.darker());
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 커서 모양: 손가락 모양
            return label;
        }
    }

    /* InfoRenderer 클래스는 JTable의 cell renderer로 사용되며, 해당 셀의 내용을 '클릭' 텍스트로 렌더링 */
    private class InfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setText("클릭"); // 텍스트를 '클릭'으로 설정
            label.setForeground(Color.BLUE.darker());
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 커서 모양: 손가락 모양
            return label;
        }
    }

    /* JComboBox의 색상 지정 */
    private class CustomComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                String item = value.toString();
                switch (item) {
                    case "보고서":
                        c.setBackground(new Color(173, 216, 230)); // 하늘색
                        break;
                    case "프로젝트":
                        c.setBackground(new Color(144, 238, 144)); // 연두색
                        break;
                    case "출석":
                        c.setBackground(new Color(255, 192, 203)); // 핑크색
                        break;
                    case "시험":
                        c.setBackground(new Color(240, 128, 128)); // 빨간색
                        break;
                    case "퀴즈":
                        c.setBackground(new Color(255, 255, 224)); // 노란색
                        break;
                }
            }
            return c;
        }
    }

    /* JTable에서 JComboBox 색상 지정                                                                                                                                                                                   */
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String item = value.toString();
                switch (item) {
                    case "보고서":
                        c.setBackground(new Color(173, 216, 230)); // 하늘색
                        break;
                    case "프로젝트":
                        c.setBackground(new Color(144, 238, 144)); // 연두색
                        break;
                    case "출석":
                        c.setBackground(new Color(255, 192, 203)); // 핑크색
                        break;
                    case "시험":
                        c.setBackground(new Color(240, 128, 128)); // 빨간색
                        break;
                    case "퀴즈":
                        c.setBackground(new Color(255, 255, 224)); // 노란색
                        break;
                }
            }
            return c;
        }
    }

    private class DueDateRenderer extends DefaultTableCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String dueDateStr = (String) table.getValueAt(row, 2);
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                    LocalDate today = LocalDate.now();
                    LocalDate twoWeeksFromNow = today.plusWeeks(2);

                    boolean isDueSoon = !dueDate.isAfter(twoWeeksFromNow);
                    Boolean completed = (Boolean) table.getValueAt(row, 0);
                    if (completed == null) completed = false;

                    if (isDueSoon && !completed) {
                        c.setForeground(Color.RED);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } catch (DateTimeParseException e) {
                    // Invalid date format, handle accordingly (e.g., log the error, set default color, etc.)
                    c.setForeground(Color.BLACK);
                }
            } else {
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }

    /* 프로그램의 진입점인 main 메서드
     * GUI 프레임을 생성하고 화면에 표시
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AssignmentDb gui = new AssignmentDb(); // AssignmentDb 인스턴스 생성
            gui.setVisible(true); // GUI 프레임 화면에 표시
        });
    }
}
