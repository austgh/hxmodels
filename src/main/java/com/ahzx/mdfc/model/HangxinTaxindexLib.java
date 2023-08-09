package com.ahzx.mdfc.model;

import com.ahzx.mdfc.dao.hshy.HyDaoImpl;
import com.ahzx.mdfc.utils.CommUtils;
import com.ahzx.mdfc.utils.MathUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Scope("prototype")
public class HangxinTaxindexLib {

    private final HyDaoImpl hyDao;

    public HangxinTaxindexLib(HyDaoImpl hyDao) {
        this.hyDao = hyDao;
    }

    public String taxLev(String nsrsbh) {
        return hyDao.queryForString("hsyh", "hangxinTax.querytaxLev", nsrsbh);
    }

    public String taxStatus(String nsrsbh) {
        String taxStatus = hyDao.queryForString("hsyh", "hangxinTax.querytaxStatus", nsrsbh);
        if ("非正常".equals(taxStatus)) {
            return "Y";
        } else {
            return "N";
        }
    }

    public String oveTax(String nsrsbh, int month) {
        String begindate = CommUtils.getDateOfMonth(-month);
        Map<String, Object> param = new HashMap<>();
        param.put("nsrsbh", nsrsbh);
        param.put("begindate", begindate);
        int oveTax = hyDao.queryForInt("hsyh", "hangxinTax.queryOveTax", param);
        return Integer.toString(oveTax);
    }
    public String interTaxSale(Map<String, Object> param){
        List<Map<String, Object>> ysxInfoList = hyDao.queryForList("hsyh", "hangxinTax.queryInterTaxSale", param);
        if(!CommUtils.isEmptyList(ysxInfoList)&&ysxInfoList.size()>0){
            try{
                //相同金额数据加总合并
                String beginDate = (String) param.get("begindate");
                String endDate = (String) param.get("enddate");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                Date date=format.parse(beginDate);

                String date1=beginDate;
                Map<String, Object> sumysxMap = new TreeMap<>();
                int i=0;
                //初始化数据
                for (Map<String, Object> ysxInfo : ysxInfoList) {
                    if(ysxInfo.get(date1)==null){
                        sumysxMap.put(date1, 0);
                    }
                    date1 = CommUtils.getFirstDateByMonth(date, ++i);
                    if(date1.compareTo(endDate)>0){
                        break;
                    }
                }

                for (Map<String, Object> ysxInfo : ysxInfoList) {
                    String sssqz = (String) ysxInfo.get("sssqz");
                    String freq = (String) ysxInfo.get("freq");
                    double sumysx;
                    if (CommUtils.isEmptyMap(sumysxMap)) {
                        sumysx = 0;
                    } else {
                        sumysx = sumysxMap.get(sssqz) == null ? 0 : (double) sumysxMap.get(sssqz);
                    }
                    //如果纳税频率为季度且销售额为0，直接返回”Y“
                    double ysx = Double.parseDouble(ysxInfo.get("ysx").toString());
                    if ("Q".equals(freq) && ysx == 0) {
                        return "Y";
                    }
                    sumysx = sumysx + ysx;
                    sumysxMap.put(sssqz, sumysx);
                }
                int zeroCount = 0;
                for (String keyvalue : sumysxMap.keySet()) {
                    double sumysx = (double) sumysxMap.get(keyvalue);
                    if (sumysx == 0) {
                        zeroCount = zeroCount + 1;
                        if (zeroCount >= 3) {
                            return "Y";
                        }
                    } else {
                        zeroCount = 0;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "N";
    }

    public String mainBusDownnow(Map<String, Object> param) {
        List<Map<String, Object>> ysxseMapList = hyDao.queryForList("hsyh", "hangxinTax.querytaxmainBusDown", param);
        double sumYsxse = 0;
        int monthsCount = 0;
        for (Map<String, Object> ysxseMap : ysxseMapList) {
            double ysxse = (double) ysxseMap.get("ysxse");
            sumYsxse = sumYsxse + ysxse;
            if ("Q".equals(ysxseMap.get("rptflg"))) {
                monthsCount = monthsCount + 3;
            } else {
                monthsCount = monthsCount + 1;
            }
        }
        double avgYsxse = MathUtils.round(MathUtils.divide(sumYsxse, monthsCount), 2);
        return Double.toString(avgYsxse);
    }

    public String yearProfit(String nsrsbh,int year) {
        Map<String, Object> param = new HashMap<>();
        param.put("nsrsbh", nsrsbh);
        param.put("lastyear", CommUtils.getDate(-year).substring(0, 4));
        double lasProfit = hyDao.queryForDouble("hsyh", "hangxinTax.queryLasProfit", param);
        if (lasProfit <= 0.00d) {
            param.put("lastyear", CommUtils.getDate(-year-1).substring(0, 4));
            lasProfit = hyDao.queryForDouble("hsyh", "hangxinTax.queryLasProfit", param);
        }
        return Double.toString(lasProfit);
    }

    /*
     *企业近三个月申报销售额
     */
    public String taxSaletenper(Map<String, Object> param) {
        double taxSaletenper = hyDao.queryForDouble("hsyh", "hangxinTax.querytaxSaletenper", param);
        return Double.toString(taxSaletenper);
    }

    public String taxSalethirtyper(Map<String, Object> param) {
        double taxSalethirtyper = hyDao.queryForDouble("hsyh", "hangxinTax.querytaxSaletenper", param);
        return Double.toString(taxSalethirtyper);
    }

    public String tax3MIllegal(String nsrsbh) {
        String begindate = CommUtils.getDateOfMonth(-3);
        Map<String, Object> param = new HashMap<>();
        param.put("nsrsbh", nsrsbh);
        param.put("begindate", begindate);
        param.put("zsxmdm", "10101");
        int taxSalethirtyper = hyDao.queryForInt("hsyh", "hangxinTax.queryTax3MIllegal", param);
        return Integer.toString(taxSalethirtyper);
    }

    public String incTax(Map<String, Object> param) {
        param.put("zsxmdm", "10104");
        double incTax = hyDao.queryForDouble("hsyh", "hangxinTax.queryIncTax", param);
        return Double.toString(incTax);
    }

    public String tax2YPro(String nsrsbh) {
        Map<String, Object> param = new HashMap<>();
        String lastyear = CommUtils.getDate(-1).substring(0, 4);
        String beforlastyear = CommUtils.getDate(-2).substring(0, 4);
        param.put("nsrsbh", nsrsbh);
        param.put("lastyear", lastyear);
        param.put("beforlastyear", beforlastyear);
        double tax2YPro = hyDao.queryForDouble("hsyh", "hangxinTax.queryTax2YPro", param);
        return Double.toString(tax2YPro);
    }
    public String tax2YNetAss(String nsrsbh) {
        Map<String, Object> param = new HashMap<>();
        String lastyear = CommUtils.getDate(-1).substring(0, 4);
        String beforlastyear = CommUtils.getDate(-2).substring(0, 4);
        param.put("nsrsbh", nsrsbh);
        param.put("lastyear", lastyear);
        param.put("beforlastyear", beforlastyear);
        double tax2YNetAss = hyDao.queryForDouble("hsyh", "hangxinTax.querytax2YNetAss", param);
        return Double.toString(tax2YNetAss);
    }
    public String taxsale3M(Map<String, Object> param) {
        double taxsale3M = hyDao.queryForDouble("hsyh", "hangxinTax.querytaxSaletenper", param);
        return Double.toString(taxsale3M);
    }
    public String ysxse12M(Map<String, Object> param) {
        double YSXSE12M = hyDao.queryForDouble("hsyh", "hangxinTax.querytaxSaletenper", param);
        return Double.toString(YSXSE12M);
    }
    public String zzsNse12m(Map<String, Object> param) {
        double zzsNse12m = hyDao.queryForDouble("hsyh", "hangxinTax.queryzzsNse", param);
        return Double.toString(zzsNse12m);
    }
    public String lastYearTax(Map<String, Object> param) {
        double lastYearTax = hyDao.queryForDouble("hsyh", "hangxinTax.querylastYearTax", param);
        return Double.toString(lastYearTax);
    }
    public String ysxsrLast(Map<String, Object> param) {
        double ysxsrLast = hyDao.queryForDouble("hsyh", "hangxinTax.queryYSXSRTax", param);
        return Double.toString(ysxsrLast);
    }
    public String tax3MIllegal_unDeal(String nsrsbh) {
        int tax3MIllegal_unDeal = hyDao.queryForInt("hsyh", "hangxinTax.queryTax3MIllegal_unDeal", nsrsbh);
        return Integer.toString(tax3MIllegal_unDeal);
    }
}
