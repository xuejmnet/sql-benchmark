package com.easyquery.benchmark.jooq;

import java.math.BigDecimal;

public class JooqOrder {
    private String id;
    private String userId;
    private String orderNo;
    private BigDecimal amount;
    private Integer status;
    private String remark;

    public JooqOrder() {
    }

    public JooqOrder(String id, String userId, String orderNo, BigDecimal amount, Integer status, String remark) {
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



