package result.resJob;

public class GetJob implements ResultJob {

    private String promth;

    public GetJob(String promth) {
        this.promth = promth;
    }

    @Override
    public String getPromth() {
        return promth;
    }
}
