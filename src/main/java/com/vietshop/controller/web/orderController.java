package com.vietshop.controller.web;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vietshop.Entity.Account;
import com.vietshop.Entity.CartItem;
import com.vietshop.Entity.Category;
import com.vietshop.Entity.Order;
import com.vietshop.Entity.OrderDetails;
import com.vietshop.Service.impl.AccountService;
import com.vietshop.Service.impl.CategoryService;
import com.vietshop.Service.impl.CreditCardService;
import com.vietshop.Service.impl.OrderDetailsService;
import com.vietshop.Service.impl.OrderService;
import com.vietshop.Service.impl.PaymentService;
import com.vietshop.Service.impl.ShippingInfoService;
import com.vietshop.dto.AccountDTO;
import com.vietshop.dto.CreditCardDTO;
import com.vietshop.dto.OrderDTO;
import com.vietshop.util.SecurityUtils;

@Controller
public class orderController {
	@Autowired
	private AccountService accountService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailsService orderDetailsService;

	@Autowired
	private ShippingInfoService shippingInfoService;

	@Autowired
	private CreditCardService creditcardService;
	
	@Autowired
	private PaymentService paymentService;
	
	
	@Autowired
    public JavaMailSender emailSender;

	@GetMapping("/checkOut")
	public String checkOut(Model model) {
		List<Category> category = categoryService.findAll();
		model.addAttribute("category", category);

		AccountDTO account = accountService.findByUserName(SecurityUtils.getPrincipal().getUsername());
		List<CartItem> items = account.getCartItems();
		model.addAttribute("cartItems", items);
		model.addAttribute("account", account);
		for(CartItem j:items) {
			if(j.getQuantity() >= j.getProduct().getQuantity()) {
				model.addAttribute("msg","S??? l?????ng " + j.getProduct().getProduct()+" kh??ng ????? theo y??u c???u.");
				return "redirect:/shopingcart";
			}
		}
		Date date = new Date(new java.util.Date().getTime());
		Order order = new Order();
		// T??nh t???ng ti???n c???a Order
		double priceTotal = 0;
		for (CartItem i : items) {
			priceTotal = priceTotal + i.getTotal();
		}
		order.setAccount(accountService.findOne(SecurityUtils.getPrincipal().getIdAccount()));
		order.setDateOrder(date);
		order.setSubTotal(priceTotal);
		model.addAttribute("order", order);
		model.addAttribute("cartItems", items);

		// ?????nh d???ng ti???n t??? VND
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");
		model.addAttribute("formatter", formatter);
		// khi ng?????i d??ng nh???p sl sp nhi???u h??n trong kho
		
		
		return "web/checkout";
	}

	@PostMapping("/checkOut")
	public String doCheckOut(Model model, @RequestParam("idAccount") Long idAccount,
			@RequestParam("methodPayment") String methodPayment, @ModelAttribute("Account") Account account)  throws MessagingException {
		List<Category> category = categoryService.findAll();
		model.addAttribute("category", category);

		Date date = new Date(new java.util.Date().getTime());
		Order order = new Order();
		List<CartItem> cartItems = accountService.findOne(idAccount).getCartItems();
		if(!cartItems.isEmpty()) { // gi??? h??ng c?? sp
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("account", account);

		// T??nh t???ng ti???n c???a Order
		double priceTotal = 0;
		for (CartItem i : cartItems) {
			priceTotal = priceTotal + i.getTotal();
		}
		order.setAccount(accountService.findOne(idAccount));
		order.setDateOrder(date);
		order.setSubTotal(priceTotal);
		orderService.save(order);

		for (CartItem i : cartItems) {
			orderDetailsService.insert(i, order);
			;
		}

		// ch???n ph????ng th???c thanh to??n

		if (methodPayment.equalsIgnoreCase("cod")) { // v??? trang ?????t h??ng th??nh c??ng v???i COD
			// th??m d??? li???u v??o shipping info
			shippingInfoService.insert(idAccount, order.getIdOrder(), "Thanh to??n khi nh???n h??ng",order.getSubTotal());			
			model.addAttribute("order", order);
			model.addAttribute("idOrder", order.getIdOrder());
			return "redirect:thankOrder";
		}
		if (methodPayment.equalsIgnoreCase("card")) { // v??? trang payment ????? nh???p th???
			model.addAttribute("order", order);
			return "web/paymentCard";

		}
		}else { // gi??? h??ng tr???ng
			model.addAttribute("idAccount",idAccount);
			return "redirect:checkOut";
		}

		return "web/home";

	}

