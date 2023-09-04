package crawler;


import app.AppProperties;
import jobs.JobQueue;
import jobs.job.FileJob;
import jobs.job.ScanType;
import result.ResultRetriever;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DirectoryCrawler implements Runnable{

    private File directory;
    private boolean dirSet = false;


    private volatile boolean running = true;
    private List<File> directoriesToSearch = new ArrayList<>();


    private final Queue<File> directories = new ConcurrentLinkedQueue<>();

    private Map<String,String> textFiles = new HashMap<>();
    private File pathToDelete;
    private boolean delete = false;


    private final JobQueue jobQueue;

    public DirectoryCrawler(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        while (running) {
            if(!directoriesToSearch.isEmpty()){
                for(File file : directoriesToSearch) {
                    searchFiles(file);
                    if(delete){
                        break;
                    }
                }
                if(delete){
                    removeFromDirectories(pathToDelete);
                    delete = false;
                }
            }
            try {
                Thread.sleep(AppProperties.dirCrawlerSleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public synchronized void stop(){
        running = false;
    }

    public synchronized void addFileToSearch(String filename) {
        File file = new File("src/" + filename);
//        System.out.println(file);

        //checking if the file already exists in the list
        boolean exists = false;
        for (File existing : directoriesToSearch) {
            if (existing.equals(file)) {
                exists = true;
            }
        }
        if (exists) {
            System.out.println("This file is already added to crawler");
            return;
        }
        directoriesToSearch.add(file);
    }
    private synchronized void removeFromDirectories(File path) {
        directoriesToSearch.remove(path);
    }


    public void searchFiles(File path) {
        File[] files = path.listFiles();
        if(path.getName().startsWith(AppProperties.fileCorpusPrefix)){
            addToJobQueue(path);
        }
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if(file.getName().startsWith(AppProperties.fileCorpusPrefix)){
                        addToJobQueue(file);
                    }
                    else {
                        searchFiles(file);
                    }
                }
            }
        }
        else{
            this.pathToDelete = path;
            this.delete = true;

            System.out.println("There is no such directory");
        }
    }


    private void addToJobQueue(File corpus){

        DateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        File[] corpusFiles = corpus.listFiles();
        boolean notAdded = false;

        for(File file : corpusFiles){
            String absPath = file.getAbsolutePath();
            String lastModified = date.format(file.lastModified());

            if(textFiles.containsKey(absPath)){
                if(!textFiles.get(absPath).equals(lastModified)){
//                    System.out.println("Izmena u "+ absPath);
                    System.out.println(corpus.getAbsolutePath()+" added to scan");
                    textFiles.put(absPath, lastModified);
                    ResultRetriever.cachedFileSummary.clear();
                    jobQueue.addJob(new FileJob(ScanType.FILE,corpus.getAbsolutePath()));
                    return;
                }
            }
            else {
                textFiles.put(absPath, lastModified);
                notAdded = true;
            }
        }
        if(notAdded) {
            System.out.println(corpus.getAbsolutePath()+" added to scan");
            ResultRetriever.cachedFileSummary.clear();
            jobQueue.addJob(new FileJob(ScanType.FILE, corpus.getAbsolutePath()));
        }
    }

}
