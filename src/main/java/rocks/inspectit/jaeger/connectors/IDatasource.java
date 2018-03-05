package rocks.inspectit.jaeger.connectors;

import java.io.IOException;
import java.util.List;

public interface IDatasource<Trace> {
    void closeConnection() throws IOException;

    List<Trace> getTraces(final String serviceName);

    List<Trace> getTraces(final String serviceName, Long startTime);

    List<Trace> getTraces(final String serviceName, Long startTime, Long endTime);

    void saveTraces(List<Trace> traces);

    void updateTraces(List<Trace> traces);
}
