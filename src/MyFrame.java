import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.Vector;

import javax.management.openmbean.OpenDataException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.synth.SynthSeparatorUI;

public class MyFrame extends JFrame implements MouseListener {
	Connect con;
	JMenuBar menuBar;
	JMenu menuFile;
	JMenuItem itemSave,itemOpen,itemNew,itemGenerate,itemCompile;
	JPanel mainPanel,drawPanel,sidePanel,componentPanel,actionPanel,compilePanel;
	JSplitPane splitPanel;
	JButton btnAdd,btnRemove,btnReset,componentButton;
	JLabel componentLabel;
	JTextField componentTextField;
	JTextArea componentTextArea;
	String tempType="Label";
	String tempValue="";
	String tempAction="Add";
	Vector<MyFile> vecFiles;
	Vector<MyComponent> vecMyComponent;
	Vector<String> vecTitles;
	Thread moveComponent;
	int openedFileId,posX,posY,frameX,frameY;
	boolean isOpenFile, onDrag;
	
	public MyFrame(){
		initComponent();
		add(mainPanel);
		setTitle("Untitled");
		setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
	}
	
	public void initComponent(){
		// init utils
		openedFileId=-1;
		isOpenFile=false;
		onDrag=false;
		
		// init connection
		con = new Connect();
		
		// init menus
		menuBar = new JMenuBar();
		menuFile = new JMenu("File");
		itemSave = new JMenuItem("Save");
		itemOpen = new JMenuItem("Open");
		itemNew = new JMenuItem("New File");
		itemGenerate = new JMenuItem("Generate Code");
		itemCompile = new JMenuItem("Compile");
		itemNew.addMouseListener(this);
		itemSave.addMouseListener(this);
		itemOpen.addMouseListener(this);
		itemGenerate.addMouseListener(this);
		itemCompile.addMouseListener(this);
		
		// init all panels
		mainPanel = new JPanel(new FlowLayout());
		drawPanel = new JPanel(null);
		drawPanel.addMouseListener(this);
		drawPanel.setPreferredSize(new Dimension(550,550));
		sidePanel = new JPanel(new GridLayout(2,1));
		componentPanel = new JPanel(new GridLayout(4,1));
		actionPanel = new JPanel(new FlowLayout());
		
		// init all components
		btnAdd = new JButton("Add");
		btnRemove = new JButton("Remove");
		btnReset = new JButton("Reset");
		btnAdd.addMouseListener(this);
		btnRemove.addMouseListener(this);
		btnReset.addMouseListener(this);
		
		componentButton = new JButton("Button");
		componentLabel = new JLabel("Label");
		componentTextField = new JTextField("Text Field");
		componentTextField.setEditable(false);
		componentTextArea = new JTextArea("Text Area");
		componentTextArea.setEditable(false);
		
		// adding menus
		menuFile.add(itemNew);
		menuFile.add(itemOpen);
		menuFile.add(itemSave);
		menuFile.add(itemGenerate);
		menuFile.add(itemCompile);
		menuBar.add(menuFile);
		setJMenuBar(menuBar);
		
		// adding action listener to the components
		componentButton.addMouseListener(this);
		componentLabel.addMouseListener(this);
		componentTextField.addMouseListener(this);
		componentTextArea.addMouseListener(this);
		itemCompile.addMouseListener(this);
		
		// adding components to component panel
		componentPanel.add(componentLabel);
		componentPanel.add(componentTextField);
		componentPanel.add(componentTextArea);
		componentPanel.add(componentButton);
		
		// adding buttons to action panel
		actionPanel.add(btnReset);
		actionPanel.add(btnAdd);
		actionPanel.add(btnRemove);
		
		sidePanel.add(componentPanel);
		sidePanel.add(actionPanel);
		
		splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,drawPanel,sidePanel);
		mainPanel.add(splitPanel,BorderLayout.CENTER);
	}
	
	public void doReset(){
		for(Component jc : drawPanel.getComponents()){
			drawPanel.remove(jc); //remove semua component dari panel draw
		}
		repaint();
	}
	
	public void doOpenFile(){
		vecFiles = new Vector<>(); //init vector of MyFile
		con.rs = con.executeQuery("SELECT * FROM files"); //select semua data di DB
		try {
			while (con.rs.next()) {
				vecFiles.add(new MyFile(con.rs.getInt(1),con.rs.getString(2)));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		if(vecFiles.size() == 0) //jika gaada data
			JOptionPane.showMessageDialog(null, "There is no data yet");
		else{
			doReset(); //kita reset dulu panelnya karna dia mw buka file baru.
			vecTitles = new Vector(); //init vector of string untuk nampung judul2 filenya
			for (MyFile myFile : vecFiles) {
				vecTitles.add(myFile.name); //kita tambahin semua judul file ke vector vecTitles
			}
			JComboBox optionList = new JComboBox(vecTitles); //bikin combo box yg isinya itu vecTitles
		    JOptionPane.showMessageDialog(null, optionList, "Open File",
		        JOptionPane.QUESTION_MESSAGE); //bikin joptionpane untuk select judul file yg mw dibuka
		    String title = optionList.getSelectedItem().toString(); //nama judul filenya ditampung
		    setTitle(title); //set nama window dengan nama file yg dipilih
		    for (MyFile myFile : vecFiles) {
				if(myFile.name.equals(title)) //loop untuk cari id file yg dipilih
					openedFileId = myFile.id; //tampung id nya
			}
		    isOpenFile=true; //boolean penanda kita lg open file
		    
		    vecMyComponent = new Vector<>(); //init vector of MyComponent
		    //select semua component yg id nya itu sama kek id filenya
		    con.rs = con.executeQuery("SELECT componentName,x,y,value FROM details WHERE fileId="+openedFileId);
			try {
				while (con.rs.next()) {
					vecMyComponent.add(new MyComponent(con.rs.getString(1),con.rs.getInt(2),con.rs.getInt(3),con.rs.getString(4)));
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			
			if(vecMyComponent.size()>0){
				for (MyComponent myComponent : vecMyComponent) {
					doRender(myComponent.type,myComponent.x,myComponent.y,myComponent.value,false);
				}
			}
		}
	}
	
	//boolean isCompile dipakai buat menandakan, kita mau render ke view
	//yang untuk saat ini atau buat di panel pas di compile
	public void doRender(String tempType,int x, int y, String value,boolean isCompile){
		if(tempType.equals("JLabel")){
			JLabel label = new JLabel(value);
			label.addMouseListener(this);
			if(isCompile)
				compilePanel.add(label);
			else
				drawPanel.add(label);
		    Dimension size = label.getPreferredSize();
		    label.setBounds(x - size.width/2,y-size.height/2, size.width, size.height);
		}
		else if(tempType.equals("JButton")){
			JButton button = new JButton(value);
			button.addMouseListener(this);
			if(isCompile)
				compilePanel.add(button);
			else
				drawPanel.add(button);
			Dimension size = button.getPreferredSize();
			button.setBounds(x - size.width/2,y-size.height/2,size.width,size.height);
		}
		else if(tempType.equals("JTextField")){
			JTextField textField = new JTextField("Text Field");
			textField.addMouseListener(this);
			if(isCompile)
				compilePanel.add(textField);
			else
				drawPanel.add(textField);
			Dimension size = textField.getPreferredSize();
			textField.setBounds(x - size.width/2,y-size.height/2,size.width,size.height);
		}
		else if(tempType.equals("JTextArea")){
			JTextArea textArea = new JTextArea("Text Area");
			textArea.addMouseListener(this);
			if(isCompile)
				compilePanel.add(textArea);
			else
				drawPanel.add(textArea);
			Dimension size = textArea.getPreferredSize();
			textArea.setBounds(x - size.width/2,y-size.height/2,size.width,size.height);
		}
	}
	
	public void doSave(){
		if(!isOpenFile){
			int id=1;
			String newFileName;
			do{
				newFileName = JOptionPane.showInputDialog(this, "Type New File Name"); //bikin joptionpane untuk input nama file baru yg mw disave
			}while(newFileName.length()==0);
			
			if(newFileName != null||newFileName.equals("")){ //validasi jika tidak kosong/null
				con.executeInsertToFiles(newFileName); //insert dulu ke header
				
				con.rs = con.executeQuery("SELECT id,name FROM files ORDER BY id DESC LIMIT 0,1");
				try {
					//ambil last id dari si data
					if(con.rs.first() != false)
						id = con.rs.getInt("id");				
				} catch (SQLException e) {
					e.printStackTrace();
				}
				for(Component jc : drawPanel.getComponents()){
					String className = jc.getClass().toString(); //ambil nama class component
					className = className.substring("class javax.swing.".length(), className.length()); //ditrim biar rapih
					String value="";
					if(jc instanceof JLabel){
						value = ((JLabel) jc).getText().toString(); //ambil text label
					}
					else if(jc instanceof JButton)
						value = ((JButton) jc).getText().toString(); //ambil text button
					con.executeInsertToDetails(id, className, jc.getX(), jc.getY(),value); //insert ke detail
				}
				setTitle(newFileName); //tinggal kita set titlenya skrng jd nama yg kita save
				isOpenFile=true; //dibuat true biar pas save gausa isi nama file baru lg tapi langsung di save
				openedFileId = id;
			}
		}
		else{
			con.executeDeleteDetails(openedFileId);
			for(Component jc : drawPanel.getComponents()){
				String className = jc.getClass().toString();
				className = className.substring("class javax.swing.".length(), className.length());
				String value="";
				if(jc instanceof JLabel){
					value = ((JLabel) jc).getText().toString();
				}
				else if(jc instanceof JButton)
					value = ((JButton) jc).getText().toString();
				con.executeInsertToDetails(openedFileId, className, jc.getX(), jc.getY(),value);
			}
		}
	}
	
	public void newFile(){
		doReset();
		setTitle("Untitled");
		isOpenFile = false;
	}
	
	public void doGenerate(){
		String myCode = "";
		for(Component jc : drawPanel.getComponents()){
			String className = jc.getClass().toString();
			className = className.substring("class javax.swing.".length(), className.length());
			myCode = myCode+(className+" "+className.toLowerCase()+" = new "+className+"();\n");
			myCode = myCode+(className.toLowerCase()+".addMouseListener(this);\n");
			myCode = myCode+("drawPanel.add(label);\n");
			myCode = myCode+("Dimension size = "+className.toLowerCase()+".getPreferredSize();\n");
			myCode = myCode+(className.toLowerCase()+".setBounds("+jc.getX()+" - size.width/2,"+jc.getY()+"-size.height/2, size.width, size.height);\n\n");
		}
			
	    JFileChooser chooser = new JFileChooser();
	    chooser.setCurrentDirectory(chooser.getCurrentDirectory());
	    int retrival = chooser.showSaveDialog(null);
	    if (retrival == JFileChooser.APPROVE_OPTION) {
	        try {
	            FileWriter fw = new FileWriter(chooser.getSelectedFile()+".java");
	            fw.write(myCode.toString());
	            fw.close();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
	}
	
	public void doOpenCompile(){
		JFrame compileFrame = new JFrame();
		compilePanel = new JPanel(null);
		for(Component jc : drawPanel.getComponents()){
			String className = jc.getClass().toString();
			className = className.substring("class javax.swing.".length(), className.length());
			String value = "";
			if(jc instanceof JLabel)
				value = ((JLabel) jc).getText().toString();
			else if(jc instanceof JButton)
				value = ((JButton) jc).getText().toString();
			
			doRender(className,jc.getX(),jc.getY(),value,true);
		}
		this.setVisible(false);
		compileFrame.add(compilePanel);
		compileFrame.setTitle("Compiled Preview");
		compileFrame.setSize(800, 600);
		compileFrame.setLocationRelativeTo(null);
		compileFrame.setVisible(true);
		compileFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				e.getWindow().dispose();
				setVisible(true);
			}
		});
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getButton() == MouseEvent.BUTTON2){
			for (Component jc : drawPanel.getComponents()) {
				if(e.getSource() == jc){
					posX = MouseInfo.getPointerInfo().getLocation().x; //posisi mouse x
					posY = MouseInfo.getPointerInfo().getLocation().y; //posisi mouse y
					frameX = this.getX(); //posisi x frame
					frameY = 120; //posisi y frame
					Dimension size = jc.getPreferredSize();
					moveComponent = new Thread(
							new Runnable() {
								@Override
								public void run() {
									onDrag=true;
									while(onDrag){ //ngelakuin supaya bisa di drag component kita
										posX = MouseInfo.getPointerInfo().getLocation().x;
										posY = MouseInfo.getPointerInfo().getLocation().y;
										jc.setBounds((posX-frameX) - size.width/2,(posY-120) -size.height/2, size.width, size.height);
									}
								}
							});
					moveComponent.start();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getButton() == MouseEvent.BUTTON2)
			onDrag=false;
		else if(e.getSource() == componentButton){
			tempType="JButton";
			tempValue="Button";
		}
		else if(e.getSource() == componentLabel) {
			tempType="JLabel";
			tempValue="Label";
		}
		else if(e.getSource() == componentTextField) 
			tempType="JTextField";
		else if(e.getSource() == componentTextArea) 
			tempType="JTextArea";
		else if(e.getSource() == drawPanel){
			int x = e.getX();
			int y = e.getY();
			if(tempAction == "Add"){
				doRender(tempType,x,y,tempValue,false);
				tempValue="";
			}
		}
		else if(e.getSource() == btnAdd)
			tempAction="Add";
		else if(e.getSource() == btnRemove)
			tempAction = "Remove";
		else if(e.getSource() == btnReset)
			doReset();
		else if(e.getSource() == itemOpen)
			doOpenFile();
		else if(e.getSource() == itemSave)
			doSave();
		else if(e.getSource() == itemNew)
			newFile();
		else if(e.getSource() == itemGenerate)
			doGenerate();
		else if(e.getSource() == itemCompile)
			doOpenCompile();
		else{
			if(tempAction == "Remove"){
				for(Component jc : drawPanel.getComponents()){
					if(e.getSource() == jc){
						drawPanel.remove(jc);
						repaint();
					}	
				}
			}
			else if(e.getButton() == MouseEvent.BUTTON3){
				for(Component jc : drawPanel.getComponents()){
					if(e.getSource() == jc){
						if(jc instanceof JLabel){
							String value = JOptionPane.showInputDialog(this, "Type the new component value");
							JLabel lblTemporary = (JLabel)jc;
							lblTemporary.setText(value);
						    Dimension size = lblTemporary.getPreferredSize();
						    lblTemporary.setBounds(jc.getX() - size.width/2,jc.getY()-size.height/2, size.width, size.height);
						}
						else if(jc instanceof JButton){
							String value = JOptionPane.showInputDialog(this, "Type the new component value");
							JButton btnTemporary = (JButton)jc;
							btnTemporary.setText(value);
							Dimension size = btnTemporary.getPreferredSize();
						    btnTemporary.setBounds(jc.getX() - size.width/2,jc.getY()-size.height/2, size.width, size.height);
						}
					}
						
				}
			}
		}
	}
}
