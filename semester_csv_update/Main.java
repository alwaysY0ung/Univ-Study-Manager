import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class Main {
    private static JFrame frame;
    private static LoginFormMain loginPanel;
    private static JoinForm joinPanel;
    private static InformationForm infoPanel;
    private static ArrayASCIIArt asciiArtPanel;
    private static TodaySchedule schedulePanel;
    private static AssignmentDb assignmentPanel;
    private static ClassDb classPanel;
    private static GradeCalculator gradeCalculatorPanel;
    private static UserCalculator userCalculatorPanel;
    private static Setting settingPanel;
    private static JTabbedPane mainTabbedPane;

    public static void main(String[] args) {
        frame = new JFrame("Main Frame");
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2));

        loginPanel = new LoginFormMain();
        asciiArtPanel = new ArrayASCIIArt();
        joinPanel = new JoinForm(loginPanel);
        infoPanel = new InformationForm(loginPanel, "");

        frame.add(loginPanel);
        frame.add(asciiArtPanel);

        frame.setVisible(true);
    }

    public static void showPanel(JPanel panel) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public static void showLoginPanel() {
        frame.getContentPane().removeAll();
        frame.setLayout(new GridLayout(1, 2));
        frame.add(loginPanel);
        frame.add(asciiArtPanel);
        frame.revalidate();
        frame.repaint();
    }

    public static void showMainTabbedPane(String userId, String semester) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        schedulePanel = new TodaySchedule(userId, semester);
        assignmentPanel = new AssignmentDb(userId, semester);
        classPanel = new ClassDb(userId, semester);
        gradeCalculatorPanel = new GradeCalculator(userId);
        userCalculatorPanel = new UserCalculator();
        settingPanel = new Setting();

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Today 일정 & 과제", schedulePanel);
        mainTabbedPane.addTab("수업 DB", assignmentPanel);
        mainTabbedPane.addTab("과제 DB", classPanel);
        mainTabbedPane.addTab("성적 계산기", gradeCalculatorPanel);
        mainTabbedPane.addTab("계산기", userCalculatorPanel);
        mainTabbedPane.addTab("설정", settingPanel);

        frame.add(mainTabbedPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
