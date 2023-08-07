package com.ahzx.mdfc.model;

import com.ahzx.mdfc.dao.hshy.HyDaoImpl;
import com.ahzx.mdfc.utils.CommUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
/**
 * 指标集中加工模块
 * **/
@Component
public class IndexManageModel {
	private final HyDaoImpl hyDao;
	private final HangxinTaxindexLib hangxinTaxindexLib;
	private static Logger log= LoggerFactory.getLogger(IndexManageModel.class);

	public IndexManageModel(HyDaoImpl hyDao, HangxinTaxindexLib hangxinTaxindexLib) {
		this.hyDao = hyDao;
		this.hangxinTaxindexLib = hangxinTaxindexLib;
	}

	public Map<String, Object> hangxinTaxModelData(String nsrsbh){
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("taxStatus",hangxinTaxindexLib.taxStatus(nsrsbh));
		modelMap.put("taxLev",hangxinTaxindexLib.taxLev(nsrsbh));
		modelMap.put("over24MTax",hangxinTaxindexLib.over24MTax(nsrsbh));
		modelMap.put("over6MTax",hangxinTaxindexLib.over6MTax(nsrsbh));
		modelMap.put("oveTax",hangxinTaxindexLib.oveTax(nsrsbh));
		modelMap.put("tax3MIllegal_unDeal",hangxinTaxindexLib.tax3MIllegal_unDeal(nsrsbh));
		modelMap.put("befLasYearProfit",hangxinTaxindexLib.befLasYearProfit(nsrsbh));
		modelMap.put("lasProfit",hangxinTaxindexLib.lasProfit(nsrsbh));
		modelMap.put("tax3MIllegal",hangxinTaxindexLib.tax3MIllegal(nsrsbh));
		modelMap.put("tax2YNetAss",hangxinTaxindexLib.tax2YNetAss(nsrsbh));
		modelMap.put("tax2YPro",hangxinTaxindexLib.tax2YPro(nsrsbh));
		/*获取申报表的最新一期申报数据的截止日期*/
		String lastRepartDate=hyDao.queryForString("mapping/hshy","hangxinTax.queryLastRepartDate",nsrsbh);
		log.info("获取申报表的最新一期申报数据的截止日期为:{}", lastRepartDate);
		if(!CommUtils.isEmptyStr(lastRepartDate)){
			modelMap.put("interTaxSale",hangxinTaxindexLib.interTaxSale(nsrsbh,lastRepartDate));
			modelMap.put("mainBusDownlast",hangxinTaxindexLib.mainBusDownlast(nsrsbh,lastRepartDate));
			modelMap.put("mainBusDownnow",hangxinTaxindexLib.mainBusDownnow(nsrsbh,lastRepartDate));
			modelMap.put("YSXSRLast",hangxinTaxindexLib.YSXSRLast(nsrsbh,lastRepartDate));
			modelMap.put("YSXSE12M",hangxinTaxindexLib.YSXSE12M(nsrsbh,lastRepartDate));
			modelMap.put("ysxse24m",hangxinTaxindexLib.ysxse24m(nsrsbh,lastRepartDate));
			modelMap.put("YSXSRBef",hangxinTaxindexLib.YSXSRBef(nsrsbh,lastRepartDate));
			modelMap.put("taxsale3M",hangxinTaxindexLib.taxsale3M(nsrsbh,lastRepartDate));
			modelMap.put("befLasYearTax",hangxinTaxindexLib.befLasYearTax(nsrsbh,lastRepartDate));
			modelMap.put("lastYearTax",hangxinTaxindexLib.lastYearTax(nsrsbh,lastRepartDate));
			modelMap.put("zzsNse12m",hangxinTaxindexLib.zzsNse12m(nsrsbh,lastRepartDate));
			modelMap.put("zzsNse24m",hangxinTaxindexLib.zzsNse24m(nsrsbh,lastRepartDate));
			modelMap.put("incTax",hangxinTaxindexLib.incTax(nsrsbh,lastRepartDate));
			modelMap.put("taxSaletenper",hangxinTaxindexLib.taxSaletenper(nsrsbh,lastRepartDate));
			modelMap.put("taxSalethirtyper",hangxinTaxindexLib.taxSalethirtyper(nsrsbh,lastRepartDate));
		}
		return modelMap;
	}

}
