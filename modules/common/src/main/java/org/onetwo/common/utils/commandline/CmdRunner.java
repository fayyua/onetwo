package org.onetwo.common.utils.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.onetwo.common.utils.LangUtils;

public class CmdRunner {

	protected Logger logger = Logger.getLogger(CmdRunner.class);

	protected CommandManager cmdManager;
	
	public CmdRunner() {
	}

	/**
	 * main template
	 * @param args
	 * @throws IOException
	 */
	public void run(String[] args) {
		startAppContext(args);
		loadCommand(args);
		waitForCommand();
	}
	
 
	protected void startAppContext(String[] args) {
	}

	protected void loadCommand(String[] args) {
		cmdManager = new DefaultCommandManager();
		cmdManager.addCommand(new ExitCommand());
		cmdManager.addCommand(new HelpCommand());
	}
	
	protected CmdContext createCmdContext(BufferedReader br){
		return new SimpleCmdContext(br);
	}

	protected void waitForCommand() {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			CmdContext cmdContext = createCmdContext(br);
			while ((str = br.readLine()) != null) {
				Command cmd = cmdManager.getCommand(str);
				if(cmd!=null)
					cmd.execute(cmdContext);
			}

		} catch (Exception e) {
			logger.error("execute command error!", e);
		} finally{
			LangUtils.closeIO(reader);
		}
	}

}
