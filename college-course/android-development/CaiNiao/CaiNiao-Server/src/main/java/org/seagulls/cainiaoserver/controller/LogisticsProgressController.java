package org.seagulls.cainiaoserver.controller;

import jakarta.annotation.Resource;
import org.seagulls.cainiaoserver.domain.LogisticsProgress;
import org.seagulls.cainiaoserver.domain.Parcel;
import org.seagulls.cainiaoserver.repository.LogisticsProgressRepository;
import org.seagulls.cainiaoserver.repository.ParcelRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogisticsProgressController {

  @Resource
  private LogisticsProgressRepository logisticsProgressRepository;

  @Resource
  private ParcelRepository parcelRepository;

  @PostMapping("/logistics-progress/add")
  public void add(@RequestBody Parcel dataParcel) {
    Parcel parcel = parcelRepository.findById(dataParcel.getId()).orElse(null);
    if (parcel != null && !dataParcel.getLogisticsProgresses().isEmpty()) {
      LogisticsProgress logisticsProgress = logisticsProgressRepository.save(
          dataParcel.getLogisticsProgresses().getFirst());
      parcel.addLogisticsProgress(logisticsProgress);
      parcelRepository.save(parcel);
    }
  }

}
