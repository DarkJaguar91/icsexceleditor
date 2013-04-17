package GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import Image.ImageHolder;

public class MainForm extends JFrame {

	/**
	 * Generated Serial ID
	 */
	private static final long serialVersionUID = -1235861920996215785L;
	JFileChooser chooser;
	JPanel pane;
	
	public MainForm() {
		chooser = new JFileChooser();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(800, 600);
		
		// menu stuffs
		JMenuBar bar = new JMenuBar();
		JMenuItem loadImage = new JMenuItem("Load Image");
		final JFrame thiframe = this;
		loadImage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int ans = chooser.showOpenDialog(thiframe);
				if (ans == JFileChooser.APPROVE_OPTION){
					if (chooser.getSelectedFile().getAbsolutePath().toLowerCase().matches(".*(jpg|tif|png|jpeg|bmp|gif)")){
						pane.add(new ImagePanel(new ImageHolder(new ImageIcon(chooser.getSelectedFile().getAbsolutePath()).getImage())), BorderLayout.CENTER);
						pane.updateUI();
					}
				}
			}
		});		
		bar.add(loadImage);
		this.setJMenuBar(bar);
		//////////////////
		pane = new JPanel();
		pane.setLayout(new GridLayout(0, 1));
		
		this.setLayout(new BorderLayout());
		this.add(pane, BorderLayout.CENTER);
		
		setVisible(true);
	}

}