	@GetMapping("paymentCard")
	public String paymentCardPage(Model model, @RequestParam("idOrder") Long idOrder) {
		model.addAttribute("idOrder", idOrder);
		AccountDTO account = accountService.findByUserName(SecurityUtils.getPrincipal().getUsername());
		List<CartItem> items = account.getCartItems();
		model.addAttribute("cartItems", items);
		model.addAttribute("account", account);

		return "web/paymentCard";
	}

	
	@PostMapping("paymentCard")
	public String paymentCard(Model model, @RequestParam("cardNumber") String cardNumber,
			@RequestParam("cvcCode") int cvcCode, @RequestParam("expMonth") int expMonth,
			@RequestParam("expYear") int expYear, @RequestParam("name") String name,
			@RequestParam("idAccount") Long idAccount, @RequestParam("idOrder") Long idOrder) {
		OrderDTO orderDTO = orderService.findOne(idOrder);

		try {
			CreditCardDTO creditCardDTO = creditcardService.findOneDTO(cardNumber);

			double balance = creditCardDTO.getBalance();

			double totalPrice = orderDTO.getSubTotal();

			double balanceAfter = balance - totalPrice;
			if (cardNumber.equals(creditCardDTO.getCardNumber()) && name.equals(creditCardDTO.getName())
					&& expMonth == creditCardDTO.getExpMonth() && expYear == creditCardDTO.getExpYear()
					&& cvcCode == creditCardDTO.getCvcCode()) {
				// th??m d??? li???u v??o shipping info
				if (balanceAfter >= 0) {
					creditcardService.setbalance(cardNumber, balanceAfter);// set lai balance sau khi gioa dich
					paymentService.insert(orderDTO, creditCardDTO, idAccount);
					shippingInfoService.insert(idAccount, idOrder,"???? thanh to??n",0);
					model.addAttribute("order", orderDTO);
					model.addAttribute("idOrder",idOrder);
					return "redirect:thankOrder";
				} else {
					model.addAttribute("msg", "S??? ti???n trong t??i kho???n kh??ng ????? ????? th???c hi???n thanh to??n.");
					model.addAttribute("idOrder", idOrder);
					model.addAttribute("order", orderDTO);
					return "web/paymentCard";
				}

			}

			else {
				model.addAttribute("msg", "Th??ng tin th??? kh??ng ????ng, vui l??ng nh???p l???i.");
				model.addAttribute("idOrder", idOrder);
				model.addAttribute("order", orderDTO);
				return "web/paymentCard";
			}
		} catch (Exception e) {
			model.addAttribute("msg", "Th??ng tin th??? kh??ng ????ng, vui l??ng nh???p l???i.");
			model.addAttribute("idOrder",idOrder);
			model.addAttribute("order", orderDTO);
			return "web/paymentCard";
		}
	}

	@GetMapping("/thankOrder")
	public String thankOrder(Model model, @RequestParam("idOrder") Long idOrder,HttpSession session) {
		session.setAttribute("total",0);  //set th??ng tin gi??? h??ng l??n header sau khi thanh to??n th??nh c??ng th?? gi??? h??ng = 0
		session.setAttribute("quantity",0);

		List<OrderDetails> items = orderService.findOne(idOrder).getOrderDetailsList();
		model.addAttribute("items",items);
		model.addAttribute("order",orderService.findOne(idOrder));
	
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");
		model.addAttribute("formatter",formatter);


		model.addAttribute("dateOrder", orderService.findOne(idOrder).getDateOrder());
		return "web/thankOrder";
	}
	
	
}