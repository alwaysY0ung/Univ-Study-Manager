import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class InformationForm extends JPanel {

    private LoginFormMain owner;
    private UserMgr userMgr;
    private String userId;
    private JTextArea check;
    private JButton btnLogout;
    private JButton btnWithdraw;

    public InformationForm(LoginFormMain owner, String userId) {
        this.owner = owner;
        this.userId = userId;
        this.userMgr = owner.getUserMgr();

        init();
        setDisplay();
        addListeners();
    }

    private void init() {
        Dimension btnSize = new Dimension(100, 25);

        check = new JTextArea(10, 30);
        check.setEditable(false);

        btnLogout = new JButton("로그아웃");
        btnLogout.setPreferredSize(btnSize);

        btnWithdraw = new JButton("탈퇴하기");
        btnWithdraw.setPreferredSize(btnSize);
    }

    private void setDisplay() {
        TitledBorder border = new TitledBorder("안녕하세요! 본인의 정보를 확인 할 수 있습니다");
        check.setBorder(border);

        JPanel southPanel = new JPanel();
        southPanel.add(btnLogout);
        southPanel.add(btnWithdraw);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(check), BorderLayout.NORTH);
        mainPanel.add(southPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void addListeners() {
        btnWithdraw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (userId != null && !userId.isEmpty()) {
                    userMgr.withdraw(userId);
                    JOptionPane.showMessageDialog(InformationForm.this, "회원 정보가 삭제되었습니다\n다음에 또 만나요!", "BYE JAVA", JOptionPane.PLAIN_MESSAGE);
                    Main.showPanel(owner);
                } else {
                    System.out.println("Withdraw button clicked: userId is null or empty");
                }
            }
        });

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(InformationForm.this, "로그아웃 되었습니다\n다음에 또 만나요!", "BYE JAVA", JOptionPane.PLAIN_MESSAGE);
                Main.showPanel(owner);
            }
        });
    }

    public void setCheck(String userInfo) {
        check.setText(userInfo);
    }
}
