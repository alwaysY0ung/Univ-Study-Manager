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
        frame.setLayout(new GridLayout(1, 2)); // Split the frame into two columns

        // Initialize panels
        loginPanel = new LoginFormMain();
        asciiArtPanel = new ArrayASCIIArt();
        joinPanel = new JoinForm(loginPanel);
        infoPanel = new InformationForm(loginPanel, "");
        schedulePanel = new TodaySchedule();
        assignmentPanel = new AssignmentDb();
        classPanel = new ClassDb();

        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Today Schedule", schedulePanel);
        mainTabbedPane.addTab("Assignment Management", assignmentPanel);
        mainTabbedPane.addTab("Class Management", classPanel);

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
