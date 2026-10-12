package com.exchange.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exchange.asset.entity.Currency;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CurrencyMapper extends BaseMapper<Currency> {
}
