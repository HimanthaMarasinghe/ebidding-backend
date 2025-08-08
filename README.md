# E-Bidding Platform - Microservices Backend

This project contains multiple microservices built using Spring Boot.  
Some services use Apache Kafka for messaging.

---

## ⚙️ Requirements

- Java 17+
- Maven
- PostgreSQL
- Apache Kafka 3.9.1 (Scala 2.12)

---

## 📦 Download and Set Up Apache Kafka

### 1. Download Kafka

Get Kafka 3.9.1 from the official site:

🔗 [kafka_2.12-3.9.1.tgz](https://downloads.apache.org/kafka/3.9.1/kafka_2.12-3.9.1.tgz)

Once downloaded, extract it to any folder you like, then open two Command Prompts in that folder.

🟢 Start Zookeeper (first terminal)

```bash
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

```

🟠 Start Kafka Broker (second terminal)

```bash
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

🔎 To make sure Kafka has started correctly, open another Command Prompt and execute this command to list Kafka topics:

```bash
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

✅ Expected Outcome:
- If Kafka is running properly, this command will show a list of topic names.
- If no topics are created yet, it will return nothing (a blank line).
- If there's an error, it means Kafka hasn't started correctly.
