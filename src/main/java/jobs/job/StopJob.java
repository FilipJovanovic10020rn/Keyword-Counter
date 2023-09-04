package jobs.job;

import java.util.Map;
import java.util.concurrent.Future;

public class StopJob  implements ScanningJob {

    private ScanType type;
    private String query;
    private Future<Map<String,Integer>> resultMap;


    public StopJob(ScanType type) {

        this.type = type;

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

    @Override
    public int getHops() {
        return 0;
    }
}