package com.bat.shop.api.bean.oms;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class OmsCartItem extends Model<OmsCartItem> {

	private String id;
	private String productId;
	private String productSkuId;
	private Integer memberId;
	private Integer quantity;
	private BigDecimal price;
	private String sp1;
	private String sp2;
	private String sp3;
	private String productPic;
	private String productName;
	private String productSubTitle;
	private String productSkuCode;
	private String memberNickname;
	private Date createDate;
	private Date modifyDate;
	private Integer deleteStatus;
	private String productCategoryId;
	private String productBrand;
	private String productSn;
	private String productAttr;
	private String isChecked;		//购物车选中状态   1-选中  0未选中

}
