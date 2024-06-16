import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphicsObject extends JPanel {
    int[] arcAngle = new int[10];

    Color[] color = {Color.RED, Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.GREEN, Color.lightGray, Color.black, Color.CYAN, Color.GRAY};

    String[] itemName = {"A+", "A0", "A-", "B+", "B0", "B-", "C+", "C0", "C-", "P"}; // 비교대상

    ChartPanel chartPanel = new ChartPanel(); // 차트판넬
    String csvFilePath; // CSV 파일 경로
    String[] grades;

    public GraphicsObject(String userId) { // 생성자
        this.csvFilePath = userId + "_GradeDB.csv";
        this.grades = extractGradesFromCSV(csvFilePath);
        setVisible(true);
        drawChart(); // 차트 메소드 호출
    }

    public static String[] extractGradesFromCSV(String csvFilePath) {
        List<String> gradeList = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            reader.readLine(); // 첫 번째 헤더 라인을 건너뜀
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length == 4) {
                    String grade = fields[3].trim();
                    gradeList.add(grade);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // List를 배열로 변환
        return gradeList.toArray(new String[0]);
    }

    public static Map<String, Integer> countOccurrences(String[] array) {
        Map<String, Integer> countMap = new HashMap<>();

        // 각 문자열의 개수 세기
        for (String str : array) {
            countMap.put(str, countMap.getOrDefault(str, 0) + 1);
        }

        return countMap;
    }

    public static int getIndexForString(String str) {
        // 문자열에 따라 저장할 인덱스를 지정
        switch (str) {
            case "A+":
                return 0;
            case "A0":
                return 1;
            case "A-":
                return 2;
            case "B+":
                return 3;
            case "B0":
                return 4;
            case "B-":
                return 5;
            case "C+":
                return 6;
            case "C0":
                return 7;
            case "C-":
                return 8;
            default:
                return 9; // P를 위한 인덱스
        }
    }

    // 결과를 배열에 저장
    int[] counts = new int[10];

    void drawChart() { // 차트를 그린다

        // 각 문자열의 개수 세기
        Map<String, Integer> countMap = countOccurrences(grades);

        // 각 문자열에 따라 저장할 인덱스를 지정
        /*
        for (int i = 0; i < grades.length; i++) {
            String str = grades[i];
            int index = getIndexForString(str);
            counts[index] = countMap.get(str);
        }
        */
        for (String str : countMap.keySet()) {
            int index = getIndexForString(str);
            counts[index] = countMap.get(str);
        }


        int sum = 0; // 초기값 0
        for (int value : counts) { // 데이터 값만큼 루프
            sum += value;
        }
        if (sum == 0)
            return;

        for (int i = 0; i < counts.length; i++) {
            arcAngle[i] = (int) Math.round((double) counts[i] / (double) sum * 360);
        }
        chartPanel.repaint(); // 차트패널의 PAINT호출
    }

    class ChartPanel extends JPanel { // 차트 표시 패널
        public void paintComponent(Graphics g) {
            super.paintComponent(g); // 부모 PAINT 호출

            int startAngle = 0;
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));

            for (int i = 0; i < counts.length; i++) {
                g.setColor(color[i]);
                g.drawString(itemName[i] + " " + Math.round(arcAngle[i] * 100 / 360) + "%", 30, 60+ i*20);
            }

            for (int i = 0; i < counts.length; i++) {
                g.setColor(color[i]);
                g.fillArc(100, 50, 200, 200, startAngle, arcAngle[i]);
                startAngle = startAngle + arcAngle[i];
            }
        }
    }
}
