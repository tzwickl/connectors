package rocks.inspectit.jaeger.connectors.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.inspectit.jaeger.connectors.IDatabase;
import rocks.inspectit.jaeger.model.config.CassandraConfig;
import rocks.inspectit.jaeger.model.trace.cassandra.Trace;

import java.util.ArrayList;
import java.util.List;

public class Cassandra implements IDatabase<Trace> {
    private static final Logger logger = LoggerFactory.getLogger(Cassandra.class);

    private Cluster cluster;
    private Session session;

    // Mappers
    private MappingManager manager;
    private Mapper<Trace> tracesMapper;

    public Cassandra(CassandraConfig config) {
        this.cluster = Cluster.builder().addContactPoint(config.getHost()).build();
        this.session = this.cluster.connect(config.getKeyspace());
        this.manager = new MappingManager(session);
        this.createMappers();
    }

    private void createMappers() {
        this.tracesMapper = manager.mapper(Trace.class);
    }

    @Override
    public List<Trace> getTraces(final String serviceName) {
        Statement query = QueryBuilder.select().from(Constants.TRACES.getValue());

        Result<Trace> traces = this.tracesMapper.map(this.session.execute(query));

        List<Trace> tracesToAnalyze = new ArrayList<>();

        traces.forEach(trace -> {
            if (trace.getProcess().getServiceName().equals(serviceName)) {
                tracesToAnalyze.add(trace);
            }
        });

        return tracesToAnalyze;
    }

    @Override
    public List<Trace> getTraces(final String serviceName, Long startTime) {
        Statement query = QueryBuilder.select().from(Constants.TRACES.getValue())
                .where(QueryBuilder.gt(Constants.START_TIME.getValue(), startTime))
                .allowFiltering();

        Result<Trace> traces = this.tracesMapper.map(this.session.execute(query));

        List<Trace> tracesToAnalyze = new ArrayList<>();

        traces.forEach(trace -> {
            if (trace.getProcess().getServiceName().equals(serviceName)) {
                tracesToAnalyze.add(trace);
            }
        });

        return tracesToAnalyze;
    }

    @Override
    public List<Trace> getTraces(final String serviceName, Long startTime, Long endTime) {
        Statement query = QueryBuilder.select().from(Constants.TRACES.getValue())
                .where(QueryBuilder.gt(Constants.START_TIME.getValue(), startTime))
                .and(QueryBuilder.lt(Constants.START_TIME.getValue(), endTime))
                .allowFiltering();

        Result<Trace> traces = this.tracesMapper.map(this.session.execute(query));

        List<Trace> tracesToAnalyze = new ArrayList<>();

        traces.forEach(trace -> {
            if (trace.getProcess().getServiceName().equals(serviceName)) {
                tracesToAnalyze.add(trace);
            }
        });

        return tracesToAnalyze;
    }

    @Override
    public void saveTraces(List<Trace> traces) {
        traces.forEach(trace -> {
            this.tracesMapper.save(trace);
        });
    }

    @Override
    public void closeConnection() {
        this.cluster.close();
    }
}
