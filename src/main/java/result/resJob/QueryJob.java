package result.resJob;

public class QueryJob implements ResultJob {

    private String promth;

    public QueryJob(String promth) {
        this.promth = promth;
    }

    @Override
    public String getPromth() {
        return promth;
    }
}
