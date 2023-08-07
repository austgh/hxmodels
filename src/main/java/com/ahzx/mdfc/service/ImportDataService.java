package com.ahzx.mdfc.service;

import com.ahzx.mdfc.dao.hshy.HyDaoImpl;
import com.ahzx.mdfc.utils.CommUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author think
 * @date 2023年08月03日 20:35
 */
@Service
public class ImportDataService {
	//定义常量
	public static int COUNT=100;
	private final Logger log= LoggerFactory.getLogger(ImportDataService.class);
	private final HyDaoImpl hyDao;

	public ImportDataService(HyDaoImpl hyDao) {
		this.hyDao = hyDao;
	}
	//@Scheduled(cron = "${modelCronExpr}")
	public void importData(){
		BufferedReader br = null;
		List<Map<String, Object>> requstList = new ArrayList<Map<String, Object>>();
		String date=CommUtils.getDate(0).replace("-","");
		String filePath="./hxData/input/"+date+"/entname.csv";
		log.info("当前日期为:{},文件名为:{}", date,filePath);
		//判断文件是否存在
		File inputfile = new File(filePath);// 线上审批
		if(!inputfile.exists()){
			log.info("当前日期为:{}下面的文件不存在,文件目录为:{}", date,filePath);
			return;
		}
		try {
			InputStreamReader fReader = new InputStreamReader(new FileInputStream(inputfile),"UTF-8");
			br = new BufferedReader(fReader);
			String readLine = "";
			int total=0;
			while ((readLine = br.readLine()) != null) {
				total++;
				log.info("第{}行数据为:{}", total, readLine);
				String[] params = readLine.split(";");
				if (CommUtils.isEmptyStr(params[2]) || CommUtils.isEmptyStr(params[1])
						|| CommUtils.isEmptyStr(params[0])) {
					continue;
				}
				Map<String, Object> map = new HashMap<>();
				map.put("serialno", params[0]);
				map.put("nsrmc",  params[1]);
				map.put("nsrsbh", params[2]);
				map.put("ifkjedai",  params[3]);
				map.put("hytype", params[4]);
				map.put("hytypecode",  params[5]);
				map.put("xedoldcust", params[6]);
				map.put("inputtime", CommUtils.getDate());
				map.put("batchdate", date);
				if(total<=5000){
					requstList.add(map);
					hyDao.insert("mapping/hshy", "common.insertData", map);
				}
			}
			//final HashMap<Object, Object> map1 = new HashMap<>();
			//List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			//for (int i = 0; i < (total + COUNT - 1) / COUNT; i++) {
			//	if (total > (i + 1) * COUNT) {
			//		map1.put("list", requstList.subList(i * COUNT, (i + 1) * COUNT));
			//	} else {
			//		map1.put("list", requstList.subList(i * COUNT, total));
			//	}
			//	hyDao.insert("mapping/hshy", "common.insertData1", map1);
			//}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
