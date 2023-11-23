package com.demo3.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName tb_hotel
 */
@TableName(value ="tb_hotel")
@Data
public class Hotel implements Serializable {
    /**
     * 酒店id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 酒店名称
     */
    private String name;

    /**
     * 酒店地址
     */
    private String address;

    /**
     * 酒店价格
     */
    private Integer price;

    /**
     * 酒店评分
     */
    private Integer score;

    /**
     * 酒店品牌
     */
    private String brand;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 酒店星级，1星到5星，1钻到5钻
     */
    private String starName;

    /**
     * 商圈
     */
    private String business;

    /**
     * 纬度:横纬
     */
    private String latitude;

    /**
     * 经度:竖经
     */
    private String longitude;

    /**
     * 酒店图片
     */
    private String pic;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}