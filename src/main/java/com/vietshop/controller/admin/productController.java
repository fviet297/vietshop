package com.vietshop.controller.admin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vietshop.Entity.Category;
import com.vietshop.Entity.Product;
import com.vietshop.Service.impl.CategoryService;
import com.vietshop.Service.impl.ProductService;
import com.vietshop.dto.CategoryDTO;
import com.vietshop.dto.ProductDTO;
import com.vietshop.repository.CategoryRepository;
import com.vietshop.repository.ProductRepository;

@Controller(value = "productControllerOfAdmin")
public class productController {
	
	
	@Autowired
	public ProductService productService;
	
	@Autowired
	public CategoryRepository categoryRepository;
	
	@Autowired
	public ProductRepository productRepository;
	
	@Autowired
	public CategoryService categoryService;

	
	
	@GetMapping("/admin/addProduct")
	public String addProduct(Model model) {
		List<Category> category = categoryService.findAll();
		model.addAttribute("category", category);
		model.addAttribute("product", new Product());
		return "admin/product/createNewProduct";
	}

	
	@PostMapping("/admin/doAddProduct")
	public String doAddProduct(ModelMap model, @Valid @ModelAttribute("product") ProductDTO productDto,
			BindingResult result) throws IOException, Exception {
		if (result.hasErrors()) {
			return "admin/product/createNewProduct";
		}
		
		productService.addProduct(productDto);

		return "redirect:/admin/list-product";
	}

	
	@GetMapping("admin/list-product")
	public String listProduct(ModelMap model, @RequestParam("p") Optional<Integer> p,
			@RequestParam(name = "sort", defaultValue = "idProduct") Optional<String> sort,
			@RequestParam(name = "updown", defaultValue = "ASC") String updown,
			@RequestParam("keyword") Optional<String> keyword) {
		// Lấy list category để hiển thị ra view select
		List<Category> category = categoryService.findAll();
		model.addAttribute("category", category);
		
		int currentPage = p.orElse(0);
		Pageable pageable = null;

		if (updown.equals("ASC")) {
			PageRequest page_req = new PageRequest(currentPage, 10, Sort.Direction.ASC, sort.orElse("idProduct"));
			pageable = page_req;
		}
		if (updown.equals("DESC")) {
			PageRequest page_req = new PageRequest(currentPage, 10, Sort.Direction.DESC, sort.orElse("idProduct"));
			pageable = page_req;
		}
		Page<ProductDTO> productPage;
		if (keyword.isPresent()) {
			productPage = productService.searchProduct(keyword, pageable);// Thực hiện chức năng tìm kiếm sản phẩm
			model.addAttribute("keyword", keyword.get());

		} else {

			productPage = productService.findAllProduct(pageable);
		}
		long size = productPage.getTotalElements();
		System.out.println(size);
		model.addAttribute("size", size);
		model.addAttribute("products", productPage);
		model.addAttribute("sorter", sort.get());
		model.addAttribute("updown", updown);
		System.out.println(sort.get());
		model.addAttribute("p", currentPage);

		// định dạng tiền tệ VND
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");
		model.addAttribute("formatter", formatter);

		return "admin/product/listProduct";
	}

	
	@GetMapping("admin/list-product-by-category")
	public String listProductbyCategory(ModelMap model, @RequestParam("p") Optional<Integer> p,
			@RequestParam(name = "sort", defaultValue = "idProduct") Optional<String> sort,
			@RequestParam(name = "updown", defaultValue = "ASC") String updown, // Sắp xếp tăng giảm ASC || DESC
			@RequestParam(name = "idCategory") Long idCategory) {
		// Lấy list category để hiển thị ra view select
		List<Category> category = categoryService.findAll();
		model.addAttribute("category", category);
		// Phân trang và sắp xếp
		int currentPage = p.orElse(0);
		Pageable pageable = null;

		if (updown.equals("ASC")) {
			PageRequest page_req = new PageRequest(currentPage, 10, Sort.Direction.ASC, sort.orElse("idProduct"));
			pageable = page_req;
		}
		if (updown.equals("DESC")) {
			PageRequest page_req = new PageRequest(currentPage, 10, Sort.Direction.DESC, sort.orElse("idProduct"));
			pageable = page_req;
		}
		Page<ProductDTO> productPage = productService.findAllByCategory(categoryService.findOne(idCategory), pageable);
		model.addAttribute("products", productPage);
		long size = productPage.getTotalElements();
		CategoryDTO cate = categoryService.findOne(idCategory);
		model.addAttribute("cateName", cate);
		model.addAttribute("size", size);
		model.addAttribute("idCategory", idCategory);
		model.addAttribute("sorter", sort.get());
		model.addAttribute("updown", updown);
		model.addAttribute("p", currentPage);

		// định dạng tiền tệ VND
		DecimalFormat formatter = new DecimalFormat("###,###,###.##");
		model.addAttribute("formatter", formatter);
		return "admin/product/listProductByCategory";

	}

//	@GetMapping(value = "admin/deleteProduct")
//	public String dodeleteProduct(Model model, @RequestParam(name = "idProduct") Long idProduct) {
//		Optional<Product> opt = productService.findByIdProduct(idProduct);
//		if (opt.isPresent()) {
//			productService.delete(idProduct);
//		}
//
//		return "redirect:/admin/list-product";
//	}

