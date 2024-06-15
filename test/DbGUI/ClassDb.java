import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
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
import java.util.HashMap;
import java.util.Map;

public class ClassDb extends JFrame {
    private JTabbedPane tabbedPane;
    private String csvFile = "class_db.csv";
    private String assignmentCsvFile = "assignment_db.csv"; // 추가: assignment_db.csv 파일 경로
    private String[] columnNames = {"주차", "예고된 강의 내용", "복습", "과제/시험", "음성기록", "관련파일", "To-Do", "완료여부", "수업일"};
    private Map<String, DefaultTableModel> modelMap = new HashMap<>();

    public ClassDb() {
        loadCsvData();

        tabbedPane = new JTabbedPane();

        for (Map.Entry<String, DefaultTableModel> entry : modelMap.entrySet()) {
            DefaultTableModel model = entry.getValue();
            addTab(entry.getKey(), model);
        }

        JButton addSubjectButton = new JButton("과목추가");
        addSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewSubject();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addSubjectButton);

        add(buttonPanel, BorderLayout.SOUTH);
        add(tabbedPane, BorderLayout.CENTER);

        setTitle("강의 관리 DB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void addNewSubject() {
        String subjectName = JOptionPane.showInputDialog(this, "새로운 과목 이름을 입력하세요:", "과목추가", JOptionPane.PLAIN_MESSAGE);
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            if (modelMap.containsKey(subjectName)) {
                JOptionPane.showMessageDialog(this, "이미 존재하는 과목 이름입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else {
                String firstClassDateStr = JOptionPane.showInputDialog(this, "첫 수업일을 입력하세요 (yyyy-MM-dd):", "첫 수업일 입력", JOptionPane.PLAIN_MESSAGE);
                if (firstClassDateStr != null && !firstClassDateStr.trim().isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    try {
                        LocalDate firstClassDate = LocalDate.parse(firstClassDateStr, formatter);
                        DefaultTableModel model = createNewSubjectModel(subjectName, firstClassDate);
                        modelMap.put(subjectName, model);
                        addTab(subjectName, model);
                        saveChangesToCsv();
                        addAssignmentToCsv(subjectName);
                    } catch (DateTimeParseException e) {
                        JOptionPane.showMessageDialog(this, "잘못된 날짜 형식입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private DefaultTableModel createNewSubjectModel(String subjectName, LocalDate firstClassDate) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 7 || columnIndex == 2) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        for (int week = 1; week <= 15; week++) {
            for (int part = 1; part <= 2; part++) {
                String classWeek = week + "-" + part;
                LocalDate classDate = firstClassDate.plusDays((week - 1) * 7 + (part - 1) * 2);
                String classDateStr = classDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                model.addRow(new Object[]{classWeek, "", false, "", "", "", "", false, classDateStr});
            }
        }

        return model;
    }

    private void addAssignmentToCsv(String subjectName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(assignmentCsvFile, true))) {
            writer.write("N,,," + subjectName + ",,,,,,,,\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTab(String title, DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                return super.getCellEditor(row, column);
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 1 || column == 6) {
                    return new InfoDateRenderer();
                }
                return super.getCellRenderer(row, column);
            }
        };

        table.getColumnModel().getColumn(4).setCellRenderer(new HyperlinkRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new FileRenderer());

        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                saveChangesToCsv();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / table.getRowHeight();
                if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                    Object value = table.getValueAt(row, column);
                    if (value instanceof String && column == 4) {
                        String url = (String) value;
                        openWebpage(url);
                    } else if (value instanceof String && column == 5) {
                        String filePath = (String) value;
                        handleFileClick(filePath, table, row, column);
                    } else if (value instanceof String && (column == 1 || column == 6)) {
                        String content = (String) value;
                        showContentPopup(content, table, row, column, column == 1 ? "예고된 강의 내용" : "To-Do");
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        tabbedPane.addTab(title, scrollPane);
    }

    private void loadCsvData() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;

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

                    String className = data[0];
                    DefaultTableModel model = modelMap.get(className);

                    if (model == null) {
                        model = new DefaultTableModel(columnNames, 0) {
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == 7 || columnIndex == 2) {
                                    return Boolean.class;
                                }
                                return String.class;
                            }
                        };
                        modelMap.put(className, model);
                    }

                    Object[] rowData = new Object[columnNames.length];
                    rowData[0] = data[1];
                    rowData[1] = data[2];
                    rowData[2] = data[3].equals("Y");
                    rowData[3] = data[4];
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

    private void saveChangesToCsv() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8)) {
            writer.write("강의명,주차,예고된 강의 내용,복습,과제/시험,음성기록,관련파일,To-Do,완료여부,수업일\n");

            for (Map.Entry<String, DefaultTableModel> entry : modelMap.entrySet()) {
                String className = entry.getKey();
                DefaultTableModel model = entry.getValue();
                for (int row = 0; row < model.getRowCount(); row++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(className).append(",");
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

    private void handleFileClick(String filePath, JTable table, int row, int column) {
        if (filePath != null && !filePath.isEmpty()) {
            int response = JOptionPane.showOptionDialog(null,
                    "파일을 열겠습니까? 파일을 교체하겠습니까?",
                    "파일 열기/교체",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"파일 열기", "파일 교체", "취소"},
                    "파일 열기");

            if (response == 0) {
                openFile(filePath);
            } else if (response == 1) {
                selectFile(table, row, column);
            }
        } else {
            selectFile(table, row, column);
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

    private void showContentPopup(String initialContent, JTable table, int row, int column, String title) {
        JTextArea textArea = new JTextArea(initialContent);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String updatedContent = textArea.getText();
            table.setValueAt(updatedContent, row, column);
            saveChangesToCsv();
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

    private class FileRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setForeground(Color.BLUE.darker());
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return label;
        }
    }

    private class InfoDateRenderer extends DefaultTableCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            String dateStr = (String) table.getValueAt(row, 8);
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    LocalDate classDate = LocalDate.parse(dateStr, formatter);
                    LocalDate today = LocalDate.now();

                    boolean isPastOrToday = !classDate.isAfter(today);
                    boolean reviewChecked = (Boolean) table.getValueAt(row, 2);
                    boolean todoCompletionChecked = (Boolean) table.getValueAt(row, 7);

                    if (column == 1 && isPastOrToday && !reviewChecked) {
                        label.setForeground(Color.RED);
                    } else if (column == 6 && isPastOrToday && !todoCompletionChecked) {
                        label.setForeground(Color.RED);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                } catch (DateTimeParseException e) {
                    label.setForeground(Color.BLACK);
                }
            } else {
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClassDb gui = new ClassDb();
            gui.setVisible(true);
        });
    }
}
