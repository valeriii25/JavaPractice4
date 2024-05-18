public record Request(int floor, RequestType type) {
    public enum RequestType {
        IN, OUT
    }

    @Override
    public String toString() {
        if (type == RequestType.IN)
            return "people enter lift on floor " + floor;
        return "people leave lift on floor " + floor;
    }
}