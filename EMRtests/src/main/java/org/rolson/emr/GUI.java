package org.rolson.emr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class GUI extends JFrame {
	JFrame guiFrame;
	ClimaBucket s3b = new ClimaBucket();
	Cluster clus = new Cluster();
	int width = 1000;
	int height =700;
	public GUI()
	{
		
		super("Testing Buttons");
		//Create and set up the window.
		modifyFontSizes();
		guiFrame = new JFrame("TabbedPaneDemo");
		//guiFrame.setSize(1000,700);
		guiFrame.setVisible(true);
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
        //Add content to the window.
        setUpTabs();
         
        //Display the window.
        guiFrame.pack();
        guiFrame.setVisible(true);
	}
	private void addAButton(JPanel p,String text,int bw, int bh)
	{
		JButton but = new JButton( text);
		but.setPreferredSize(new Dimension(bw, bh));
		p.add(but, BorderLayout.CENTER);
		ButtonHandler handler = new ButtonHandler();
		but.addActionListener(handler);
	}
	
	private void addButtonsFixedGrid(JPanel panel,List<String> buttonCmds,int cols,int rows)
	{
	
		JPanel[][] panelHolder = new JPanel[rows][cols];  
		int pw = (int)(width*0.95/cols);
		int ph = (int)(height*0.95/rows);
		panel.setLayout(new GridLayout(rows,cols));
		int bw = (int)(pw*0.95);
		int bh = (int)(ph*0.95);
		for(int m = 0; m < rows; m++) {
		   for(int n = 0; n < cols; n++) {
		      panelHolder[m][n] = new JPanel();
		      panelHolder[m][n].setPreferredSize(new Dimension(pw, ph));
		      panel.add(panelHolder[m][n]);
		      if(n==0)
		      {
		    	  if(m<buttonCmds.size())
		    	  {
		    	  addAButton(panelHolder[m][n],buttonCmds.get(m),bw,bh);
		    	  }
		      }
		   }
		}
	}
	private List<String> setWorkflowButtons()
	{
		List<String> buttonCmds = new ArrayList<String>();
		buttonCmds.add("Hadoop Map Reduce");
		buttonCmds.add("K-means clustering");
		buttonCmds.add("Linear Regression");
		buttonCmds.add("Hadoop Map Reduce");
		buttonCmds.add("K-means clustering");
		buttonCmds.add("Linear Regression");
		return buttonCmds;
	}
	private List<String> setAWSTestButtons()
	{
		List<String> buttonCmds = new ArrayList<String>();
		buttonCmds.add("Start Cluster");
		buttonCmds.add("Stop Cluster");
		buttonCmds.add("Start Spark Cluster");
		buttonCmds.add("Create Bucket");
		buttonCmds.add("Delete Bucket");
		buttonCmds.add("List all Buckets");
		buttonCmds.add("Cluster Status");
		buttonCmds.add("Terminate all clusters");
		buttonCmds.add("Count NOAA Stations");
		buttonCmds.add("Put a file in bucket");
		return buttonCmds;
	}
	private void setUpTabs()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel workflowpanel = new JPanel();
		addButtonsFixedGrid(workflowpanel,setWorkflowButtons(),3,12);	
		tabbedPane.addTab("Workflows", workflowpanel);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_W);

		JPanel monitorpanel = new JPanel(new GridLayout(1,0));
		
		createWorkflowTable(monitorpanel);
		tabbedPane.addTab("Monitor",  monitorpanel);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_M);

		JPanel visualisepanel = new JPanel();
		tabbedPane.addTab("Visualise",  visualisepanel);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_V);

		JPanel datapanel = new JPanel();
		datapanel.setPreferredSize(new Dimension(width, height));
		tabbedPane.addTab("Data Manager",  datapanel);
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_D);

		JPanel buttonHolder = new JPanel();
		addButtonsFixedGrid(buttonHolder,setAWSTestButtons(),3,12);	
		tabbedPane.addTab("AWSTests", buttonHolder);
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_T);
		
		guiFrame.getContentPane().add(tabbedPane);

	}

	private class ButtonHandler implements ActionListener
	{
		// handle button event
		
		public void actionPerformed(ActionEvent event)
		{
			
		String cmd = event.getActionCommand();
		
		switch(cmd) {
		case "Create Bucket": actionMessage("Creating Bucket");
		
		s3b.createBucket("climacolombiabucket");
		break;
		case "Delete Bucket": actionMessage("Deleting Bucket");
		
		s3b.deleteBucket("climacolombiabucket");
		break;
		case "Start Cluster": actionMessage("Starting Cluster");
		
		clus.launch();
		break;
		case "Stop Cluster": actionMessage("Stopping Cluster");
		break;
		case "List all Buckets": actionMessage("Listing buckets");
		
		s3b.listBuckets();
		break;
			case "Cluster Status": actionMessage("Status of clusters");
		
		clus.clusterStatusReport();
		break;
		case "Terminate all clusters": actionMessage("Status of clusters");
		
		clus.terminateAllClusters();
		break;
		case "Start Spark Cluster": actionMessage("Starting a spark cluster");
		
		clus.launchSparkCluster();
		break;
		case "Count NOAA Stations": actionMessage("Starting a spark cluster");
		
		clus.launchNOAACounter();
		break;
		case "Put a file in bucket": actionMessage("Adding file to bucket");
		
		s3b.uploadMultiPart();
		break;
		case "Hadoop Map Reduce": actionMessage("Hadoop");
			break;
		case "K-means clustering": actionMessage("K-means");
			break;
		case "Linear Regression": actionMessage("Linear regression");
			break;
		}
		}
	}
	private void actionMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	private void createWorkflowTable(JPanel p)
	{
		String[] headers = {"name","id","status","date started"};
		Object[][] data = {
			    {"Kathy", "Smith",
			     "Snowboarding", new Integer(5), new Boolean(false)},
			    {"John", "Doe",
			     "Rowing", new Integer(3), new Boolean(true)},
			    {"Sue", "Black",
			     "Knitting", new Integer(2), new Boolean(false)},
			    {"Jane", "White",
			     "Speed reading", new Integer(20), new Boolean(true)},
			    {"Joe", "Brown",
			     "Pool", new Integer(10), new Boolean(false)}
			     
			};
		JTable table = new JTable(data, headers);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(false);
		p.add(scrollPane);
	}
	private void modifyFontSizes(){
		FontUIResource font = new FontUIResource(new Font("Dialog", Font.BOLD, 22));
		 UIManager.put("Label.font",font );
		    UIManager.put("Button.font", font);
		    UIManager.put("TextField.font", font);
		    UIManager.put("Button.font", font);
		    UIManager.put("ToggleButton.font", font);
		    UIManager.put("RadioButton.font", font);
		    UIManager.put("CheckBox.font", font);
		    UIManager.put("ColorChooser.font", font);
		    UIManager.put("ComboBox.font", font);
		    UIManager.put("Label.font", font);
		    UIManager.put("List.font", font);
		    UIManager.put("MenuBar.font", font);
		    UIManager.put("MenuItem.font", font);
		    UIManager.put("RadioButtonMenuItem.font", font);
		    UIManager.put("CheckBoxMenuItem.font", font);
		    UIManager.put("Menu.font", font);
		    UIManager.put("PopupMenu.font", font);
		    UIManager.put("OptionPane.font", font);
		    UIManager.put("Panel.font", font);
		    UIManager.put("ProgressBar.font", font);
		    UIManager.put("ScrollPane.font", font);
		    UIManager.put("Viewport.font", font);
		    UIManager.put("TabbedPane.font", font);
		    UIManager.put("Table.font", font);
		    UIManager.put("TableHeader.font", font);
		    UIManager.put("TextField.font", font);
		    UIManager.put("PasswordField.font", font);
		    UIManager.put("TextArea.font", font);
		    UIManager.put("TextPane.font", font);
		    UIManager.put("EditorPane.font", font);
		    UIManager.put("TitledBorder.font", font);
		    UIManager.put("ToolBar.font", font);
		    UIManager.put("ToolTip.font", font);
		    UIManager.put("Tree.font", font);
	}
}
