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

public class JoinForm extends JPanel {

    private LoginFormMain loginForm;
    private UserMgr userMgr;

    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel pwLabel;
    private JLabel reLabel;

    private JTextField idTxt;
    private JPasswordField pwTxt;
    private JPasswordField reTxt;

    private JButton joinBtn;
    private JButton cancelBtn;

    public JoinForm(LoginFormMain loginForm) {
        this.loginForm = loginForm;
        this.userMgr = loginForm.getUserMgr();

        init();
        setDisplay();
        addListeners();
    }

    private void init() {
        int txtSize = 10;

        Dimension labelSize = new Dimension(80, 30);
        Dimension btnSize = new Dimension(100, 25);

        titleLabel = new JLabel("- 개인 정보를 입력하세요 -", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));

        idLabel = new JLabel("아이디", JLabel.LEFT);
        idLabel.setPreferredSize(labelSize);

        pwLabel = new JLabel("암호", JLabel.LEFT);
        pwLabel.setPreferredSize(labelSize);

        reLabel = new JLabel("암호 재입력", JLabel.LEFT);
        reLabel.setPreferredSize(labelSize);

        idTxt = new JTextField(txtSize);
        pwTxt = new JPasswordField(txtSize);
        reTxt = new JPasswordField(txtSize);

        joinBtn = new JButton("JOIN");
        joinBtn.setPreferredSize(btnSize);
        cancelBtn = new JButton("CANCEL");
        cancelBtn.setPreferredSize(btnSize);
    }

    private void setDisplay() {
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
        add(reLabel, gbc);

        gbc.gridx = 1;
        add(reTxt, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(joinBtn, gbc);

        gbc.gridx = 1;
        add(cancelBtn, gbc);
    }

    private void addListeners() {
        // 취소 버튼 클릭 이벤트: 창을 닫고 LoginFormMain을 다시 보여줌
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Main.showLoginPanel();
            }
        });

        // 회원가입 버튼 이벤트: 사용자가 입력한 정보를 UserData 클래스에 저장하고 회원가입 완료 메시지 출력
        joinBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (isBlank()) {
                    JOptionPane.showMessageDialog(JoinForm.this, "모든 정보를 입력해주세요", "RETRY", JOptionPane.WARNING_MESSAGE);
                } else {
                    if (userMgr.isIdOverlap(idTxt.getText())) {
                        JOptionPane.showMessageDialog(JoinForm.this, "이미 존재하는 아이디입니다", "RETRY", JOptionPane.WARNING_MESSAGE);
                        idTxt.requestFocus();
                    } else if (!String.valueOf(pwTxt.getPassword()).equals(String.valueOf(reTxt.getPassword()))) {
                        JOptionPane.showMessageDialog(JoinForm.this, "암호가 일치하지 않습니다");
                        pwTxt.requestFocus();
                    } else {
                        userMgr.addUser(new User(idTxt.getText(), String.valueOf(pwTxt.getPassword())));
                        JOptionPane.showMessageDialog(JoinForm.this, "회원가입을 완료했습니다", "WELCOME", JOptionPane.PLAIN_MESSAGE);
                        Main.showLoginPanel();
                    }
                }
            }
        });
    }

    // 입력 필드 검증
    public boolean isBlank() {
        boolean result = false;
        if (idTxt.getText().isEmpty()) {
            idTxt.requestFocus();
            return true;
        }
        if (String.valueOf(pwTxt.getPassword()).isEmpty()) {
            pwTxt.requestFocus();
            return true;
        }
        if (String.valueOf(reTxt.getPassword()).isEmpty()) {
            reTxt.requestFocus();
            return true;
        }
        return result;
    }
}