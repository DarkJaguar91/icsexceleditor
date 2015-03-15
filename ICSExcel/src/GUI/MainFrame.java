package GUI;

import GUI.progressgui.ProgressBarPanel;
import ICSContainerCreator.ContainerCreator;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements ProgressBarPanel.ProgressCompleteListener {

    public static final String CONTAINER = "Container";
    protected JRadioButton rdbContainerSheet;
    protected JButton btnStart;
    protected ProgressBarPanel progressBar;
    protected ButtonGroup radioGroup;

    public MainFrame() throws HeadlessException {
        super("Excel Sheet Creator");

        setupLayout();

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void setupLayout() {
        rdbContainerSheet = new JRadioButton("Container Spread Sheet", true);
        btnStart = new JButton("Start");
        btnStart.addActionListener(e -> performAction());
        progressBar = new ProgressBarPanel(this);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(0, 1));
        radioPanel.add(rdbContainerSheet);

        radioGroup = new ButtonGroup();
        radioGroup.add(rdbContainerSheet);

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup()
                                        .addComponent(radioPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(btnStart, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        )
                        )
                        .addContainerGap()
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(radioPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup()
                                        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnStart, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )

                        .addContainerGap()
        );

        this.setMinimumSize(new Dimension(300, 0));
        this.pack();
        this.setResizable(false);
    }

    private void performAction() {
        btnStart.setEnabled(false);
        if (rdbContainerSheet.isSelected()) {
            new Thread(new ContainerCreator(progressBar)).start();
        }
    }

    @Override
    public void progressComplete() {
        btnStart.setEnabled(true);
    }
}
