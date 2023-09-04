package app;




import crawler.DirectoryCrawler;
import jobs.JobDispatcher;
import jobs.JobQueue;
import jobs.job.ScanType;
import jobs.job.ScanningJob;
import jobs.job.StopJob;
import jobs.job.WebJob;
import result.ResultQueue;
import result.ResultRetriever;
import result.resJob.GetJob;
import result.resJob.QueryJob;
import result.resJob.ResultJob;
import result.resJob.StopResultJob;
import scanner.WebRemoveUrl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainCLI {

    public static Map<String, List<String>> parentChildLinks = new HashMap<>();
    public static Map<String,Long> linksTimer = new HashMap<>();
    public static void main(String[] args) {

        System.out.print("Starting...\n");

        Scanner scanner = new Scanner(System.in);

        // App Properties
        AppProperties.initProperties();


        // Job Queue
        BlockingQueue<ScanningJob> scanningJobs = new LinkedBlockingQueue<>();
        JobQueue jobQueue = new JobQueue(scanningJobs);

        // Result Queue
        BlockingQueue<ResultJob> resultJobs = new LinkedBlockingQueue<>();
        ResultQueue resultQueue = new ResultQueue(resultJobs);

        //DirectoryCrawler thread
        DirectoryCrawler directoryCrawler = new DirectoryCrawler(jobQueue);
        Thread directoryCrawlerThread = new Thread(directoryCrawler);
        directoryCrawlerThread.start();


        // JobDispatcher thread
        JobDispatcher jobDispatcher = new JobDispatcher(jobQueue);
        Thread jobDispatcherThread = new Thread(jobDispatcher);
        jobDispatcherThread.start();


        // ResultRetriver thread
        ResultRetriever resultRetriever = new ResultRetriever(resultQueue);
        Thread resultRetriverThread = new Thread(resultRetriever);
        resultRetriverThread.start();

        // Web Remover
        WebRemoveUrl webRemoveUrlThread = new WebRemoveUrl();
        webRemoveUrlThread.setDaemon(true);
        webRemoveUrlThread.start();


        System.out.print("Started...\n");

        while (true) {


            String input = scanner.nextLine();

            String[] parts = input.trim().split("\\s+");
            String command = parts[0];
            String argument = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "ad":
                    if (argument.isEmpty()) {
                        System.out.println("Missing filename");
                    } else {
                        String filename = argument;
                        directoryCrawler.addFileToSearch(filename);
                    }
                    break;
                case "aw":
                    if (argument.isEmpty()) {
                        System.out.println("Missing web link");
                    } else {
                        String webName = argument;
                        if(!parentChildLinks.containsKey(argument.toLowerCase())) {
                            jobQueue.addJob(new WebJob(ScanType.WEB, argument, AppProperties.hopCount));
                            linksTimer.put(argument.toLowerCase(),System.currentTimeMillis()+AppProperties.urlRefreshTime);
                            resultRetriever.clearWebCashe();
                        }
                        else {
                            System.out.println("Link already added");
                        }
                    }
                    break;
                case "get":
                    if (argument.isEmpty()) {
                        System.out.println("Missing filename");
                    }
                    resultQueue.addJob(new GetJob(argument));
                    break;
                case "query":
                    if (argument.isEmpty()) {
                        System.out.println("Missing filename ");
                    }
                    resultQueue.addJob(new QueryJob(argument));
                    break;
                case "cws":
                    if (argument.isEmpty()) {
                        resultRetriever.clearWebCashe();
                        break;
                    }
                    System.out.println("cfs doesnt get parms");
                    break;
                case "cfs":
                    if (argument.isEmpty()) {
                        resultRetriever.clearFileCashe();
                        break;
                    }
                    System.out.println("cfs doesnt get parms");
                    break;
                case "stop":
                    if (argument.isEmpty()) {
                        System.out.println("Stopping program...");
                        directoryCrawler.stop();
                        resultQueue.addJob(new StopResultJob("stop"));
                        jobQueue.addJob(new StopJob(ScanType.STOP));
                        scanner.close();
                        System.out.println("Program stopped...");
                        return;
                    }
                    System.out.println("stop doesnt get parms");
                    break;
                case "help":
                    String help = """
                            commands:
                                *   ad directory_name
                                *   aw web_link
                                *   get (web or file)|(directory_name or web_link or summary)
                                *   query (web or file)|(directory_name or web_link or summary)
                                *   csw
                                *   csf
                                *   stop
                            """;
                    System.out.println(help);
                    break;
                default:
                    System.out.println("Invalid command. Try help");
            }

        }
    }


}