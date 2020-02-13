package org.webcurator.core.reader;

import java.io.*;
import java.util.*;

import org.webcurator.core.harvester.agent.HarvesterStatusUtil;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.test.WCTTestUtils;

public class MockLogProvider implements LogProvider{

	private class LogFileFilter implements FileFilter {
	    public boolean accept(File f) {
	        if (f.isDirectory()) return false;
	        String name = f.getName().toLowerCase();
	        return name.endsWith("log") || name.endsWith("txt") || name.endsWith("xml");
	    }//end accept
	}//end class LogFileFilter
	
	private String basePath = "";
	private String pageImagePrefix = "PageImage";
	private String aqaReportPrefix = "aqa-report";
	
	public MockLogProvider(String basePath)
	{
		if(basePath.endsWith("/"))
		{
			basePath = basePath.substring(basePath.length()-1);
		}
		this.basePath = basePath;
	}
	
	public File getLogFile(String aJob, String aFileName)
	{
        String logFilename = basePath + "/" + aFileName;
        File logFile = WCTTestUtils.getResourceAsFile(logFilename);

        return logFile;
	}
	
	public File getAQAFile(String aJob, String aFileName)
	{
        String aqaFilename = basePath + "/" + aFileName;
        File aqaFile = WCTTestUtils.getResourceAsFile(aqaFilename);

        return aqaFile;
	}

	public List<String> getLogFileNames(String aJob)
	{
		List<String> filenames = new ArrayList<>();
        File dir = WCTTestUtils.getResourceAsFile(basePath);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(new LogFileFilter());
            for (int i = 0; i < files.length; i++) {
                filenames.add(files[i].getName());
            }
        }
		return filenames;
	}

	public List<LogFilePropertiesDTO> getLogFileAttributes(String aJob)
	{
		List<LogFilePropertiesDTO> arProps = new ArrayList<>();
		File dir = WCTTestUtils.getResourceAsFile(basePath);
		if(dir.isDirectory())
		{
			File[] files = dir.listFiles(new LogFileFilter());
			for(int i = 0; i < files.length; i++)
			{
				File f = files[i];
				LogFilePropertiesDTO props = new LogFilePropertiesDTO();
				props.setLastModifiedDate(new Date(f.lastModified()));
				props.setLengthString(HarvesterStatusUtil.formatData(f.length()));
				props.setName(f.getName());
				props.setPath(f.getAbsolutePath());
				
        		//Special case for AQA reports and images
        		if(f.getName().startsWith(pageImagePrefix))
        		{
        			props.setViewer("content-viewer.html");
        		}
        		else if(f.getName().startsWith(aqaReportPrefix))
        		{
        			props.setViewer("aqa-viewer.html");
        		}
        		
				arProps.add(props);
			}
		}

		return arProps;
	}
}
