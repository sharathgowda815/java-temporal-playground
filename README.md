# java-temporal-playground

Distributed order processing system using **Spring Boot**, **Apache Kafka**, and **Temporal** parent-child workflows.

## Architecture

```
POST /orders (JSON body)
  -> OrderController
  -> Kafka producer (topic: order-events)
  -> Kafka consumer
  -> Temporal OrderWorkflow (parent)
       |
       |-- 1. validateOrder (activity)
       |
       |-- 2. PaymentWorkflow (child)  ---|  (parallel via Async.function)
       |-- 3. InventoryWorkflow (child) ---|
       |
       |-- 4. Workflow.await(approval signal)
       |
       |       POST /orders/{id}/approve  ->  signalApproval(true)
       |       POST /orders/{id}/reject   ->  signalApproval(false)
       |
       |-- 5. ShippingWorkflow (child)
       |
       |-- 6. sendConfirmation (activity)
```

### Child Workflows

| Workflow | Purpose | Key Feature |
|---|---|---|
| **PaymentWorkflow** | Processes payment | Retry policy: 3 attempts, 2s interval, 2x backoff |
| **InventoryWorkflow** | Reserves inventory | Runs in parallel with Payment |
| **ShippingWorkflow** | Ships the order | Runs after approval signal |

## Prerequisites

- Java 21
- Docker & Docker Compose

## How to Run

### 1. Start infrastructure

```bash
docker-compose up -d
```

This starts:
- **MySQL** (Temporal persistence) -- port 3308
- **Temporal Server** -- port 7233
- **Temporal UI** -- port 8123
- **Kafka** (KRaft mode) -- port 9092

### 2. Run Spring Boot

```bash
./mvnw spring-boot:run
```

### 3. Create an order

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId": "123", "customerId": "cust-1", "amount": 99.99}'
```

This publishes a Kafka event. The consumer picks it up and starts the parent OrderWorkflow, which launches Payment and Inventory child workflows in parallel.

### 4. Approve the order

After payment and inventory complete, the workflow pauses and waits for an approval signal:

```bash
curl -X POST http://localhost:8080/orders/123/approve
```

Or reject it:

```bash
curl -X POST http://localhost:8080/orders/123/reject
```

Approval starts the Shipping child workflow and sends a confirmation.

### 5. View workflows in Temporal UI

Open [http://localhost:8123](http://localhost:8123) to see:
- Parent workflow execution and its child workflow relationships
- Payment retry attempts (random failures demonstrate retry policy)
- Signal history (approval/rejection)
- Full event history for each workflow

## Key Concepts Demonstrated

| Concept | Where |
|---|---|
| Parent-child workflows | `OrderWorkflowImpl` orchestrates 3 child workflows |
| Parallel execution | `Async.function()` runs Payment + Inventory concurrently |
| Temporal Signal | `signalApproval()` + `Workflow.await()` |
| Retry Policy | PaymentWorkflow: max 3 attempts, 2s initial, 2x backoff |
| Kafka integration | REST -> Kafka -> Temporal workflow |
| Activity separation | Dedicated interfaces per domain (Payment, Inventory, Shipping) |
