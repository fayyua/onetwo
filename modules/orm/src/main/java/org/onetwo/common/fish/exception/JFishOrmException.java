package org.onetwo.common.fish.exception;

import org.onetwo.common.exception.BaseException;

@SuppressWarnings("serial")
public class JFishOrmException extends BaseException{

	public JFishOrmException() {
		super("jfish orm error!");
	}

	public JFishOrmException(String msg, String code) {
		super(msg, code);
	}

	public JFishOrmException(String msg, Throwable cause, String code) {
		super(msg, cause, code);
	}

	public JFishOrmException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public JFishOrmException(String msg) {
		super(msg);
	}

	public JFishOrmException(Throwable cause, String code) {
		super(cause, code);
	}

	public JFishOrmException(Throwable cause) {
		super(cause);
	}
	
	protected String getBaseCode(){
		return JFishErrorCode.ORM_ERROR;
	}

}
