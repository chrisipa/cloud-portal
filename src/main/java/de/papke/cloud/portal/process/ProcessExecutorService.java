package de.papke.cloud.portal.process;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.IntStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessExecutorService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProcessExecutorService.class);

	public String execute(String command) {
		return execute(command, null);
	}
	
	public String execute(String command, File workingDirectory) {
		return execute(command, workingDirectory, new ByteArrayOutputStream());
	}
	
	public String execute(String command, File workingDirectory, OutputStream outputStream) {
		
		String output = null;
		
		try {
		
			// create command line from command string
	        CommandLine commandLine = CommandLine.parse(command);
	
	        // create executor for command
	        DefaultExecutor executor = new DefaultExecutor();
	        
	        // set working directory
	        if (workingDirectory != null) {
	        	executor.setWorkingDirectory(workingDirectory);
	        }
	        
	        // set possible exit values
	        executor.setExitValues(IntStream.range(0, 255).toArray());
	
	        // create output stream for command
	        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
	        executor.setStreamHandler(streamHandler);

	        // execute command
	        executor.execute(commandLine);
	        
	        // get output from command
	        output = outputStream.toString();
		}
		catch(IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return output;
	}
}
