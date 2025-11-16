package com.fruit.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fruit.entity.Retailer;
import com.fruit.service.RetailerService;
import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class RetailerController extends BaseController {

  @Resource
  private RetailerService retailerService;

  @RequestMapping("/retailer/list.action")
  public String list(Model model, Retailer retailer, String startTime, String endTime) {
    Map<String, Object> map = this.reatailerToMap(retailer);
    if (startTime != null && !startTime.isEmpty()) {
      map.put("startTime", startTime);
    }
    if (endTime != null && !endTime.isEmpty()) {
      map.put("endTime", endTime);
    }
    List<Retailer> retailerList = retailerService.find(map);
    model.addAttribute("retailer", retailer);
    model.addAttribute("startTime", startTime);
    model.addAttribute("endTime", endTime);
    model.addAttribute("list", retailerList.isEmpty() ? null : retailerList);
    model.addAttribute("currentPage", retailer.getCurrentPage());
    model.addAttribute("startPage", retailer.getStartPage());
    int countNumber = retailerService.count(map);
    model.addAttribute("countNumber", countNumber);
    int pageSize = retailer.getPageSize();
    model.addAttribute("pageSize", pageSize);
    int sumPageNumber = countNumber % pageSize == 0 ? (countNumber / pageSize)
        : ((countNumber / pageSize) + 1);
    model.addAttribute("sumPageNumber", sumPageNumber);
    return "/retailer/retailerHome.jsp";
  }

  private Map<String, Object> reatailerToMap(Retailer retailer) {
    Map<String, Object> map = new HashMap<>();
    map.put("name", checkStringIsEmpty(retailer.getName()));
    map.put("telephone", checkStringIsEmpty(retailer.getTelephone()));
    map.put("address", checkStringIsEmpty(retailer.getAddress()));
    map.put("status", retailer.getStatus() == -1 ? null : retailer.getStatus());
    map.put("createTime", checkStringIsEmpty(retailer.getCreateTime()));
    map.put("startPage", retailer.getStartPage());
    map.put("pageSize", retailer.getPageSize());
    return map;
  }

  private String checkStringIsEmpty(String param) {
    if (param == null) {
      return null;
    }
    return param.isEmpty() ? null : "%" + param + "%";
  }

  @RequestMapping("/retailer/editRetailer.action")
  public @ResponseBody Retailer editRetailer(@RequestBody String json) {
    String id = JSONObject.parseObject(json).getString("id");
    return retailerService.get(id);
  }

  @RequestMapping("/retailer/edit.action")
  public String edit(Model model, Retailer retailer) {
    retailerService.update(retailer);
    Retailer queryRetailer = new Retailer();

    queryRetailer.setStartPage(retailer.getStartPage());
    queryRetailer.setCurrentPage(retailer.getCurrentPage());
    queryRetailer.setPageSize(retailer.getPageSize());
    queryRetailer.setStatus(-1);
    return list(model, queryRetailer, null, null);
  }

  @RequestMapping("/retailer/delete.action")
  public String delete(Model model, Retailer retailer) {
    retailerService.deleteById(retailer.getRetailerId());

    Retailer queryRetailer = new Retailer();
    queryRetailer.setStartPage(retailer.getStartPage());
    queryRetailer.setCurrentPage(retailer.getCurrentPage());
    queryRetailer.setPageSize(retailer.getPageSize());
    queryRetailer.setStatus(-1);
    return list(model, queryRetailer, null, null);
  }

  @RequestMapping("/retailer/add.action")
  public String add(Model model, Retailer retailer) {
    retailer.setRetailerId(UUID.randomUUID().toString());
    retailer.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    retailerService.insert(retailer);

    Retailer queryRetailer = new Retailer();
    queryRetailer.setStatus(-1);
    return list(model, queryRetailer, null, null);
  }

}
