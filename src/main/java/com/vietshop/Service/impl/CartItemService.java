package com.vietshop.Service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.vietshop.repository.AccountRepository;
import com.vietshop.repository.CartItemRepository;
import com.vietshop.repository.ProductRepository;
import com.vietshop.util.SecurityUtils;
import com.vietshop.Entity.Account;
import com.vietshop.Entity.CartItem;
import com.vietshop.Entity.Product;
import com.vietshop.Service.iCartItemService;
import com.vietshop.dto.AccountDTO;
import com.vietshop.dto.CartItemDTO;
import com.vietshop.dto.ProductDTO;

@Service
public class CartItemService implements iCartItemService{
	@Autowired 
	public CartItemRepository cartItemRepo;
	
	@Autowired
	public ProductRepository productRepository;
	
	@Autowired
	public AccountRepository accountRepository;

	@Override
	public void deleteAll() {
		cartItemRepo.deleteAll();
	}

	@Override
	public <S extends CartItem> S save(S entity) {
		return cartItemRepo.save(entity);
	}

	@Override
	public <S extends CartItem> List<S> save(Iterable<S> entities) {
		return cartItemRepo.save(entities);
	}

	@Override
	public void deleteByIdAccount(Long idAccount) {
		cartItemRepo.deleteByIdAccount(idAccount);
	}
	
	
	public CartItemDTO findByAccountAndProduct(AccountDTO accountDTO,Long idProduct) {
		Account account = new Account();
		BeanUtils.copyProperties(accountDTO, account);
		CartItem cartItem = cartItemRepo.findByAccountAndProduct(account, productRepository.findOne(idProduct));
		CartItemDTO cartItemDTO = new CartItemDTO();
		BeanUtils.copyProperties(cartItem, cartItemDTO);
		return cartItemDTO;
	}
	
	
	public void addProducttoCart(Long idProduct,Long quantity) {
		Account account = accountRepository.findByUserName(SecurityUtils.getPrincipal().getUsername());

		Long addQuantity = quantity ;
		Product product = productRepository.findOne(idProduct);

		CartItem cartItem = cartItemRepo.findByAccountAndProduct(account, product);
		if(cartItem !=null ) {
			addQuantity = cartItem.getQuantity()+quantity;
			cartItem.setQuantity(addQuantity);
			double total = cartItem.getQuantity()*product.getCost();
			cartItem.setTotal(total);
			
			
			
		}else {
			
			cartItem = new CartItem();
			cartItem.setAccount(account);
			cartItem.setProduct(product);
			cartItem.setQuantity(quantity);
			double total = cartItem.getQuantity()*product.getCost();
			cartItem.setTotal(total);
			System.out.println(quantity+"aasas");
		}
		cartItemRepo.save(cartItem);
	}
	
	public void doAddProductToCart(Long idProduct, Long quantity) {
		Account account = accountRepository.findByUserName(SecurityUtils.getPrincipal().getUsername());
		Product product = productRepository.findByIdProduct(idProduct).get();
		CartItem cartItem = cartItemRepo.findByAccountAndProduct(account, product);
		if(cartItem !=null ) {
			cartItem.setQuantity(quantity);
			double total = cartItem.getQuantity()*product.getCost();
			cartItem.setTotal(total);
		}else {
			cartItem = new CartItem();
			cartItem.setAccount(account);
			cartItem.setProduct(product);
			cartItem.setQuantity(quantity);
			double total = cartItem.getQuantity()*product.getCost();
			cartItem.setTotal(total);
			
		}
		cartItemRepo.save(cartItem);
	}
	
	

}
