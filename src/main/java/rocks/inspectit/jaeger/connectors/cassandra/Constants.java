package rocks.inspectit.jaeger.connectors.cassandra;

public enum Constants {
    TRACES("traces"),
    START_TIME("start_time");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
