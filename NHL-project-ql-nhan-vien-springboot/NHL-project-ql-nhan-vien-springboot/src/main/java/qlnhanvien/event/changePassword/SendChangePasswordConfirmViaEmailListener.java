package qlnhanvien.event.changePassword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import qlnhanvien.service.IAccountService;


@Component
public class SendChangePasswordConfirmViaEmailListener
		implements ApplicationListener<OnSendChangePasswordConfirmViaEmailEvent> {

	@Autowired
	private IAccountService service;

	@Override
	public void onApplicationEvent(OnSendChangePasswordConfirmViaEmailEvent event) {
		sendConfirmViaEmail(event.getEmail());
	}

	private void sendConfirmViaEmail(String email) {
		service.sendChangePasswordConfirm(email);
	}
}
