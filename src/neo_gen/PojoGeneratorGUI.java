package neo_gen;

import java.io.File;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EtchedBorder;
import javax.swing.JScrollPane;

/**
 * <pre>
 * neo_gen
 *		|_ PojoGeneratorGUI.java
 * </pre>
 *
 * @desc	: Pojo변환모듈 GUI
 * @author	: jy.lee
 * @date	: 2024.04.24
 * ===========================================================
 * DATE				AUTHOR			NOTE
 * -----------------------------------------------------------
 * 2024.04.24		jy.lee			최초작성
 */
public class PojoGeneratorGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;

    private Font fontTitle;
    private Font fontLabal;

    private JFileChooser jfcSelFile;
    private JFileChooser jfcSelFolder;

    private JTextField txtFieldSelFile;
    private JTextField txtFieldSelFolder;
    private JTextField txtFieldPackagePath;

    private JButton btnSelFile;
    private JButton btnSelFolder;
    private JButton btnConvertor;

    private DefaultListModel<String> selFileListModel;
    private JScrollPane scrolled;
    private JList<String> selFileList;

    private JComboBox<String> modelTypeCombo;

    private JRadioButton rdoInnerClass1;
    private JRadioButton rdoInnerClass2;

    /**
     * @desc	: Launch the application.
     * @method	: main
     * @param args
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    PojoGeneratorGUI frame = new PojoGeneratorGUI();
                    frame.setVisible(true);
                    frame.setResizable(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public PojoGeneratorGUI() {

        // 제목
        super("POJO-Generator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 646, 520);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // 전사폰트 설정
        UIManager.put("OptionPane.messageFont", new Font("돋움",Font.PLAIN,12));
        fontTitle = new Font("돋움", Font.ITALIC, 13);
        fontLabal = new Font("돋움", Font.PLAIN , 12);

        JPanel panel = new JPanel();
        panel.setBorder(
            new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                "변환옵션 설정",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                fontTitle ,
                new Color(0, 0, 0)
            )
        );
        panel.setBounds(6, 10, 619, 160);
        contentPane.add(panel);
        panel.setLayout(null);

        JLabel lblNewLabel1 = new JLabel("◈ 대상파일선택(*.json)");
        lblNewLabel1.setFont(fontLabal);
        lblNewLabel1.setBounds(12, 32, 140, 15);
        panel.add(lblNewLabel1);

        txtFieldSelFile = new JTextField();
        txtFieldSelFile.setBounds(148, 29, 339, 23);
        txtFieldSelFile.setEditable(false);
        txtFieldSelFile.setBackground(getForeground());

        panel.add(txtFieldSelFile);

        btnSelFile = new JButton("파일선택");
        btnSelFile.setFont(fontLabal);
        btnSelFile.setBounds(486, 29, 97, 23);
        panel.add(btnSelFile);

        JLabel lblNewLabel2 = new JLabel("◈ 생성폴더선택");
        lblNewLabel2.setFont(fontLabal);
        lblNewLabel2.setBounds(12, 61, 140, 15);
        panel.add(lblNewLabel2);

        txtFieldSelFolder = new JTextField();
        txtFieldSelFolder.setBounds(148, 58, 339, 23);
        txtFieldSelFolder.setEditable(false);
        txtFieldSelFolder.setBackground(getForeground());
        panel.add(txtFieldSelFolder);

        btnSelFolder = new JButton("폴더선택");
        btnSelFolder.setFont(fontLabal);
        btnSelFolder.setBounds(486, 58, 97, 23);
        panel.add(btnSelFolder);

        jfcSelFile   = new JFileChooser();
        jfcSelFolder = new JFileChooser();

        JLabel lblNewLabel3 = new JLabel("◈ JAVA 패키지경로");
        lblNewLabel3.setFont(fontLabal);
        lblNewLabel3.setBounds(12, 90, 140, 15);
        panel.add(lblNewLabel3);

        txtFieldPackagePath = new JTextField();
        txtFieldPackagePath.setBounds(148, 87, 339, 23);
        panel.add(txtFieldPackagePath);

        JLabel lblNewLabel4 = new JLabel("◈ POJO Model-Type");
        lblNewLabel4.setFont(fontLabal);
        lblNewLabel4.setBounds(12, 120, 140, 15);
        panel.add(lblNewLabel4);

        String[] modelTypes = {"DTO", "VO", "DAO", "Form"};
        modelTypeCombo =new JComboBox<String>(modelTypes);
        modelTypeCombo.setFont(fontLabal);
        modelTypeCombo.setBackground(new Color(255, 255, 255));
        modelTypeCombo.setBounds(148, 116, 100, 23);
        panel.add(modelTypeCombo);

        ButtonGroup group = new ButtonGroup();
        rdoInnerClass1 = new JRadioButton("개별 Class로 생성", true);
        rdoInnerClass1.setFont(fontLabal);
        rdoInnerClass1.setBounds(270, 120, 130, 15);

        rdoInnerClass2 = new JRadioButton("InnerClass로 생성");
        rdoInnerClass2.setBounds(400, 120, 130, 15);
        rdoInnerClass2.setFont(fontLabal);

        group.add(rdoInnerClass1);
        group.add(rdoInnerClass2);
        panel.add(rdoInnerClass1);
        panel.add(rdoInnerClass2);

        btnConvertor = new JButton("변환하기");
        btnConvertor.setFont(fontLabal);
        btnConvertor.setBounds(270, 435, 97, 23);

        contentPane.add(btnConvertor);

        btnConvertor.addActionListener(this);

        selFileListModel=new DefaultListModel<String>();
        selFileList=new JList<String>(selFileListModel);
        selFileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // 다중 선택 될 수 있도록
        scrolled=new JScrollPane(selFileList);
        scrolled.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(9, 172, 613, 255);
        contentPane.add(scrollPane);

        scrollPane.setViewportView(selFileList);

        btnSelFile.addActionListener(this);

        jfcSelFile.setFileFilter(new FileNameExtensionFilter("json", "JSON"));
        jfcSelFile.setMultiSelectionEnabled(true);//다중 선택 사능

        btnSelFolder.addActionListener(this);
        jfcSelFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    /**
     * Override > ActionListener
     */
    @Override
    public void actionPerformed(ActionEvent event) {

        // 파일선택 이벤트
        if(event.getSource() == btnSelFile) {

            if(jfcSelFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                if(jfcSelFile.getSelectedFiles().length == 0) {
                    return;
                }

                String tarFolder = "";
                for(File f : jfcSelFile.getSelectedFiles()) {
                    addModelFilePath(f.getPath());
                    tarFolder = f.getParent();
                }
                txtFieldSelFile.setText(tarFolder);
            }
        // 폴더선택 이벤트
        } else if(event.getSource() == btnSelFolder) {

            if(jfcSelFolder.showOpenDialog(this) == JFileChooser.FILES_ONLY) {

                txtFieldSelFolder.setText("");
                File dir = jfcSelFolder.getSelectedFile();

                if(dir != null) {
                    txtFieldSelFolder.setText(dir.getPath());
                }
            }
        // 변환하기버튼 이벤트
        } else if(event.getSource() == btnConvertor) {

            String outputFolder = txtFieldSelFolder.getText();
            String packagePath  = txtFieldPackagePath.getText();
            String modelType    = (String) modelTypeCombo.getSelectedItem();
            boolean isInnerClass= false;

            if (rdoInnerClass2.isSelected()) {
                isInnerClass = true;
            }

            if(selFileListModel.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "대상파일(*.json)을 선택하세요 !", "확인", JOptionPane.WARNING_MESSAGE);
                btnSelFile.doClick();
                return;
            }
            if(outputFolder == null || outputFolder.isBlank()) {
                JOptionPane.showMessageDialog(this, "생성폴더를 선택하세요 !", "확인", JOptionPane.WARNING_MESSAGE);
                btnSelFolder.doClick();
                return;
            }
            if(packagePath == null || packagePath.isBlank()) {
                txtFieldPackagePath.requestFocus();
                JOptionPane.showMessageDialog(this, "JAVA 패키지 경로를 입력하세요 !", "확인", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int option = JOptionPane.showConfirmDialog(
                this,
                "변환을 진행하시겠습니까?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null    // icon imega
            );

            if (option != JOptionPane.YES_OPTION) {
                return;
            }

            String filePath = "";
            for (int i = 0; i < selFileListModel.getSize(); i++) {
                filePath = (String) selFileListModel.getElementAt(i);
                JsonToPojoGenerator.jsonToPojo(filePath, outputFolder, packagePath, modelType, isInnerClass);
            }

            JOptionPane.showMessageDialog(this, "POJO 변환이 완료되었습니다~");
        }
    }


    /**
     * @desc	: JListModel에 filePath data를 셋팅
     * @method	: addModelFilePath
     * @param pathString
     */
    public void addModelFilePath(String filePath ) {

        if(filePath == null || filePath.length() == 0 || selFileListModel.contains(filePath)) {
            return;
        }

        selFileListModel.addElement(filePath);
        scrolled.getVerticalScrollBar().setValue(scrolled.getVerticalScrollBar().getMaximum());
    }
}
