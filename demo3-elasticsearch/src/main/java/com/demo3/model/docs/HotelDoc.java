package com.demo3.model.docs;

import com.demo3.constants.HotelDocField;
import com.demo3.entity.Hotel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@NoArgsConstructor
@Document(indexName = "hotel")
public class HotelDoc {
    @Id
    private Long id;
    @Field(name = HotelDocField.NAME, type = FieldType.Text, copyTo = "all")
    private String name;
    @Field(name = HotelDocField.ADDRESS,type = FieldType.Text,index = false)
    private String address;
    @Field(name = HotelDocField.PRICE,type = FieldType.Integer)
    private Integer price;
    @Field(name = HotelDocField.SCORE,type = FieldType.Integer)
    private Integer score;
    @Field(name = HotelDocField.BRAND,type = FieldType.Keyword, copyTo = "all")
    private String brand;
    @Field(name = HotelDocField.CITY,type = FieldType.Keyword, copyTo = "all")
    private String city;
    @Field(name = HotelDocField.STAR_NAME,type = FieldType.Keyword)
    private String starName;
    @Field(name = HotelDocField.BUSINESS,type = FieldType.Keyword)
    private String business;
    @Field(name = HotelDocField.LOCATION,type = FieldType.Keyword,index = false)
    private String location;
    @Field(name = HotelDocField.PIC,type = FieldType.Keyword,index = false)
    private String pic;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String all;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
    }
}

