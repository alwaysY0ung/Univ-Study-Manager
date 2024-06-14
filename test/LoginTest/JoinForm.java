import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

// Java Swing 라이브러리를 사용하여 회원가입 창(JoinForm)을 구현
public class JoinForm extends JPanel {

    private LoginFormMain loginForm;
    private UserMgr userMgr;

    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel pwLabel;
    private JLabel reLabel;
    private JLabel nameLabel;
    private JLabel nickNameLabel;

    private JTextField idTxt;
    private JPasswordField pwTxt;
    private JPasswordField reTxt;
    private JTextField nameTextField;
    private JTextField nickNameTextField;

    private JButton joinBtn;
    private JButton cancelBtn;
    private JPanel mainCPanel;

    // JoinForm 클래스는 LoginFormMain 클래스를 상속받아 만든 대화상자 형태의 GUI 창
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

        idLabel = new JLabel("아이디", JLabel.LEFT);
        idLabel.setPreferredSize(labelSize);

        pwLabel = new JLabel("암호", JLabel.LEFT);
        pwLabel.setPreferredSize(labelSize);

        reLabel = new JLabel("암호 재입력", JLabel.LEFT);
        reLabel.setPreferredSize(labelSize);

        nameLabel = new JLabel("이름", JLabel.LEFT);
        nameLabel.setPreferredSize(labelSize);

        nickNameLabel = new JLabel("닉네임", JLabel.LEFT);
        nickNameLabel.setPreferredSize(labelSize);

        idTxt = new JTextField(txtSize);
        pwTxt = new JPasswordField(txtSize);
        reTxt = new JPasswordField(txtSize);
        nameTextField = new JTextField(txtSize);
        nickNameTextField = new JTextField(txtSize);

        joinBtn = new JButton("JOIN");
        joinBtn.setPreferredSize(btnSize);
        cancelBtn = new JButton("CANCEL");
        cancelBtn.setPreferredSize(btnSize);

        // 레이아웃의 열과 행을 설정, 0은 열의 값을 제한하지 않는다
        mainCPanel = new JPanel(new GridLayout(0, 1));
    }

    // setDisplay 메소드는 GUI 창을 구성하는 레이아웃을 설정하는 메소드
    private void setDisplay() {
        FlowLayout flowLeft = new FlowLayout(FlowLayout.LEFT);

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        northPanel.add(titleLabel);

        JPanel idPanel = new JPanel(flowLeft);
        idPanel.add(idLabel);
        idPanel.add(idTxt);

        JPanel pwPanel = new JPanel(flowLeft);
        pwPanel.add(pwLabel);
        pwPanel.add(pwTxt);

        JPanel rePanel = new JPanel(flowLeft);
        rePanel.add(reLabel);
        rePanel.add(reTxt);

        JPanel namePanel = new JPanel(flowLeft);
        namePanel.add(nameLabel);
        namePanel.add(nameTextField);

        JPanel nickNamePanel = new JPanel(flowLeft);
        nickNamePanel.add(nickNameLabel);
        nickNamePanel.add(nickNameTextField);

        // mainCPanel에 위에서 설정한 레이아웃을 적용
        mainCPanel.add(idPanel);
        mainCPanel.add(pwPanel);
        mainCPanel.add(rePanel);
        mainCPanel.add(namePanel);
        mainCPanel.add(nickNamePanel);

        JPanel southPanel = new JPanel();
        southPanel.add(joinBtn);
        southPanel.add(cancelBtn);

        mainCPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        southPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // mainCPanel에 northPanel, smsPanel, southPanel을 추가하여 전체 레이아웃을 구성
        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(mainCPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    // 이벤트 처리: addListeners 메소드는 GUI 창의 버튼을 처리하는 리스너를 등록
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
        if (nameTextField.getText().isEmpty()) {
            nameTextField.requestFocus();
            return true;
        }
        if (nickNameTextField.getText().isEmpty()) {
            nickNameTextField.requestFocus();
            return true;
        }
        return result;
    }
}
