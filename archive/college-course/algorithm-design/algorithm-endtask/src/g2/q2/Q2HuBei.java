package g2.q2;

import java.util.ArrayList;
import java.util.List;

public class Q2HuBei {

  public static void main(String[] args) {
    // 创建所有省份
    Province heiLongJiang = new Province("黑龙江");
    Province jiLin = new Province("吉林");
    Province liaoNing = new Province("辽宁");
    Province heBei = new Province("河北");
    Province shanXi = new Province("山西");
    Province neiMengGu = new Province("内蒙古");
    Province shanDong = new Province("山东");
    Province heNan = new Province("河南");
    Province anHui = new Province("安徽");
    Province jiangSu = new Province("江苏");
    Province shangHai = new Province("上海");
    Province zheJiang = new Province("浙江");
    Province jiangXi = new Province("江西");
    Province huBei = new Province("湖北");
    Province huNan = new Province("湖南");
    Province guangDong = new Province("广东");
    Province guangXi = new Province("广西");
    Province haiNan = new Province("海南");
    Province siChuan = new Province("四川");
    Province guiZhou = new Province("贵州");
    Province yunNan = new Province("云南");
    Province xiangGang = new Province("香港");
    Province aoMen = new Province("澳门");
    Province taiWan = new Province("台湾");
    Province chongQing = new Province("重庆");
    Province xinJiang = new Province("新疆");
    Province xiZang = new Province("西藏");
    Province ningXia = new Province("宁夏");
    Province qingHai = new Province("青海");
    Province ganSu = new Province("甘肃");
    Province shaanXi = new Province("陕西");
    Province beiJing = new Province("北京");
    Province tianJin = new Province("天津");
    Province fujian = new Province("福建");

    // addNeighbours 方法会为该省份和邻居省份添加同一条边
    // 不需要为每一条边都添加两次
    heiLongJiang.addNeighboursBoth(jiLin, neiMengGu);
    jiLin.addNeighboursBoth(liaoNing, neiMengGu);
    liaoNing.addNeighboursBoth(neiMengGu, heBei);
    neiMengGu.addNeighboursBoth(heBei, shanXi, shaanXi, ganSu, ningXia);
    heBei.addNeighboursBoth(shanXi, beiJing, tianJin, shanDong, heNan);
    shanXi.addNeighboursBoth(shaanXi, heNan);
    beiJing.addNeighboursBoth(tianJin);
    tianJin.addNeighboursBoth();
    ningXia.addNeighboursBoth(ganSu, shaanXi);
    shaanXi.addNeighboursBoth(ganSu, chongQing, siChuan, heNan, huBei);
    heNan.addNeighboursBoth(huBei, anHui, shanDong);
    shanDong.addNeighboursBoth(anHui, jiangSu);
    jiangSu.addNeighboursBoth(anHui, zheJiang, shangHai);
    anHui.addNeighboursBoth(zheJiang, jiangXi, huBei);
    huBei.addNeighboursBoth(chongQing, huNan, jiangXi);
    huNan.addNeighboursBoth(jiangXi, guangDong, guangXi, guiZhou, chongQing);
    chongQing.addNeighboursBoth(guiZhou, siChuan);
    siChuan.addNeighboursBoth(guiZhou, yunNan, ganSu, xiZang, qingHai);
    ganSu.addNeighboursBoth(qingHai, xinJiang);
    qingHai.addNeighboursBoth(xinJiang, xiZang);
    xinJiang.addNeighboursBoth(xiZang);
    xiZang.addNeighboursBoth(yunNan);
    yunNan.addNeighboursBoth(guiZhou, guangXi);
    guiZhou.addNeighboursBoth(guangXi);
    guangXi.addNeighboursBoth(guangDong);
    guangDong.addNeighboursBoth(haiNan, aoMen, xiangGang);
    jiangXi.addNeighboursBoth(fujian, zheJiang);
    fujian.addNeighboursBoth(zheJiang, taiWan);
    zheJiang.addNeighboursBoth(shangHai);
    shangHai.addNeighboursBoth();
    aoMen.addNeighboursBoth(xiangGang);
    xiangGang.addNeighboursBoth();
    taiWan.addNeighboursBoth();
    haiNan.addNeighboursBoth();

    // 创建所有省份的列表，除了湖北
    List<Province> allProvincesExceptHuBei = new ArrayList<>();
    allProvincesExceptHuBei.add(heiLongJiang);
    allProvincesExceptHuBei.add(jiLin);
    allProvincesExceptHuBei.add(liaoNing);
    allProvincesExceptHuBei.add(heBei);
    allProvincesExceptHuBei.add(shanXi);
    allProvincesExceptHuBei.add(neiMengGu);
    allProvincesExceptHuBei.add(shanDong);
    allProvincesExceptHuBei.add(heNan);
    allProvincesExceptHuBei.add(anHui);
    allProvincesExceptHuBei.add(jiangSu);
    allProvincesExceptHuBei.add(shangHai);
    allProvincesExceptHuBei.add(zheJiang);
    allProvincesExceptHuBei.add(jiangXi);
    allProvincesExceptHuBei.add(huNan);
    allProvincesExceptHuBei.add(guangDong);
    allProvincesExceptHuBei.add(guangXi);
    allProvincesExceptHuBei.add(haiNan);
    allProvincesExceptHuBei.add(siChuan);
    allProvincesExceptHuBei.add(guiZhou);
    allProvincesExceptHuBei.add(yunNan);
    allProvincesExceptHuBei.add(xiangGang);
    allProvincesExceptHuBei.add(aoMen);
    allProvincesExceptHuBei.add(taiWan);
    allProvincesExceptHuBei.add(chongQing);
    allProvincesExceptHuBei.add(xinJiang);
    allProvincesExceptHuBei.add(xiZang);
    allProvincesExceptHuBei.add(ningXia);
    allProvincesExceptHuBei.add(qingHai);
    allProvincesExceptHuBei.add(ganSu);
    allProvincesExceptHuBei.add(shaanXi);
    allProvincesExceptHuBei.add(beiJing);
    allProvincesExceptHuBei.add(tianJin);
    allProvincesExceptHuBei.add(fujian);

    // 找去往其他身份的最短路径
    huBei.test(allProvincesExceptHuBei);
  }

}
