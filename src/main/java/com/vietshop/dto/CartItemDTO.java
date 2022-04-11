package com.vietshop.dto;

public class CartItemDTO {
	private long idCart;
	
	private AccountDTO accountDTO;
	
	private ProductDTO productDTO;
	
	private long quantity;
	
	private double total;

	
	public long getIdCart() {
		return idCart;
	}
	public void setIdCart(long idCart) {
		this.idCart = idCart;
	}
	public AccountDTO getAccountDTO() {
		return accountDTO;
	}
	public void setAccountDTO(AccountDTO accountDTO) {
		this.accountDTO = accountDTO;
	}
	public ProductDTO getProductDTO() {
		return productDTO;
	}
	public void setProductDTO(ProductDTO productDTO) {
		this.productDTO = productDTO;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	
}
