package result;

import app.AppProperties;
import app.MainCLI;
import result.resJob.ResultJob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetriverQueryWorker implements Runnable {

    private ResultJob job;

    public ResultRetriverQueryWorker(ResultJob job) {
            this.job = job;
    }


    @Override
    public void run() {
        if(!this.job.getPromth().contains("|")){
            System.out.println("Nepravilno unesena komanda");
            System.out.println("Hint: Primer komande query web|google.com");
            return;
        }
        String[] promth = this.job.getPromth().toLowerCase().split("\\|");

        if(promth[0].equals("file")){
            if(promth[1].equals("summary")){
                if(!ResultRetriever.cachedFileSummary.isEmpty()){
                    System.out.println(ResultRetriever.cachedFileSummary);
                }
                else {
                    try {
                        getCalculateFileSummary();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else if(promth[1].startsWith(AppProperties.fileCorpusPrefix)) {
                if (!ResultRetriever.resultsFile.containsKey(promth[1])) {
                    System.out.println("Ovaj korpus nije ubacen u listu za citanje");
                } else {
                    if(!ResultRetriever.resultsFile.get(promth[1]).isDone()){
                        System.out.println("Nije jos izracunato jos za " + promth[1]);
                        return;
                    }
                    try {
                        System.out.println("Za " + promth[1] + " rezultat je : " + ResultRetriever.resultsFile.get(promth[1]).get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else{
                System.out.println("Nepravilno unesena komanda");
                System.out.println("Hint: Unesite naziv korpusa ili summary kao drugi parametar");
            }
        }
        else if(promth[0].equals("web")){
            if(promth[1].equals("summary")){
                if(!ResultRetriever.cachedWebSummary.isEmpty()){
                    System.out.println(ResultRetriever.cachedWebSummary);
                }
                else {
                    try {
                        this.getCalculateWebSummary();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else if(promth[1].startsWith("http")){
                Map<String,Integer> result = new HashMap<>();
                try {
//                    ReadWriteLock lock = new ReentrantReadWriteLock();
                    synchronized (MainCLI.parentChildLinks) {
                        System.out.println("Cekam i ja da ne bi doslo do greske");
                    }
                    result = this.getCalculateWebLinkRecursive(promth[1],AppProperties.hopCount);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(result==null){
                    return;
                }
                else {
                    System.out.println(result);
                }
            }
            else{
                System.out.println("Nepravilno unesena komanda");
                System.out.println("Hint: Unesite naziv domena ili summary kao drugi parametar");
            }
        }
        else{

        }
    }

    private void getCalculateFileSummary() throws ExecutionException, InterruptedException {
        Map<String,Integer> summary = new HashMap<>();
        if(ResultRetriever.resultsFile.isEmpty()){
            System.out.println("Nema fajlova");
            return;
        }
        for (Map.Entry<String, Future<Map<String,Integer>>> entry : ResultRetriever.resultsFile.entrySet()){

            for(String keyWord: AppProperties.keywords) {
                if(!entry.getValue().isDone()){
                    System.out.println("Nije jos izracunato jos za " + entry.getKey());
                    return;
                }
                if(!summary.containsKey(keyWord)){
                    summary.put(keyWord,entry.getValue().get().get(keyWord));
                }
                else{
                    Integer prev = summary.get(keyWord);
                    Integer adding = entry.getValue().get().get(keyWord);
                    summary.put(keyWord,prev+adding);
                }
            }
        }
        ResultRetriever.cachedFileSummary = summary;
        System.out.println(ResultRetriever.cachedFileSummary);
    }

    private void getCalculateWebSummary() throws ExecutionException, InterruptedException {
        Map<String,Integer> summary = new HashMap<>();
        if(ResultRetriever.resultsWeb.isEmpty()){
            System.out.println("Nema ni jednog ubacenog linka");
            return;
        }
        for (Map.Entry<String, Future<Map<String,Integer>>> entry : ResultRetriever.resultsWeb.entrySet()){

            for(String keyWord: AppProperties.keywords) {
                if(!entry.getValue().isDone()){
                    System.out.println("Nije jos izracunato jos za " + entry.getKey());
                    return;
                }
                if(!summary.containsKey(keyWord)){
                    summary.put(keyWord,entry.getValue().get().get(keyWord));
                }
                else{
                    Integer prev = summary.get(keyWord);
                    Integer adding = entry.getValue().get().get(keyWord);
                    summary.put(keyWord,prev+adding);
                }
            }
        }
        ResultRetriever.cachedWebSummary = summary;
        System.out.println(ResultRetriever.cachedWebSummary);
    }

    private Map<String, Integer> getCalculateWebLinkRecursive(String url, int depth) throws ExecutionException, InterruptedException {
        if(ResultRetriever.resultsWeb.isEmpty()){
            System.out.println("Nema ni jednog ubacenog linka");
            return null;
        }
        if(!MainCLI.parentChildLinks.containsKey(url.toLowerCase()) && depth == AppProperties.hopCount){
            System.out.println("Ovaj link nije ubacen u listu");
            return null;
        }
        if(!ResultRetriever.resultsWeb.get(url.toLowerCase()).isDone()) {
            System.out.println(url + " nije gotov sa racunanjem");
            return null;
        }

        if (depth == 0) {
            if(!ResultRetriever.resultsWeb.containsKey(url.toLowerCase())){
                return null;
            }
            return ResultRetriever.resultsWeb.get(url.toLowerCase()).get();
        }

        Map<String,Integer> summary = new HashMap<>();
        for(String keyWord:AppProperties.keywords){
            summary.put(keyWord,ResultRetriever.resultsWeb.get(url.toLowerCase()).get().get(keyWord));
        }
        // ovo ko je smislio da ovako radi da bog da crko xd
//        summary = ResultRetriever.resultsWeb.get(url).get();


//        synchronized (MainCLI.parentChildLinks){
            if(MainCLI.parentChildLinks.containsKey(url.toLowerCase())){
                List<String> children = MainCLI.parentChildLinks.get(url.toLowerCase());
                for(String child : children){
                    Map<String,Integer> tempMap = getCalculateWebLinkRecursive(child, depth - 1);
                    if(tempMap == null){
                        return null;
                    }
                    for(String keyWord: AppProperties.keywords) {
                        Integer prev = summary.get(keyWord);
                        Integer adding = tempMap.get(keyWord);
                        summary.put(keyWord, prev + adding);
                    }
                }
            }
//        }

        return summary;
    }
}
