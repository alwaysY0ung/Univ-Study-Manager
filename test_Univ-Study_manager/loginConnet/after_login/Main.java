import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("공부잡았숙");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            TodaySchedule todaySchedulePanel = new TodaySchedule();
            ClassDb classDbPanel = new ClassDb();
            AssignmentDb assignmentDbPanel = new AssignmentDb();
            GradeCalculator gradeCalculatorPanel = new GradeCalculator();
            // UserCalculator userCalculatorPanel = new UserCalculator();
            // Setting settingPanel = new Setting();

            JTabbedPane mainTabbedPane = new JTabbedPane();
            mainTabbedPane.addTab("오늘의 일정", todaySchedulePanel);
            mainTabbedPane.addTab("강의 관리", classDbPanel);
            mainTabbedPane.addTab("과제 관리", assignmentDbPanel);
            mainTabbedPane.addTab("성적 계산기", gradeCalculatorPanel);
            // mainTabbedPane.addTab("사용자 계산기", userCalculatorPanel);
            // mainTabbedPane.addTab("설정", settingPanel);

            frame.add(mainTabbedPane, BorderLayout.CENTER);
            frame.setSize(900, 700); // 프레임 크기 설정
            frame.setLocationRelativeTo(null); // 화면 중앙에 배치
            frame.setVisible(true);
        });
    }
}