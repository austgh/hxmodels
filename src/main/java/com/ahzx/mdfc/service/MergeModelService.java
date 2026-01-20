package com.ahzx.mdfc.service;


import com.ahzx.mdfc.dao.hshy.HyDaoImpl;
import com.ahzx.mdfc.model.IndexManageModel;
import com.ahzx.mdfc.utils.CommUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * @author gonghe
 * @date 2023年07月2023/7/25日15:54
 */
@Service
public class MergeModelService {
    private final Logger log = LoggerFactory.getLogger(MergeModelService.class);
    private final HyDaoImpl hyDao;

    IndexManageModel indexManageModel;

    public MergeModelService(HyDaoImpl hyDao, IndexManageModel indexManageModel) {
        this.hyDao = hyDao;
        this.indexManageModel = indexManageModel;
    }
    @Scheduled(cron = "${modelCronExpr}")
    public void process() throws IOException {
        long startTime = System.currentTimeMillis();
        log.info("开始处理数据!");
        String date = CommUtils.getDate(0).replace("-", "");
        String yesterday = CommUtils.getDate(0, 0, -1).replace("-", "");
        if(date.compareTo("20271231")>0){
            return;
        }
        long lineCount=0;
        //文件路径
        String xedFilePath = "./hxData/output/" + date + "/xed/result.csv";
        String kjedFilePath = "./hxData/output/" + date + "/kjed/result.csv";
        String xedSuccessFlag = "./hxData/output/" + date + "/xed/success.ok";
        String kjedSuccessFlag = "./hxData/output/" + date + "/kjed/success.ok";

        boolean xed_ok=false;
        File xedFileName = new File(xedSuccessFlag);
        if (xedFileName.exists()) {
            xed_ok=true;
            log.info("{}，目标文件已存在！", xedFilePath);
        }
        boolean kjed_ok = false;
        File kjedFileName = new File(kjedSuccessFlag);
        if (kjedFileName.exists()) {
            kjed_ok = true;
            log.info("{}，目标文件已存在！", kjedFilePath);
        }
        String  mergedFilePath ="./hxData/output/" + date + "/result.csv";
        //两个文件都生成 则合并数据写入到result.csv中
        if(xed_ok&&kjed_ok){
            try {
                BufferedReader reader1 = new BufferedReader(new FileReader(xedFilePath));
                BufferedReader reader2 = new BufferedReader(new FileReader(kjedFilePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFilePath));

                String line;

                // 将第一个文件的内容写入合并文件
                while ((line = reader1.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    lineCount++;
                }

                // 将第二个文件的内容写入合并文件
                while ((line = reader2.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    lineCount++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                String successFlag = "./hxData/output/" + date + "/success.ok";
                File fileSuccess = new File(successFlag);
                if (fileSuccess.createNewFile()) {
                    log.info("标志文件创建成功！");
                }
            }
        }

        long endTime = System.currentTimeMillis();
        int time = (int) ((endTime - startTime) / 1000);
        log.info("{}条数据导入共耗时{}秒",lineCount,time);
    }

}
