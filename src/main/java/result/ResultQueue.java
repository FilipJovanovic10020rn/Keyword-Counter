package result;


import result.resJob.ResultJob;

import java.util.concurrent.BlockingQueue;

public class ResultQueue {

    private BlockingQueue<ResultJob> queue;

    public ResultQueue(BlockingQueue<ResultJob> queue) {
        this.queue = queue;
    }

    public void addJob(ResultJob job) {
        try {
            queue.put(job);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ResultJob takeJob() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
