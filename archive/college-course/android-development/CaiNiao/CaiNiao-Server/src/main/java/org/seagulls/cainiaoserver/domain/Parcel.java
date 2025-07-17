package org.seagulls.cainiaoserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.seagulls.cainiaoserver.enums.LogisticsCompany;
import org.seagulls.cainiaoserver.enums.LogisticsStatus;

@Entity
@Data
public class Parcel implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column
  private String title;

  @Column
  private String imageUrl;

  @Column
  private String logisticsId;

  @Enumerated(EnumType.STRING)
  @Column
  private LogisticsStatus logisticsStatus;

  @OneToMany
  @JoinTable(name = "parcel_logistics_progress", inverseJoinColumns = @JoinColumn(name = "logistics_progress_id"))
  private List<LogisticsProgress> logisticsProgresses;

  @Enumerated(EnumType.STRING)
  @Column
  private LogisticsCompany logisticsCompany;

  @OneToOne
  private ParcelStakeholder receiver;

  @OneToOne
  private ParcelStakeholder sender;

  public void addLogisticsProgress(LogisticsProgress logisticsProgress) {
    if (logisticsProgresses == null) {
      logisticsProgresses = new ArrayList<>();
    }
    logisticsProgresses.add(logisticsProgress);
    logisticsStatus = logisticsProgress.getStatus();
  }

}
