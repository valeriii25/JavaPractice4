import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Lift implements Runnable {
    private final ConcurrentLinkedQueue<Request> requests;
    private LiftStatus currentStatus;
    private int currentFloor;
    private final int id;
    private final Building building;
    private BuildingUI ui;

    public LiftStatus getCurrentStatus() {
        return currentStatus;
    }

    private void setCurrentStatus(LiftStatus newStatus) {
        currentStatus = newStatus;
        updateUI();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    private void setCurrentFloor(int newCurrentFloor) {
        currentFloor = newCurrentFloor;
        updateUI();
    }

    private void updateUI() {
        if (ui == null) return;
        ui.manageLiftChanges(id, currentFloor, currentStatus);
    }

    public int upperFloorRequest() {
        synchronized (requests) {
            int max = -1;
            for (var request : requests) {
                max = Math.max(max, request.floor());
            }
            return max == -1 ? Building.UpperFloor : max;
        }
    }

    public int lowerFloorRequest() {
        synchronized (requests) {
            int min = 100;
            for (var request : requests) {
                min = Math.min(min, request.floor());
            }
            return min == 100 ? Building.LowerFloor : min;
        }
    }

    public Lift(int id, Building building) {
        this.id = id;
        setCurrentStatus(LiftStatus.FREE);
        setCurrentFloor(Building.LowerFloor);
        this.building = building;
        this.requests = new ConcurrentLinkedQueue<>();
    }

    public void setBuildingUI(BuildingUI buildingUI) {
        this.ui = buildingUI;
    }

    public void addRequest(Request request) {
        if (requests.peek() != null && requests.peek().floor() == request.floor())
            return;
        System.out.println("Request added to Lift " + id + ": " + request);
        requests.add(request);
        synchronized (this) {
            notifyAll();
        }
    }

    private Integer getNextFloor() {
        synchronized (requests) {
            if (requests.isEmpty())
                return null;
            if (Objects.requireNonNull(currentStatus) == LiftStatus.FREE) {
                return requests.peek().floor();
            }
            int minFloor = 100;
            int maxFloor = -1;
            for(var request : requests) {
                if (currentStatus == LiftStatus.UP) {
                    if (request.floor() >= currentFloor)
                        minFloor = Math.min(request.floor(), minFloor);
                } else if (currentStatus == LiftStatus.DOWN) {
                    if (request.floor() <= currentFloor)
                        maxFloor = Math.max(request.floor(), maxFloor);
                }
            }
            if (currentStatus == LiftStatus.UP)
                return minFloor;
            return maxFloor;
        }
    }

    @Override
    public void run() {
        while (true) {
            Integer nextFloor;
            synchronized (this) {
                nextFloor = waitForNextFloor();
                if (nextFloor == currentFloor) {
                    requests.poll();
                    continue;
                }
                setCurrentStatus(nextFloor > currentFloor ? LiftStatus.UP : LiftStatus.DOWN);
            }
            liftRide(nextFloor);
        }
    }

    private Integer waitForNextFloor() {
        Integer nextFloor;
        while ((nextFloor = getNextFloor()) == null) {
            try {
                System.out.println("Lift " + id + " waits for calls");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return nextFloor;
    }

    private void liftRide(int floor) {
        try {
            System.out.println("Lift " + id + " goes from floor " + currentFloor + " to floor " + floor);
            while (currentFloor != floor) {
                manageIntermediateFloors(floor);
            }
            setCurrentStatus(LiftStatus.FREE);
            Thread.sleep(1000L);
            requests.removeIf(r -> r.floor() == currentFloor);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void manageIntermediateFloors(int floor) throws InterruptedException {
        if (currentFloor < floor) this.setCurrentFloor(currentFloor + 1);
        else this.setCurrentFloor(currentFloor - 1);
        boolean createOutRequest = false, requestsWereDeleted = false;
        synchronized (requests) {
            for (var request : requests) {
                if (request.floor() == currentFloor) {
                    if (request.type() == Request.RequestType.IN)
                        createOutRequest = true;
                    requests.remove(request);
                    requestsWereDeleted = true;
                }
            }
        }
        if (currentFloor != floor && requestsWereDeleted){
            var currentState = this.currentStatus;
            Thread.sleep(1000L);
            setCurrentStatus(LiftStatus.FREE);
            Thread.sleep(1000L);
            setCurrentStatus(currentState);
        }
        if (createOutRequest)
            building.inRequestOccured(id);
        Thread.sleep(1000L);
    }
}