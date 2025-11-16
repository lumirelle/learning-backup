package org.seagulls.cainiaoserver.controller;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import org.seagulls.cainiaoserver.domain.ParcelStakeholder;
import org.seagulls.cainiaoserver.repository.StakeholderRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StakeholderController {

  @Resource
  private StakeholderRepository stakeholderRepository;

  @GetMapping("/stakeholder/queryByUserName")
  public List<ParcelStakeholder> queryByUserName(String userName) {
    ExampleMatcher matcher = ExampleMatcher.matching()
        .withMatcher("name", ExampleMatcher.GenericPropertyMatcher::exact)
        .withIgnorePaths("id");
    ParcelStakeholder stakeholder = new ParcelStakeholder();
    stakeholder.setName(userName);
    Example<ParcelStakeholder> example = Example.of(stakeholder, matcher);
    return stakeholderRepository.findAll(example);
  }

  @PostMapping("/stakeholder/add")
  public int add(@RequestBody ParcelStakeholder stakeholder) {
    ExampleMatcher matcher = ExampleMatcher.matching()
        .withMatcher("name", ExampleMatcher.GenericPropertyMatcher::exact)
        .withMatcher("telephone", ExampleMatcher.GenericPropertyMatcher::exact)
        .withMatcher("address", ExampleMatcher.GenericPropertyMatcher::exact)
        .withIgnorePaths("id");
    Example<ParcelStakeholder> example = Example.of(stakeholder, matcher);
    ParcelStakeholder stakeholderInDB = stakeholderRepository.findOne(example)
        .orElse(null);
    return Objects.requireNonNullElseGet(stakeholderInDB,
        () -> stakeholderRepository.save(stakeholder)).getId();
  }

}
