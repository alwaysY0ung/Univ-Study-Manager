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
    private static GradeCalculator GradeCalPanel;
    private static UserCalculator UserCalPanel;
    private static Setting settingPanel;
    
    private static JTabbedPane mainTabbedPane;
    

    public static void main(String[] args) {
        frame = new JFrame("Main Frame");
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 2)); // Split the frame into two columns

        // Initialize panels
        loginPanel = new LoginFormMain();
        asciiArtPanel = new ArrayASCIIArt();
        joinPanel = new JoinForm(loginPanel);
        infoPanel = new InformationForm(loginPanel, "");
        schedulePanel = new TodaySchedule();
        classPanel = new ClassDb();
        assignmentPanel = new AssignmentDb();
        //GradeCalPanel = new GradeCalculator();
        UserCalPanel = new UserCalculator();
        settingPanel = new Setting();


        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("오늘의 일정 & 과제", schedulePanel);
        mainTabbedPane.addTab("수업 DB", classPanel);
        mainTabbedPane.addTab("과제 DB", assignmentPanel);
        //mainTabbedPane.addTab("성적계산기", GradeCalPanel);
        mainTabbedPane.addTab("자율계산기", UserCalPanel);
        mainTabbedPane.addTab("설정", settingPanel);

        // Add login panel and ascii art panel to frame initially
        frame.add(loginPanel);
        frame.add(asciiArtPanel);

        frame.setVisible(true);
    }

    public static void showPanel(JPanel panel) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout()); // Reset layout to BorderLayout
        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public static void showLoginPanel() {
        frame.getContentPane().removeAll();
        frame.setLayout(new GridLayout(1, 2)); // Split the frame into two columns
        frame.add(loginPanel);
        frame.add(asciiArtPanel);
        frame.revalidate();
        frame.repaint();
    }

    public static void showMainTabbedPane() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        frame.add(mainTabbedPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
