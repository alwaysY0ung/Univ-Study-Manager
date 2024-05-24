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

public class AssignmentDb extends JFrame {
    private JTabbedPane tabbedPane;

    public AssignmentDb() {
        String csvFile = "C:/Users/SM-PC/Desktop/java_teamproject/assignment_db.csv";
        String[] columnNames = {"완료", "과제명", "마감일", "관련수업", "과제종류", "성적비율", "환산점수", "과제만점", "내 점수", "관련파일", "관련URL", "정보"};

        // 관련수업별 모델을 저장하는 맵
        Map<String, DefaultTableModel> modelMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            StringBuilder sb = new StringBuilder();
            String line;

            // 첫 번째 줄을 읽어서 건너뛴다.
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

                    String relatedClass = data[3]; // 관련수업 열 인덱스
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
            table.getColumnModel().getColumn(10).setCellRenderer(new HyperlinkRenderer());
            table.getColumnModel().getColumn(11).setCellRenderer(new InfoRenderer());
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = table.getColumnModel().getColumnIndexAtX(e.getX());
                    int row = e.getY() / table.getRowHeight();
                    if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                        Object value = table.getValueAt(row, column);
                        if (value instanceof String && column == 10) {
                            String url = (String) value;
                            openWebpage(url);
                        } else if (value instanceof String && column == 11) {
                            String info = (String) value;
                            showInfoPopup(info);
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            tabbedPane.addTab(entry.getKey(), scrollPane);
        }

        add(tabbedPane);

        setTitle("과제 & 시험 DB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
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
        JOptionPane.showMessageDialog(this, scrollPane, "정보", JOptionPane.PLAIN_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AssignmentDb gui = new AssignmentDb();
            gui.setVisible(true);
        });
    }
}
