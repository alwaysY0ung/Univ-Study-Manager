import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFormMain extends JPanel {

    private UserMgr userMgr;
    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel pwLabel;
    private JTextField idTxt;
    private JPasswordField pwTxt;
    private JButton logBtn;
    private JButton joinBtn;

    public LoginFormMain() {
        userMgr = new UserMgr();

        init();
        setDisplay(); // UI 화면을 설정하는 메서드
        addListeners(); // 리스너를 추가하는 메서드
    }

    public void init() {
        // 사이즈 통일
        Dimension labelSize = new Dimension(80, 30);
        int txtSize = 10;
        Dimension btnSize = new Dimension(100, 25);

        titleLabel = new JLabel("Login & Signup");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));

        idLabel = new JLabel("아이디:");
        idLabel.setPreferredSize(labelSize);
        pwLabel = new JLabel("암호:");
        pwLabel.setPreferredSize(labelSize);

        idTxt = new JTextField(txtSize);
        pwTxt = new JPasswordField(txtSize);

        logBtn = new JButton("로그인");
        logBtn.setPreferredSize(btnSize);
        joinBtn = new JButton("회원가입");
        joinBtn.setPreferredSize(btnSize);
    }

    public UserMgr getUserMgr() {
        return userMgr;
    }

    public String getIdTxt() {
        return idTxt.getText();
    }

    // setDisplay() 메서드에서 UI 화면을 설정
    public void setDisplay() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        add(idLabel, gbc);

        gbc.gridx = 1;
        add(idTxt, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(pwLabel, gbc);

        gbc.gridx = 1;
        add(pwTxt, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(logBtn, gbc);

        gbc.gridx = 1;
        add(joinBtn, gbc);
    }

    // addListeners() 메서드에서 GUI 컴포넌트의 이벤트 리스너를 등록
    public void addListeners() {
        // JOIN button 이벤트: 로그인 창을 숨기고 회원가입 창 표시
        joinBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.showPanel(new JoinForm(LoginFormMain.this));
                idTxt.setText("");
                pwTxt.setText("");
            }
        });

        // LOGIN button 이벤트: 사용자 입력 값을 검증하고 로그인 성공 시 정보창을 출력
        logBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ID 입력하지 않았을 경우
                if (idTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFormMain.this, "아이디를 입력하세요", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                    // 이미 존재하는 ID일 경우
                } else if (userMgr.isIdOverlap(idTxt.getText())) {
                    // PW 입력하지 않았을 경우
                    if (String.valueOf(pwTxt.getPassword()).isEmpty()) {
                        JOptionPane.showMessageDialog(LoginFormMain.this, "암호를 입력하세요", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                        // PW 틀렸을 경우
                    } else if (!userMgr.chkMem(idTxt.getText(), String.valueOf(pwTxt.getPassword()))) {
                        JOptionPane.showMessageDialog(LoginFormMain.this, "암호가 일치하지 않습니다", "RETRY", JOptionPane.WARNING_MESSAGE);
                    } else {
                        Main.showMainTabbedPane();
                        idTxt.setText("");
                        pwTxt.setText("");
                    }
                    // 존재하지 않는 ID일 경우
                } else {
                    JOptionPane.showMessageDialog(LoginFormMain.this, "존재하지 않는 ID 입니다", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
}
