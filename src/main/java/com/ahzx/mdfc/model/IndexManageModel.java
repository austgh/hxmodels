package com.ahzx.mdfc.model;

import com.ahzx.mdfc.dao.hshy.HyDaoImpl;
import com.ahzx.mdfc.utils.CommUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * 指标集中加工模块
 * **/
@Component
public class IndexManageModel {
	private final HyDaoImpl hyDao;
	private final HangxinTaxindexLib hangxinTaxindexLib;
	private static final Logger log= LoggerFactory.getLogger(IndexManageModel.class);

	public IndexManageModel(HyDaoImpl hyDao, HangxinTaxindexLib hangxinTaxindexLib) {
		this.hyDao = hyDao;
		this.hangxinTaxindexLib = hangxinTaxindexLib;
	}

	public Map<String, Object> hangxinTaxModelData(String nsrsbh) throws ParseException {
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("taxStatus",hangxinTaxindexLib.taxStatus(nsrsbh));
		modelMap.put("taxLev",hangxinTaxindexLib.taxLev(nsrsbh));
		modelMap.put("over24MTax",hangxinTaxindexLib.oveTax(nsrsbh,24));
		modelMap.put("over6MTax",hangxinTaxindexLib.oveTax(nsrsbh,6));
		modelMap.put("oveTax",hangxinTaxindexLib.oveTax(nsrsbh,12));
		modelMap.put("tax3MIllegal_unDeal",hangxinTaxindexLib.tax3MIllegal_unDeal(nsrsbh));

		modelMap.put("befLasYearProfit",hangxinTaxindexLib.befLasYearProfit(nsrsbh));
		modelMap.put("lasProfit",hangxinTaxindexLib.lasProfit(nsrsbh));

		modelMap.put("tax3MIllegal",hangxinTaxindexLib.tax3MIllegal(nsrsbh));
		modelMap.put("tax2YNetAss",hangxinTaxindexLib.tax2YNetAss(nsrsbh));
		modelMap.put("tax2YPro",hangxinTaxindexLib.tax2YPro(nsrsbh));

		//最近一笔增值税的日期
		String volumeUpDate=hyDao.queryForString("hsyh","hangxinTax.queryRecentlyDate",nsrsbh);
		log.info("获取申报表的最新一期增值税数据的截止日期为:{}", volumeUpDate);

		if(!CommUtils.isEmptyStr(volumeUpDate)){
			Date date=new SimpleDateFormat("yyyyy-MM-dd").parse(volumeUpDate);

			modelMap.put("interTaxSale",hangxinTaxindexLib.interTaxSale(nsrsbh,date));
			modelMap.put("mainBusDownlast",hangxinTaxindexLib.mainBusDownnow(nsrsbh,date,23));
			modelMap.put("mainBusDownnow",hangxinTaxindexLib.mainBusDownnow(nsrsbh,date,11));
			modelMap.put("befLasYearTax",hangxinTaxindexLib.befLasYearTax(nsrsbh,date,23));
			modelMap.put("lastYearTax",hangxinTaxindexLib.befLasYearTax(nsrsbh,date,11));
			modelMap.put("YSXSRLast",hangxinTaxindexLib.YSXSRLast(nsrsbh,date,11));
			modelMap.put("YSXSRBef",hangxinTaxindexLib.YSXSRLast(nsrsbh,date,23));
			modelMap.put("YSXSE12M",hangxinTaxindexLib.YSXSE12M(nsrsbh,date,11));
			modelMap.put("ysxse24m",hangxinTaxindexLib.YSXSE12M(nsrsbh,date,23));
			modelMap.put("taxsale3M",hangxinTaxindexLib.taxsale3M(nsrsbh,date));
			modelMap.put("zzsNse12m",hangxinTaxindexLib.zzsNse12m(nsrsbh,date,11));
			modelMap.put("zzsNse24m",hangxinTaxindexLib.zzsNse12m(nsrsbh,date,23));
			modelMap.put("taxSaletenper",hangxinTaxindexLib.taxSaletenper(nsrsbh,date));
			modelMap.put("taxSalethirtyper",hangxinTaxindexLib.taxSalethirtyper(nsrsbh,date));
		}
		/*获取申报表的最新一期申报数据的截止日期*/
		String lastRepartDate=hyDao.queryForString("hsyh","hangxinTax.queryRecentlyIncomeTaxDate",nsrsbh);
		log.info("获取申报表的最新一期所得税数据的截止日期为:{}", lastRepartDate);
		//待处理日期
		if(!CommUtils.isEmptyStr(lastRepartDate)){
			Date lastdate = new SimpleDateFormat("yyyy-MM-dd").parse(lastRepartDate);
			modelMap.put("incTax",hangxinTaxindexLib.incTax(nsrsbh,lastdate));
		}
		return modelMap;
	}

}
