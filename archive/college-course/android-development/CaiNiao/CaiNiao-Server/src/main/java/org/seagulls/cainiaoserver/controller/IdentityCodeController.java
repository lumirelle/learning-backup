package org.seagulls.cainiaoserver.controller;

import static org.seagulls.cainiaoserver.util.UtilsHolder.RANDOM;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityCodeController {

  @GetMapping("/identityCode/queryByUserName")
  public String queryByUserName(@RequestParam String userName) {
    // TODO: 使用用户名称生成身份码
    StringBuilder barcodeData = new StringBuilder("'");
    for (int i = 0; i < 9; i++) {
      barcodeData.append(RANDOM.nextInt(10));
    }
    barcodeData.append("'");
    return barcodeData.toString();
  }

}
