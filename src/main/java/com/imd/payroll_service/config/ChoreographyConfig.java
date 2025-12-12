package com.imd.payroll_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imd.common.events.EmployeeCreationRequested;
import com.imd.common.events.EmployeeEvent; // Interface pai
import com.imd.common.events.SalaryRejected;
import com.imd.common.events.SalaryValidated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@Configuration
@Profile("choreography")
public class ChoreographyConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public Function<Message<String>, Message<EmployeeEvent>> validateSalary() {
        return message -> {
            try {
                String json = message.getPayload();
                EmployeeCreationRequested event = objectMapper.readValue(json, EmployeeCreationRequested.class);

                boolean approved = event.salary() <= 50000;
                EmployeeEvent responseEvent;
                String routingKey; // <--- VAMOS USAR ISSO

                if (approved) {
                    responseEvent = new SalaryValidated(event.eventId(), event.name(), event.position(), event.salary());
                    routingKey = "salary.validated"; // Etiqueta de Sucesso
                } else {
                    responseEvent = new SalaryRejected(event.eventId(), "Salário muito alto");
                    routingKey = "salary.rejected";  // Etiqueta de Falha
                }

                return MessageBuilder.withPayload(responseEvent)
                        .setHeader("type", responseEvent.getClass().getSimpleName())
                        // O SEGREDO: Injetamos a chave no header para o Binder ler
                        .setHeader("myRoutingKey", routingKey)
                        .build();

            } catch (Exception e) {
                e.printStackTrace(); // Log de erro é vital
                return null;
            }
        };
    }
}