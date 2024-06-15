import java.awt.*;
import javax.swing.*;

public class NungyeolPanel extends JPanel {
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawNungyeol(g);
    }

    public void drawNungyeol(Graphics g) {
        // 눈결이 동그란 몸 2개
        g.setColor(Color.WHITE);
        g.fillOval(100, 50, 200, 200); // 머리
        g.fillOval(75, 200, 250, 250); // 몸

        // 양동이
        g.setColor(Color.GRAY);
        int[] xp = {250, 270, 260, 220};
        int[] yp = {35, 45, 90, 70};
        Polygon bucket = new Polygon(xp, yp, 4);
        g.fillPolygon(bucket);

        // 목도리
        g.setColor(Color.BLUE);
        g.fillRect(130, 220, 140, 20);
        g.fillRect(150, 240, 40, 60);

        // 단추
        g.setColor(Color.BLUE);
        g.fillOval(190, 290, 20, 20);
        g.fillOval(190, 330, 20, 20);

        // 눈
        g.setColor(Color.BLUE);
        g.fillOval(160, 100, 10, 10);
        g.fillOval(230, 100, 10, 10);

        // 입
        g.setColor(Color.BLACK);
        g.drawArc(170, 140, 60, 20, 0, -180);

        // 눈썹
        g.setColor(new Color(185, 226, 241)); // 하늘색 지정
        g.drawArc(160, 85, 20, 10, 0, 180);
        g.drawArc(230, 85, 20, 10, 0, 180);

        // 볼터치
        g.setColor(new Color(185, 226, 241)); // 하늘색 지정
        g.fillOval(140, 120, 20, 10);
        g.fillOval(240, 120, 20, 10);
    }
}
