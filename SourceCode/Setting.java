import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Setting extends JPanel {
    private JTextField idField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JButton changeButton;
    private JButton lightModeButton;
    private JButton darkModeButton;
    private JButton logoutButton;
    private JButton withdrawButton;
    private boolean isDarkMode = false;

    public Setting() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 아이디, 현재 비밀번호, 새 비밀번호 입력 패널
        JPanel inputPanel = new JPanel(new GridLayout(1, 6));
        inputPanel.add(new JLabel("아이디:"));
        idField = new JTextField(10);
        inputPanel.add(idField);
        inputPanel.add(new JLabel("현재 비밀번호:"));
        currentPasswordField = new JPasswordField(10);
        inputPanel.add(currentPasswordField);
        inputPanel.add(new JLabel("새 비밀번호:"));
        newPasswordField = new JPasswordField(10);
        inputPanel.add(newPasswordField);
        changeButton = new JButton("변경");
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
        inputPanel.add(changeButton);
        add(inputPanel);

        // 테마 변경 패널
        JPanel themePanel = new JPanel();
        themePanel.add(new JLabel("테마 변경:"));
        lightModeButton = new JButton("Light Mode");
        lightModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLightMode();
            }
        });
        themePanel.add(lightModeButton);
        darkModeButton = new JButton("Dark Mode");
        darkModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDarkMode();
            }
        });
        themePanel.add(darkModeButton);
        add(themePanel);

        // 로그아웃, 탈퇴 버튼 패널
        JPanel buttonPanel = new JPanel();
        logoutButton = new JButton("로그아웃");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        buttonPanel.add(logoutButton);
        withdrawButton = new JButton("탈퇴");
        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                withdraw();
            }
        });
        buttonPanel.add(withdrawButton);
        add(buttonPanel);
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

    // Light Mode로 설정
    private void setLightMode() {
        isDarkMode = false;
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
    }

    // Dark Mode로 설정
    private void setDarkMode() {
        isDarkMode = true;
        setBackground(Color.DARK_GRAY);
        setForeground(Color.WHITE);
    }

    // 로그아웃 메서드
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION) {
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