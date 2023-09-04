package jobs;


import jobs.job.ScanType;
import jobs.job.ScanningJob;
import result.ResultRetriever;
import scanner.FileScanner;
import scanner.WebScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class JobDispatcher implements Runnable {


    private final JobQueue jobQueue;
    private final ExecutorService executorServiceWeb;
    private final ForkJoinPool executorServiceFile;

    private volatile boolean running = true;

    public JobDispatcher(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
        this.executorServiceWeb = Executors.newCachedThreadPool();
        this.executorServiceFile = new ForkJoinPool();
    }

    @Override
    public void run() {
        while (running){
            // TODO on treba da poziva tred pul?
            // TODO i verovatno treba da bude onaj que? i da ceka

            try {
                ScanningJob job = jobQueue.takeJob();

                if(job.getType() == ScanType.STOP){
                    this.stop();
                }
                else if(job.getType() == ScanType.FILE){

                    Future<Map<String,Integer>> resultMap  = executorServiceFile.submit(new FileScanner(job));
                    String fileName = job.getQuery().substring(job.getQuery().lastIndexOf("\\") + 1);
                    ResultRetriever.resultsFile.put(fileName,resultMap);
                    ResultRetriever.cachedFileSummary.clear();

                }
                else if (job.getType()== ScanType.WEB){

                    // todo ovo treba negde ubaciti
//                    Map<String,Long> listaLinkova = new HashMap<>();
//                    listaLinkova.put(job.getQuery(),System.currentTimeMillis());
//                    System.out.println(listaLinkova);

//                    if(!ResultRetriever.resultsWeb.containsKey(job.getQuery().toLowerCase())){
                        Future<Map<String,Integer>> mapFuture = executorServiceWeb.submit(new WebScanner(job,this.jobQueue));
                        ResultRetriever.resultsWeb.putIfAbsent(job.getQuery().toLowerCase(),mapFuture);
                        ResultRetriever.cachedWebSummary.clear();
//                    }


                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        executorServiceWeb.shutdown();
        executorServiceFile.shutdown();
    }

    public synchronized void stop() {
        this.running = false;
    }


}
