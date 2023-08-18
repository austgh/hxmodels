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

	public Map<String, Object> hangxinTaxModelData(String nsrsbh,Map<String, Object> entInfo) throws ParseException {
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("taxnsrlx",hangxinTaxindexLib.taxNsrlx(nsrsbh));
		modelMap.put("taxStatus",hangxinTaxindexLib.taxStatus(nsrsbh));
		modelMap.put("taxLev",hangxinTaxindexLib.taxLev(nsrsbh));
		modelMap.put("over24MTax",hangxinTaxindexLib.oveTax(nsrsbh,24));
		modelMap.put("over6MTax",hangxinTaxindexLib.oveTax(nsrsbh,6));
		modelMap.put("oveTax",hangxinTaxindexLib.oveTax(nsrsbh,12));
		modelMap.put("tax3MIllegal_unDeal",hangxinTaxindexLib.tax3MIllegal_unDeal(nsrsbh));

		modelMap.put("lasProfit",hangxinTaxindexLib.yearProfit(nsrsbh,1));
		modelMap.put("befLasYearProfit",hangxinTaxindexLib.yearProfit(nsrsbh,2));


		modelMap.put("tax3MIllegal",hangxinTaxindexLib.tax3MIllegal(nsrsbh));
		modelMap.put("tax2YNetAss",hangxinTaxindexLib.tax2YNetAss(nsrsbh));
		modelMap.put("tax2YPro",hangxinTaxindexLib.tax2YPro(nsrsbh));

		//最近一笔增值税的结束日期 sssqz  2023-01-31
		String volumeUpDate=hyDao.queryForString("hsyh","hangxinTax.queryRecentlyDate",nsrsbh);
		log.info("获取申报表的最新一期增值税数据的截止日期为:{}", volumeUpDate);

		if(!CommUtils.isEmptyStr(volumeUpDate)){
			Date date=new SimpleDateFormat("yyyyy-MM-dd").parse(volumeUpDate);
			//最近一年内
			String begindate1Year = CommUtils.getFirstDateByMonth(date, -11);
			String enddate1Year = CommUtils.getFirstDateByMonth(date, 1);
			//最近两年内
			String begindate2Year = CommUtils.getFirstDateByMonth(date, -23);
			String enddate2Year = CommUtils.getFirstDateByMonth(date, -11);
			//最近3个月内
			String begindate3M = CommUtils.getFirstDateByMonth(date, -2);
			String enddate3M = CommUtils.getFirstDateByMonth(date, 1);
			Map<String, Object> param1Year = initParamMap(nsrsbh, begindate1Year, enddate1Year);
			Map<String, Object> param2Year = initParamMap(nsrsbh, begindate2Year, enddate2Year);
			Map<String, Object> param3M = initParamMap(nsrsbh, begindate3M, enddate3M);
			modelMap.put("interTaxSale",hangxinTaxindexLib.interTaxSale(param1Year));

			modelMap.put("mainBusDownnow",hangxinTaxindexLib.mainBusDownnow(param1Year));
			modelMap.put("mainBusDownlast",hangxinTaxindexLib.mainBusDownnow(param2Year));

			modelMap.put("lastYearTax",hangxinTaxindexLib.lastYearTax(param1Year));
			modelMap.put("befLasYearTax",hangxinTaxindexLib.lastYearTax(param2Year));

			modelMap.put("YSXSRLast",hangxinTaxindexLib.ysxsrLast(param1Year));
			modelMap.put("YSXSRBef",hangxinTaxindexLib.ysxsrLast(param2Year));


			modelMap.put("YSXSE12M",hangxinTaxindexLib.ysxse12M(param1Year));

			modelMap.put("ysxse24m",hangxinTaxindexLib.ysxse12M(param2Year));

			modelMap.put("zzsNse12m",hangxinTaxindexLib.zzsNse12m(param1Year));
			modelMap.put("zzsNse24m",hangxinTaxindexLib.zzsNse12m(param2Year));

			modelMap.put("taxSalethirtyper",hangxinTaxindexLib.taxSalethirtyper(param1Year));

			modelMap.put("taxsale3M",hangxinTaxindexLib.taxsale3M(param3M));
			modelMap.put("taxSaletenper",hangxinTaxindexLib.taxSaletenper(param3M));


		}
		/*获取申报表的最新一期申报数据的截止日期*/
		String lastRepartDate=hyDao.queryForString("hsyh","hangxinTax.queryRecentlyIncomeTaxDate",nsrsbh);
		log.info("获取申报表的最新一期所得税数据的截止日期为:{}", lastRepartDate);
		//待处理日期
		if(!CommUtils.isEmptyStr(lastRepartDate)){
			Date lastDate = new SimpleDateFormat("yyyy-MM-dd").parse(lastRepartDate);
			String begindate1Year = CommUtils.getFirstDateByMonth(lastDate, -11);
			String enddate1Year = CommUtils.getFirstDateByMonth(lastDate, 1);
			Map<String, Object> param = initParamMap(nsrsbh, begindate1Year, enddate1Year);
			modelMap.put("incTax",hangxinTaxindexLib.incTax(param));
		}
		modelMap.putAll(entInfo);
		log.info("{}指标加工结果:{}", entInfo.get("nsrmc"), modelMap);
		hyDao.insert("hsyh", "common.insertHsyhDcmTransLog", modelMap);
		return modelMap;
	}
	/*
	 *开始时间和结束时间一年内的增值税
	 */
	private Map<String, Object> initParamMap(String nsrsbh, String beginDate, String endDate) {
		Map<String, Object> param = new HashMap<>();
		param.put("nsrsbh", nsrsbh);
		param.put("begindate", beginDate);
		param.put("enddate", endDate);
		param.put("zsxmdm", "10101");
		param.put("year", beginDate.substring(0,4));
		return param;
	}
}
