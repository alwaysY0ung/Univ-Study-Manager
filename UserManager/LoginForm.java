package login.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginForm extends JFrame {

    private UserMgr userMgr;
    private JLabel idLabel;
    private JLabel pwLabel;
    private JTextField idTxt;
    private JPasswordField pwTxt;
    private JButton logBtn;
    private JButton joinBtn;
    private LayoutManager flowLeft;

    public LoginForm() {
        userMgr = new UserMgr();

        init();
        setDisplay(); // UI 화면을 설정하는 메서드
        addListeners(); // 리스너를 추가하는 메서드
        showFrame(); // 프레임을 화면에 표시하는 메서드
    }

    public void init() {
        // 사이즈 통일
        Dimension labelSize = new Dimension(80, 30);
        int txtSize = 10;
        Dimension btnSize = new Dimension(100, 25);

        idLabel = new JLabel("아이디");
        idLabel.setPreferredSize(labelSize);
        pwLabel = new JLabel("암호");
        pwLabel.setPreferredSize(labelSize);

        idTxt = new JTextField(txtSize);
        pwTxt = new JPasswordField(txtSize);

        logBtn = new JButton("로그인");
        logBtn.setPreferredSize(btnSize);
        joinBtn = new JButton("회원가입");
        joinBtn.setPreferredSize(btnSize);

        flowLeft = new FlowLayout(FlowLayout.LEFT);
    }

    public UserMgr getUserMgr() {
        return userMgr;
    }

    public String getIdTxt() {
        return idTxt.getText();
    }

    // setDisplay() 메서드에서 UI 화면을 설정
    public void setDisplay() {
        // 컴포넌트를 왼쪽 정렬로 배치
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);

        JPanel northPanel = new JPanel(new GridLayout(0, 1));

        JPanel idPanel = new JPanel(flowLeft);
        idPanel.add(idLabel);
        idPanel.add(idTxt);

        JPanel pwPanel = new JPanel(flowLeft);
        pwPanel.add(pwLabel);
        pwPanel.add(pwTxt);

        northPanel.add(idPanel);
        northPanel.add(pwPanel);

        JPanel southPanel = new JPanel();
        southPanel.add(logBtn);
        southPanel.add(joinBtn);

        northPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        southPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        add(northPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    // addListeners() 메서드에서 GUI 컴포넌트의 이벤트 리스너를 등록
    public void addListeners() {
        // JOIN button 이벤트: 로그인 창을 숨기고 회원가입 창 표시
        joinBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new JoinForm(LoginForm.this);
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
                    JOptionPane.showMessageDialog(LoginForm.this, "아이디를 입력하세요", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                    // 이미 존재하는 ID일 경우
                } else if (userMgr.isIdOverlap(idTxt.getText())) {
                    // PW 입력하지 않았을 경우
                    if (String.valueOf(pwTxt.getPassword()).isEmpty()) {
                        JOptionPane.showMessageDialog(LoginForm.this, "암호를 입력하세요", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                        // PW 틀렸을 경우
                    } else if (!userMgr.chkMem(idTxt.getText(), String.valueOf(pwTxt.getPassword()))) {
                        JOptionPane.showMessageDialog(LoginForm.this, "암호가 일치하지 않습니다", "RETRY", JOptionPane.WARNING_MESSAGE);
                    } else {
                        InformationForm informationForm = new InformationForm(LoginForm.this, idTxt.getText());
                        informationForm.setCheck(userMgr.getUser(idTxt.getText()).toString());
                        setVisible(false);
                        informationForm.setVisible(true);
                        idTxt.setText("");
                        pwTxt.setText("");
                    }
                    // 존재하지 않는 ID일 경우
                } else {
                    JOptionPane.showMessageDialog(LoginForm.this, "존재하지 않는 ID 입니다", "JAVA LOGIN", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                int choice = JOptionPane.showConfirmDialog(
                        LoginForm.this, "다음에 또 만나요!", "BYE JAVA", JOptionPane.OK_CANCEL_OPTION);
                if (choice == JOptionPane.OK_OPTION) {
                    System.exit(0);
                }
            }
        });
    }


    public void showFrame() {
        setTitle("Welcome to JAVA");
        pack(); // 프레임의 크기를 컨텐츠에 맞게 조정
        setLocationRelativeTo(null); // 프레임을 화면 중앙에 위치
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // 프레임이 닫힐 때 아무 동작도 하지 않도록 설정
        setResizable(false); // 프레임의 크기 고정
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
