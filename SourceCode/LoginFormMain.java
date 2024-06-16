import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
    private JComboBox<String> semesterComboBox;

    public LoginFormMain() {
        userMgr = new UserMgr();

        init();
        setDisplay();
        addListeners();
    }

    public void init() {
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

        semesterComboBox = new JComboBox<>(new String[]{
                "1-1학기", "1-2학기", "2-1학기", "2-2학기", "3-1학기", "3-2학기", "4-1학기", "4-2학기"
        });
        semesterComboBox.setPreferredSize(new Dimension(120, 25));
    }

    public UserMgr getUserMgr() {
        return userMgr;
    }

    public String getIdTxt() {
        return idTxt.getText();
    }

    public String getSelectedSemester() {
        return (String) semesterComboBox.getSelectedItem();
    }

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
        add(new JLabel("학기:"), gbc);

        gbc.gridx = 1;
        add(semesterComboBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        add(logBtn, gbc);

        gbc.gridx = 1;
        add(joinBtn, gbc);
    }

    public void addListeners() {
        joinBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.showPanel(new JoinForm(LoginFormMain.this));
                idTxt.setText("");
                pwTxt.setText("");
            }
        });

        logBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (idTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFormMain.this, "아이디를 입력하세요", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                } else if (userMgr.isIdOverlap(idTxt.getText())) {
                    if (String.valueOf(pwTxt.getPassword()).isEmpty()) {
                        JOptionPane.showMessageDialog(LoginFormMain.this, "암호를 입력하세요", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                    } else if (!userMgr.chkMem(idTxt.getText(), String.valueOf(pwTxt.getPassword()))) {
                        JOptionPane.showMessageDialog(LoginFormMain.this, "암호가 일치하지 않습니다", "RETRY", JOptionPane.WARNING_MESSAGE);
                    } else {
                        Main.showMainTabbedPane(idTxt.getText(), getSelectedSemester());
                        idTxt.setText("");
                        pwTxt.setText("");
                    }
                } else {
                    JOptionPane.showMessageDialog(LoginFormMain.this, "존재하지 않는 ID 입니다", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
}
