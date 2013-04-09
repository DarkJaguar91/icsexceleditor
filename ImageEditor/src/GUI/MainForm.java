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
import javax.swing.JFrame;
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
	ImageHolder holder, holder2;
	
	public MainForm(){
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setSize(800, 600);
		
		holder = new ImageHolder(new ImageIcon("input.jpg").getImage());
		holder2 = ImageProcessor.equalizeImage(holder);
		
		p = new JPanel(){
			public void paint(Graphics g){
				g.drawImage(holder.getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
			}
		};
		
		p2 = new JPanel(){
			public void paint(Graphics g){
				g.drawImage(holder2.getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
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
		
		setVisible(true);
	}
	
}
