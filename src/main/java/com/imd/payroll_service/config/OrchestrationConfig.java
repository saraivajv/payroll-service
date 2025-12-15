package com.imd.payroll_service.config;

import com.imd.common.events.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.util.function.Function;

@Configuration
@Profile("orchestration") // <--- SÓ CARREGA SE O PERFIL FOR ORCHESTRATION
public class OrchestrationConfig {

    @Bean
    public Function<ValidateSalaryCommand, SalaryValidationResult> processSalaryCommand() {
        return cmd -> {
            System.out.println("[Orquestração] Recebido comando para validar: " + cmd.name());
            boolean approved = cmd.salary() <= 50000;

            String reason = approved ? "OK" : "Salário muito alto";
            System.out.println("-> Resultado: " + (approved ? "APROVADO" : "REJEITADO"));

            return new SalaryValidationResult(
                    cmd.sagaId(),
                    approved,
                    reason
            );
        };
    }
}