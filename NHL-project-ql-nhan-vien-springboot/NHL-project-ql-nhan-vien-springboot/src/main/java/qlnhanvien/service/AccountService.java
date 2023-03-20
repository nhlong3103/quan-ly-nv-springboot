package qlnhanvien.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import qlnhanvien.entity.Account;
import qlnhanvien.entity.Department;
import qlnhanvien.entity.Account.AccountStatus;
import qlnhanvien.entity.Account.Role;
import qlnhanvien.entity.RegistrationAccountToken;
import qlnhanvien.event.changePassword.OnSendChangePasswordConfirmViaEmailEvent;
import qlnhanvien.event.registration.OnSendRegistrationAccontConfirmViaEmailEvent;
import qlnhanvien.form.AccountFilterForm;
import qlnhanvien.form.ChangePasswordForm;
import qlnhanvien.form.ChangeRoleForm;
import qlnhanvien.form.CreateAccountForm;
import qlnhanvien.form.UpdateAccountForm;
import qlnhanvien.repository.IAccountRepository;
import qlnhanvien.repository.IRegistrationAccountTokenRepository;
import qlnhanvien.specification.account.AccountSpecification;

@Component
@Service
@Transactional
public class AccountService implements IAccountService {
	@Autowired
	private IAccountRepository repository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private IRegistrationAccountTokenRepository registrationAccountTokenRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public Page<Account> getAllAccounts(Pageable pageable, String search, AccountFilterForm filterForm) {

		Specification<Account> where = AccountSpecification.buildWhere(search, filterForm);
		return repository.findAll(where, pageable);
	}

	public Account getAccountById(int id) {
		return repository.findById(id).get();
	}

	public void createAccount(CreateAccountForm form) {

		// omit id field
		TypeMap<CreateAccountForm, Account> typeMap = modelMapper.getTypeMap(CreateAccountForm.class, Account.class);
		if (typeMap == null) { // if not already added
			// skip field
			modelMapper.addMappings(new PropertyMap<CreateAccountForm, Account>() {
				@Override
				protected void configure() {
					skip(destination.getId());
				}
			});
		}

		// convert form to entity
		Account account = modelMapper.map(form, Account.class);

		account.setPassword(passwordEncoder.encode(account.getPassword()));

		repository.save(account);

		// create new token
		createNewRegistrationAccountToken(account);

		// send email
		sendConfirmAccountRegistrationViaEmail(account.getEmail());
	}

	public void requestChangePassword(String username) {
		Account account = repository.findByUsername(username);

		// create new token
		createNewChangePasswordToken(account);

		// send email
		sendConfirmChangePasswordViaEmail(account.getEmail());
	}

	public void updateAccount(int id, UpdateAccountForm form) {

		form.setId(id);

		// omit id field
		TypeMap<UpdateAccountForm, Account> typeMap = modelMapper.getTypeMap(UpdateAccountForm.class, Account.class);
		if (typeMap == null) { // if not already added
			// skip field
			modelMapper.addMappings(new PropertyMap<UpdateAccountForm, Account>() {
				@Override
				protected void configure() {
					skip(destination.getId());
				}
			});
		}

		repository.updateUsernameAndDepartmentId(form.getUsername(), form.getDepartmentId(), form.getId());
	}

	public void updateRoleAccount(int id, ChangeRoleForm form) {
		form.setId(id);
		Account account = repository.findById(id).get();
		account.setRole(form.getRole());
		repository.save(account);

	}

	public boolean isAccountExistsByUsername(String username) {
		return repository.existsByUsername(username);
	}

	public void deleteAccount(int id) {
		repository.deleteById(id);
	}

	@Override
	public List<Account> getAccountByDepartmentIdIsNull() {
		return repository.findByDepartmentIdIsNull();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account = repository.findByUsername(username);

		if (account == null) {
			throw new UsernameNotFoundException(username);
		}

		return new User(account.getUsername(), account.getPassword(),
				AuthorityUtils.createAuthorityList(account.getRole().toString()));
	}

	public Account getAccountByUsername(String username) {
		return repository.findByUsername(username);
	}

	@Override
	public List<Account> getAccountByDepartmentIdIsNullOrDepartmentIdIsParam(int departmentId) {
		return repository.findByDepartmentIdIsNullORDepartmentIdIsParam(departmentId);
	}

	@Override
	public void sendRegistrationAccountConfirm(String email) {
		Account account = repository.findByEmail(email);
		String token = registrationAccountTokenRepository.findByAccountId(account.getId());

		String confirmationUrl = "http://localhost:8080/api/qlNhanVien/accounts/activeAccount?token=" + token;

		String subject = "Xác Nhận Đăng Ký Account";
		String content = "Bạn đã đăng ký thành công. Click vào link dưới đây để kích hoạt tài khoản \n"
				+ confirmationUrl;

		sendEmail(email, subject, content);
	}

	public void sendChangePasswordConfirm(String email) {
		Account account = repository.findByEmail(email);
		String token = registrationAccountTokenRepository.findByAccountId(account.getId());

		String confirmationUrl = "Mã xác nhận của bạn là: " + token;

		String subject = "Xác nhận đổi mật khẩu";
		String content = "Dùng mã xác nhận bên dưới để đổi mật khẩu \n" + confirmationUrl;

		sendEmail(email, subject, content);
	}

	@Override
	public void activeAccount(String token) {
		RegistrationAccountToken registrationAccountToken = registrationAccountTokenRepository.findByToken(token);

		Account account = registrationAccountToken.getAccount();
		account.setAccountStatus(AccountStatus.ACTIVE);

		repository.save(account);

		// remove Registration User Token
		registrationAccountTokenRepository.deleteById(registrationAccountToken.getId());
	}

	public void changePasswordViaEmail(ChangePasswordForm form) {
		RegistrationAccountToken registrationAccountToken = registrationAccountTokenRepository
				.findByToken(form.getToken());
		Account account = registrationAccountToken.getAccount();

		repository.changePassword(passwordEncoder.encode(form.getPassword()), account.getEmail());
		registrationAccountTokenRepository.deleteByAccountId(account.getId());
	}

	public Account getAccountByEmail(String email) {
		return repository.findByEmail(email);
	}

	private void createNewRegistrationAccountToken(Account account) {
		// create new token for confirm Registration
		final String newToken = UUID.randomUUID().toString();
		RegistrationAccountToken token = new RegistrationAccountToken(newToken, account);

		registrationAccountTokenRepository.save(token);
	}

	private void createNewChangePasswordToken(Account account) {
		// create new token for confirm Registration
		final String newToken = UUID.randomUUID().toString();
		RegistrationAccountToken token = new RegistrationAccountToken(newToken, account);

		registrationAccountTokenRepository.save(token);
	}

	private void sendConfirmAccountRegistrationViaEmail(String email) {
		eventPublisher.publishEvent(new OnSendRegistrationAccontConfirmViaEmailEvent(email));
	}

	private void sendConfirmChangePasswordViaEmail(String email) {
		eventPublisher.publishEvent(new OnSendChangePasswordConfirmViaEmailEvent(email));
	}

	private void sendEmail(final String recipientEmail, final String subject, final String content) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(recipientEmail);
		message.setSubject(subject);
		message.setText(content);

		mailSender.send(message);
	}
}
