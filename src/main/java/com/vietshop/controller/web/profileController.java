package com.vietshop.controller.web;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vietshop.Entity.OrderDetails;
import com.vietshop.Service.impl.AccountService;
import com.vietshop.Service.impl.OrderService;
import com.vietshop.dto.AccountDTO;
import com.vietshop.dto.OrderDTO;
import com.vietshop.util.SecurityUtils;

@Controller
public class profileController {

	@Autowired
	public PasswordEncoder passwordEncoder
	;
	@Autowired
	private AccountService accountService;

	@Autowired
	private OrderService orderService;

	@GetMapping("/profile")
	public String profile(Model model, @RequestParam(name = "mess", defaultValue = " ") String mess) {
		AccountDTO account = accountService.findByUserName(SecurityUtils.getPrincipal().getUsername());
		model.addAttribute("account", account);
		model.addAttribute("mess", mess);// mess cho method post change password
		SecurityUtils.getPrincipal().getPassword();
		return "web/profile";
	}

	
	@PostMapping("/updateProfile")
	public String updateProfile(Model model, @ModelAttribute("account") AccountDTO account) {
		accountService.updateProfile(account);
		return "redirect:/profile";
	}

	
	@PostMapping("/changePassword")
	public String changePassword(Model model, @ModelAttribute("account") AccountDTO account) {

		if (account.getOldpass().equals(account.getNewpass())) {
			accountService.changePassword(account);
			model.addAttribute("mess", "Đổi mật khẩu thành công !");
			return "redirect:/profile";
		} else {
			model.addAttribute("mess", "Mật khẩu mới không khớp !");
			return "redirect:/profile";
		}
	}

	
	@GetMapping("/myorderdetail")
	public String myorderdetail(Model model, @RequestParam("idOrder") Long idOrder) {
		List<OrderDetails> items = orderService.findOne(idOrder).getOrderDetailsList();
		model.addAttribute("items", items);
		model.addAttribute("order", orderService.findOne(idOrder));
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");
		model.addAttribute("formatter", formatter);
		return "web/myOrder";
	}

	
	@GetMapping("/mylistorder")
	public String mylistorder(Model model, @RequestParam("p") Optional<Integer> p) {
		
		Page<OrderDTO> pageOrder = orderService.mylistOrder(p);
		model.addAttribute("pageOrders", pageOrder);
		model.addAttribute("elements", pageOrder.getTotalElements());
		// định dạng tiền tệ VND
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");
		model.addAttribute("formatter", formatter);
		return "web/mylistOrder";
	}
}
