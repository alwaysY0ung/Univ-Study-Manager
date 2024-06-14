/* 
AssignmentDB.java에서는 과제관련 정보를 csv 파일에서 읽어와 GUI로 표시하는 기능을 제공합니다
각 정보는 JTable로 표시되며, 관련 수업별로 탭으로 구분되어 있습니다. 
 */
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
import java.time.format.DateTimeParseException;

public class AssignmentDb extends JFrame {
    private JTabbedPane tabbedPane; // 탭 패널을 저장하는 변수

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

            while ((line = br.readLine()) != null) { // 파일의 모든 행을 read
                sb.append(line);
                int quoteCount = countOccurrences(sb.toString(), '\"');

                if (quoteCount % 2 == 0) { // 완전한 csv 행인 경우(따옴표 개수가 짝수인 경우)
                    String csvLine = sb.toString();
                    String[] data = parseCSVLine(csvLine); // csv 행을 배열로 변환

                    // 최소한 4개 이상의 요소를 포함하는지 확인
                    if (data.length < 4) {
                        sb.setLength(0); // Clear the buffer for the next line
                        continue;
                    }

                    String relatedClass = data[3]; // 관련수업 열 인덱스
                    DefaultTableModel model = modelMap.get(relatedClass); // 관련수업에 대한 TableModel 가져오기

                    if (model == null) { // 테이블 모델이 없는 경우, 새로 생성
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
                    rowData[0] = data[0].equals("Y"); // 완료 여부가 "Y"이면 true
                    for (int i = 1; i < columnNames.length && i < data.length; i++) {
                        rowData[i] = data[i]; // 나머지 열은 csv 데이터에서 가져옴
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

        tabbedPane = new JTabbedPane(); // 탭 패널 생성

        // model Map의 각 entry에 대해 JTable 생성 및 구성 
        for (Map.Entry<String, DefaultTableModel> entry : modelMap.entrySet()) {
            JTable table = new JTable(entry.getValue()); // 테이블 모델로부터 JTable 생성
            table.getColumnModel().getColumn(10).setCellRenderer(new HyperlinkRenderer()); // URL 열에 하이퍼링크 renderer 설정
            table.getColumnModel().getColumn(11).setCellRenderer(new InfoRenderer()); // 정보 열에 정보 renderer 설정

            table.setDefaultRenderer(String.class, new DueDateRenderer());

            table.addMouseListener(new MouseAdapter() { // 마우스 이벤트 Listener 추가
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // 클릭한 열 인텍스
                    int row = e.getY() / table.getRowHeight(); // 클릭한 행 인덱스

                    // 유효한 행과 열 범위 내에 있는 겅우
                    if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                        Object value = table.getValueAt(row, column); // 앞서 선택한(클릭한) 셀의 값
                        if (value instanceof String && column == 10) { // (URL 열 && 값이 문자열)인 경우
                            String url = (String) value;
                            openWebpage(url);
                        }
                        else if (value instanceof String && column == 11) { // (정보 열 && 값이 문자열)인 경우
                            String info = (String) value;
                            showInfoPopup(info); // 정보 팝업 
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table); // table을 Scroll 패널에 추가
            tabbedPane.addTab(entry.getKey(), scrollPane); // tab에 Scroll 패널에 추가
        }

        add(tabbedPane); // 프레임에 tab 패널 추가

        setTitle("과제 & 시험 DB"); // 프레임 제목: 과제 & 시험 DB
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 프레임 종료시, 프로그램 종료
        pack();
        setLocationRelativeTo(null); // 프레임을 화면 중앙에 배치
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

    /* showInfoPopup 메서드는 정보 문자열을 팝업 창에 표시하는 메서드 */
    private void showInfoPopup(String info) {
        JTextArea textArea = new JTextArea(info); // 정보 문자열로 텍스트 area 생성
        textArea.setEditable(false); // 주어진 텍스트 영역을 편집할 수 없도록 설정
        JScrollPane scrollPane = new JScrollPane(textArea); // 텍스트 영역을 스크롤 패널에 추가
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "정보", JOptionPane.PLAIN_MESSAGE); // 팝업 창에 스크롤 패널 표시
    }

    /* HyperlinkRenderer 클래스는 JTabledml cell renderer로 사용되며, 해당 셀의 내용을 하이퍼링크처럼 렌더링 */
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
                        // Invalid date format, handle accordingly (e.g., log the error, set default color, etc.)
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
