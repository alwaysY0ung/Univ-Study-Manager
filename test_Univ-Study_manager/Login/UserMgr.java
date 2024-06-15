import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class UserMgr {
    private static final String FILE_PATH = "user.txt";
    private ArrayList<User> users;

    public UserMgr() {
        users = new ArrayList<>(); // 비어있는 ArrayList를 초기화
        loadTxt();
    }

    // 텍스트 파일에서 사용자 정보를 불러오는 메서드
    void loadTxt() {
        try {
            FileInputStream in = new FileInputStream(FILE_PATH);
            Scanner s = new Scanner(in);
            while (s.hasNext()) {
                String id = s.next();
                String pw = s.next();
                users.add(new User(id, pw));
            }
            s.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 사용자 추가 메서드
    public void addUser(User user) {
        users.add(user);
        System.out.println("신규등록이 완료되었습니다.");
        uploadTxt();
    }

    // 아이디 중복 여부를 확인하는 메서드
    public boolean isIdOverlap(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // 특정 사용자가 있는지 확인하는 메서드
    public boolean contains(User user) {
        return users.contains(user);
    }

    // 특정 아이디의 사용자를 조회하는 메서드
    public User getUser(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    // 주어진 아이디와 비밀번호가 일치하는지 확인하는 메서드
    public boolean chkMem(String id, String pw) {
        User user = getUser(id);
        return user != null && user.getPw().equals(pw);
    }

    // 특정 아이디의 사용자를 삭제하는 메서드
    public void withdraw(String id) {
        User user = getUser(id);
        if (user != null) {
            users.remove(user);
            System.out.println("회원정보가 성공적으로 삭제되었습니다.");
            uploadTxt();
        }
    }

    // ArrayList에 저장된 사용자 정보를 텍스트 파일에 업로드하는 메서드
    void uploadTxt() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH));
            for (User user : users) {
                writer.write(user.getId() + " " + user.getPw());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
