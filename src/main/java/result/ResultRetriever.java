package result;



import result.resJob.GetJob;
import result.resJob.QueryJob;
import result.resJob.ResultJob;
import result.resJob.StopResultJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ResultRetriever implements Runnable{

    private volatile boolean running = true;

    public static Map<String,Future<Map<String,Integer>>> resultsWeb = new HashMap<>();
    public static Map<String,Future<Map<String,Integer>>> resultsFile = new HashMap<>();
    public static Map<String,Integer> cachedFileSummary = new HashMap<>();
    public static Map<String,Integer> cachedWebSummary = new HashMap<>();
    public static List<String> allLinks = new ArrayList<>();

//    public static

    ResultQueue resultQueue;

    private final ExecutorService executorService;
    public ResultRetriever(ResultQueue resultQueue) {
        this.resultQueue = resultQueue;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (running){
            // Todo neki reuslt que koji ce ovde da prosledi task

            ResultJob job = resultQueue.takeJob();
            if(job instanceof StopResultJob){
                this.stop();
            }
            else if(job instanceof GetJob){
                executorService.submit(new ResultRetriverGetWorker(job));
            }
            else if(job instanceof QueryJob){
                executorService.submit(new ResultRetriverQueryWorker(job));
            }
        }
        executorService.shutdown();
    }

    public synchronized void stop() {
        this.running = false;
    }

    public void clearFileCashe() {
        cachedFileSummary.clear();
    }

    public void clearWebCashe() {
        cachedWebSummary.clear();
    }
}
