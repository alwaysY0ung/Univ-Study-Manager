import javax.swing.*;

class NungyeolFrame extends JFrame {
    public NungyeolFrame() {
        setTitle("눈결이");
        NungyeolPanel panel = new NungyeolPanel();
        add(panel);
        setSize(400, 500);
        setVisible(true);
    }
}
