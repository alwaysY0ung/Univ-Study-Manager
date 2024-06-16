import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.io.IOException;

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
    private static GraphicsObject graphicsPanel;
    private static Thread watchThread;

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
        int selectedTabIndex = (mainTabbedPane != null) ? mainTabbedPane.getSelectedIndex() : 0;
        int selectedAssignmentTabIndex = (assignmentPanel != null) ? assignmentPanel.getSelectedTabIndex() : 0;
        int selectedClassTabIndex = (classPanel != null) ? classPanel.getSelectedTabIndex() : 0;

        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        schedulePanel = new TodaySchedule(userId, semester);
        assignmentPanel = new AssignmentDb(userId, semester);
        classPanel = new ClassDb(userId, semester);
        gradeCalculatorPanel = new GradeCalculator(userId);
        userCalculatorPanel = new UserCalculator();
        graphicsPanel = new GraphicsObject(userId);
        settingPanel = new Setting();

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Today 일정 & 과제", schedulePanel);
        mainTabbedPane.addTab("수업 DB", classPanel);
        mainTabbedPane.addTab("과제 DB", assignmentPanel);
        mainTabbedPane.addTab("성적 계산기", gradeCalculatorPanel);
        mainTabbedPane.addTab("계산기", userCalculatorPanel);
        mainTabbedPane.addTab("설정", settingPanel);

        frame.add(mainTabbedPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();

        // Restore the selected tabs
        mainTabbedPane.setSelectedIndex(selectedTabIndex);
        assignmentPanel.setSelectedTabIndex(selectedAssignmentTabIndex);
        classPanel.setSelectedTabIndex(selectedClassTabIndex);

        // Start the CSV watch thread
        startWatchThread(userId, semester);
    }

    private static void startWatchThread(String userId, String semester) {
        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
        }

        watchThread = new Thread(() -> {
            try {
                String year = semester.split("-")[0];
                String sem = semester.split("-")[1].substring(0, 1);
                Path assignmentPath = Paths.get(userId + "_" + year + "_" + sem + "_assignment_db.csv").toAbsolutePath();
                Path classPath = Paths.get(userId + "_" + year + "_" + sem + "_class_db.csv").toAbsolutePath();
                Path gradePath = Paths.get(userId + "_GradeDB.csv").toAbsolutePath();

                WatchService watchService = FileSystems.getDefault().newWatchService();
                gradePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.endsWith(assignmentPath.getFileName()) || changed.endsWith(classPath.getFileName()) || changed.endsWith(gradePath.getFileName())) {
                            // Update the panels
                            SwingUtilities.invokeLater(() -> {
                                int selectedTabIndex = mainTabbedPane.getSelectedIndex();
                                int selectedAssignmentTabIndex = assignmentPanel.getSelectedTabIndex();
                                int selectedClassTabIndex = classPanel.getSelectedTabIndex();

                                frame.remove(mainTabbedPane);
                                showMainTabbedPane(userId, semester);

                                mainTabbedPane.setSelectedIndex(selectedTabIndex);
                                assignmentPanel.setSelectedTabIndex(selectedAssignmentTabIndex);
                                classPanel.setSelectedTabIndex(selectedClassTabIndex);

                                // Update GraphicsObject
                                graphicsPanel = new GraphicsObject(userId);
                                mainTabbedPane.setComponentAt(mainTabbedPane.indexOfTab("그래프"), graphicsPanel);
                            });
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        watchThread.start();
    }
}
