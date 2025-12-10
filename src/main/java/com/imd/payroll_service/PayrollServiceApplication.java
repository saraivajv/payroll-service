package com.imd.payroll_service;

import com.imd.common.events.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.function.Function;

@SpringBootApplication
@EnableDiscoveryClient
public class PayrollServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayrollServiceApplication.class, args);
	}
	// VERSÃO 1: COREOGRAFIA (Event-Driven)
	@Bean
	@Profile("choreography")
	public Function<EmployeeCreationRequested, Object> validateSalary() {
		return event -> {
			System.out.println("[Coreografia] Validando salário para: " + event.name());
			if (event.salary() > 50000) {
				System.out.println("-> REJEITADO");
				return new SalaryRejected(event.eventId(), "Salary exceeds limit of 50,000");
			}
			System.out.println("-> APROVADO");
			return new SalaryValidated(event.eventId(), event.name(), event.position(), event.salary());
		};
	}
	// VERSÃO 2: ORQUESTRAÇÃO (Command-Driven)
	@Bean
	@Profile("orchestration")
	public Function<ValidateSalaryCommand, SalaryValidationResult> processSalaryCommand() {
		return cmd -> {
			System.out.println("[Orquestração] Recebido comando para validar: " + cmd.name());
			boolean approved = cmd.salary() <= 50000;

			String reason = approved ? "OK" : "Salary exceeds limit of 50,000";
			System.out.println("-> Resultado: " + (approved ? "APROVADO" : "REJEITADO"));

			return new SalaryValidationResult(
					cmd.sagaId(),
					approved,
					reason
			);
		};
	}
}