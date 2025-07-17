package org.seagulls.cainiaoserver.controller;

import jakarta.annotation.Resource;
import java.util.List;
import org.seagulls.cainiaoserver.domain.Parcel;
import org.seagulls.cainiaoserver.domain.ParcelStakeholder;
import org.seagulls.cainiaoserver.repository.ParcelRepository;
import org.seagulls.cainiaoserver.util.TestDataGenerator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParcelController {

  @Resource
  private ParcelRepository parcelRepository;

  @GetMapping("/parcel/queryByUserName")
  public List<Parcel> queryByUserName(@RequestParam String userName) {
    ExampleMatcher matcher = ExampleMatcher.matchingAny()
        .withMatcher("sender.name",
            ExampleMatcher.GenericPropertyMatcher::exact)
        .withMatcher("receiver.name",
            ExampleMatcher.GenericPropertyMatcher::exact)
        .withIgnorePaths("id")
        .withIgnorePaths("sender.id")
        .withIgnorePaths("receiver.id");
    Parcel parcel = new Parcel();
    ParcelStakeholder stakeholder = new ParcelStakeholder();
    stakeholder.setName(userName);
    parcel.setSender(stakeholder);
    parcel.setReceiver(stakeholder);
    Example<Parcel> example = Example.of(parcel, matcher);
    return parcelRepository.findAll(example);
  }

  @GetMapping("/parcel/queryById")
  public Parcel queryById(@RequestParam int id) {
    return parcelRepository.findById(id).orElse(null);
  }

  @PostMapping("/parcel/add")
  public void add(@RequestBody Parcel parcel) {
    parcel.setImageUrl(TestDataGenerator.generateImageUrl());
    parcel.setLogisticsCompany(TestDataGenerator.generateLogisticsCompany());
    parcel.setLogisticsId(
        TestDataGenerator.generateLogisticsId(parcel.getLogisticsCompany()));
    parcelRepository.save(parcel);
  }

}
