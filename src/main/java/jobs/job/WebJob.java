package jobs.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class WebJob implements ScanningJob {

    private ScanType type;
    private String query;
    private Future<Map<String,Integer>> resultMap;
    private List<String> listOfScannedLinks;
    private int hops;


    public WebJob(ScanType type, String query, int hops) {

        this.type = type;
        this.query = query;
        this.listOfScannedLinks = new ArrayList<>();
        this.hops = hops;

    }

    public List<String> getListOfScannedLinks() {
        return listOfScannedLinks;
    }

    @Override
    public int getHops() {
        return hops;
    }

    public void addToList(String link) {
        this.listOfScannedLinks.add(link);
    }

    @Override
    public ScanType getType() {
        return this.type;
    }

    @Override
    public String getQuery() {
        return this.query;
    }

    @Override
    public Future<Map<String, Integer>> initiate() {
        // TODO ovde verovatno logika kojom se racuna
        return null;
    }
}