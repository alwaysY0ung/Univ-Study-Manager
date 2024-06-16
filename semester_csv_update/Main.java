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

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Today Schedule", schedulePanel);
        mainTabbedPane.addTab("Assignment Management", assignmentPanel);
        mainTabbedPane.addTab("Class Management", classPanel);

        frame.add(mainTabbedPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
