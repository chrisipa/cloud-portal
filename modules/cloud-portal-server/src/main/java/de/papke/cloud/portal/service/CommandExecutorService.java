package de.papke.cloud.portal.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.IntStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.unboundid.util.TeeOutputStream;

import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.stream.AutoFlushingPumpStreamHandler;

@Service
public class CommandExecutorService {

	private static final Logger LOG = LoggerFactory.getLogger(CommandExecutorService.class);
	
	public CommandResult execute(CommandLine commandLine) {
		return execute(commandLine, null);
	}

	public CommandResult execute(CommandLine commandLine, File workingDirectory) {
		return execute(commandLine, workingDirectory, new ByteArrayOutputStream());
	}

	public CommandResult execute(CommandLine commandLine, File workingDirectory, OutputStream outputStream) {

		CommandResult commandResult = new CommandResult();

		try {

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
			TeeOutputStream teeOutputStream = new TeeOutputStream(outputStream, byteArrayOutputStream);
			AutoFlushingPumpStreamHandler streamHandler = new AutoFlushingPumpStreamHandler(teeOutputStream);
			executor.setStreamHandler(streamHandler);

			// execute command
			int exitValue = executor.execute(commandLine);

			// fill command result
			commandResult.setOutput(byteArrayOutputStream.toString());
			commandResult.setSuccess(exitValue == 0 ? true : false);
		}
		catch(IOException e) {
			LOG.error(e.getMessage(), e);
		}

		return commandResult;
	}
}
