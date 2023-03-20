package qlnhanvien.event.changePassword;

import org.springframework.context.ApplicationEvent;

public class OnSendChangePasswordConfirmViaEmailEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private String email;

	public OnSendChangePasswordConfirmViaEmailEvent(String email) {
		super(email);
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
