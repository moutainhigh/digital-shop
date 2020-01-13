package com.bat.shop.api.bean.pms;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @param
 * @return
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsSkuSaleAttrValue extends Model<PmsSkuSaleAttrValue> {

    String id;
    String skuId;
    String saleAttrId;
    String saleAttrValueId;
    String saleAttrName;
    String saleAttrValueName;


}
