import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.eclipse.swt.widgets.Button;

import java.awt.Checkbox;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ProgressBar;

import ifs.fnd.service.IfsEncryption;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class Check {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Check window = new Check();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read instance Conf file and fill the variables
	 */
	//IFS Home
	String IFS_HOME, INSTANCE;
	
	//Passwords
	String IFSSYS_PW, IFSADMIN_PW, IFSCONNECT_PW, IFSPLSQLAP_PW, IFSPRINT_PW, IFSWEBCONFIG_PW, IFS_PW;
	
	//JDBC
	String JDBC_URL;
	
	String LOG_LINE;
	String NEWLINE = "\n";
	
	public void addLogLine(String line, StyledText logger){
		LOG_LINE = "";
		LOG_LINE = line;
		
		if (line.endsWith("NEWLINE")){
			//logger.setText(logger.getText() + line);
			logger.append(line);
		}
		else {
			//logger.setText(logger.getText() + line + NEWLINE);
			logger.append(line + NEWLINE);
		}
		//logger.append(fdfd);
		//logger.setTopIndex(logger.getLineCount() - 1);
	}
	
	public String getParams(){
		IFS_HOME = "E:\\IFSAppInstallation\\App8SP2_ifshome\\";
		INSTANCE = "DSJAPP8SP2";
		String confFilePath_ = IFS_HOME +  "instance\\" + INSTANCE + "\\" +  INSTANCE + "_configuration.xml";		
		File xmlConf = new File(confFilePath_);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlConf);
		    doc.getDocumentElement().normalize();
		    
		    NodeList nList = doc.getElementsByTagName("internal-users");
		    Node nNode = nList.item(0);
		    Element eElement = (Element) nNode;
		    String ifssys_encrypted_, ifsadmin_encrypted_, ifsconnect_encrypted_, ifsplsqlap_encrypted_, ifsprint_encrypted_, ifswebconfig_encrypted_, ifs_encrypted_;
		    
		    //IFSSYS
		    ifssys_encrypted_ = eElement.getElementsByTagName("ifs.ifssys.password.encrypted").item(0).getTextContent();
		    IFSSYS_PW = IfsEncryption.decrypt(ifssys_encrypted_);
		    
		    //IFSADMIN
		    ifsadmin_encrypted_ = eElement.getElementsByTagName("ifs.ifsadmin.password.encrypted").item(0).getTextContent();
		    IFSADMIN_PW = IfsEncryption.decrypt(ifsadmin_encrypted_);
		    
		    //IFSCONNECT
		    ifsconnect_encrypted_ = eElement.getElementsByTagName("ifs.ifsconnect.password.encrypted").item(0).getTextContent();
		    IFSCONNECT_PW = IfsEncryption.decrypt(ifsconnect_encrypted_);
		    
		    //IFSPLSQLAP
		    ifsplsqlap_encrypted_ = eElement.getElementsByTagName("ifs.ifsplsqlap.password.encrypted").item(0).getTextContent();
		    IFSPLSQLAP_PW = IfsEncryption.decrypt(ifsplsqlap_encrypted_);
		    
		    //IFSPRINT
		    ifsprint_encrypted_ = eElement.getElementsByTagName("ifs.ifsprint.password.encrypted").item(0).getTextContent();
		    IFSPRINT_PW = IfsEncryption.decrypt(ifsprint_encrypted_);
		    
		    //IFSWEBCONFIG
		    ifswebconfig_encrypted_ = eElement.getElementsByTagName("ifs.ifswebconfig.password.encrypted").item(0).getTextContent();
		    IFSWEBCONFIG_PW = IfsEncryption.decrypt(ifswebconfig_encrypted_);
		    
		    // JDBC URL
		    JDBC_URL = eElement.getElementsByTagName("internal.jdbc.url").item(0).getTextContent();
		    
		    
		    // Cluster configurations
		    
		    
		    
		    //IFS
		    ifs_encrypted_ = eElement.getElementsByTagName("ifs.ifswebconfig.password.encrypted").item(0).getTextContent();
		    IFS_PW = IfsEncryption.decrypt(ifs_encrypted_);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		return JDBC_URL;
	}
	/**
	 * Connect to database using given username, password and verify connection
	 */	
	private String testDBConnection(String username, String password, StyledText logger) {
        Connection connection = null;
        String result;
        try {
		Properties p = new Properties();
		p.setProperty("user", username);
        p.setProperty("password", password);
        addLogLine("Connecting to Database with user " + username, logger);
        connection = DriverManager.getConnection(JDBC_URL, p);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select 1 from dual");
        rs.close();
        stmt.close();
        //JOptionPane.showMessageDialog(null, "Connection successful.", "Test JDBC Connection", JOptionPane.INFORMATION_MESSAGE);
        result = "Database Connection Success!";
        addLogLine(result, logger);
        }
        catch (SQLException e) {
        	String errMsg = e.getMessage();
        	result = errMsg;
        	//JOptionPane.showMessageDialog(null, "Cannot connect to the database: " + errMsg, "Initialization Error", JOptionPane.INFORMATION_MESSAGE);
        }
		return result;
	}
	
	/**
	 * Run check_server_status.cmd
	 */
	private String checkServerStatus(StyledText logger) {
		String binPath_ = IFS_HOME +  "instance\\" + INSTANCE + "\\bin\\";
		List<String> commands = new ArrayList<String>();
        commands.add(binPath_ + "check_server_status.cmd");
        commands.add(IFS_PW);
		
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.redirectErrorStream(true);
        Process p;
		try {
			p = builder.start();
	        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
	        String line;
	        while (true) {
	            line = r.readLine();
	            if (line == null) {	            	
	            	break; 
	            	}
	            if (line.startsWith("Exiting WebLogic Scripting Tool") ){	            	
	            	String newLine = "\n\r";
                    w.write(newLine);
                    break;
	            }
	            System.out.println(line);
	            addLogLine(line, logger);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}

		return "success";
	}

	/**
	 * Run check_application_status.cmd
	 */
	private String checkApplicationStatus(StyledText logger) {	
		String binPath_ = IFS_HOME +  "instance\\" + INSTANCE + "\\bin\\";
		List<String> commands = new ArrayList<String>();
        commands.add(binPath_ + "check_application_status.cmd");
        commands.add("-password=" + IFS_PW);
		
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.redirectErrorStream(true);
        Process p;
		try {
			p = builder.start();
	        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
	        String line;
	        while (true) {
	            line = r.readLine();
	            if (line == null) {	            	
	            	break; 
	            	}
	            if (line.startsWith("Exiting WebLogic Scripting Tool") ){	            	
	            	String newLine = "\n\r";
                    w.write(newLine);
                    break;
	            }
	            System.out.println(line);
	            addLogLine(line, logger);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}

		return "success";
	}	
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(611, 367);
		shell.setText("SWT Application");		
		
		Button btnClick = new Button(shell, SWT.NONE);
		
		btnClick.setBounds(297, 89, 75, 25);
		btnClick.setText("Click");
		
		Label lblAnswer = new Label(shell, SWT.NONE);
		lblAnswer.setBounds(10, 119, 575, 15);
		
		ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(30, 170, 555, 17);
		
		TextViewer textViewer = new TextViewer(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		StyledText loggerTxt = textViewer.getTextWidget();

		loggerTxt.setEditable(false);
		loggerTxt.setBounds(30, 202, 555, 100);

		
		
		btnClick.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				//ToDo:
				//Prompt for IFS Home and instance. currently it's hardquoted in getParams()
				//read the conf file and fetch passwords for internal users
				getParams();
				//lblAnswer.setText();
				addLogLine("Starting System Check...", loggerTxt);
				addLogLine("Verifying database connection",loggerTxt);
				testDBConnection("IFSSYS", IFSSYS_PW, loggerTxt);
				progressBar.setSelection(progressBar.getSelection()+ 10);
				
				addLogLine("***********************************************************", loggerTxt);
				addLogLine("Verifying internal users",loggerTxt);
				testDBConnection("IFSADMIN", IFSADMIN_PW, loggerTxt);
				progressBar.setSelection(progressBar.getSelection()+ 10);
				
				addLogLine("***********************************************************", loggerTxt);
				addLogLine("Verifying Middleware server status...",loggerTxt);
				addLogLine("Check Server Status...",loggerTxt);				
				checkServerStatus(loggerTxt);
				progressBar.setSelection(progressBar.getSelection()+ 10);
				
				addLogLine("Check Application Status...",loggerTxt);
				checkApplicationStatus(loggerTxt);
				progressBar.setSelection(progressBar.getSelection()+ 10);

			}
		});

	}
}
