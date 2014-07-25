package com.data;

public class DataManager {
	private static SourceManager mSourceManager;
	
	public DataManager() {
		mSourceManager = new SourceManager();
	}
	
	public static SourceManager getSourceManager() {
		if (mSourceManager == null)
			return new SourceManager();
		
		return mSourceManager;
	}
}
