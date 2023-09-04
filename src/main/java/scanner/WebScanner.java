package scanner;

import app.AppProperties;
import app.MainCLI;
import jobs.JobDispatcher;
import jobs.JobQueue;
import jobs.job.FileJob;
import jobs.job.ScanType;
import jobs.job.ScanningJob;
import jobs.job.WebJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import result.ResultRetriever;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebScanner implements Callable<Map<String, Integer>> {

    private ScanningJob job;
    private Map<String, Integer> resultsMap;

    private final JobQueue jobQueue;


    public WebScanner(ScanningJob job,JobQueue jobQueue) {
        this.job = job;
        this.jobQueue = jobQueue;
        this.resultsMap = new HashMap<>();
    }


//  get web|https://en.wikipedia.org/wiki/Number
//  get web|https://about.gitlab.com/blog/2022/04/18/comparing-static-site-generators/
//  query web|https://about.gitlab.com/blog/2022/04/18/comparing-static-site-generators/
//  query web|https://en.wikipedia.org/wiki/Number
//  get web|summary
//  query web|summary
//  aw https://en.wikipedia.org/wiki/Number
//  aw https://about.gitlab.com/blog/2022/04/18/comparing-static-site-generators/
    @Override
    public Map<String, Integer> call() throws Exception {

        if(job.getHops()>0){
            this.findLinksOnSite(this.job.getQuery(),1);
        }

        //        System.out.println(MainCLI.parentChildLinks);

//        if(ResultRetriever.resultsWeb.containsKey(this.job.getQuery())){
//            return ResultRetriever.resultsWeb.get(this.job.getQuery()).get();
//        }
        return this.calculateWords(this.job.getQuery());

    }

    private Map<String, Integer> calculateWords(String url) {
        Map<String, Integer> reciMoje = new HashMap<>();
        for (String key : AppProperties.keywords) {
            reciMoje.put(key.toLowerCase(), 0);
        }
        try {
            Document doc = Jsoup.connect(url).get();
            String text = doc.text();
            String[] words = text.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().split("\\s+");

            for (String word : words) {
                if (reciMoje.containsKey(word.toLowerCase())) {
                    int count = reciMoje.get(word.toLowerCase());
                    reciMoje.put(word.toLowerCase(), count + 1);
                }
            }
        }
        catch(IOException e) {
//            e.printStackTrace();
//            System.out.println("puko sam");
            return reciMoje;
        }
//        if(url.equals("https://en.wikipedia.org/wiki/Number"))
//            System.out.println(reciMoje);
        return reciMoje;
    }
    private void findLinksOnSite(String url,int brojac) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");


            // prvo ih ubacim u listu da ne bi doslo do neke greske
            // i ovo je u sync bloku da se ne bi desilo da resRet proba da cita a da jos nisam stavio koja su mu deca
            synchronized (MainCLI.parentChildLinks) {
                for (int i = 0; i < links.size(); i++) {
                    String link = links.get(i).attr("abs:href").replaceAll("\\s+", " ");

                    if(link.toLowerCase().equals(url.toLowerCase())){
                        continue;
                    }

                    MainCLI.parentChildLinks.putIfAbsent(url.toLowerCase(), new ArrayList<>());

                    if (!MainCLI.parentChildLinks.get(url.toLowerCase()).contains(link.toLowerCase())) {
                        MainCLI.parentChildLinks.get(url.toLowerCase()).add(link.toLowerCase());
                    }
                    if(job.getHops()-brojac>0){
                        findLinksOnSite(link,brojac+1);
                    }
                }
            }
            // druga for petlja gde ih saljem da ne bi bilo slucaja da dete naidje na link koji mozda ima roditelj
            for (int i = 0; i < links.size(); i++) {
                String link = links.get(i).attr("abs:href").replaceAll("\\s+", " ");
                if(!ResultRetriever.resultsWeb.containsKey(link.toLowerCase())) {
                    this.jobQueue.addJob(new WebJob(ScanType.WEB, link, 0));
                    MainCLI.linksTimer.put(link,System.currentTimeMillis()+AppProperties.urlRefreshTime);
                }
            }
        }catch (IOException e){
//            e.printStackTrace();
        }
    }




}
