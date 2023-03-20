package qlnhanvien.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.NoArgsConstructor;
import qlnhanvien.validation.account.AccountUsernameNotExists;

@Data
@NoArgsConstructor
public class CreateAccountForm {

	@NotBlank(message = "Bạn phải điền username!")
	@Length(max = 50, message = "username không được dài quá 50 kí tự")
	@AccountUsernameNotExists
	private String username;

	@Email(message = "Email không đúng định dạng")
	@NotEmpty(message = "Bạn phải điền email")
	private String email;

	@NotEmpty(message = "Bạn phải mật khẩu")
	private String password;

	private Integer departmentId;
}
