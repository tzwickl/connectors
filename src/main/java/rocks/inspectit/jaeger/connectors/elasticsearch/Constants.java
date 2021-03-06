package rocks.inspectit.jaeger.connectors.elasticsearch;

public enum Constants {
    START_TIME("startTime"),
    SERVICE_NAME_PATH("process.serviceName"),
    TYPE_TRACE("trace");

    private String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
