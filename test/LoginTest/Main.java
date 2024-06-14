import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class Main {
    private static JFrame frame;
    private static LoginFormMain loginPanel;
    private static JoinForm joinPanel;
    private static InformationForm infoPanel;
    private static ArrayASCIIArt asciiArtPanel;
    private static TodaySchedule schedulePanel;

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

        // Add panels to frame
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
}
