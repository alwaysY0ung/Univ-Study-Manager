import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClassDb extends JFrame {
    private JTabbedPane tabbedPane;
    private String[] columnNames = {"주차", "예고된 강의 내용", "복습", "과제/시험", "음성기록", "관련파일", "To-Do", "완료여부", "수업일"};
    private Map<String, DefaultTableModel> modelMap = new HashMap<>();

    public ClassDb() {
        String csvFile = "class_db.csv";;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            StringBuilder sb = new StringBuilder();
            String line;

            // 첫 번째 줄(컬럼명)을 읽어서 건너뛴다.
            br.readLine();

            while ((line = br.readLine()) != null) {
                sb.append(line);
                int quoteCount = countOccurrences(sb.toString(), '\"');

                if (quoteCount % 2 == 0) {
                    String csvLine = sb.toString();
                    String[] data = parseCSVLine(csvLine);

                    // 최소한 4개 이상의 요소를 포함하는지 확인
                    if (data.length < 4) {
                        sb.setLength(0); // Clear the buffer for the next line
                        continue;
                    }

                    String className = data[0]; // 강의명 열
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
                    for (int i = 0; i < columnNames.length && i < data.length - 1; i++) {
                        if (i == 7) {
                            rowData[i] = data[i + 1].equals("Y");
                        } else if (i == 2) {
                            rowData[i] = data[i + 1].equals("Y");
                        } else {
                            rowData[i] = data[i + 1];
                        }
                    }
                    model.addRow(rowData);
                    sb.setLength(0); // Clear the buffer
                } else {
                    sb.append("\n"); // Add a newline character to preserve multi-line fields
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tabbedPane = new JTabbedPane();

        for (Map.Entry<String, DefaultTableModel> entry : modelMap.entrySet()) {
            JTable table = new JTable(entry.getValue());
            table.getColumnModel().getColumn(5).setCellRenderer(new HyperlinkRenderer());
            table.getColumnModel().getColumn(1).setCellRenderer(new InfoRenderer());
            table.setDefaultRenderer(String.class, new DateRenderer());


            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = table.getColumnModel().getColumnIndexAtX(e.getX());
                    int row = e.getY() / table.getRowHeight();
                    if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                        Object value = table.getValueAt(row, column);
                        if (value instanceof String && column == 5) {
                            String url = (String) value;
                            openWebpage(url);
                        } else if (value instanceof String && column == 1) {
                            String info = (String) value;
                            showInfoPopup(info);
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            tabbedPane.addTab(entry.getKey(), scrollPane);
        }

        // "+" 탭 추가
        JPanel newTabPanel = new JPanel();
        newTabPanel.setPreferredSize(new Dimension(100, 100));
        tabbedPane.addTab("+", newTabPanel);

        // "+" 탭 클릭 시 새로운 탭 생성
        newTabPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                createNewTab();
            }
        });

        add(tabbedPane);

        setTitle("강의 관리 DB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void createNewTab() {
        String newClassName = JOptionPane.showInputDialog(this, "새로운 강의명을 입력하세요:");
        if (newClassName != null && !newClassName.isEmpty()) {
            DefaultTableModel newModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 7 || columnIndex == 2) {
                        return Boolean.class;
                    }
                    return String.class;
                }
            };
            modelMap.put(newClassName, newModel);

            JTable newTable = new JTable(newModel);
            newTable.getColumnModel().getColumn(5).setCellRenderer(new HyperlinkRenderer());
            newTable.getColumnModel().getColumn(6).setCellRenderer(new InfoRenderer());
            newTable.setDefaultRenderer(String.class, new DateRenderer());


            newTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = newTable.getColumnModel().getColumnIndexAtX(e.getX());
                    int row = e.getY() / newTable.getRowHeight();
                    if (row < newTable.getRowCount() && row >= 0 && column < newTable.getColumnCount() && column >= 0) {
                        Object value = newTable.getValueAt(row, column);
                        if (value instanceof String && column == 5) {
                            String url = (String) value;
                            openWebpage(url);
                        } else if (value instanceof String && column == 1) {
                            String info = (String) value;
                            showInfoPopup(info);
                        }
                    }
                }
            });

            JScrollPane newScrollPane = new JScrollPane(newTable);
            tabbedPane.insertTab(newClassName, null, newScrollPane, null, tabbedPane.getTabCount() - 1);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 2);
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
                sb.setLength(0); // Clear the buffer
            } else {
                sb.append(ch);
            }
        }
        fields.add(sb.toString().trim()); // Add the last field

        return fields.toArray(new String[0]);
    }

    private void openWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfoPopup(String info) {
        JTextArea textArea = new JTextArea(info);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "예고된 강의 내용", JOptionPane.PLAIN_MESSAGE); // AssignmentDb.java에 있는 거랑 이 저 string 인자만 달라서 나중에 클래스 분리할 때 상속으로도 될듯
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
            label.setForeground(Color.BLUE.darker());
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return label;
        }
    }

    // 수업일이 당일이거나 전날일 경우, 체크박스 미체크시 빨간색 처리
    // "복습" 체크박스 미체크시 "예고된 강의 내용" 빨간색처리 완료
    // "완료여부" 체크박스 미체크시 "To-Do" 빨간색 처리: 시도했으나 이유 모르게 작동하지 않음.
    // 둘은 같은 로직인데 왜 하나는 작동하지 않는지 모르겠음. 고쳐야한다.
    private class DateRenderer extends DefaultTableCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 1 || column == 6) { // "예고된 강의 내용" or "To-Do"
                String dateStr = (String) table.getValueAt(row, 8); // "수업일" 열의 값
                LocalDate classDate = LocalDate.parse(dateStr, formatter);
                LocalDate today = LocalDate.now();

                boolean isPastOrToday = !classDate.isAfter(today);
                boolean reviewChecked = (Boolean) table.getValueAt(row, 2);
                boolean todoCompletionChecked = (Boolean) table.getValueAt(row, 7);

                if (column == 1 && isPastOrToday && !reviewChecked) {
                    c.setForeground(Color.RED);
                } else if (column == 6 && isPastOrToday && !todoCompletionChecked) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
            } else {
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClassDb gui = new ClassDb();
            gui.setVisible(true);
        });
    }
}