package de.papke.cloud.portal.service;

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

import de.papke.cloud.portal.pojo.CommandResult;

@Service
public class CommandExecutorService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CommandExecutorService.class);

	public CommandResult execute(String command) {
		return execute(command, null);
	}
	
	public CommandResult execute(String command, File workingDirectory) {
		return execute(command, workingDirectory, new ByteArrayOutputStream());
	}
	
	public CommandResult execute(String command, File workingDirectory, OutputStream outputStream) {
		
		CommandResult commandResult = new CommandResult();
		
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
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        PumpStreamHandler streamHandler = new PumpStreamHandler(byteArrayOutputStream);
	        executor.setStreamHandler(streamHandler);

	        // execute command
	        int exitValue = executor.execute(commandLine);
	        
	        // fill command result
	        commandResult.setOutput(byteArrayOutputStream.toString());
	        commandResult.setSuccess(exitValue == 0 ? true : false);
	        
	        // write to request output stream
	        outputStream.write(commandResult.getOutput().getBytes());
	        outputStream.flush();
		}
		catch(IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return commandResult;
	}
}
