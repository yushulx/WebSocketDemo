package com.ui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.data.DataManager;
import com.data.SourceManager;
import com.server.WSHandler;
import com.server.WebSocketServer;
import com.util.ImageUtil;

 
public class UIMain extends JPanel
                             implements ActionListener {
    private JButton mLoad, mSend, mScan;
    private JFileChooser mFileChooser;
    private JLabel mImage;
    private byte[] mData;
    private WebSocketServer mWebSocketServer;
    private static DataManager mDataManager;
 
    public UIMain() {
        super(new BorderLayout());
        
        mDataManager = new DataManager();
        
        //Create a file chooser
        mFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".png.jpg", "png","jpg");
        mFileChooser.setFileFilter(filter);
        mLoad = new JButton("Load");
        mLoad.addActionListener(this);

        mSend = new JButton("Send");
        mSend.addActionListener(this);
        mSend.setEnabled(false);
		
        mScan = new JButton("Scan");
        mScan.addActionListener(this);
        mScan.setEnabled(true);
        // button panel
        JPanel buttonPanel = new JPanel(); 
        buttonPanel.add(mLoad);
		buttonPanel.add(mSend);
		buttonPanel.add(mScan);
        add(buttonPanel, BorderLayout.PAGE_START);
        
        // image panel
		JPanel imageViewer = new JPanel();
		mImage = new JLabel();
		mImage.setSize(480, 640);
		imageViewer.add(mImage);
		add(imageViewer, BorderLayout.CENTER);
		
		// WebSocketServer
		mWebSocketServer = new WebSocketServer();
		mWebSocketServer.start();
    }

    @Override
	public void actionPerformed(ActionEvent e) {

        if (e.getSource() == mLoad) {
	        
            int returnVal = mFileChooser.showOpenDialog(UIMain.this);
 
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = mFileChooser.getSelectedFile();     
                
                // load image data to byte array
                mData = ImageUtil.getImageBytes(file);
                mImage.setIcon(new ImageIcon(mData));
                mSend.setEnabled(true);
            }
        } 
        else if (e.getSource() == mSend) {
        	ArrayList<WSHandler> sessions = WSHandler.getAllSessions();
        	for (WSHandler session : sessions) {
        		session.sendImage(mData);
        	}
        	mSend.setEnabled(false);
        }
        else if (e.getSource() == mScan) {
        	DataManager.getSourceManager().acquireImage(1);
        }
    }
 
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("WebSocket Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new UIMain());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(480, 700);
        
        double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        frame.setLocation((int)(width - frameWidth) / 2, (int)(height - frameHeight) / 2);
    }
 
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}

