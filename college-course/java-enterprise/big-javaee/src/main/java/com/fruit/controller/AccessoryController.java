package com.fruit.controller;

import com.fruit.entity.Accessory;
import com.fruit.service.AccessoryService;
import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessoryController extends BaseController {

  @Resource
  private AccessoryService accessoryService;

  @RequestMapping("/accessory/list.action")
  public String list(Model model, Accessory accessory) {
    Map<String, Object> map = new HashMap<>();
    map.put("fruitId", accessory.getFruitId());
    List<Accessory> accessoryList = accessoryService.find(map);
    model.addAttribute("fruitId", accessory.getFruitId());
    model.addAttribute("list", accessoryList.isEmpty() ? null : accessoryList);
    model.addAttribute("sumPrice", sumPrice(accessoryList));
    return "/accessory/accessoryHome.jsp";
  }

  private double sumPrice(List<Accessory> accessoryList) {
    double sum = 0.0;
    for (Accessory accessory : accessoryList) {
      sum += accessory.getPrice();
    }
    return sum;
  }

  @RequestMapping("/accessory/add.action")
  public String add(Model model, Accessory accessory) {
    accessory.setAccessoryId(UUID.randomUUID().toString());
    accessory.setFruitId(accessory.getFruitId());
    accessory.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    accessoryService.insert(accessory);
    return list(model, accessory);
  }

  @RequestMapping("/accessory/delete.action")
  public String delete(Model model, Accessory accessory) {
    accessoryService.deleteById(accessory.getAccessoryId());
    return list(model, accessory);
  }

  @RequestMapping("/accessory/deleteList.action")
  public String deleteList(Model model, String[] arrays, Accessory accessory) {
    accessoryService.delete(arrays);
    return list(model, accessory);
  }

}
