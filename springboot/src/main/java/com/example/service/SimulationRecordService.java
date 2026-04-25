package com.example.service;

import com.example.entity.SimulationRecord;
import com.example.mapper.SimulationRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimulationRecordService {

    @Resource
    private SimulationRecordMapper recordMapper;


    // 保存模拟记录
    public void saveRecord(SimulationRecord simulationRecord) {
        recordMapper.insert(simulationRecord);
    }

    // 获取用户的所有记录
    public List<SimulationRecord> getUserRecords(Integer userId) {
        return recordMapper.findByUserId(userId);
    }


    // 获取单条记录详情
    public SimulationRecord getRecordById(Integer id) {
        return recordMapper.findById(id);
    }

    // 获取用户的统计数据
    public Map<String, Object> getUserStats(Integer userId) {
        Map<String, Object> result = new HashMap<>();

        long successCount = recordMapper.countByUserIdAndResult(userId, "success");
        long failCount = recordMapper.countByUserIdAndResult(userId, "fail");
        long totalCount = successCount + failCount;

        double successRate = totalCount > 0 ?
                Math.round((successCount * 100.0 / totalCount) * 100) / 100.0 : 0;


        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("successRate", successRate);
        return result;
    }

    public PageInfo<SimulationRecord> selectPage(Integer userId,String scene, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SimulationRecord> list = recordMapper.findByUserIdAndScene(userId,scene);
        return PageInfo.of(list);
    }

    // 统计总模拟次数
    public int countAll() {
        return recordMapper.countAll();
    }

    // 按日期统计模拟次数
    public int countByDate(String date) {
        return recordMapper.countByDate(date);
    }
}