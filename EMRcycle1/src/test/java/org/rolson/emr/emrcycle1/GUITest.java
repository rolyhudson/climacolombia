package org.rolson.emr.emrcycle1;

import static org.junit.Assert.*;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.Test;

public class GUITest {
GUI gui = new GUI();
	@Test
	public void testGUI() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetComponentByName() {
		
		gui.addToMap("testHM", new JLabel("TestLabel"));
		JLabel l = (JLabel)gui.getComponentByName("testHM");
		assertEquals(l.getText(),"TestLabel");
	}

}
