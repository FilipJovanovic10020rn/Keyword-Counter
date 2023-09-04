package result.resJob;

public class StopResultJob implements ResultJob {

    private String promth;

    public StopResultJob(String promth) {
        this.promth = promth;
    }

    @Override
    public String getPromth() {
        return promth;
    }
}