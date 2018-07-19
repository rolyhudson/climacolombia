package org.rolson.emr.emrcycle1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;


public class GUI extends JFrame {
	JFrame guiFrame;
	ClimaBucket s3b = new ClimaBucket();
	Cluster clus = new Cluster();
	private DataManager datamanager = new DataManager();
	private HashMap<String,JComponent> buttonLabelMap = new HashMap<String,JComponent>();
	int width = 1000;
	int height =700;
	private WorkflowCoordinator coordinator;
	public GUI(WorkflowCoordinator wfc)
	{
		
		super("Testing Buttons");
		coordinator = wfc;
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
	private void addALabel(JPanel p,String btntext,int bw, int bh)
	{
		JLabel l = new JLabel("Result:");
		l.setFont(new Font(l.getFont().getName(), Font.PLAIN, 12));
		l.setPreferredSize(new Dimension(bw, bh));
		p.add(l);
		buttonLabelMap.put(btntext, l);
	}
	public void addToMap(String s, JComponent c)
	{
		buttonLabelMap.put(s, c);
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
		      if(n==1)
		      {
		    	  if(m<buttonCmds.size())
		    	  {
		    	  addALabel(panelHolder[m][n],buttonCmds.get(m),bw,bh);
		    	  }
		      }
		   }
		}
	}
	private List<String> setDataManagerButtons()
	{
		List<String> buttonCmds = new ArrayList<String>();
		buttonCmds.add("Upload dataset");
		buttonCmds.add("Upload JAR file");
		
		return buttonCmds;
	}
	private List<String> setWorkflowButtons()
	{
		List<String> buttonCmds = new ArrayList<String>();
		buttonCmds.add("Hadoop Map Reduce");
		buttonCmds.add("K-means clustering");
		buttonCmds.add("Linear Regression");
		buttonCmds.add("Message log agregator");
		buttonCmds.add("Monthly records totals");
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
		addButtonsFixedGrid(datapanel,setDataManagerButtons(),3,12);
		datapanel.setPreferredSize(new Dimension(width, height));
		tabbedPane.addTab("Data Manager",  datapanel);
		tabbedPane.setMnemonicAt(3, KeyEvent.VK_D);

		JPanel buttonHolder = new JPanel();
		addButtonsFixedGrid(buttonHolder,setAWSTestButtons(),3,12);	
		tabbedPane.addTab("AWSTests", buttonHolder);
		tabbedPane.setMnemonicAt(4, KeyEvent.VK_T);
		
		JPanel gridbagButtons = new JPanel();
		gridbagButtons.setLayout(new GridBagLayout());
		addButtonsGridBag(gridbagButtons,setDataManagerButtons());
		tabbedPane.addTab("Layout test", gridbagButtons);
		tabbedPane.setMnemonicAt(5, KeyEvent.VK_L);
		
		guiFrame.getContentPane().add(tabbedPane);

	}
	private void addButtonsGridBag(JPanel pane,List<String> btnNames)
	{
		GridBagConstraints c = new GridBagConstraints();
		//c.anchor = GridBagConstraints.FIRST_LINE_START;
	    for(int i=0;i<btnNames.size();i++)
	    {
	    	JButton button = new JButton(btnNames.get(i));
	    	c.fill = GridBagConstraints.HORIZONTAL;
	    	
	    	c.weightx =0.0;
	    	c.weighty =0.1;
	    	//c.gridwidth =1;
	    	c.gridheight=1;
		    c.gridx = 0;
		    c.gridy = i;
		    pane.add(button, c);
		    
		    JLabel l = new JLabel("Info:");
		    l.setBorder(BorderFactory.createLineBorder(Color.black));
		    c.fill = GridBagConstraints.HORIZONTAL;
		    c.weightx =1;
		    c.weighty =0.0;
		    //c.gridwidth =2;
		    c.gridx = 1;
		    c.gridy = i;
		    pane.add(l, c);

	    }  
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
		case "Hadoop Map Reduce": actionMessage(cmd);
			coordinator.addPredfined(cmd);
			coordinator.runWorkflow(cmd);
			break;
		case "Message log agregator": actionMessage(cmd);
			coordinator.addPredfined(cmd);
			coordinator.runWorkflow(cmd);
			break;
		case "Monthly records totals": actionMessage(cmd);
			coordinator.addPredfined(cmd);
			coordinator.runWorkflow(cmd);
		break;
		case "K-means clustering": actionMessage(cmd);
			break;
		case "Linear Regression": actionMessage(cmd);
			break;
		case "Upload dataset":
			List<String> dataexts = Arrays.asList("csv", "txt");
			getFileForUpload(dataexts, cmd);
		break;
		case "Upload JAR file": 
			List<String> processexts = Arrays.asList("jar");
			getFileForUpload(processexts, cmd);
		break;
		
		}
		}
	}
	private String getStringInput(String message)
	{
		JFrame frame = new JFrame("InputDialog Example #1");
		// prompt the user to enter 
	    String input = JOptionPane.showInputDialog(frame, message);

	    return input;
	}
	public void getFileForUpload(List<String> extensions, String cmd)
	{
		JFileChooser jfc = new JFileChooser();
		int returnVal = jfc.showOpenDialog(GUI.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            boolean required =false;
            String foundext = Utils.getFileExtension(file);
            for(String ext:extensions){
            	if(ext.equals(foundext))required=true;
            }
            if(required) {
            	//String bucket = getStringInput("enter bucket name");
            	String subfolderpath = getStringInput("enter folderpath");
            	JLabel l = (JLabel)getComponentByName(cmd);
            	if(datamanager.upload(file,subfolderpath))
            	{
            		//write result message
            		
            		l.setText("Result: "+file.getName()+ " uploaded");
            	}
            	else{
                	//
            		l.setText("Result: "+file.getName()+ " upload failed");
                }
            }
            else{
            	JOptionPane.showMessageDialog(null, "file extension not accepted");
            }
            
        } 
        //Open command cancelled by user
	}
	public JComponent getComponentByName(String name) {
        if (buttonLabelMap.containsKey(name)) {
                return (JComponent) buttonLabelMap.get(name);
        }
        else return null;
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
