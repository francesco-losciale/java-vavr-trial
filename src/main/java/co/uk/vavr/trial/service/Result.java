package co.uk.vavr.trial.service;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Result {
    private BigDecimal weight;
    private String name;
    private String surname;
}
