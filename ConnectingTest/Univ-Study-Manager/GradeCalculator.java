import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeCalculator extends JFrame {
    private Map<Integer, List<SubjectGrade>> semesterGradesMap;

    public GradeCalculator() {
        setTitle("성적계산기");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널 생성
        JPanel topPanel = new JPanel();
        JButton gradeInputButton = new JButton("성적기입");
        topPanel.add(gradeInputButton);
        add(topPanel, BorderLayout.NORTH);

        // 왼쪽 패널 생성
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.LIGHT_GRAY);

        // 오른쪽 패널 생성
        JPanel rightPanel = new JPanel(new BorderLayout());

        // 오른쪽 위 패널 생성
        JPanel rightTopPanel = new JPanel();
        rightTopPanel.setBackground(Color.CYAN);

        // 오른쪽 아래 패널 생성
        GraphicsObject a = new GraphicsObject();
        JPanel rightBottomPanel = a.chartPanel;
        rightBottomPanel.setBackground(Color.white);

        // 오른쪽 패널을 위, 아래로 나누기
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTopPanel, rightBottomPanel);
        rightSplitPane.setResizeWeight(0.5); // 상단과 하단 패널의 크기를 1:1 비율로 설정
        rightSplitPane.setDividerLocation(0.5);

        rightPanel.add(rightSplitPane, BorderLayout.CENTER);

        // 아래 패널을 왼쪽, 오른쪽으로 나누기
        JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        bottomSplitPane.setResizeWeight(0.5); // 좌측과 우측 패널의 크기를 1:1 비율로 설정
        bottomSplitPane.setDividerLocation(0.5);

        add(bottomSplitPane, BorderLayout.CENTER);

        // CSV 데이터를 HashMap에 로드
        semesterGradesMap = loadCSVToHashMap("grade_db.csv");

        gradeInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createPopup();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createPopup() {
        JFrame popupFrame = new JFrame("성적 데이터 수정");
        popupFrame.setSize(300, 600);
        popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadHashMapDataToTable(semesterGradesMap, table);

        JButton saveButton = new JButton("저장");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCSVData("grade_db.csv", (DefaultTableModel) table.getModel());
            }
        });
        panel.add(saveButton, BorderLayout.SOUTH);

        popupFrame.add(panel);
        popupFrame.setVisible(true);
    }

    private Map<Integer, List<SubjectGrade>> loadCSVToHashMap(String filePath) {
        Map<Integer, List<SubjectGrade>> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    int semester = Integer.parseInt(data[0].trim());
                    String subjectName = data[1].trim();
                    String grade = data[2].trim();

                    SubjectGrade subjectGrade = new SubjectGrade(subjectName, grade);
                    map.computeIfAbsent(semester, k -> new ArrayList<>()).add(subjectGrade);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void loadHashMapDataToTable(Map<Integer, List<SubjectGrade>> map, JTable table) {
        String[] columns = {"학기", "과목명", "성적"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Map.Entry<Integer, List<SubjectGrade>> entry : map.entrySet()) {
            int semester = entry.getKey();
            for (SubjectGrade subjectGrade : entry.getValue()) {
                model.addRow(new Object[]{semester, subjectGrade.getSubjectName(), subjectGrade.getGrade()});
            }
        }
        table.setModel(model);
    }

    private void saveCSVData(String filePath, DefaultTableModel model) {
        try (FileWriter fw = new FileWriter(filePath)) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                fw.write(model.getColumnName(i) + (i == model.getColumnCount() - 1 ? "\n" : ","));
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    fw.write(model.getValueAt(i, j) + (j == model.getColumnCount() - 1 ? "\n" : ","));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GradeCalculator();
            }
        });
    }
}

class SubjectGrade {
    private String subjectName;
    private String grade;

    public SubjectGrade(String subjectName, String grade) {
        this.subjectName = subjectName;
        this.grade = grade;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getGrade() {
        return grade;
    }
}