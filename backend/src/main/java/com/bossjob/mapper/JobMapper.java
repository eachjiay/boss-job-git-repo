package com.bossjob.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bossjob.entity.Job;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