	@GetMapping("admin/updateProduct")
	public String updateProduct(Model model, @RequestParam("idProduct") Long idProduct) {
		ProductDTO productDTO = productService.getOne(idProduct);
		model.addAttribute("product", productDTO);
		List<Category> category = categoryService.findAll();
		model.addAttribute("category", category);
		return "admin/product/updateProduct";

	}

	
	@PostMapping("admin/doUpdateProduct")
	public String doUpdateProduct(Model model, @Valid @ModelAttribute("product") ProductDTO productDto,
			@RequestParam("addQuantity") Long addQuantity, BindingResult result) {
		if (result.hasErrors()) {
			List<Category> category = categoryService.findAll();
			model.addAttribute("category", category);
			return "admin/product/update-product";
		}
		productService.updateProduct(productDto, addQuantity);
		
		return "redirect:/admin/list-product";
	}
	
	
	@GetMapping("admin/doHideProduct")
	public String doHideProduct(Model model,@RequestParam("idProduct")Long idProduct,@RequestParam("p") Optional<Integer> p) {
		productService.doHideProduct(idProduct);
		int page = p.orElse(0);
		return "redirect:/admin/list-product?p="+page;
	}
	
	
	@GetMapping("admin/doDisplayProduct")
	public String doDisplayProduct(Model model,@RequestParam("idProduct")Long idProduct,@RequestParam("p") Optional<Integer> p) {
		productService.doDisplayProduct(idProduct);
		int page = p.orElse(0);
		return "redirect:/admin/list-product?p="+page;
	}
	
	
	@GetMapping("admin/doHideProductByCate")
	public String doHideProductByCate(Model model,@RequestParam("idProduct")Long idProduct,@RequestParam("p") Optional<Integer> p,@RequestParam("idCategory") Long idCategory) {
		productService.doHideProduct(idProduct);
		int page = p.orElse(0);
		return "redirect:/admin/list-product-by-category?idCategory="+idCategory+"&"+"p="+page;
	}
	
	
	@GetMapping("admin/doDisplayProductByCate")
	public String doDisplayProductByCate(Model model,@RequestParam("idProduct")Long idProduct,@RequestParam("p") Optional<Integer> p,@RequestParam("idCategory") Long idCategory) {
		productService.doDisplayProduct(idProduct);
		int page = p.orElse(0);
		return "redirect:/admin/list-product-by-category?idCategory="+idCategory+"&"+"p="+page;
	}

}
