import java.awt.*;
import javax.swing.*;

public class NungyeolPanel extends JPanel {
    private int totalAssignments;
    private int completedAssignments;

    public NungyeolPanel() {
        setPreferredSize(new Dimension(200, 200)); // 크기 설정
    }

    public void updateState(int totalAssignments, int completedAssignments) {
        this.totalAssignments = totalAssignments;
        this.completedAssignments = completedAssignments;
        repaint(); // 다시 그리기 요청
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        double completionRate = (double) completedAssignments / totalAssignments;

        // 전체 몸
        g.setColor(Color.WHITE);
        g.fillOval(50, 25, 100, 100); // 머리
        g.fillOval(37, 100, 125, 125); // 몸

        // 양동이
        g.setColor(Color.GRAY);
        int[] xp = {125, 135, 130, 110};
        int[] yp = {17, 22, 45, 35};
        Polygon bucket = new Polygon(xp, yp, 4);
        g.fillPolygon(bucket);

        // 목도리
        g.setColor(Color.BLUE);
        g.fillRect(65, 110, 70, 10);
        g.fillRect(75, 120, 20, 30);

        // 단추
        g.setColor(Color.BLUE);
        g.fillOval(95, 145, 10, 10);
        g.fillOval(95, 165, 10, 10);

        // 눈
        g.setColor(Color.BLUE);
        g.fillOval(80, 50, 5, 5);
        g.fillOval(115, 50, 5, 5);

        if (completionRate == 0) {
            drawCase1(g);
        }
        else if (completionRate < 1) {
            drawCase2(g);
        }
        else {
            drawCase3(g);
        }
    }

    private void drawCase1 (Graphics g) {
        // 무표정 입
        g.setColor(Color.BLACK);
        g.drawLine(85, 70, 115, 70);
    }

    private void drawCase2 (Graphics g) {
        // 웃는 입
        g.setColor(Color.BLACK);
        g.drawArc(85, 70, 30, 10, 0, -180);
    }

    private void drawCase3 (Graphics g) {
        // 입
        g.setColor(Color.BLACK);
        g.drawArc(85, 70, 30, 10, 0, -180);

        // 눈썹
        g.setColor(new Color(185, 226, 241)); // 하늘색 지정
        g.drawArc(80, 42, 10, 5, 0, 180);
        g.drawArc(115, 42, 10, 5, 0, 180);

        // 볼터치
        g.setColor(new Color(185, 226, 241)); // 하늘색 지정
        g.fillOval(70, 60, 10, 5);
        g.fillOval(120, 60, 10, 5);
    }
}
