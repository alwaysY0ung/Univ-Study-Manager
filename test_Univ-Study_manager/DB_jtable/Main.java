import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("과제 & 강의 관리 시스템");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            AssignmentDb assignmentDbPanel = new AssignmentDb();
            ClassDb classDbPanel = new ClassDb();

            JTabbedPane mainTabbedPane = new JTabbedPane();
            mainTabbedPane.addTab("과제 관리", assignmentDbPanel);
            mainTabbedPane.addTab("강의 관리", classDbPanel);

            frame.add(mainTabbedPane, BorderLayout.CENTER);

            frame.setSize(900, 700); // 프레임 크기 설정
            frame.setLocationRelativeTo(null); // 화면 중앙에 배치
            frame.setVisible(true);
        });
    }
}
