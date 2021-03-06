package com.bat.qmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.bat.qmall.annotations.LoginRequired;
import com.bat.qmall.webUtils.WebUtil;
import com.bat.shop.api.bean.oms.OmsCartItem;
import com.bat.shop.api.bean.pms.PmsSkuInfo;
import com.bat.shop.api.service.oms.CartService;
import com.bat.shop.api.service.pms.SkuService;
import com.bat.qmall.Const.OmsConst;
import com.bat.qmall.exception.MsgException;
import com.bat.qmall.utils.Validator;
import com.bat.qmall.webUtils.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: zhouR
 * @date: Create in 2020/1/14 - 18:59
 * @function:
 */
@Controller
public class CartController {

	@Reference
	CartService cartService;
	@Reference
	SkuService skuService;
	



	/**
	 * 修改购物车状态，返回给内嵌页
	 * @param param
	 * @param model
	 * @return
	 */
	@RequestMapping("/checkCart")
	@LoginRequired(false)
	public String checkCart(@RequestParam Map<String,Object> param,Model model,HttpServletRequest request,HttpServletResponse response){
		String isChecked = WebUtil.getParam(param, "isChecked");
		String productSkuId = WebUtil.getParam(param, "productSkuId");

		Integer memberId = (Integer)request.getAttribute("memberId");
		String nickname = (String)request.getAttribute("nickname");

		List<OmsCartItem> cartList;

		if(Validator.isNotEmpty(memberId)){
			//用户登录
			OmsCartItem omsCartItem = new OmsCartItem();
			omsCartItem.setMemberId(memberId);
			omsCartItem.setIsChecked(isChecked);
			omsCartItem.setProductSkuId(productSkuId);
			omsCartItem.setMemberNickname(nickname);
			//修改购物车状态
			cartService.checkCart(omsCartItem);

			cartList = cartService.getCartListByMemberId(memberId);
		
		}else {//用户没登录

			//从cookie中获取购物车
			String cookieValue = CookieUtil.getCookieValue(request, OmsConst.CART_COOKIE, true);
			cartList = JSON.parseArray(cookieValue,OmsCartItem.class);
			//改变当前勾选状态
			if(cartList!=null){
				for (OmsCartItem omsCartItem : cartList) {
					if(productSkuId.equals(omsCartItem.getProductSkuId())){
						omsCartItem.setIsChecked(isChecked);
					}
				}
			}
			//重新写入cookie
			String cartListString = JSON.toJSONString(cartList);
			CookieUtil.setCookie(request,response,OmsConst.CART_COOKIE,cartListString,OmsConst.COOKIE_MAXAGE,true);

		}

		BigDecimal total_amount = null;
		if(cartList!=null){
			total_amount = getTotalAmount(cartList);
		}

		model.addAttribute("total_amount",total_amount);
		model.addAttribute("cartList",cartList);

		return "cartListInner";
	}

	/**
	 *查询购物车
	 * @return
	 */

	@RequestMapping("/cartList")
	@LoginRequired(false)
	public String cartList(HttpServletRequest request,Model model){

		List<OmsCartItem> cartList = new ArrayList<>();
		//Integer memberId = null;
		Integer memberId = (Integer)request.getAttribute("memberId");
		String nickname = (String)request.getAttribute("nickname");


		if(Validator.isNotEmpty(memberId)){
			//用户登录，查询数据库
			//cartService.flushCacheByMemberId(memberId);
			cartList = cartService.getCartListByMemberId(memberId);
		}else {
			//用户未登录，查询cookie
			String cartListCookie = CookieUtil.getCookieValue(request,OmsConst.CART_COOKIE, true);
			if(Validator.isNotEmpty(cartListCookie)){
				//如果cookie不为空
				cartList = JSON.parseArray(cartListCookie, OmsCartItem.class);
			}else {
				//如果cookie为空
			}
		}
		//总价格
		BigDecimal total_amount = getTotalAmount(cartList);
		model.addAttribute("total_amount",total_amount);

		model.addAttribute("cartList",cartList);

		return "cartList.html";
	}


	/**
	 * 计算被勾选的总价格
	 * @return
	 */
	private BigDecimal getTotalAmount(List<OmsCartItem> cartList){
		BigDecimal total = new BigDecimal("0");

		for (OmsCartItem omsCartItem : cartList) {
			if(omsCartItem.getIsChecked().equals(OmsConst.CHECK)){

				BigDecimal quantity = new BigDecimal(omsCartItem.getQuantity());
				BigDecimal multiply = omsCartItem.getPrice().multiply(quantity);

				total = total.add(multiply);
			}

		}

		return total;
	}



