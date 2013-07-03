package org.onetwo.common.web.asyn2;


abstract public class DeamonTask {
	
	private String name;
//	private Tasker<T> tasker;
	private int threadPriority = Thread.NORM_PRIORITY;
	
	private boolean finished;
	private Exception exception;
	
	private int taskIndex;
	
	
	public DeamonTask(String name, int taskIndex) {
		super();
		this.name = name;
		this.taskIndex = taskIndex;
//		this.tasker = tasker;
	}


	public void start(){
		try {
			final Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						execute();
					} catch (Exception e) {
						exception = e;
					}
					finished = true;
				}
				
			});
			t.setName(name+"#"+taskIndex);
			t.setPriority(threadPriority);
			t.start();
		} catch (Exception e) {
			this.exception = e;
		}
	}
	
	abstract public void execute() throws Exception; 

	public String getName() {
		return name;
	}

	public boolean isFinished() {
		return finished;
	}

	public Exception getException() {
		return exception;
	}
	
	public boolean isError(){
		return getException()!=null;
	}

	public int getTaskIndex() {
		return taskIndex;
	}

}
