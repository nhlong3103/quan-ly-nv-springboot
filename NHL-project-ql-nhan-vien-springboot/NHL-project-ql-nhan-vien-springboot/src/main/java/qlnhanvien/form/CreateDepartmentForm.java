package qlnhanvien.form;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.NoArgsConstructor;
import qlnhanvien.validation.account.AccountUsernameNotExists;
import qlnhanvien.validation.department.DepartmentNameNotExists;

@Data
@NoArgsConstructor
public class CreateDepartmentForm {
	@NotBlank(message = "Bạn phải điền tên department!")
	@Length(max = 50, message = "Tên department không được dài quá 50 ký tự!")
	@DepartmentNameNotExists
	private String name;

	@Pattern(regexp = "DEV|TEST|PM", message = "Bạn phải điền dạng DEV, TEST hoặc PM!")
	private String type;
	
	private List<@Valid Account> accounts;

	@Data
	@NoArgsConstructor
	public static class Account {
		@Length(max = 50, message = "Tên username không được dài quá 50 ký tự!")
		@AccountUsernameNotExists
		private String username;
	}
}
