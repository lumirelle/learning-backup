package com.fruit.controller;


import com.alibaba.fastjson2.JSONObject;
import com.fruit.entity.Commodities;
import com.fruit.service.AccessoryService;
import com.fruit.service.CommoditiesService;
import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommoditiesController extends BaseController {

  @Resource
  private CommoditiesService commoditiesService;

  @Resource
  private AccessoryService accessoryService;

  private final Log log = LogFactory.getLog(this.getClass());

  @RequestMapping("/commodities/list.action")
  public String list(Model model, Commodities commodities,
      @RequestParam(defaultValue = "0.0") double startPrice,
      @RequestParam(defaultValue = "0.0") double endPrice, String startTime, String endTime) {
    Map<String, Object> map = this.commoditiesToMap(commodities);
    if (startTime != null && !startTime.isBlank()) {
      map.put("startTime", startTime);
    }
    if (endTime != null && !endTime.isBlank()) {
      map.put("endTime", endTime);
    }
    if (startPrice > 0.0) {
      map.put("startPrice", startPrice);
    }
    if (endPrice > 0.0) {
      map.put("endPrice", endPrice);
    }
    List<Commodities> commoditiesList = commoditiesService.find(map);
    model.addAttribute("commodities", commodities);
    model.addAttribute("startPrice", startPrice);
    model.addAttribute("endPrice", endPrice);
    model.addAttribute("startTime", startTime);
    model.addAttribute("endTime", endTime);
    model.addAttribute("list", commoditiesList.isEmpty() ? null : commoditiesList);
    model.addAttribute("currentPage", commodities.getCurrentPage());
    model.addAttribute("startPage", commodities.getStartPage());
    int countNumber = commoditiesService.count(map);
    model.addAttribute("countNumber", countNumber);
    int pageSize = commodities.getPageSize();
    model.addAttribute("pageSize", pageSize);
    int sumPageNumber = countNumber % pageSize == 0 ? (countNumber / pageSize)
        : ((countNumber / pageSize) + 1);
    model.addAttribute("sumPageNumber", sumPageNumber);
    return "/commodities/commoditiesHome.jsp";
  }

  private Map<String, Object> commoditiesToMap(Commodities commodities) {
    Map<String, Object> map = new HashMap<>();
    map.put("name", checkStringIsEmpty(commodities.getName()));
    map.put("locality", checkStringIsEmpty(commodities.getLocality()));
    map.put("createTime", checkStringIsEmpty(commodities.getCreateTime()));
    map.put("startPage", commodities.getStartPage());
    map.put("pageSize", commodities.getPageSize());
    return map;
  }

  private String checkStringIsEmpty(String param) {
    if (param == null) {
      return null;
    }
    return param.isEmpty() ? null : "%" + param + "%";
  }

  @RequestMapping("/commodities/editCommodities.action")
  public @ResponseBody Commodities editCommodities(@RequestBody String json) {
    String id = JSONObject.parseObject(json).getString("id");
    return commoditiesService.get(id);
  }

  @RequestMapping("/commodities/edit.action")
  public String edit(Model model, Commodities commodities) {
    commoditiesService.update(commodities);
    Commodities queryCommodities = new Commodities();
    queryCommodities.setStartPage(commodities.getStartPage());
    queryCommodities.setCurrentPage(commodities.getCurrentPage());
    queryCommodities.setPageSize(commodities.getPageSize());
    return list(model, queryCommodities, 0.0, 0.0, null, null);
  }

  @RequestMapping("/commodities/delete.action")
  public String delete(Model model, Commodities commodities) {
    commoditiesService.deleteById(commodities.getFruitId());
    int result = accessoryService.deleteByFruitId(commodities.getFruitId());
    log.info("delete fruitId=" + commodities.getFruitId() + "'s accessories number:" + result);

    Commodities queryCommodities = new Commodities();
    queryCommodities.setStartPage(commodities.getStartPage());
    queryCommodities.setCurrentPage(commodities.getCurrentPage());
    queryCommodities.setPageSize(commodities.getPageSize());
    return list(model, queryCommodities, 0.0, 0.0, null, null);
  }

  @RequestMapping("/commodities/add.action")
  public String add(Model model, Commodities commodities) {
    commodities.setFruitId(UUID.randomUUID().toString());
    commodities.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    commoditiesService.insert(commodities);

    return list(model, new Commodities(), 0.0, 0.0, null, null);
  }

}
