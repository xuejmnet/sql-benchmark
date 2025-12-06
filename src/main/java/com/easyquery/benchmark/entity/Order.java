package com.easyquery.benchmark.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easyquery.benchmark.entity.proxy.OrderProxy;

import java.math.BigDecimal;

@Table("t_order")
@EntityProxy
public class Order implements ProxyEntityAvailable<Order, OrderProxy> {
    
    @Column(primaryKey = true)
    private String id;
    
    @Column
    private String userId;
    
    @Column
    private String orderNo;
    
    @Column
    private BigDecimal amount;
    
    @Column
    private Integer status;
    
    @Column
    private String remark;

    public Order() {
    }

    public Order(String id, String userId, String orderNo, BigDecimal amount, Integer status, String remark) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.amount = amount;
        this.status = status;
        this.remark = remark;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

