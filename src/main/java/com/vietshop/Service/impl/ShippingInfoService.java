package com.vietshop.Service.impl;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.vietshop.Entity.Account;
import com.vietshop.Entity.CartItem;
import com.vietshop.Entity.Order;
import com.vietshop.Entity.Product;
import com.vietshop.Entity.ShippingInfo;
import com.vietshop.repository.AccountRepository;
import com.vietshop.repository.CartItemRepository;
import com.vietshop.repository.OrderRepository;
import com.vietshop.repository.ProductRepository;
import com.vietshop.repository.ShippingInfoRepository;

@Service
public class ShippingInfoService {
@Autowired
private ShippingInfoRepository shippingInfoRepo;

@Autowired
private AccountRepository accountRepository;

@Autowired
private OrderRepository orderRepository;

@Autowired
public JavaMailSender emailSender;
@Autowired
private ProductRepository productRepository;

@Autowired
private CartItemRepository cartItemRepository;

public <S extends ShippingInfo> S save(S entity) {
	return shippingInfoRepo.save(entity);
}

public <S extends ShippingInfo> S findOne(Example<S> example) {
	return shippingInfoRepo.findOne(example);
}

public Page<ShippingInfo> findAll(Pageable pageable) {
	return shippingInfoRepo.findAll(pageable);
}

public List<ShippingInfo> findAll() {
	return shippingInfoRepo.findAll();
}

public ShippingInfo findOne(Long id) {
	return shippingInfoRepo.findOne(id);
}

public List<ShippingInfo> findAll(Sort sort) {
	return shippingInfoRepo.findAll(sort);
}

public List<ShippingInfo> findAll(Iterable<Long> ids) {
	return shippingInfoRepo.findAll(ids);
}

public boolean exists(Long id) {
	return shippingInfoRepo.exists(id);
}

public <S extends ShippingInfo> List<S> save(Iterable<S> entities) {
	return shippingInfoRepo.save(entities);
}

public void flush() {
	shippingInfoRepo.flush();
}

public <S extends ShippingInfo> S saveAndFlush(S entity) {
	return shippingInfoRepo.saveAndFlush(entity);
}

public long count() {
	return shippingInfoRepo.count();
}

public void deleteInBatch(Iterable<ShippingInfo> entities) {
	shippingInfoRepo.deleteInBatch(entities);
}

public <S extends ShippingInfo> Page<S> findAll(Example<S> example, Pageable pageable) {
	return shippingInfoRepo.findAll(example, pageable);
}

public void delete(Long id) {
	shippingInfoRepo.delete(id);
}

public void deleteAllInBatch() {
	shippingInfoRepo.deleteAllInBatch();
}

public void delete(ShippingInfo entity) {
	shippingInfoRepo.delete(entity);
}

public ShippingInfo getOne(Long id) {
	return shippingInfoRepo.getOne(id);
}

public <S extends ShippingInfo> long count(Example<S> example) {
	return shippingInfoRepo.count(example);
}

public void delete(Iterable<? extends ShippingInfo> entities) {
	shippingInfoRepo.delete(entities);
}

public <S extends ShippingInfo> List<S> findAll(Example<S> example) {
	return shippingInfoRepo.findAll(example);
}

public void deleteAll() {
	shippingInfoRepo.deleteAll();
}

public <S extends ShippingInfo> boolean exists(Example<S> example) {
	return shippingInfoRepo.exists(example);
}

public <S extends ShippingInfo> List<S> findAll(Example<S> example, Sort sort) {
	return shippingInfoRepo.findAll(example, sort);
}


public void insert(Long idAccount, Long idOrder,String status,double shippingCost) throws MessagingException {
	Account account = accountRepository.findOne(idAccount);
	Order order = orderRepository.findOne(idOrder);
	ShippingInfo shipInfo = new ShippingInfo();
	shipInfo.setCustomer(account.getFullName());
	shipInfo.setAddress(account.getAddress());
	shipInfo.setPhone(account.getPhone());
	shipInfo.setShippingCost(0); // Đã thanh toán qua thẻ
	shipInfo.setOrder(order);
	order.setShippingInfo(shipInfo);
	order.setStatus(status);

	shippingInfoRepo.save(shipInfo);
	orderRepository.save(order);
	
	List<CartItem> cartItems = accountRepository.findOne(idAccount).getCartItems();

	// Trừ đi số lượng còn lại trong kho
	for (CartItem i : cartItems) {
		Long quantited = i.getProduct().getQuantity() - i.getQuantity();
		Product product = i.getProduct();
		product.setSoldQuantity(product.getSoldQuantity()+i.getQuantity());// thêm vào số lượng sp đã bán
		product.setQuantity(quantited);
		productRepository.save(product);

	}
	cartItemRepository.deleteByIdAccount(idAccount); // clear giỏ hàng khi đã đặt hàng thành công

	
	// Gửi mail
	MimeMessage message = emailSender.createMimeMessage();
	boolean multipart = true;
	
	MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "utf-8");
	String htmlMsg = "<a>Thank you for your order !</a>"+ 
			"<a href='http://localhost:8080/vietshop/thankOrder?idOrder="+order.getIdOrder()+"'>Details</a>";   
    
    message.setContent(htmlMsg, "text/html");
    helper.setTo(accountRepository.findOne(idAccount).getEmail());
    
    helper.setSubject("Order Success: "+"000"+order.getIdOrder());
    this.emailSender.send(message);

}
}
