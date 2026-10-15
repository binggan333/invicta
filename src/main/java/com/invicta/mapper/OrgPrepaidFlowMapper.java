package com.invicta.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.invicta.entity.OrgPrepaidFlowEntity;
import com.invicta.model.PrepaidModels;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface OrgPrepaidFlowMapper extends BaseMapper<OrgPrepaidFlowEntity> {
    IPage<PrepaidModels.Flow> selectFlowPage(
            Page<PrepaidModels.Flow> page,
            @Param("orgId") long orgId,
            @Param("chainId") Integer chainId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
