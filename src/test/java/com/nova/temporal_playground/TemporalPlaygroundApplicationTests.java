package com.nova.temporal_playground;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
class TemporalPlaygroundApplicationTests {

	@MockitoBean
	private WorkflowServiceStubs workflowServiceStubs;

	@MockitoBean
	private WorkflowClient workflowClient;

	@MockitoBean
	private WorkerFactory workerFactory;

	@MockitoBean
	private Worker worker;

	@Test
	void contextLoads() {
		// Verifies that the Spring context starts successfully
		// with mocked Temporal beans and embedded Kafka
		assertTrue(true);
	}
}
