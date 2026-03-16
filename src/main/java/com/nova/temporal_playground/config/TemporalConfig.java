package com.nova.temporal_playground.config;

import com.nova.temporal_playground.activity.impl.InventoryActivitiesImpl;
import com.nova.temporal_playground.activity.impl.OrderActivitiesImpl;
import com.nova.temporal_playground.activity.impl.PaymentActivitiesImpl;
import com.nova.temporal_playground.activity.impl.ShippingActivitiesImpl;
import com.nova.temporal_playground.workflow.OrderWorkflowImpl;
import com.nova.temporal_playground.workflow.inventory.InventoryWorkflowImpl;
import com.nova.temporal_playground.workflow.payment.PaymentWorkflowImpl;
import com.nova.temporal_playground.workflow.shipping.ShippingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TemporalConfig {

    public static final String ORDER_TASK_QUEUE = "order-task-queue";

    @Value("${temporal.server.address:127.0.0.1:7233}")
    private String temporalServerAddress;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalServerAddress)
                        .build()
        );
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs);
    }

    @Bean(destroyMethod = "shutdown")
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public Worker worker(WorkerFactory workerFactory) {
        Worker worker = workerFactory.newWorker(ORDER_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(
                OrderWorkflowImpl.class,
                PaymentWorkflowImpl.class,
                InventoryWorkflowImpl.class,
                ShippingWorkflowImpl.class
        );

        worker.registerActivitiesImplementations(
                new OrderActivitiesImpl(),
                new PaymentActivitiesImpl(),
                new InventoryActivitiesImpl(),
                new ShippingActivitiesImpl()
        );

        return worker;
    }

    @Bean
    public CommandLineRunner startWorkerFactory(WorkerFactory workerFactory) {
        return args -> {
            workerFactory.start();
            log.info("Temporal worker started on task queue: {}", ORDER_TASK_QUEUE);
        };
    }
}
