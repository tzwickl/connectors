package rocks.inspectit.jaeger.connectors.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.inspectit.jaeger.connectors.IDatabase;
import rocks.inspectit.jaeger.model.config.KafkaConfig;
import rocks.inspectit.jaeger.model.trace.kafka.Trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Kafka implements IDatabase<Trace> {
    private static final Logger logger = LoggerFactory.getLogger(Kafka.class);
    private final String serviceName;
    private Producer<String, Trace> producer;
    private KafkaConsumer<String, Trace> consumer;
    private String outputTopic;

    public Kafka(String serviceName, KafkaConfig kafkaConfig) {
        this.serviceName = serviceName;
        this.outputTopic = kafkaConfig.getOutputTopic();
        this.initConsumer(kafkaConfig);
        this.initProducer(kafkaConfig);
    }

    private void initProducer(KafkaConfig kafkaConfig) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConfig.getBootstrapServers());
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "rocks.inspectit.jaeger.model.trace.kafka.TraceSerializer");
        producer = new KafkaProducer<>(props);
    }

    private void initConsumer(KafkaConfig kafkaConfig) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConfig.getBootstrapServers());
        props.put("group.id", kafkaConfig.getGroupId());
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "5000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "rocks.inspectit.jaeger.model.trace.kafka.TraceDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(kafkaConfig.getInputTopic()));
    }

    @Override
    public void closeConnection() throws IOException {
        this.consumer.close();
        this.producer.close();
    }

    @Override
    public List<Trace> getTraces(final String serviceName) {
        ConsumerRecords<String, Trace> records = consumer.poll(10000);
        List<Trace> traces = new ArrayList<>();
        records.forEach(record -> {
            if (record.value() != null) {
                traces.add(record.value());
            }
        });
        return traces;
    }

    @Override
    public List<Trace> getTraces(final String serviceName, Long startTime) {
        return this.getTraces(serviceName);
    }

    @Override
    public List<Trace> getTraces(final String serviceName, Long startTime, Long endTime) {
        return this.getTraces(serviceName);
    }

    @Override
    public void saveTraces(List<Trace> traces) {
        traces.forEach(trace -> {
            ProducerRecord<String, Trace> producerRecord = new ProducerRecord<>(outputTopic, trace.getUUID(), trace);
            producer.send(producerRecord);
        });
        //consumer.commitSync();
    }
}
