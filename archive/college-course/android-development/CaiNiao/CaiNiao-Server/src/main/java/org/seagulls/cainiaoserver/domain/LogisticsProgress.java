package org.seagulls.cainiaoserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.seagulls.cainiaoserver.enums.LogisticsStatus;

@Entity
@Data
public class LogisticsProgress implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column
  private String datetime;

  @Enumerated(EnumType.STRING)
  @Column
  private LogisticsStatus status;

  @Column
  private String message;

}
