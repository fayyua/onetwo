package org.onetwo.common.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class Consoler {
	
	public static Consoler create(BufferedReader consoleReader){
		return new Consoler(consoleReader);
	}
	
	public static interface ConsoleAction {
		public void execute(String in);
	}
	
	public static class ExitAction implements ConsoleAction {
		@Override
		public void execute(String in) {
			System.exit(0);
		}
	}

	private BufferedReader consoleReader;

	public Consoler(BufferedReader consoleReader) {
		super();
		this.consoleReader = consoleReader;
	}
	
	public void waitIf(String in, ConsoleAction action){
		try {
			String input = null;
			while((input = consoleReader.readLine())!=null){
				if(input.equals(in))
					action.execute(input);
			}
		} catch (IOException e) {
			LangUtils.throwBaseException("console error: " + e.getMessage());
		}
	}
	
	public void exitIf(String in){
		waitIf(in, new ExitAction());
	}

}