	/**
	 * 购物车添加功能
	 *
	 * @return 购物车类型：
	 * DB：		cartListDb
	 * Redis：	cartListRedis
	 * Cookie：cartListCookie
	 */
	@RequestMapping("addToCart")
	@LoginRequired(false)
	public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response) {
		List<OmsCartItem> cartList = new ArrayList<>();

		//1、传递参数（商品skuId，商品数量）
		//根据skuId调用skuService查询商品详情信息
		PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

		//2、将商品详细信息封装成购物车信息
		OmsCartItem cartItem = new OmsCartItem();

		Date date = new Date();
		cartItem.setCreateDate(date);                            //新创建时间
		cartItem.setModifyDate(date);                            //修改时间
		cartItem.setDeleteStatus(OmsConst.CART_NOT_DELETED);     //删除状态
		cartItem.setPrice(skuInfo.getPrice());                   //价格
		cartItem.setProductAttr("");                             //销售属性
		cartItem.setProductBrand("");                            //商标
		cartItem.setProductCategoryId(skuInfo.getCatalog3Id());  //三级分类id
		cartItem.setProductId(skuInfo.getProductId());           //商品id
		cartItem.setProductName(skuInfo.getSkuName());           //商品名称
		cartItem.setProductPic(skuInfo.getSkuDefaultImg());      //图片
		cartItem.setProductSkuCode("11111111111");               //条形码
		cartItem.setProductSkuId(skuId);                         //skuId
		cartItem.setQuantity(quantity);                          //购买数量
		cartItem.setIsChecked(OmsConst.CHECK);					 //是否勾选


		//3、判断用户是否登录
		Integer memberId = (Integer)request.getAttribute("memberId");
		String nickname = (String)request.getAttribute("nickname");

		//4、根据用户登录信息，判断走cookie的分支还是db，购物车数据的写入操作
		if (Validator.isEmpty(memberId)) {
			//用户没登录

			String cartListCookie = CookieUtil.getCookieValue(request, OmsConst.CART_COOKIE, true);

			if (Validator.isEmpty(cartListCookie)) {
				//如果cookie为空，直接添加
				cartList.add(cartItem);
			} else {
				//cookie不为空
				cartList = JSON.parseArray(cartListCookie, OmsCartItem.class);
				boolean exist = isInArray(skuId, cartList);
				if(exist){
					//有添加过相同的商品，将数量+新加的数量
					for (OmsCartItem item : cartList) {
						if(item.getProductSkuId().equals(skuId)){
							item.setQuantity(item.getQuantity()+quantity);
							break;
						}
					}
				}else {
					//新商品
					cartList.add(cartItem);
				}
			}

			//设置进cookie
			String cartListJson = JSON.toJSON(cartList).toString();
			CookieUtil.setCookie(request, response, OmsConst.CART_COOKIE, cartListJson, OmsConst.COOKIE_MAXAGE, true);


		} else {
			//用户已经登录

			//去数据库中，根据用户id和要添加的商品，其查询购物车有没有该项
			OmsCartItem cart = cartService.getOmsCartItemByMemberIdAndSkuId(memberId,skuId);

			if (Validator.isEmpty(cart)) {
				//如果该用户没添加过这个商品

				//直接添加购物车
				cartItem.setMemberId(memberId);
				cartItem.setMemberNickname(nickname);
				try {
					cartService.save(cartItem);
				} catch (MsgException e) {
					e.printStackTrace();
				}


			}else {
				//该用户添加过这个商品

				//数量相加
				cart.setQuantity(cart.getQuantity()+quantity);
				try {
					cartService.save(cart);
				} catch (MsgException e) {
					e.printStackTrace();
				}
			}

			//同步缓存

			cartService.flushCacheByMemberId(memberId);

		}

		return "redirect:/success.html";
	}


	/**
	 * 判断要添加的商品是否在cookie内
	 * true/存在  false/不存在
	 * @param skuId
	 * @param list
	 * @return
	 */
	private boolean isInArray(String skuId, List<OmsCartItem> list) {
		for (OmsCartItem item : list) {
			if (item.getProductSkuId().equals(skuId)) {
				return true;
			}
		}
		return false;
	}


	@RequestMapping("/GotoShoppingCart")
	public String GotoShoppingCart() {

		return "";
	}


}
