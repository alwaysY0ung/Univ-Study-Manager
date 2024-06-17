public class UpdateFlag {
    private volatile boolean updateRequired;

    public synchronized boolean isUpdateRequired() {
        return updateRequired;
    }

    public synchronized void setUpdateRequired(boolean updateRequired) {
        this.updateRequired = updateRequired;
    }

    public synchronized boolean checkAndReset() {
        boolean wasUpdateRequired = updateRequired;
        updateRequired = false;
        return wasUpdateRequired;
    }
}
