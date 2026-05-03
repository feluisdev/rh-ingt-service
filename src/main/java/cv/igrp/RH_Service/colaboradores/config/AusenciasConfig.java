package cv.igrp.RH_Service.colaboradores.config;

import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AusenciasConfig {

    @Bean
    public DiasUteisCalculator diasUteisCalculator() {
        return new DiasUteisCalculator();
    }
}
