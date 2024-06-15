import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class Setting extends JPanel {
    private LoginFormMain owner;
    private UserMgr userMgr;
    private String userId;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JButton btnChangePassword;
    private JButton btnToggleTheme;
    private JButton btnLogout;
    private JButton btnWithdraw;
    private InformationForm informationForm;

    public Setting(LoginFormMain owner, String userId, InformationForm informationForm) {
        this.owner = owner;
        this.userId = userId;
        this.userMgr = owner.getUserMgr();
        this.informationForm = informationForm;

        init();
        setDisplay();
        addListeners();
    }

    private void init() {
        Dimension btnSize = new Dimension(120, 25);

        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);

        btnChangePassword = new JButton("비밀번호 변경");
        btnChangePassword.setPreferredSize(btnSize);

        btnToggleTheme = new JButton("테마 반전");
        btnToggleTheme.setPreferredSize(btnSize);

        btnLogout = new JButton("로그아웃");
        btnLogout.setPreferredSize(btnSize);

        btnWithdraw = new JButton("탈퇴하기");
        btnWithdraw.setPreferredSize(btnSize);
    }

    private void setDisplay() {
        JPanel passwordPanel = new JPanel();
        passwordPanel.add(new JLabel("현재 비밀번호:"));
        passwordPanel.add(currentPasswordField);
        passwordPanel.add(new JLabel("새 비밀번호:"));
        passwordPanel.add(newPasswordField);
        passwordPanel.add(btnChangePassword);

        JPanel themePanel = new JPanel();
        themePanel.add(btnToggleTheme);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnLogout);
        bottomPanel.add(btnWithdraw);

        setLayout(new BorderLayout());
        add(passwordPanel, BorderLayout.NORTH);
        add(themePanel, BorderLayout.CENTER);
        
        JPanel bottomCenterPanel = new JPanel(new BorderLayout());
        bottomCenterPanel.add(new JPanel(), BorderLayout.WEST);
        bottomCenterPanel.add(bottomPanel, BorderLayout.CENTER);
        bottomCenterPanel.add(new JPanel(), BorderLayout.EAST);

        add(bottomCenterPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        btnChangePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentPassword = new String(currentPasswordField.getPassword());
                String newPassword = new String(newPasswordField.getPassword());

                if (userMgr.changePassword(currentPassword, newPassword)) {
                    JOptionPane.showMessageDialog(Setting.this, "비밀번호가 성공적으로 변경되었습니다.", "비밀번호 변경", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(Setting.this, "비밀번호 변경에 실패하였습니다. 현재 비밀번호를 확인해주세요.", "비밀번호 변경 실패", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnToggleTheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color backgroundColor = getBackground();
                Color fontColor = getForeground();

                setBackground(fontColor);
                setForeground(backgroundColor);
            }
        });

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                informationForm.btnLogout.doClick();
            }
        });

        btnWithdraw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                informationForm.btnWithdraw.doClick();
            }
        });
    }
}