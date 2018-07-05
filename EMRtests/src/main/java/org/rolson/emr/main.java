package org.rolson.emr;

import javax.swing.JFrame;

public class main {

	public static void main(String[] args) {
		GUI gui = new GUI();
		
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("climaProto1");
		gui.setSize(600,500);
		gui.setVisible(true);

	}

}
