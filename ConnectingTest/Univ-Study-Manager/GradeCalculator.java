import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeCalculator extends JPanel {
    private Map<Integer, List<SubjectGrade>> semesterGradesMap;
    private JTable gradeTable;
    private JTextArea semesterGPATextArea;
    private JPanel rightTopPanel; // rightTopPanel을 멤버 변수로 선언

    public GradeCalculator() {
        setLayout(new BorderLayout());

        // 상단 패널 생성
        JPanel topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);

        // 왼쪽 패널 생성
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);

        // 오른쪽 패널 생성
        JPanel rightPanel = new JPanel(new BorderLayout());

        // 오른쪽 위 패널 생성
        rightTopPanel = new JPanel(new BorderLayout());
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
        bottomSplitPane.setResizeWeight(0.01); // 좌측과 우측 패널의 크기를 1:1 비율로 설정
        bottomSplitPane.setDividerLocation(0.9);

        add(bottomSplitPane, BorderLayout.CENTER);

        // CSV 데이터를 HashMap에 로드
        semesterGradesMap = loadCSVToHashMap("grade_db.csv");
        // 테이블 생성 및 왼쪽 패널에 추가
        gradeTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(gradeTable);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // 오른쪽 위 패널에 학기별 평균 점수 표시
        semesterGPATextArea = new JTextArea();
        semesterGPATextArea.setEditable(false);
        JScrollPane scrollPane2 = new JScrollPane(semesterGPATextArea);
        rightTopPanel.setLayout(new BorderLayout());
        rightTopPanel.add(scrollPane2, BorderLayout.CENTER);

        // 오른쪽 위 패널에 꺾은선 그래프 추가
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                drawLineGraph(g2d);
            }
        };
        rightTopPanel.add(graphPanel, BorderLayout.CENTER);

        // 테이블에 데이터 설정
        updateGradeTable();
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
                    int credit = Integer.parseInt(data[2].trim());
                    String grade = data[3].trim();

                    SubjectGrade subjectGrade = new SubjectGrade(subjectName, credit, grade);
                    map.computeIfAbsent(semester, k -> new ArrayList<>()).add(subjectGrade);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void updateGradeTable() {
        String[] columnNames = {"학기", "과목명", "학점", "성적"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (Map.Entry<Integer, List<SubjectGrade>> entry : semesterGradesMap.entrySet()) {
            int semester = entry.getKey();
            List<SubjectGrade> subjectGrades = entry.getValue();

            for (SubjectGrade subjectGrade : subjectGrades) {
                Object[] rowData = {semester, subjectGrade.getSubjectName(), subjectGrade.getCredit(), subjectGrade.getGrade()};
                tableModel.addRow(rowData);
            }
        }

        gradeTable.setModel(tableModel);
        gradeTable.setFillsViewportHeight(true);
        //gradeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // 컬럼 크기 조정
        adjustColumnWidths(gradeTable);

        // 두 번째 컬럼을 제외한 나머지 컬럼 가운데 정렬
        alignColumns(gradeTable);

        // 학기별 평균 점수 계산 및 표시
        displaySemesterGPA();
    }

    
    private void adjustColumnWidths(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableColumnModel columnModel = table.getColumnModel();

        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 50; // 최소 컬럼 너비
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
    
    private void alignColumns(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    
        for (int column = 0; column < table.getColumnCount(); column++) {
            if (column != 1) {
                table.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
            }
        }
    }

    private void displaySemesterGPA() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<SubjectGrade>> entry : semesterGradesMap.entrySet()) {
            int semester = entry.getKey();
            List<SubjectGrade> subjectGrades = entry.getValue();

            double totalCredit = 0.0;
            double totalGradePoint = 0.0;

            for (SubjectGrade subjectGrade : subjectGrades) {
                int credit = subjectGrade.getCredit();
                double gradePoint = calculateGradePoint(subjectGrade.getGrade());

                totalCredit += credit;
                totalGradePoint += credit * gradePoint;
            }

            double semesterGPA = totalGradePoint / totalCredit;
            sb.append(semester).append("학기 평균 점수: ").append(String.format("%.2f", semesterGPA)).append("\n");
        }

        semesterGPATextArea.setText(sb.toString());
    }

    private double calculateGradePoint(String grade) {
        switch (grade) {
            case "A+":
                return 4.3;
            case "A0":
                return 4.0;
            case "A-":
                return 3.7;
            case "B+":
                return 3.3;
            case "B0":
                return 3.0;
            case "B-":
                return 2.7;
            case "C+":
                return 2.3;
            case "C0":
                return 2.0;
            case "C-":
                return 1.7;
            case "D+":
                return 1.3;
            case "D0":
                return 1.0;
            case "D-":
                return 0.7;
            default:
                return 0.0;
        }
    }

    private void drawLineGraph(Graphics2D g2d) {
        int width = rightTopPanel.getWidth();
        int height = rightTopPanel.getHeight();
        int padding = 20;
        int labelPadding = 25;
        double maxScore = 5.0;
        double minScore = 0.0;
    
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
    
        // 축 그리기
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding + labelPadding, height - padding, padding + labelPadding, padding);
        g2d.drawLine(padding + labelPadding, height - padding, width - padding, height - padding);
    
        // y축 눈금 그리기
        int numYTicks = 6;
        for (int i = 1; i < numYTicks; i++) {
            double score = minScore + (maxScore - minScore) * i / (numYTicks - 1);
            int y = (int) ((maxScore - score) / (maxScore - minScore) * (height - 2 * padding - labelPadding) + padding);
            g2d.drawLine(padding + labelPadding - 5, y, padding + labelPadding, y);
            if (i != numYTicks - 1) {
                g2d.drawString(String.format("%.0f", score), padding, y);
            }
        }
    
        // x축 눈금 그리기
        int numXTicks = 10;
        for (int i = 1; i < numXTicks; i++) {
            int x = (int) (((double) i / (numXTicks - 1)) * (width - 2 * padding - labelPadding) + padding + labelPadding);
            g2d.drawLine(x, height - padding, x, height - padding + 5);
            if (i != numXTicks - 1) {
                g2d.drawString(i + "학기", x - 10, height - padding + 20);
            }
            
        }
    
        // 축 레이블 그리기
        // g2d.drawString("학기", width / 2 - 10, height - 5);
        g2d.rotate(-Math.PI / 2);
        // g2d.drawString("평균 점수", -height / 2 + 10, padding);
        g2d.drawString("평균 점수", -height / 2 + 10, padding - 8);
        g2d.rotate(Math.PI / 2);
    
        // 데이터 포인트 계산
        java.util.List<Point> graphPoints = new ArrayList<>();
        java.util.List<Double> semesterGPAList = new ArrayList<>();
        for (Map.Entry<Integer, List<SubjectGrade>> entry : semesterGradesMap.entrySet()) {
            int semester = entry.getKey();
            List<SubjectGrade> subjectGrades = entry.getValue();

            double totalCredit = 0.0;
            double totalGradePoint = 0.0;

            for (SubjectGrade subjectGrade : subjectGrades) {
                int credit = subjectGrade.getCredit();
                double gradePoint = calculateGradePoint(subjectGrade.getGrade());

                totalCredit += credit;
                totalGradePoint += credit * gradePoint;
            }

            double semesterGPA = totalGradePoint / totalCredit;
            int x = (int) (((double) semester / (8)) * (width - 2 * padding - labelPadding) + padding + labelPadding);
            int y = (int) ((maxScore - semesterGPA) / (maxScore - minScore) * (height - 2 * padding - labelPadding) + padding);
            graphPoints.add(new Point(x, y));
            semesterGPAList.add(semesterGPA);
        }
    
        // 점 그리기
        g2d.setColor(Color.RED);
        for (Point point : graphPoints) {
            g2d.fillOval(point.x - 4, point.y - 4, 8, 8);
        }
    
        // 선 그리기
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2d.drawLine(x1, y1, x2, y2);
        }

        // 평균 점수 텍스트 그리기
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < graphPoints.size(); i++) {
            Point point = graphPoints.get(i);
            double semesterGPA = semesterGPAList.get(i);
            String text = String.format("%.2f", semesterGPA);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            int x = point.x - textWidth / 2;
            int y = point.y - 10;
            g2d.drawString(text, x, y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("성적 계산기");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(900, 700);

                GradeCalculator gradeCalculator = new GradeCalculator();
                frame.add(gradeCalculator);

                frame.setVisible(true);
            }
        });
    }
}

class SubjectGrade {
    private String subjectName;
    private int credit;
    private String grade;

    public SubjectGrade(String subjectName, int credit, String grade) {
        this.subjectName = subjectName;
        this.credit = credit;
        this.grade = grade;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getCredit() {
        return credit;
    }

    public String getGrade() {
        return grade;
    }
}
