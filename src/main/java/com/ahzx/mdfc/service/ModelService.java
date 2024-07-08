package com.ahzx.mdfc.service;


import com.ahzx.mdfc.dao.hshy.HyDaoImpl;
import com.ahzx.mdfc.model.IndexManageModel;
import com.ahzx.mdfc.utils.CommUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author gonghe
 * @date 2023年07月2023/7/25日15:54
 */
@Service
public class ModelService {
    private final Logger log = LoggerFactory.getLogger(ModelService.class);
    private final HyDaoImpl hyDao;

    IndexManageModel indexManageModel;

    public ModelService(HyDaoImpl hyDao, IndexManageModel indexManageModel) {
        this.hyDao = hyDao;
        this.indexManageModel = indexManageModel;
    }

    @Scheduled(cron = "${modelCronExpr}")
    public void process() throws IOException {
        long startTime = System.currentTimeMillis();
        log.info("开始处理数据!");
        String date = CommUtils.getDate(0).replace("-", "");
        String yesterday = CommUtils.getDate(0, 0, -1).replace("-", "");
        if(date.compareTo("20260630")>0){
            return;
        }

        List<Map<String, Object>> todoList = hyDao.queryForList("hsyh", "common.queryTodoListInfo", yesterday);
        if(CommUtils.isEmptyList(todoList)){
            log.info("{}没有对应的数据需要导出", date);
            return;
        }
        //文件路径
        String filePath = "./hxData/output/" + date + "/result.csv";
        File fileName = getFile(date, filePath);
        if (fileName == null) {
            return;
        }

        ListIterator<Map<String, Object>> iterator = todoList.listIterator();
        String entname = "";
        String serialno = "";
        BufferedWriter bufferwriter =
                new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(fileName.toPath()),
                StandardCharsets.UTF_8));
        while (iterator.hasNext()) {
            try {
                Map<String, Object> entInfo = iterator.next();
                entname = (String) entInfo.get("nsrmc");
                log.info("【检索到需要处理的企业为{}】", entname);
                serialno = (String) entInfo.get("serialno");
                String nsrsbh = (String) entInfo.get("nsrsbh");

                //加工指标
                Map<String, Object> modelMap = indexManageModel.hangxinTaxModelData(nsrsbh,entInfo);
                //处理结果
                Map<String, Object> resultMap = getModelRule(modelMap, entInfo,date);

                //写文件中
                writeData(bufferwriter, resultMap, log);
            } catch (Exception e) {
                saveExceptionInfo(entname, serialno, e);
            } finally {
                // 关闭流
                log.info("企业{}处理完毕!】", entname);
            }
        }
        bufferwriter.close();
        log.info("文件写入成功");
        String successFlag = "./hxData/output/" + date + "/success.ok";
        File fileSuccess = new File(successFlag);
        if (fileSuccess.createNewFile()) {
            log.info("标志文件创建成功！");
        }
        long endTime = System.currentTimeMillis();
        int time = (int) ((endTime - startTime) / 1000);
        log.info("{}条数据导入共耗时{}秒",todoList.size(),time);
    }

    static void writeData(BufferedWriter bufferwriter, Map<String, Object> resultMap, Logger log) throws IOException {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(resultMap.get("serialno")).append(";").append(resultMap.get("nsrmc")).append(";").append(resultMap.get("nsrsbh")).append(";").append(resultMap.get("inputtime")).append(";").append(resultMap.get("rulecd")).append(";").append(resultMap.get("score"));
        log.info("数据为:{}", stringBuffer);
        bufferwriter.write(stringBuffer.toString());
        bufferwriter.newLine();
    }

    private Map<String, Object> getModelRule(Map<String, Object> modelMap, Map<String, Object> entInfo,String date) {
        Map<String, Object> resultMap = new HashMap<>(entInfo);
        String ifkjedai = entInfo.get("ifkjedai") == null ? "N" : (String) entInfo.get("ifkjedai");
        String hytype = (String) entInfo.get("hytype");
        String hytypecode = (String) entInfo.get("hytypecode");
        String xedoldcust = entInfo.get("xedoldcust") == null ? "N" : (String) entInfo.get("xedoldcust");
        int score = 0;
        StringBuilder result = new StringBuilder();
        String taxLev = modelMap.get("taxLev") == null ? "" : modelMap.get("taxLev").toString();
        if (!"A".equals(taxLev) && !"B".equals(taxLev) && !"M".equals(taxLev)) {
            result.append("AP26,");
            score += 500;
        }

        String taxNsrlx = modelMap.get("taxnsrlx") == null ? "N" : modelMap.get("taxnsrlx").toString();
        if ("Y".equals(taxNsrlx)) {
            result.append("BL69,");
            score += 500;
        }


        String taxStatus = modelMap.get("taxStatus") == null ? "" : modelMap.get("taxStatus").toString();
        if ("Y".equals(taxStatus)) {
            result.append("AP27,");
        }


        String lastYearTax = modelMap.get("lastYearTax") == null ? "0" : modelMap.get("lastYearTax").toString();
        String befLasYearTax = modelMap.get("befLasYearTax") == null ? "0" : modelMap.get("befLasYearTax").toString();
        double lastYearTaxValue = Double.parseDouble(lastYearTax);
        double befLasYearTaxValue = Double.parseDouble(befLasYearTax);
        if(lastYearTaxValue!=0||befLasYearTaxValue!=0||!"N".equals(xedoldcust)){
            if ("Y".equals(xedoldcust) && lastYearTaxValue <= 100000) {
                score += 20;
                result.append("AP28,");
            }
            if (!"Y".equals(xedoldcust) && "N".equals(ifkjedai)) {
                if ("C".equals(hytype) && lastYearTaxValue <= befLasYearTaxValue * 0.58) {
                    score += 40;
                    result.append("AP28,");
                }
                if (("E".equals(hytype) || "G".equals(hytype) || "K".equals(hytype)) && lastYearTaxValue <= befLasYearTaxValue * 0.62) {
                    score += 20;
                    result.append("AP28,");
                }
                if (!"C".equals(hytype) && !"E".equals(hytype) && !"G".equals(hytype) && !"K".equals(hytype) && lastYearTaxValue <= befLasYearTaxValue * 0.7) {
                    score += 40;
                    result.append("AP28,");
                }
            }
            if (!"Y".equals(xedoldcust) && "Y".equals(ifkjedai)) {
                if (lastYearTaxValue <= befLasYearTaxValue * 0.7) {
                    score += 40;
                    result.append("AP28,");
                }
            }
        }

        String incTax = modelMap.get("incTax") == null ? "0" : modelMap.get("incTax").toString();

        double incTaxValue = Double.parseDouble(incTax);
        if (incTaxValue <= 0 && !"Y".equals(xedoldcust)) {
            if ("N".equals(ifkjedai)) {
                score += 20;
            }
            result.append("AP29,");
        }


        String taxSaletenper = modelMap.get("taxSaletenper") == null ? "0" : modelMap.get("taxSaletenper").toString();
        String taxSalethirtyper = modelMap.get("taxSalethirtyper") == null ? "0" :
                modelMap.get("taxSalethirtyper").toString();
        double taxSaletenperValue = Double.parseDouble(taxSaletenper);
        double taxSalethirtyperValue = Double.parseDouble(taxSalethirtyper);
        if (!"Y".equals(xedoldcust) && taxSalethirtyperValue > 0 && taxSalethirtyperValue / 4 * 0.7 >= taxSaletenperValue && taxSalethirtyperValue / 4 * 0.5 < taxSaletenperValue) {
            score += 20;
            result.append("AP30,");
        }

        if ("N".equals(ifkjedai) && taxSalethirtyperValue < 20000000 && ("516".equals(hytypecode) || "5164".equals(hytypecode) || "5165".equals(hytypecode))) {
            score += 500;
            result.append("AP39,");
        } else if ("N".equals(ifkjedai) && taxSalethirtyperValue < 5000000) {
            score += 500;
            result.append("AP39,");
        } else if ("Y".equals(ifkjedai) && taxSalethirtyperValue < 2000000) {
            score += 500;
            result.append("AP39,");
        }

        String mainBusDownnow = modelMap.get("mainBusDownnow") == null ? "0" :
                modelMap.get("mainBusDownnow").toString();
        String mainBusDownlast = modelMap.get("mainBusDownlast") == null ? "0" :
                modelMap.get("mainBusDownlast").toString();

        double mainBusDownnowValue = Double.parseDouble(mainBusDownnow);
        double mainBusDownlastValue = Double.parseDouble(mainBusDownlast);
        boolean less_than_20000000 = (taxSalethirtyperValue >= 0 && taxSalethirtyperValue < 20000000);
        boolean between_20000000_and_50000000 = (taxSalethirtyperValue >= 20000000 && taxSalethirtyperValue < 50000000);
        boolean more_than_50000000 = taxSalethirtyperValue >= 50000000;
        boolean less_than_50000000 = (taxSalethirtyperValue >= 0 && taxSalethirtyperValue < 50000000);
        boolean between_50000000_and_100000000 = taxSalethirtyperValue >= 50000000 && taxSalethirtyperValue < 100000000;
        boolean more_than_100000000 = taxSalethirtyperValue >= 100000000;

        boolean less_than_0_5 = mainBusDownnowValue < 0.5 * mainBusDownlastValue;
        boolean between_0_6_and_0_7 =
                mainBusDownnowValue < mainBusDownlastValue * 0.7 && mainBusDownnowValue >= mainBusDownlastValue * 0.6;
        boolean between_0_5_and_0_6 =
                mainBusDownnowValue < mainBusDownlastValue * 0.6 && mainBusDownnowValue >= mainBusDownlastValue * 0.5;

        if ("Y".equals(xedoldcust)) {
            if (less_than_20000000 && less_than_0_5) {
                score += 220;
                result.append("AP37,");
            } else if (between_20000000_and_50000000 && less_than_0_5) {
                score += 200;
                result.append("AP37,");
            } else if (more_than_50000000 && less_than_0_5) {
                score += 170;
                result.append("AP37,");
            } else if ((taxSalethirtyperValue / 4 * 0.3 >= taxSaletenperValue) && taxSalethirtyperValue <= 20000000) {
                score += 20;
                result.append("AP31,");
            }
        } else {
            if (less_than_50000000 && between_0_6_and_0_7) {
                score += 150;
                result.append("AP37,");
            } else if (less_than_50000000 && between_0_5_and_0_6) {
                score += 200;
                result.append("AP37,");
            } else if (less_than_50000000 && less_than_0_5) {
                score += 250;
                result.append("AP37,");
            } else if (between_50000000_and_100000000 && between_0_6_and_0_7) {
                score += 120;
                result.append("AP37,");
            } else if (between_50000000_and_100000000 && between_0_5_and_0_6) {
                score += 170;
                result.append("AP37,");
            } else if (between_50000000_and_100000000 && less_than_0_5) {
                score += 220;
                result.append("AP37,");
            } else if (more_than_100000000 && between_0_6_and_0_7) {
                score += 100;
                result.append("AP37,");
            } else if (more_than_100000000 && between_0_5_and_0_6) {
                score += 150;
                result.append("AP37,");
            } else if (more_than_100000000 && less_than_0_5) {
                score += 200;
                result.append("AP37,");
            } else if ((taxSalethirtyperValue / 4 * 0.5 >= taxSaletenperValue)) {
                score += 50;
                result.append("AP31,");
            }
        }

        String over6MTax = modelMap.get("over6MTax") == null ? "0" : modelMap.get("over6MTax").toString();
        String oveTax = modelMap.get("oveTax") == null ? "0" : modelMap.get("oveTax").toString();
        double over6MTaxValue = Double.parseDouble(over6MTax);
        double oveTaxValue = Double.parseDouble(oveTax);
        if (oveTaxValue >= 4 || over6MTaxValue >= 2) {
            score += 500;
            result.append("AP32,");
        }

        String interTaxSale = modelMap.get("interTaxSale") == null ? "" : modelMap.get("interTaxSale").toString();
        if (!"Y".equals(xedoldcust) && "Y".equals(interTaxSale)) {
            score += 50;
            result.append("AP33,");
        }

        String befLasYearProfit = modelMap.get("befLasYearProfit") == null ? "0" :
                modelMap.get("befLasYearProfit").toString();
        String lasProfit = modelMap.get("lasProfit") == null ? "0" : modelMap.get("lasProfit").toString();

        double befLasYearProfitValue = Double.parseDouble(befLasYearProfit);
        double lasProfitValue = Double.parseDouble(lasProfit);
        if ("Y".equals(xedoldcust)) {
            result.append("AP38,");
        } else if (befLasYearProfitValue < 0 && lasProfitValue < 0) {
            score += 30;
            result.append("AP38,");
        }

        String tax3MIllegal_unDeal = modelMap.get("tax3MIllegal_unDeal") == null ? "0" : modelMap.get(
                "tax3MIllegal_unDeal").toString();
        String tax3MIllegal = modelMap.get("tax3MIllegal") == null ? "0" : modelMap.get("tax3MIllegal").toString();
        if ("Y".equals(xedoldcust) && Double.parseDouble(tax3MIllegal_unDeal) > 0) {
            score += 500;
            result.append("AP119,");
        }
        if (!"Y".equals(xedoldcust) && Double.parseDouble(tax3MIllegal) > 0) {
            score += 500;
            result.append("AP119,");
        }

        String YSXSRLast = modelMap.get("YSXSRLast") == null ? "0" : modelMap.get("YSXSRLast").toString();
        if (Double.parseDouble(YSXSRLast) < 1000000 && Double.parseDouble(incTax) + Double.parseDouble(lastYearTax) < 40000) {
            score += 500;
            result.append("AP120,");
        }

        String taxsale3M = modelMap.get("taxsale3M") == null ? "0" : modelMap.get("taxsale3M").toString();
        if (Double.parseDouble(taxsale3M) <= 0) {
            score += 500;
            result.append("BL72,");
        }
        double taxsale3MValue=Double.parseDouble(taxsale3M);
        if(!"Y".equals(xedoldcust)){
            if(taxsale3MValue>taxSalethirtyperValue*0.9){
                score+=200;
                result.append("AP141,");
            } else if (taxsale3MValue>taxSalethirtyperValue*0.7) {
                score+=150;
                result.append("AP141,");
            }else if(taxsale3MValue>taxSalethirtyperValue*0.5){
                score+=100;
                result.append("AP141,");
            }
        }

        String tax2YNetAss = modelMap.get("tax2YNetAss") == null ? "0" : modelMap.get("tax2YNetAss").toString();
        String tax2YPro = modelMap.get("tax2YPro") == null ? "0" : modelMap.get("tax2YPro").toString();

        String YSXSRBef = modelMap.get("YSXSRBef") == null ? "0" : modelMap.get("YSXSRBef").toString();
        double ysxsr = Double.parseDouble(YSXSRBef) + Double.parseDouble(YSXSRLast);
        double tax2YProValue = Double.parseDouble(tax2YPro);
        double tax2YNetAssValue = Double.parseDouble(tax2YNetAss);
        if ((!"Y".equals(xedoldcust) || !"N".equals(ifkjedai))) {
            if ("N".equals(ifkjedai) && (tax2YProValue < -500000 || tax2YProValue < -0.1 * ysxsr) && (tax2YNetAssValue < 400000 || tax2YNetAssValue < 0.15 * ysxsr)) {
                score += 500;
                result.append("AP122,");
            } else if ("Y".equals(ifkjedai) && (tax2YProValue < -0.1 * ysxsr) && (tax2YNetAssValue < 0.15 * ysxsr) && "Y".equals(xedoldcust)) {
                score += 500;
                result.append("AP122,");
            } else if ("Y".equals(ifkjedai) && (tax2YProValue < -500000 || tax2YProValue < -0.1 * ysxsr) && (tax2YNetAssValue < 400000 || tax2YNetAssValue < 0.15 * ysxsr)) {
                score += 500;
                result.append("AP122,");
            }
        }

        resultMap.put("admtrsltsts", "C");
        if (score <= 260) {
            resultMap.put("admtrsltsts", "A");
        }
        String resultStr=result.toString();
        if (!CommUtils.isEmptyStr(resultStr)) {
            log.info("触发项为:{}", resultStr);
            final int length = resultStr.lastIndexOf(",");
            result = new StringBuilder(resultStr.substring(0, length));
        }

        resultMap.put("score", score);
        resultMap.put("result", result.toString());
        resultMap.put("rulecd", result.toString());
        resultMap.put("status", "1");
        resultMap.put("batchdate", date);
        hyDao.insert("hsyh", "common.insertHsyhTransLog", resultMap);
        return resultMap;
    }

    private File getFile(String date, String filePath) throws IOException {
        log.info("当前日期为:{},文件名为:{}", date, filePath);
        File fileName = new File(filePath);
        if (fileName.exists()) {
            log.info("创建单个文件{}失败，目标文件已存在！", filePath);
            return null;
        }
        if (filePath.endsWith(File.separator)) {
            log.info("创建单个文件{}失败，目标文件不能为目录!", filePath);
            return null;
        }
        if (!fileName.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            log.info("目标文件所在目录不存在，准备创建它！");
            if (!fileName.getParentFile().mkdirs()) {
                log.info("创建目标文件所在目录失败！");
            }
        }
        if (fileName.createNewFile()) {
            log.info("文件创建成功！");
        }
        return fileName;
    }

    public void saveExceptionInfo(String entname, String serialno, Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        String exception = baos.toString();
        log.error("exception:{}", exception);

        Map<String, Object> msg = new HashMap<>();
        msg.put("entname", entname);
        msg.put("serialno", serialno);
        msg.put("msg", exception);
        msg.put("inputtime", CommUtils.getDate());
        hyDao.insert("hsyh", "common.saveExceptionInfo", msg);
    }

}
