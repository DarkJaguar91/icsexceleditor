package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import Image.ImageHolder;
import Image.ImageProcessor;

public class MainForm extends JFrame {

	/**
	 * Generated Serial ID
	 */
	private static final long serialVersionUID = -1235861920996215785L;

	JPanel p, p2, cont;
	ImageHolder holder = null, holder2 = null;
	JFileChooser chooser;

	public MainForm() {
		chooser = new JFileChooser();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(800, 600);

//		holder = new ImageHolder(new ImageIcon("input.jpg").getImage());
//		holder2 = ImageProcessor.equalizeImage(holder);

		p = new JPanel() {
			/**
			 * generated serial
			 */
			private static final long serialVersionUID = -4018883558096863318L;

			public void paint(Graphics g) {
				if (holder != null)
					g.drawImage(holder.getImage(), 0, 0, this.getWidth(),
							this.getHeight(), null);
			}
		};

		p2 = new JPanel() {
			/**
			 * generated serial
			 */
			private static final long serialVersionUID = 8183757901024549645L;

			public void paint(Graphics g) {
				if (holder2 != null)
					g.drawImage(holder2.getImage(), 0, 0, this.getWidth(),
							this.getHeight(), null);
			}
		};

		this.setLayout(new BorderLayout());

		cont = new JPanel();

		p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		p2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		cont.setLayout(new GridLayout(0, 2));
		cont.add(p);
		cont.add(p2);

		this.add(cont, BorderLayout.CENTER);
		
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
						holder = new ImageHolder(new ImageIcon(chooser.getSelectedFile().getAbsolutePath()).getImage());
						holder2 = ImageProcessor.equalizeImage(holder);
						cont.updateUI();
					}
				}
			}
		});
		
		bar.add(loadImage);
		
		this.setJMenuBar(bar);
		
		setVisible(true);
	}

}
