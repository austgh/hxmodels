package com.ahzx.mdfc.dao;

import java.util.List;
import java.util.Map;

/**
 * @Auther: yjntue
 * @Date: 2019/1/3 18:45
 * @Description: DAO基础服务层
 */
public interface BaseDao {
    abstract List<Map<String, Object>> queryForList(String type, String sqlId, Object reqData);
    abstract boolean queryForBool(String type, String sqlId, Object reqData);
    abstract int queryForInt(String type, String sqlId, Object reqData);
    abstract String queryForString(String type, String sqlId, Object reqData);
    abstract Map<String, Object> queryForMap(String type, String sqlId, Object reqData);
    abstract boolean insert(String type, String sqlId, Object reqData);
    abstract boolean delete(String type, String sqlId, Object reqData);
    abstract boolean update(String type, String sqlId, Object reqData);
    abstract double queryForDouble(String type, String sqlId, Object reqData);
    abstract List<String> queryForStrList(String type, String sqlId, Object reqData);
    abstract String querySerialNo(String type,String sqlId);
    abstract Object queryForObject(String type, String sqlId, Object reqData);

}
