package org.example.autobot.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaCommandConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaCommandConsumer.class);

    /**
     * Метод слушает указанный топик Kafka и обрабатывает входящие команды.
     * Команды должны быть переданы в виде строки, например: "start", "stop", "status"
     */
    @KafkaListener(
            topics = "${telegram.commands.topic:telegram-commands}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(String message) {
        log.info("📥 Получена команда: {}", message);

        try {
            handleCommand(message.trim().toLowerCase());
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке команды '{}': {}", message, e.getMessage(), e);
        }
    }

    /**
     * Простая маршрутизация команд. Позже можно вынести в отдельный CommandDispatcher.
     */
    private void handleCommand(String command) {
        switch (command) {
            case "start":
                log.info("▶ Запуск процесса по команде 'start'");
                break;

            case "stop":
                log.info("⏹ Остановка процесса по команде 'stop'");
                break;

            case "status":
                log.info("ℹ Запрос статуса: всё работает!");
                break;

            default:
                log.warn("⚠ Неизвестная команда: '{}'", command);
        }
    }
}

