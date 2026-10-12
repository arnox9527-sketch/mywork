package com.exchange.trade.feign;

import com.exchange.trade.dto.FreezeRequest;
import com.exchange.trade.dto.SettleRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "asset-service")
public interface AssetFeignClient {

    @PostMapping("/api/v1/assets/freeze")
    Map<String, Object> freeze(@RequestBody FreezeRequest request);

    @PostMapping("/api/v1/assets/settle")
    Map<String, Object> settle(@RequestBody SettleRequest request);
}
