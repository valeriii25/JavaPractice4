public class Main {
    public static void main(String[] args) {
        int elevatorCount = 2;
        var building = new Building(elevatorCount);
        var buildingUI = new BuildingUI(building);
        building.setUI(buildingUI);
        for (int i = 1; i <= elevatorCount; i++) {
            buildingUI.manageLiftChanges(i, 0, LiftStatus.FREE);
        }
        buildingUI.setVisible(true);
        var generation = new Thread(building::generateRequests);
        generation.start();
    }
}
