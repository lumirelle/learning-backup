package com.fruit.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fruit.entity.Commodities;
import com.fruit.entity.Contract;
import com.fruit.entity.ContractVo;
import com.fruit.entity.Retailer;
import com.fruit.service.AccessoryService;
import com.fruit.service.CommoditiesService;
import com.fruit.service.ContractService;
import com.fruit.service.RetailerService;
import com.mysql.cj.util.StringUtils;
import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class ContractController {

  @Resource
  private ContractService contractService;

  @Resource
  private RetailerService retailerService;

  @Resource
  private CommoditiesService commoditiesService;

  @Resource
  private AccessoryService accessoryService;

  @RequestMapping("/contract/list.action")
  public String list(Model model, ContractVo contractVo, String startTime, String endTime) {
    Map<String, Object> map = contractToMap(contractVo);
    if (startTime != null && !startTime.isBlank()) {
      map.put("startTime", startTime);
    }
    if (endTime != null && !endTime.isBlank()) {
      map.put("endTime", endTime);
    }
    List<ContractVo> contractList = contractService.findContractList(map);
    model.addAttribute("contract", contractVo);
    model.addAttribute("startTime", startTime);
    model.addAttribute("endTime", endTime);
    model.addAttribute("list", contractList.isEmpty() ? null : contractList);
    model.addAttribute("currentPage", contractVo.getCurrentPage());
    model.addAttribute("startPage", contractVo.getStartPage());
    int countNumber = contractService.count(map);
    model.addAttribute("countNumber", countNumber);
    int pageSize = contractVo.getPageSize();
    model.addAttribute("pageSize", pageSize);
    int sumPageNumber = countNumber % pageSize == 0 ? (countNumber / pageSize)
        : ((countNumber / pageSize) + 1);
    model.addAttribute("sumPageNumber", sumPageNumber);
    return "/contract/contractHome.jsp";
  }

  private Map<String, Object> contractToMap(ContractVo contractVo) {
    Map<String, Object> map = new HashMap<>();
    map.put("barCode", checkStringIsEmpty(contractVo.getBarCode()));
    map.put("retailerName", checkStringIsEmpty(contractVo.getRetailerName()));
    map.put("type", contractVo.getType() == -1 ? null : contractVo.getType());
    map.put("startPage", contractVo.getStartPage());
    map.put("pageSize", contractVo.getPageSize());
    return map;
  }

  private String checkStringIsEmpty(String param) {
    if (param == null) {
      return null;
    }
    return param.isEmpty() ? null : "%" + param + "%";
  }

  @RequestMapping("/contract/toAddPage.action")
  public String toAddPage(Model model, ContractVo contractVo) {
    return "/contract/addContract.jsp";
  }

  @RequestMapping("/contract/toEditPage.action")
  public String toEditPage(Model model, String contractId) {
    Contract contract = contractService.get(contractId);
    model.addAttribute("contract", contract);
    model.addAttribute("commoditiesList", JSONObject.toJSONString(contract.getCommoditiesList()));
    return "/contract/editContract.jsp";
  }

  @RequestMapping("/contract/getAllRetailer.action")
  public @ResponseBody List<Retailer> getAllRetailer(@RequestBody String json) {
    Map<String, Object> map = new HashMap<>();
    map.put("status", 1);
    if (!StringUtils.isNullOrEmpty(json)) {
      String name = JSONObject.parseObject(json).getString("name");
      if (!StringUtils.isNullOrEmpty(name)) {
        map.put("name", "%" + name + "%");
      }
    }
    return retailerService.find(map);
  }

  @RequestMapping("/contract/getAllCommodities.action")
  public @ResponseBody List<Commodities> getAllCommodities(@RequestBody String json) {
    Map<String, Object> map = new HashMap<>();
    if (!StringUtils.isNullOrEmpty(json)) {
      String name = JSONObject.parseObject(json).getString("name");
      if (!StringUtils.isNullOrEmpty(name)) {
        map.put("name", "%" + name + "%");
      }
    }
    return commoditiesService.find(map);
  }

  @RequestMapping("/contract/getCommoditiesAndAccessory.action")
  public @ResponseBody List<Map<String, Object>> getCommoditiesAndAccessory(String[] arrays) {
    List<Map<String, Object>> cList = new ArrayList<>();
    Map<String, Object> cMap;
    for (String item : arrays) {
      cMap = new HashMap<>();
      cMap.put("commodities", commoditiesService.get(item));
      Map<String, Object> map = new HashMap<>();
      map.put("fruitId", item);
      cMap.put("accessory", accessoryService.find(map));
      cList.add(cMap);
    }
    return cList;
  }

  private String getCode() {
    String codeHead = new SimpleDateFormat("yyyyMMdd").format(new Date());
    String barCode;
    String maxBarCode = contractService.getMaxBarCode();
    if (!StringUtils.isNullOrEmpty(maxBarCode)) {
      if (maxBarCode.substring(0, 8).equals(codeHead)) {
        maxBarCode = maxBarCode.substring(8);
      } else {
        maxBarCode = "0";
      }
    } else {
      maxBarCode = "0";
    }
    int maxNumber = Integer.parseInt(maxBarCode);
    int newNumber = maxNumber + 1;
    if (newNumber < 10) {
      barCode = codeHead + "000" + newNumber;
    } else if (newNumber < 100) {
      barCode = codeHead + "00" + newNumber;
    } else if (newNumber < 1000) {
      barCode = codeHead + "0" + newNumber;
    } else {
      barCode = codeHead + newNumber;
    }
    return barCode;
  }

  @RequestMapping("/contract/add.action")
  public String add(Model model, Contract contract, String retailerId, String[] commoditiesIdArrays,
      String[] priceArrays) {
    contract.setRetailer(retailerService.get(retailerId));
    String barCode = getCode();
    contract.setBarCode(barCode);
    contract.setContractId(UUID.randomUUID().toString());
    contract.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    contractService.insert(contract, commoditiesIdArrays, priceArrays);
    model.addAttribute("resultMessage", "添加成功！合同编号为：" + barCode);
    return "/contract/addContract.jsp";
  }

  @RequestMapping("/contract/edit.action")
  public String edit(Model model, Contract contract, String retailerId,
      String[] commoditiesIdArrays, String[] priceArrays) {
    contract.setRetailer(retailerService.get(retailerId));
    contractService.update(contract, commoditiesIdArrays, priceArrays);

    Contract queryContract = contractService.get(contract.getContractId());
    model.addAttribute("contract", queryContract);
    model.addAttribute("commoditiesList",
        JSONObject.toJSONString(queryContract.getCommoditiesList()));
    model.addAttribute("resultMessage", "修改成功！");
    return "/contract/editContract.jsp";
  }

  @RequestMapping("/contract/getContractDetail.action")
  public String getContractDetail(Model model, String contractId) {
    Contract contract = contractService.get(contractId);
    model.addAttribute("contract", contract);
    return "/contract/contractDetail.jsp";
  }

  @RequestMapping("/contract/delete.action")
  public String delete(Model model, ContractVo contractVo) {
    contractService.deleteById(contractVo.getContractId());
    ContractVo queryContract = new ContractVo();
    queryContract.setType(-1);
    queryContract.setStartPage(contractVo.getStartPage());
    queryContract.setCurrentPage(contractVo.getCurrentPage());
    queryContract.setPageSize(contractVo.getPageSize());
    return list(model, queryContract, null, null);
  }

}
