package com.data;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javatwain.DotNetScanner;
import javatwain.IJavaProxy;
import javatwain.INativeProxy;

import javax.swing.Timer;

import com.server.WSHandler;
import com.util.ImageUtil;

import net.sf.jni4net.Bridge;
 
public class SourceManager implements INativeProxy {
	private IJavaProxy mScanner;
	private String[] mSources;
	private ScanAction mScanAction;
 
    public SourceManager() {
		initTWAIN();
		mScanAction = new ScanAction();
    }
	
	private void initTWAIN() {
		try {
			Bridge.init();
			Bridge.LoadAndRegisterAssemblyFrom(new java.io.File("libs\\jni\\JavaTwain.j4n.dll"));
		}
		catch (Exception e) {
            e.printStackTrace();
        }
		
		mScanner = new DotNetScanner();
		mScanner.RegisterListener(this);
		mSources = mScanner.GetSources();
	}
	
	public String[]	getSources() {
		return mSources;
	}
	
	public synchronized void acquireImage(int index) {
		mScanAction.setIndex(index);
		mScanAction.start();
	}
	
    @Override
	public boolean Notify(String message, String value) {
    	ArrayList<WSHandler> sessions = WSHandler.getAllSessions();
    	for (WSHandler session : sessions) {
    		session.sendImage(ImageUtil.getImageBytes(new File(value)));
    	}
		
        return true;
    }
    
    public class ScanAction {
    	private int mIndex;
    	private int mDelay = 1;
    	private Timer mTimer;
    	
    	public ScanAction() {
    		mTimer = new Timer(mDelay, mTaskPerformer);
    		mTimer.setRepeats(false);
    	}
    	
    	private ActionListener mTaskPerformer = new ActionListener() {
            @Override
    		public void actionPerformed(ActionEvent evt) {
        		mScanner.AcquireImage(mIndex);
            	ActionListener taskPerformer = new ActionListener() {
                    @Override
        			public void actionPerformed(ActionEvent evt) {
        				mScanner.CloseSource();
                    }
                };
        		int delay = 1; 
                Timer timer = new Timer(delay, taskPerformer);
                timer.setRepeats(false);
                timer.start();
            }
        };
        
        public void setIndex(int index) {
        	mIndex = index;
        }
        
        public void start() {
        	mTimer.start();
        }
    }
}
