package com.pcmmtl.azure.cosmosdb.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private String id;
    private String currency;
    private double amount;
    private String bic;
}
