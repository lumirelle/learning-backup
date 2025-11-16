package com.dorm.entity.card.telephone;

import com.dorm.enums.card.telephone.TelephoneCardOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QueryTelephoneCardDTO {

    private String telephone;
}
