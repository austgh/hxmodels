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
        return hyDao.queryForString("mapping/hshy","hangxinTax.querytaxLev",nsrsbh);
    }

    public String taxStatus(String nsrsbh) {
        String taxStatus=hyDao.queryForString("mapping/hshy","hangxinTax.querytaxStatus",nsrsbh);
        if("非正常".equals(taxStatus)){
            return "Y";
        }else{
            return "N";
        }
    }

    public String taxSaletenper(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-2);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",lastdatestr);
            param.put("zsxmdm","10101");
            double taxSaletenper=hyDao.queryForDouble("mapping/hshy","hangxinTax.querytaxSaletenper",param);
            return Double.toString(taxSaletenper);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String oveTax(String nsrsbh) {
        String begindate=CommUtils.getDateOfMonth(-12);
        Map<String,Object> param=new HashMap<>();
        param.put("nsrsbh",nsrsbh);
        param.put("begindate",begindate);
        int oveTax=hyDao.queryForInt("mapping/hshy","hangxinTax.queryOveTax",param);
        return  Integer.toString(oveTax);
    }

    public String over6MTax(String nsrsbh) {
        String begindate=CommUtils.getDateOfMonth(-6);
        Map<String,Object> param=new HashMap<>();
        param.put("nsrsbh",nsrsbh);
        param.put("begindate",begindate);
        int oveTax=hyDao.queryForInt("mapping/hshy","hangxinTax.queryOveTax",param);
        return  Integer.toString(oveTax);
    }

    public String over24MTax(String nsrsbh) {
        String begindate=CommUtils.getDateOfMonth(-24);
        Map<String,Object> param=new HashMap<>();
        param.put("nsrsbh",nsrsbh);
        param.put("begindate",begindate);
        int oveTax=hyDao.queryForInt("mapping/hshy","hangxinTax.queryOveTax",param);
        return  Integer.toString(oveTax);
    }

    public String interTaxSale(String nsrsbh,String lastDateStr) {
        try {
            Date lastDate=new SimpleDateFormat("yyyyy/MM/dd").parse(lastDateStr);
            String beginDate=CommUtils.getFirstDateByMonth(lastDate,-11);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",beginDate);
            param.put("enddate",lastDateStr);
            param.put("zsxmdm","10101");
            List<Map<String,Object>> ysxInfoList=hyDao.queryForList("mapping/hshy", "hangxinTax.queryInterTaxSale", param);

            //相同金额数据加总合并
            Map<String,Object> sumysxMap=new TreeMap<>();
            for(Map<String,Object> ysxInfo:ysxInfoList){
                String sssqz=(String)ysxInfo.get("sssqz");
                String freq=(String)ysxInfo.get("freq");
                double sumysx;
                if( CommUtils.isEmptyMap(sumysxMap)){
                    sumysx=0;
                }else{
                    sumysx= sumysxMap.get(sssqz)==null?0: (double) sumysxMap.get(sssqz);
                }
                //如果纳税频率为季度且销售额为0，直接返回”Y“
                double ysx=Double.parseDouble(ysxInfo.get("ysx").toString());
                if("Q".equals(freq)&&ysx==0){
                    return "Y";
                }
                sumysx=sumysx+ysx;
                sumysxMap.put(sssqz,sumysx);
            }
            int zeroCount=0;
            for(String keyvalue:sumysxMap.keySet()){
                double sumysx =(double)sumysxMap.get(keyvalue);
                if(sumysx==0){
                    zeroCount=zeroCount+1;
                    if(zeroCount>=3){
                        return "Y";
                    }
                }else{
                    zeroCount=0;
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return "N";
    }

    public String mainBusDownnow(String nsrsbh,String lastdatestr) {
        try {
            SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-12);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",lastdatestr);
            param.put("zsxmdm","10101");
            List<Map<String,Object>> ysxseMapList=hyDao.queryForList("mapping/hshy","hangxinTax.querytaxmainBusDown",param);
            double sumYsxse=0;
            int monthsCount=0;
            for(Map<String,Object> ysxseMap:ysxseMapList){
              double ysxse= (double) ysxseMap.get("ysxse");
              sumYsxse=sumYsxse+ysxse;
              if("Q".equals(ysxseMap.get("rptflg"))){
                  monthsCount=monthsCount+3;
              }else{
                  monthsCount=monthsCount+1;
              }
            }
            double avgYsxse=MathUtils.round(MathUtils.divide(sumYsxse, monthsCount), 2);
            return Double.toString(avgYsxse);
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String mainBusDownlast(String nsrsbh,String lastdatestr) {
        try {
            SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-23);
            String enddate=CommUtils.getFirstDateByMonth(lastdate,-12);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",enddate);
            param.put("zsxmdm","10101");
            List<Map<String,Object>> ysxseMapList=hyDao.queryForList("mapping/hshy","hangxinTax.querytaxmainBusDown",param);
            double sumYsxse=0;
            int monthsCount=0;
            if(!CommUtils.isEmptyList(ysxseMapList)){
                for(Map<String,Object> ysxseMap:ysxseMapList){
                    double ysxse= (double) ysxseMap.get("ysxse");
                    sumYsxse=sumYsxse+ysxse;
                    if("Q".equals(ysxseMap.get("rptflg"))){
                        monthsCount=monthsCount+3;
                    }else{
                        monthsCount=monthsCount+1;
                    }
                }
                double avgYsxse=MathUtils.round(MathUtils.divide(sumYsxse, monthsCount), 2);
                return Double.toString(avgYsxse);
            }
            return "0";

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String lasProfit(String nsrsbh) {
        //查询上年度的利润总额lastyear
        String laetyear = CommUtils.getDateOfMonth(-1).substring(0,4);
        Map<String,Object> param=new HashMap<>();
        param.put("nsrsbh",nsrsbh);
        param.put("lastyear",laetyear);
        double lasProfit=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryLasProfit",param);
        if(lasProfit==0.00d){
            laetyear = CommUtils.getDateOfMonth(-2).substring(0,4);
            param.put("lastyear",laetyear);
            lasProfit=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryLasProfit",param);
        }
        return Double.toString(lasProfit);
    }

    public String befLasYearProfit(String nsrsbh) {
        //查询上年度的利润总额lastyear
        String laetyear = CommUtils.getDateOfMonth(-2).substring(0,4);
        Map<String,Object> param=new HashMap<>();
        param.put("nsrsbh",nsrsbh);
        param.put("lastyear",laetyear);
        double lasProfit=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryLasProfit",param);
        if(lasProfit==0.00d){
            laetyear = CommUtils.getDateOfMonth(-3).substring(0,4);
            param.put("lastyear",laetyear);
            lasProfit=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryLasProfit",param);
        }
        return Double.toString(lasProfit);
    }

    public String taxSalethirtyper(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-11);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",lastdatestr);
            param.put("zsxmdm","10101");
            double taxSalethirtyper=hyDao.queryForDouble("mapping/hshy","hangxinTax.querytaxSaletenper",param);
            return Double.toString(taxSalethirtyper);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String tax3MIllegal(String nsrsbh) {
        String begindate=CommUtils.getDateOfMonth(-3);
        Map<String,Object> param=new HashMap<>();
        param.put("nsrsbh",nsrsbh);
        param.put("begindate",begindate);
        param.put("zsxmdm","10101");
        int taxSalethirtyper=hyDao.queryForInt("mapping/hshy","hangxinTax.queryTax3MIllegal",param);
        return Integer.toString(taxSalethirtyper);
    }

    public String incTax(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-11);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("beginyear",begindate.substring(0,4));
            param.put("zsxmdm","10104");
            double incTax=hyDao.queryForDouble("mapping/hshy","hangxinTax.querylastYearTax",param);
            return Double.toString(incTax);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String tax2YPro(String nsrsbh) {
        Map<String,Object> param=new HashMap<>();
        String lastyear=CommUtils.getDateOfMonth(-1).substring(0,4);
        String beforlastyear=CommUtils.getDateOfMonth(-2).substring(0,4);
        param.put("nsrsbh",nsrsbh);
        param.put("lastyear",lastyear);
        param.put("beforlastyear",beforlastyear);
        double tax2YPro=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryTax2YPro",param);
        return Double.toString(tax2YPro);
    }

    public String tax2YNetAss(String nsrsbh) {
        Map<String,Object> param=new HashMap<>();
        String lastyear=CommUtils.getDateOfMonth(-1).substring(0,4);
        String beforlastyear=CommUtils.getDateOfMonth(-2).substring(0,4);
        param.put("nsrsbh",nsrsbh);
        param.put("lastyear",lastyear);
        param.put("beforlastyear",beforlastyear);
        double tax2YNetAss=hyDao.queryForDouble("mapping/hshy","hangxinTax.querytax2YNetAss",param);
        return Double.toString(tax2YNetAss);
    }


    public String taxsale3M(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-2);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",lastdatestr);
            param.put("zsxmdm","10101");
            double taxsale3M=hyDao.queryForDouble("mapping/hshy","hangxinTax.querytaxSaletenper",param);
            return Double.toString(taxsale3M);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String YSXSE12M(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-11);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",lastdatestr);
            param.put("zsxmdm","10101");
            double YSXSE12M=hyDao.queryForDouble("mapping/hshy","hangxinTax.querytaxSaletenper",param);
            return Double.toString(YSXSE12M);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String ysxse24m(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-11);
            String enddate=CommUtils.getFirstDateByMonth(lastdate,-12);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",enddate);
            param.put("zsxmdm","10101");
            double ysxse24m=hyDao.queryForDouble("mapping/hshy","hangxinTax.querytaxSaletenper",param);
            return Double.toString(ysxse24m);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String zzsNse12m(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-11);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate", lastdatestr);
            param.put("zsxmdm","10101");
            double zzsNse12m=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryzzsNse",param);
            return Double.toString(zzsNse12m);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String zzsNse24m(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-23);
            String enddate=CommUtils.getFirstDateByMonth(lastdate,-12);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("begindate",begindate);
            param.put("enddate",enddate);
            param.put("zsxmdm","10101");
            double zzsNse24m=hyDao.queryForDouble("mapping/hshy","hangxinTax.queryzzsNse",param);
            return Double.toString(zzsNse24m);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String lastYearTax(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-23);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("beginyear",begindate.substring(0,4));
            param.put("zsxmdm","10101");
            double lastYearTax=hyDao.queryForDouble("mapping/hshy","hangxinTax.querylastYearTax",param);
            return Double.toString(lastYearTax);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String befLasYearTax(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-23);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("beginyear",begindate.substring(0,4));
            param.put("zsxmdm","10101");
            double befLasYearTax=hyDao.queryForDouble("mapping/hshy","hangxinTax.querylastYearTax",param);
            return Double.toString(befLasYearTax);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String YSXSRLast(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-11);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("beginyear",begindate.substring(0,4));
            param.put("zsxmdm","10101");
            double lastYearTax=hyDao.queryForDouble("mapping/hshy","hangxinTax.querylastYearTax",param);
            return Double.toString(lastYearTax);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }

    public String YSXSRBef(String nsrsbh,String lastdatestr) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date lastdate=format.parse(lastdatestr);
            String begindate=CommUtils.getFirstDateByMonth(lastdate,-23);
            Map<String,Object> param=new HashMap<>();
            param.put("nsrsbh",nsrsbh);
            param.put("beginyear",begindate.substring(0,4));
            param.put("zsxmdm","10101");
            double lastYearTax=hyDao.queryForDouble("mapping/hshy","hangxinTax.querylastYearTax",param);
            return Double.toString(lastYearTax);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return "0";
        }
    }


    public String tax3MIllegal_unDeal(String nsrsbh) {
        int tax3MIllegal_unDeal=hyDao.queryForInt("mapping/hshy","hangxinTax.queryTax3MIllegal_unDeal",nsrsbh);
        return Integer.toString(tax3MIllegal_unDeal);
    }
}