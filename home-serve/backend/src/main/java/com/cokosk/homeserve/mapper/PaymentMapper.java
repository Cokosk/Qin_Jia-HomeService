package com.cokosk.homeserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cokosk.homeserve.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
