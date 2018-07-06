package org.rolson.emr.emrcycle1;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class main {

	public static void main(String[] args) {

		//Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

        GUI gui = new GUI();
            }
        });

	}

}
