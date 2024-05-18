import java.util.ArrayList;
import java.util.Random;

public class Building {
    protected final ArrayList<Lift> lifts = new ArrayList<>();
    public static final int UpperFloor = 9;
    public static final int LowerFloor = 0;
    private static final int MaxRequests = 10;
    private final Random rand = new Random();

    public Building(int numberOfElevators) {
        for (int i = 1; i <= numberOfElevators; i++) {
            var lift = new Lift(i, this);
            lifts.add(lift);
            Thread elevatorThread = new Thread(lift);
            elevatorThread.start();
        }
    }

    public void setUI(BuildingUI ui) {
        for (var lift : lifts) {
            lift.setBuildingUI(ui);
        }
    }

    public void chooseSuitableLift(int floor) {
        Lift suitableLift = null;
        int minDistance = 100;
        for (var lift : lifts) {
            int distance = checkDistance(lift, floor);
            if (distance < minDistance) {
                minDistance = distance;
                suitableLift = lift;
            }
        }
        if (suitableLift == null) return;
        suitableLift.addRequest(new Request(floor, Request.RequestType.IN));
    }

    private int checkDistance(Lift lift, int floor) {
        var currentFloor = lift.getCurrentFloor();
        var topFloor = lift.upperFloorRequest();
        var bottomFloor = lift.lowerFloorRequest();
        if (lift.getCurrentStatus() == LiftStatus.FREE) {
            return Math.abs(currentFloor - floor);
        } else if (lift.getCurrentStatus() == LiftStatus.UP) {
            if (floor >= currentFloor) return floor - currentFloor;
            return topFloor - currentFloor + topFloor - floor;
        } else if (lift.getCurrentStatus() == LiftStatus.DOWN) {
            if (floor <= currentFloor) return currentFloor - floor;
            return floor - currentFloor + floor - bottomFloor;
        } else if (lift.getCurrentStatus() == null) {
            return Integer.MAX_VALUE;
        }
        throw new IllegalArgumentException();
    }

    public void generateRequests() {
        for (int i = 0; i < MaxRequests; i++) {
            chooseSuitableLift(rand.nextInt(10));
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void inRequestOccured(int elevatorId) {
        createOutRequest(rand.nextInt(10), Request.RequestType.OUT, elevatorId);
    }

    public void createOutRequest(int floor, Request.RequestType type, int elevatorId) {
        lifts.get(elevatorId - 1).addRequest(new Request(floor, type));
    }
}
