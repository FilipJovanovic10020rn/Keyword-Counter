package scanner;

import app.AppProperties;
import jobs.job.ScanningJob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

// RucursiveTask<Map<Stirng,int>>
public class FileScanner extends RecursiveTask {

    private ScanningJob job;
    private List<File> txtFiles;
    private boolean listEmpty = false;
    private Map<String, Integer> resultsMap;

    public FileScanner(ScanningJob job) {
        this.job = job;
        this.txtFiles = new ArrayList<>();
        this.resultsMap = new HashMap<>();
        setInitialFiles();
    }

    private void setInitialFiles() {

        File corpus = new File(this.job.getQuery());
        File[] corpusFiles = corpus.listFiles();


        for(File file : corpusFiles){
            this.txtFiles.add(file);
        }
        // ako je iz nekog razloka lista emtpy da uopste ne udje
        this.listEmpty = this.txtFiles.isEmpty();
    }

    public FileScanner(ScanningJob job, List<File> txtFiles) {
//        System.out.println("kreirao sam se sa listom " + txtFiles);
        this.job = job;
        this.txtFiles = txtFiles;
        resultsMap = new HashMap<>();
    }


    @Override
    protected Object compute() {

        for(String keyword: AppProperties.keywords) {
            this.resultsMap.put(keyword,0);
        }


        if(listEmpty){
            System.out.println("Lista je prazna 😳 (jedan fajl je veci nego sto je moguca velicina)");
            return this.resultsMap;
        }
        if(checkFileSize()){
//            System.out.println("taman je velicina");
            this.calculateWords();
        }
        else {

            long sizeSum = 0L;
            List<File> leftList = new ArrayList<>();
            List<File> rightList = new ArrayList<>();

            for (File f : this.txtFiles) {
                rightList.add(f);
            }

            for (File file : txtFiles) {
//            System.out.println(file.getName() + " je velicine " + file.length());
                long size = file.length();

                if (sizeSum + size >= AppProperties.fileScanningSizeLimit) {
                    break;
                }
                sizeSum += size;
                leftList.add(file);

            }
            for (File file : leftList) {
                rightList.remove(file);
            }

            FileScanner left = new FileScanner(this.job, leftList);
            FileScanner right = new FileScanner(this.job, rightList);


            left.fork();
            Map<String,Integer> rightCount = (Map<String, Integer>) right.compute();

            Map<String,Integer> leftCount = (Map<String, Integer>) left.join();


            for(String key: AppProperties.keywords){
                int leftInt = leftCount.get(key);
                int rightInt = rightCount.get(key);
                this.resultsMap.put(key,leftInt+rightInt);
            }


        }
        return this.resultsMap;
    }

    private boolean checkFileSize(){

        long sizeSum = 0L;

        for (File file: txtFiles) {
            long size = file.length();
            sizeSum += size;
        }
        return sizeSum < AppProperties.fileScanningSizeLimit;
    }

    private void calculateWords() {

        for (File f : txtFiles) {
            try (BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split("\\W+");
                    for (String word : words) {
                        for(String keyword: AppProperties.keywords) {
                            if (word.equalsIgnoreCase(keyword)) {
                                int old = this.resultsMap.get(keyword);
                                this.resultsMap.put(keyword,old+1);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("puko sam pri citanju");
                e.printStackTrace();
            }

        }
    }

}
