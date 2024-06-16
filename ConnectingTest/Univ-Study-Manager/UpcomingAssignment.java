import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UpcomingAssignment extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private String csvFile;

    public UpcomingAssignment(String userId, String semester) {
        String[] parts = semester.split("-");
        int year = Integer.parseInt(parts[0]);
        int sem = Integer.parseInt(parts[1].substring(0, 1));

        csvFile = userId + "_" + year + "_" + sem + "_assignment_db.csv";
        String[] columnNames = {"완료", "과제명", "마감일", "관련수업", "과제종류", "성적비율", "환산점수", "과제만점", "내 점수", "관련파일", "관련URL", "정보"};

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        loadCsvData();

        table = new JTable(model) {
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
                        showInfoPopup(info); // 정보 팝업
                    } else if (value instanceof String && column == 1) { // (과제명 열 && 값이 문자열)인 경우
                        String assignmentName = (String) value;
                        showAssignmentPopup(assignmentName); // 과제명 팝업
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table); // table을 Scroll 패널에 추가

        add(scrollPane); // 프레임에 Scroll 패널 추가

        setTitle("과제 & 시험 DB"); // 프레임 제목: 과제 & 시험 DB
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 프레임 종료시, 팝업만 닫힘
        pack();
        setLocationRelativeTo(null); // 프레임을 화면 중앙에 배치
    }

    private void loadCsvData() {
        List<Object[]> rowDataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            br.readLine(); // Skip header

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

                    if (!data[0].equals("Y")) {
                        Object[] rowData = new Object[model.getColumnCount()];
                        rowData[0] = data[0].equals("Y");
                        for (int i = 1; i < rowData.length && i < data.length; i++) {
                            rowData[i] = data[i];
                        }
                        rowDataList.add(rowData);
                    }
                    sb.setLength(0);
                } else {
                    sb.append("\n");
                }
            }

            Collections.sort(rowDataList, new Comparator<Object[]>() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                @Override
                public int compare(Object[] o1, Object[] o2) {
                    String dateStr1 = (String) o1[2];
                    String dateStr2 = (String) o2[2];
                    if (dateStr1.isEmpty() && dateStr2.isEmpty()) return 0;
                    if (dateStr1.isEmpty()) return 1;
                    if (dateStr2.isEmpty()) return -1;
                    LocalDate date1 = LocalDate.parse(dateStr1, formatter);
                    LocalDate date2 = LocalDate.parse(dateStr2, formatter);
                    return date1.compareTo(date2);
                }
            });

            for (Object[] rowData : rowDataList) {
                model.addRow(rowData);
            }
        } catch (IOException | DateTimeParseException e) {
            e.printStackTrace();
        }
    }

    private void saveChangesToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            writer.write("완료,과제명,마감일,관련수업,과제종류,성적비율,환산점수,과제만점,내 점수,관련파일,관련URL,정보\n");

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
                        if (valueStr.contains(",") || valueStr.contains("\"")) {
                            valueStr = "\"" + valueStr.replace("\"", "\"\"") + "\"";
                        }
                        sb.append(valueStr);
                    }
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConvertedScore(JTable table, int row) {
        try {
            double gradeRatio = Double.parseDouble(table.getValueAt(row, 5).toString());
            double maxScore = Double.parseDouble(table.getValueAt(row, 7).toString());
            double myScore = Double.parseDouble(table.getValueAt(row, 8).toString());

            double convertedScore = (gradeRatio * myScore) / maxScore;
            table.setValueAt(String.format("%.2f", convertedScore), row, 6);
        } catch (NumberFormatException e) {
            table.setValueAt("0.00", row, 6);
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
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        java.util.List<String> fields = new java.util.ArrayList<>();

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

    private void openWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
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

    private void showInfoPopup(String info) {
        JTextArea textArea = new JTextArea(info);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "정보", JOptionPane.PLAIN_MESSAGE);
    }

    private void showAssignmentPopup(String assignmentName) {
        JTextArea textArea = new JTextArea(assignmentName);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "과제명", JOptionPane.PLAIN_MESSAGE);
    }

    private class HyperlinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setForeground(Color.BLUE.darker());
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return label;
        }
    }

    private class InfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setText("클릭");
            label.setForeground(Color.BLUE.darker());
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return label;
        }
    }

    private class CustomComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                String item = value.toString();
                switch (item) {
                    case "보고서":
                        c.setBackground(new Color(173, 216, 230));
                        break;
                    case "프로젝트":
                        c.setBackground(new Color(144, 238, 144));
                        break;
                    case "출석":
                        c.setBackground(new Color(255, 192, 203));
                        break;
                    case "시험":
                        c.setBackground(new Color(240, 128, 128));
                        break;
                    case "퀴즈":
                        c.setBackground(new Color(255, 255, 224));
                        break;
                }
            }
            return c;
        }
    }

    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String item = value.toString();
                switch (item) {
                    case "보고서":
                        c.setBackground(new Color(173, 216, 230));
                        break;
                    case "프로젝트":
                        c.setBackground(new Color(144, 238, 144));
                        break;
                    case "출석":
                        c.setBackground(new Color(255, 192, 203));
                        break;
                    case "시험":
                        c.setBackground(new Color(240, 128, 128));
                        break;
                    case "퀴즈":
                        c.setBackground(new Color(255, 255, 224));
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

            if (column == 1 || column == 2 || column == 3 || column == 4) {
                String dueDateStr = (String) table.getValueAt(row, 2);
                if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                    try {
                        LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                        LocalDate today = LocalDate.now();
                        LocalDate twoWeeksFromNow = today.plusWeeks(2);

                        boolean isDueSoon = !dueDate.isAfter(twoWeeksFromNow);
                        boolean completed = (Boolean) table.getValueAt(row, 0);

                        if (isDueSoon && !completed) {
                            c.setForeground(Color.RED);
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } catch (DateTimeParseException e) {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
            } else {
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }
}
