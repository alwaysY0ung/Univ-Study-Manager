import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class TodaySchedule extends JPanel {
    private JTable todoTable;
    private JTable scheduleTable;
    private JTable assignmentTable;
    private JComboBox<String> sortComboBox;
    private JComboBox<String> filterComboBox;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> typeComboBox;

    public TodaySchedule() {
        setLayout(new BorderLayout());

        // TO-DO Deadline
        JPanel todoPanel = new JPanel(new BorderLayout());
        todoPanel.setBorder(BorderFactory.createTitledBorder("TO-DO Deadline"));
        String[] todoColumns = {"할 일", "완료", "마감일"};
        Object[][] todoData = {
                {"과제1", true, "2024. 5. 10."},
                {"과제2", false, "2024. 5. 15."},
                {"과제3", false, "2024. 5. 20"}
        };

        DefaultTableModel todoTableModel = new DefaultTableModel(todoData, todoColumns);
        todoTable = new JTable(todoTableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };

        todoTable.setRowSelectionAllowed(false);
        todoTable.setShowGrid(false);

        todoPanel.add(new JScrollPane(todoTable), BorderLayout.CENTER);

        todoTable.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                int col = todoTable.getSelectedColumn();
                if (col == 1) {
                    todoTable.repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        // 미완료 과제 빨간색으로 강조
        todoTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // 체크박스가 체크되었는지 확인
                boolean isChecked = (boolean) table.getValueAt(row, 1);

                // 체크박스 체크 여부에 따른 수행
                if (column == 0 ) {
                    System.out.println(c.getForeground().toString());
                    if (isChecked) {
                        // 체크박스가 체크되었을 경우, 글자 색상을 기본 색상으로 변경
                        c.setForeground(table.getForeground());
                    } else {
                        // 체크박스가 체크되지 않은 경우, 첫 번째 요소와 세 번째 요소의 글자 색상을 빨간색으로 변경
                            c.setForeground(Color.RED);
                    }
                }
                return c;
            }
        });


        // 오늘 수업
        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBorder(BorderFactory.createTitledBorder("오늘 수업"));
        String[] scheduleColumns = {"과목명", "진도 내용", "과제"};
        Object[][] scheduleData = {
                {"객체지향프로그래밍", "Java 이벤트 프로그래밍", "-"},
                {"데이터구조", "Queue의 활용", "-"}
        };
        DefaultTableModel scheduleTableModel = new DefaultTableModel(scheduleData, scheduleColumns);
        scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setRowSelectionAllowed(false);
        scheduleTable.setShowGrid(false);
        schedulePanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // 시간표 팝업 창
        JButton timeTableButton = new JButton("시간표");
        timeTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 시간표 팝업 창 구현
                JDialog timeTableDialog = new JDialog();
                timeTableDialog.setTitle("시간표");
                timeTableDialog.setSize(400, 300);
                timeTableDialog.setLocationRelativeTo(null);
                // 시간표 DB에서 정보를 가져와 표 생성
                // ...
                timeTableDialog.setVisible(true);
            }
        });

        // 예정된 과제
        JPanel assignmentPanel = new JPanel(new BorderLayout());
        assignmentPanel.setBorder(BorderFactory.createTitledBorder("예정된 과제"));
        String[] assignmentColumns = {"과목", "과제", "마감일"};
        Object[][] assignmentData = {
                {"과목1", "과제1", "2024.5.10"},
                {"과목2", "과제2", "2024.5.15"},
                {"과목3", "과제3", "2024.5.20"}
        };
        DefaultTableModel assignmentTableModel = new DefaultTableModel(assignmentData, assignmentColumns);
        assignmentTable = new JTable(assignmentTableModel);
        assignmentTable.setRowSelectionAllowed(false);
        assignmentTable.setShowGrid(false);
        assignmentPanel.add(new JScrollPane(assignmentTable), BorderLayout.CENTER);

        // 정렬 & 필터 선택
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] sortOptions = {"시간순", "과목순"};
        sortComboBox = new JComboBox<>(sortOptions);
        filterPanel.add(new JLabel("정렬: "));
        filterPanel.add(sortComboBox);

        String[] filterOptions = {"전과목", "전공과목", "교양과목"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterPanel.add(new JLabel("필터: "));
        filterPanel.add(filterComboBox);

        String[] statusOptions = {"미완과제", "완료과제", "전체과제"};
        statusComboBox = new JComboBox<>(statusOptions);
        filterPanel.add(statusComboBox);

        String[] typeOptions = {"전체종류", "시험", "보고서", "퀴즈", "프로젝트"};
        typeComboBox = new JComboBox<>(typeOptions);
        filterPanel.add(typeComboBox);

        assignmentPanel.add(filterPanel, BorderLayout.NORTH);

        // 예정된 TO-DO
        JPanel upcomingPanel = new JPanel(new BorderLayout());
        upcomingPanel.setBorder(BorderFactory.createTitledBorder("예정된 TO-DO"));
        // 예정된 TO-DO 목록을 DB에서 가져와 표시
        // ...

        // 창 분할
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());

        JSplitPane leftVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel todoButtonPanel = new JPanel(new BorderLayout());
        todoButtonPanel.add(todoPanel, BorderLayout.CENTER);
        todoButtonPanel.add(timeTableButton, BorderLayout.SOUTH);
        leftVerticalSplitPane.setTopComponent(todoButtonPanel);
        leftVerticalSplitPane.setBottomComponent(schedulePanel);
        leftVerticalSplitPane.setResizeWeight(0.5);
        leftPanel.add(leftVerticalSplitPane, BorderLayout.CENTER);

        JSplitPane rightVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightVerticalSplitPane.setTopComponent(assignmentPanel);
        rightVerticalSplitPane.setBottomComponent(upcomingPanel);
        rightVerticalSplitPane.setResizeWeight(0.5);
        rightPanel.add(rightVerticalSplitPane, BorderLayout.CENTER);

        horizontalSplitPane.setLeftComponent(leftPanel);
        horizontalSplitPane.setRightComponent(rightPanel);
        horizontalSplitPane.setResizeWeight(0.5);
        add(horizontalSplitPane, BorderLayout.CENTER);
    }
}