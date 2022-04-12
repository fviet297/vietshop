package com.vietshop.Service.impl;

import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.omg.CORBA.UserException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vietshop.dto.AccountDTO;
import com.vietshop.repository.AccountRepository;
import com.vietshop.repository.RoleRepository;
import com.vietshop.Entity.Account;
import com.vietshop.Entity.Role;
import com.vietshop.Service.iAccountService;

@Service // Để class có thể thực hiện cơ chế DI và IOC
public class AccountService implements iAccountService {

	@Autowired
	public PasswordEncoder passwordEncoder;

	@Autowired
	public JavaMailSender emailSender;

	@Autowired
	public RoleService roleService;

	@Autowired

	public RoleRepository roleRepository;

	public List<Account> findAllCustomer() {
		return accountRepository.findAllCustomer();
	}

	@Autowired
	AccountRepository accountRepository;

	@Override
	public Page<Account> findAll(Pageable pageable) {
		return accountRepository.findAll(pageable);
	}

	@Override
	public List<Account> findAll() {
		return accountRepository.findAll();
	}

	@Override
	public Account findOne(Long id) {
		return accountRepository.findOne(id);
	}

	@Override
	public List<Account> findAll(Sort sort) {
		return accountRepository.findAll(sort);
	}

	@Override
	public Account registerNewUserAccount(AccountDTO accountDto) throws UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean emailExist(String email) {
		return accountRepository.findByEmail(email) != null;
	}

	@Override
	public boolean userExist(String username) {
		return accountRepository.findByUserName(username) != null;
	}

	@Override
	public Account findOneByUserNameAndStatus(String name, int status) {
		return accountRepository.findOneByUserNameAndStatus(name, status);
	}

	@Override
	public Account findByEmail(String email) {
		return accountRepository.findByEmail(email);
	}

	@Override
	public AccountDTO findByUserName(String userName) {
		Account account = accountRepository.findByUserName(userName);
		AccountDTO accountDTO = new AccountDTO();
		BeanUtils.copyProperties(account, accountDTO);
		return accountDTO;

	}

	@Override
	public Page<Account> findByUserName(String keyword, Pageable pageable) {
		return accountRepository.findByUserName(keyword, pageable);
	}

	@Override
	public Optional<Account> findByIdAccount(Long id) {
		return accountRepository.findByIdAccount(id);
	}

	public AccountDTO findByIdAccountDTO(Long id) {
		Account account = accountRepository.findOne(id);

		AccountDTO accountDTO = new AccountDTO();

		BeanUtils.copyProperties(account, accountDTO);

		return accountDTO;
	}

	@Override
	public void deleteByIdAccount(Long id) {
		accountRepository.deleteByIdAccount(id);
	}

	@Override
	public <S extends Account> S save(S entity) {
		return accountRepository.save(entity);
	}

	@Override
	public Account getOne(Long id) {
		return accountRepository.getOne(id);
	}

	@Override
	public void updateAccount(AccountDTO accountDTO) {
		Account account = accountRepository.findOne(accountDTO.getId());
		BeanUtils.copyProperties(accountDTO, account);
		accountRepository.save(account);
	}

	@Override
	public void activeAccount(Long idAccount) {
		Account account = accountRepository.findOne(idAccount);
		account.setStatus(1);
		accountRepository.save(account);
	}

	@Override
	public void deActiveAccount(Long idAccount) {
		Account account = accountRepository.findOne(idAccount);
		account.setStatus(0);
		accountRepository.save(account);
	}

	public void register(AccountDTO accountDTO) throws MessagingException {
		Role role = new Role();
		role = roleRepository.getOne(accountDTO.getidRole());
		Account newAcc = new Account();
		newAcc.setUserName(accountDTO.getUserName());
		newAcc.setPassword(passwordEncoder.encode(accountDTO.getNewpass()));
		newAcc.setDob(accountDTO.getDob());
		newAcc.setAddress(accountDTO.getAddress());
		newAcc.setFullName(accountDTO.getFullName());
		newAcc.setPhone(accountDTO.getPhone());
		newAcc.setEmail(accountDTO.getEmail());
		newAcc.setStatus(1);
		newAcc.setRole(role);
		accountRepository.save(newAcc);

		MimeMessage message = emailSender.createMimeMessage();
		boolean multipart = true;

		MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "utf-8");
		String htmlMsg = "<a>Đăng ký tài khoản vShop thành công</a>" + "<br>"
				+ "<a href='http://localhost:8080/vietshop/trang-chu'>Go to vShop</a>";
		message.setContent(htmlMsg, "text/html");
		helper.setTo(newAcc.getEmail());
		helper.setSubject("Đăng ký tài khoản vShop thành công");
		this.emailSender.send(message);
	}

	public void updateProfile(AccountDTO accountDTO) {
		Account acc = accountRepository.findOne(accountDTO.getId());
		acc.setFullName(accountDTO.getFullName());
		acc.setEmail(accountDTO.getEmail());
		acc.setPhone(accountDTO.getPhone());
		acc.setAddress(accountDTO.getAddress());
		accountRepository.save(acc);
	}

	public void changePassword(AccountDTO accountDTO) {
		Account acc = accountRepository.findOne(accountDTO.getId());
		if (accountDTO.getOldpass().equals(accountDTO.getNewpass())) {
			acc.setPassword(passwordEncoder.encode(accountDTO.getNewpass()));
			accountRepository.save(acc);
		}
	}

}
