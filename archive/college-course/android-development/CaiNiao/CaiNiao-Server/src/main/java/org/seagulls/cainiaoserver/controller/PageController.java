package org.seagulls.cainiaoserver.controller;

import jakarta.annotation.Resource;
import org.seagulls.cainiaoserver.domain.Parcel;
import org.seagulls.cainiaoserver.domain.ParcelStakeholder;
import org.seagulls.cainiaoserver.repository.ParcelRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

  @Resource
  private ParcelRepository parcelRepository;

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/parcels-manager")
  public String parcels(Model model, @RequestParam String userName) {
    model.addAttribute("userName", userName);
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
    model.addAttribute("parcels", parcelRepository.findAll(example));
    return "parcels-manager";
  }

}
