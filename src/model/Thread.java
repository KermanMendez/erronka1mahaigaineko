package model;

public class Thread implements Runnable {
    private java.lang.Thread thread;
    private String threadName;

    public Thread(String name) {
        this.threadName = name;
    }

    @Override
    public void run() {
        // TODO: Thread logic here
    	for (int i = 0; i < 5; i++) {
			System.out.println("Thread " + threadName + " is running: " + i);
			try {
				// Simulate some work with sleep
				java.lang.Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Thread " + threadName + " interrupted.");
			}
		}
        System.out.println("Thread " + threadName + "in execution.");
    }

    public void start() {
        if (thread == null) {
            thread = new java.lang.Thread(this, threadName);
            thread.start();
        }
    }
}