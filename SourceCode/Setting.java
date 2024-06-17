import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Setting extends JPanel {
    private JTextField idField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JButton changeButton;
    private JButton logoutButton;
    private JButton withdrawButton;

    public Setting() {
        init();
        setDisplay();
        addListeners();
    }

    private void init() {
        int txtSize = 10;

        Dimension labelSize = new Dimension(100, 30);
        Dimension btnSize = new Dimension(120, 30);

        idField = new JTextField(txtSize);
        currentPasswordField = new JPasswordField(txtSize);
        newPasswordField = new JPasswordField(txtSize);

        changeButton = new JButton("변경");
        changeButton.setPreferredSize(btnSize);
        logoutButton = new JButton("로그아웃");
        logoutButton.setPreferredSize(btnSize);
        withdrawButton = new JButton("탈퇴");
        withdrawButton.setPreferredSize(btnSize);
    }

    private void setDisplay() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("- 설정 -", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("아이디:", JLabel.RIGHT), gbc);

        gbc.gridx = 1;
        add(idField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("현재 비밀번호:", JLabel.RIGHT), gbc);

        gbc.gridx = 1;
        add(currentPasswordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("새 비밀번호:", JLabel.RIGHT), gbc);

        gbc.gridx = 1;
        add(newPasswordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(changeButton, gbc);

        gbc.gridx = 1;
        add(logoutButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(withdrawButton, gbc);
    }

    private void addListeners() {
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                withdraw();
            }
        });
    }

    // 비밀번호 변경 메서드
    private void changePassword() {
        String id = idField.getText();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());

        UserMgr userMgr = new UserMgr();
        User user = userMgr.getUser(id);

        if (user != null && user.getPw().equals(currentPassword)) {
            user.setPw(newPassword);
            userMgr.uploadTxt();
            JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.", "비밀번호 변경 성공", JOptionPane.INFORMATION_MESSAGE);
            logout(); // 비밀 번호 변경 후, 로그아웃 처리
        } else {
            JOptionPane.showMessageDialog(this, "현재 비밀번호가 일치하지 않습니다.", "비밀번호 변경 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    // 로그아웃 메서드
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Main.showLoginPanel(); // 로그아웃 처리
            JOptionPane.showMessageDialog(this, "로그아웃되었습니다.", "로그아웃 성공", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 탈퇴 메서드
    private void withdraw() {
        String id = JOptionPane.showInputDialog(this, "탈퇴할 아이디를 입력하세요:");
        if (id == null || id.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디를 입력해주세요.", "탈퇴 실패", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String password = JOptionPane.showInputDialog(this, "비밀번호를 입력하세요:");
        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "비밀번호를 입력해주세요.", "탈퇴 실패", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserMgr userMgr = new UserMgr();
        User user = userMgr.getUser(id);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "해당 아이디의 사용자가 존재하지 않습니다.", "탈퇴 실패", JOptionPane.WARNING_MESSAGE);
        } else if (!user.getPw().equals(password)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.", "탈퇴 실패", JOptionPane.WARNING_MESSAGE);
        } else {
            userMgr.withdraw(id);
            JOptionPane.showMessageDialog(this, "탈퇴가 완료되었습니다.", "탈퇴 성공", JOptionPane.INFORMATION_MESSAGE);
            // 탈퇴 후 로그인 화면으로 이동하는 코드 추가
            // 예: LoginFormMain.showLoginForm();
        }
    }
}
