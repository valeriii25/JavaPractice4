public class Main {
    public static void main(String[] args) {
        int liftsCount = 2;
        var building = new Building(liftsCount);
        var buildingUI = new BuildingUI(building);
        building.setUI(buildingUI);
        for (int i = 1; i <= liftsCount; i++) {
            buildingUI.manageLiftChanges(i, 0, LiftStatus.FREE);
        }
        buildingUI.setVisible(true);
        var generation = new Thread(building::generateRequests);
        generation.start();
    }
}
