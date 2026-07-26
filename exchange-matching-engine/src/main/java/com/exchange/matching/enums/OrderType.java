package com.exchange.matching.enums;

/**
 * 订单类型
 */
public enum OrderType {
    /**
     * 限价单：指定价格和数量，按指定价格或更优价格成交
     */
    LIMIT,
    
    /**
     * 市价单：按当前最优市场价格立即成交
     */
    MARKET,
    
    /**
     * 只做Maker单：只挂单，不主动吃单
     */
    POST_ONLY,
    
    /**
     * 立即成交或取消：立即成交，未成交部分自动取消
     */
    IOC,
    
    /**
     * 全部成交或取消：必须全部成交，否则整单取消
     */
    FOK
}