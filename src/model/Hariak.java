package model;

public class Hariak implements Runnable {

	private Thread thread;
	private String threadName;

	public Hariak(String name) {
		this.threadName = name;
	}

	@Override
	public void run() {
		for (int i = 0; i < 5; i++) {
			System.out.println("Thread " + threadName + " is running: " + i);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Thread " + threadName + " interrupted.");
			}
		}
		System.out.println("Thread " + threadName + " in execution.");
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}
}