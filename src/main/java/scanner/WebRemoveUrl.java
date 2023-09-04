package scanner;

import app.MainCLI;
import result.ResultRetriever;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebRemoveUrl extends Thread {

    public WebRemoveUrl() {
    }

    @Override
    public void run() {
        while (true) {

            List<String> linksToDelete = new ArrayList<>();
            for(Map.Entry<String,Long> entry : MainCLI.linksTimer.entrySet()){
                if(entry.getValue() < System.currentTimeMillis()){
                    linksToDelete.add(entry.getKey());
                }
            }
            synchronized (MainCLI.parentChildLinks){
                for(String link : linksToDelete){
                    MainCLI.linksTimer.remove(link.toLowerCase());
                    MainCLI.parentChildLinks.remove(link.toLowerCase());
                    ResultRetriever.resultsWeb.remove(link.toLowerCase());
                }
            }
            linksToDelete.clear();
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}