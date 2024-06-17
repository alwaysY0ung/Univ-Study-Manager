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
        users = new ArrayList<>();
        loadTxt();
    }

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

    public void addUser(User user) {
        users.add(user);
        System.out.println("신규등록이 완료되었습니다.");
        createCsvFiles(user.getId());
        uploadTxt();
    }

    private void createCsvFiles(String userId) {
        try {
            for (int year = 1; year <= 4; year++) {
                for (int semester = 1; semester <= 2; semester++) {
                    String assignmentFileName = userId + "_" + year + "_" + semester + "_assignment_db.csv";
                    String classFileName = userId + "_" + year + "_" + semester + "_class_db.csv";
                    createCsvFile(assignmentFileName, "완료,과제명,마감일,관련수업,과제종류,성적비율,환산점수,과제만점,내 점수,관련파일,관련URL,정보\n");
                    createCsvFile(classFileName, "강의명,주차,예고된 강의 내용,복습,과제/시험,음성기록,관련파일,To-Do,완료여부,수업일\n");
                }
            }
            createCsvFile(userId + "_GradeDB.csv", "학기,과목명,수강학점,성적\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCsvFile(String fileName, String header) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(header);
        }
    }

    public boolean isIdOverlap(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(User user) {
        return users.contains(user);
    }

    public User getUser(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public boolean chkMem(String id, String pw) {
        User user = getUser(id);
        return user != null && user.getPw().equals(pw);
    }

    public void withdraw(String id) {
        User user = getUser(id);
        if (user != null) {
            users.remove(user);
            System.out.println("회원정보가 성공적으로 삭제되었습니다.");
            uploadTxt();
        }
    }

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

    public boolean changePassword(String currentPassword, String newPassword) {
        User user = getUserByPassword(currentPassword);
        if (user != null) {
            user.setPw(newPassword);
            System.out.println("비밀번호가 성공적으로 변경되었습니다.");
            uploadTxt();
            return true;
        } else {
            System.out.println("현재 비밀번호가 일치하는 사용자가 없습니다.");
            return false;
        }
    }

    private User getUserByPassword(String password) {
        for (User user : users) {
            if (user.getPw().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
