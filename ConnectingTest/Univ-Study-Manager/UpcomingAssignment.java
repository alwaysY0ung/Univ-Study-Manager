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
import java.util.ArrayList;
import java.util.Comparator;

public class UpcomingAssignment extends JPanel {
    private String csvFile;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

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
                if (column == 4) {
                    JComboBox<String> comboBox = new JComboBox<>(new String[]{"보고서", "프로젝트", "출석", "시험", "퀴즈"});
                    return new DefaultCellEditor(comboBox);
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 4) {
                    return new CustomTableCellRenderer();
                }
                return super.getCellRenderer(row, column);
            }
        };

        sorter = new TableRowSorter<>(model);
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
        applyRowFilter();

        table.getColumnModel().getColumn(10).setCellRenderer(new HyperlinkRenderer());
        table.getColumnModel().getColumn(11).setCellRenderer(new InfoRenderer());

        table.setDefaultRenderer(String.class, new DueDateRenderer());

        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (column == 2) {
                    sortAndSaveCsv();
                }

                if (column == 5 || column == 7 || column == 8) {
                    updateConvertedScore(table, row);
                }

                saveChangesToCsv();
                applyRowFilter();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / table.getRowHeight();

                if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                    Object value = table.getValueAt(row, column);
                    if (column == 9) {
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
                    } else if (value instanceof String && column == 10) {
                        String url = (String) value;
                        openWebpage(url);
                    } else if (value instanceof String && column == 11) {
                        String info = (String) value;
                        showInfoPopup(info, table, row, column);
                    } else if (value instanceof String && column == 1) {
                        String assignmentName = (String) value;
                        showAssignmentPopup(assignmentName, table, row, column);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

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

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void applyRowFilter() {
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                Boolean completed = (Boolean) entry.getValue(0);
                return completed == null || !completed;
            }
        });
    }

    private void addNewRow() {
        Object[] newRow = new Object[model.getColumnCount()];
        model.addRow(newRow);
        saveChangesToCsv();
        applyRowFilter();
    }

    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(table.convertRowIndexToModel(selectedRow));
            saveChangesToCsv();
            applyRowFilter();
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

    private void sortAndSaveCsv() {
        java.util.List<Object[]> rows = new ArrayList<>();

        for (int row = 0; row < model.getRowCount(); row++) {
            Object[] rowData = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                rowData[col] = model.getValueAt(row, col);
            }
            rows.add(rowData);
        }

        rows.sort(Comparator.comparing(o -> {
            String dateStr = (String) o[2];
            if (dateStr.isEmpty()) {
                return LocalDate.MAX;
            }
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }));

        model.setRowCount(0);
        for (Object[] rowData : rows) {
            model.addRow(rowData);
        }

        saveChangesToCsv();
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
                        if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
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
        java.util.List<String> fields = new ArrayList<>();

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
            applyRowFilter();
        }
    }

    private void showInfoPopup(String initialInfo, JTable table, int row, int column) {
        JTextArea textArea = new JTextArea(initialInfo);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "정보", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String updatedInfo = textArea.getText();
            updateTableAndSave(table, row, column, updatedInfo);
        }
    }

    private void updateTableAndSave(JTable table, int row, int column, String updatedInfo) {
        table.setValueAt(updatedInfo, row, column);
        saveChangesToCsv();
        applyRowFilter();
    }

    private void showAssignmentPopup(String initialAssignmentName, JTable table, int row, int column) {
        JTextArea textArea = new JTextArea(initialAssignmentName);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "과제명", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String updatedAssignmentName = textArea.getText();
            table.setValueAt(updatedAssignmentName, row, column);
            saveChangesToCsv();
            applyRowFilter();
        }
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
                    c.setForeground(Color.BLACK);
                }
            } else {
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }
}
