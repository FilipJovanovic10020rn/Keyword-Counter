package jobs;



import jobs.job.ScanningJob;

import java.util.concurrent.BlockingQueue;

public class JobQueue {

    private BlockingQueue<ScanningJob> queue;

    public JobQueue(BlockingQueue<ScanningJob> queue) {
        this.queue = queue;
    }

    public void addJob(ScanningJob job) {
        try {
            queue.put(job);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ScanningJob takeJob() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
