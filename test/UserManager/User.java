// https://velog.io/@eg_log/Java-%EB%A1%9C%EA%B7%B8%EC%9D%B8%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85-%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%A8Swing-GUI#3-user 참고
// 2024.5.24 made by 정소윤, 유은서

package login.project;

public class User {
    private String id;
    private String pw;

    // 아이디만 받아들이는 생성자
    public User(String id) {
        this.id = id;
    }

    // 모든 사용자 정보를 받아들이는 생성자
    public User(String id, String pw) {
        this.id = id;
        this.pw = pw;
    }

    public String getId() {
        return id;
    }

    public String getPw() {
        return pw;
    }

    @Override
    // 다른 User 객체와의 아이디 일치 여부를 확인하는 메서드
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User user = (User) obj;
            return id.equals(user.id);
        }
        return false;
    }

    @Override
    // User 객체의 정보를 string으로 반환하는 메서드
    public String toString() {
        return "ID: " + id;
    }
}
