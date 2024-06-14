/*
프로그램 메인입니다.
크게 다음 단계로 나눌 수 있습니다.
1. 로그인 -> 2. 여러 개의 탭으로 이동할 수 있는 화면 -> 3. 종료 시 데이터 저장 후 종료

탭 추가 중 코드가 작성되지 않은 부분은 해당 Class.java가 아직 작성되지 않은 상태인 것입니다.
*/

import javax.swing.*;
import java.awt.*;

public class UnivStudyManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UnivStudyManagerGUI();
        });
    }

    private static void UnivStudyManagerGUI() {

        /*
        로그인 코드가 필요합니다.
        */

        // JFrame 설정
        JFrame frame = new JFrame("Univ Study Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        // JTabbedPane 생성
        JTabbedPane tabbedPane = new JTabbedPane();

        // // 오늘의 일정 & 과제
        // ClassDb ScheduleAndTodo = new ScheduleAndTodo();
        // tabbedPane.addTab("오늘의 일정 & 과제", classDb.getContentPane());

        // // ClassDb 탭 추가
        // ClassDb classDb = new ClassDb();
        // tabbedPane.addTab("수업 DB", classDb.getContentPane());

        // // AssignmentDb 탭 추가
        // AssignmentDb assignmentDb = new AssignmentDb();
        // tabbedPane.addTab("과제&시험 DB", assignmentDb.getContentPane());

        // '성적계산기' 탭 추가

        // Calculator 탭 추가

        // '캘린더' 탭 추가
        
        // '과거수업조회' 탭 추가

        // '클로드와 Q&A' 탭 추가

        // JTabbedPane을 JFrame에 추가
        frame.add(tabbedPane, BorderLayout.CENTER);

        // JFrame을 표시
        setSize(900, 700);
        frame.setVisible(true);
    }

    /*
    종료 시 데이터를 csv파일에 저장하는 코드가 필요합니다.
    */
}
