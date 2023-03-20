package qlnhanvien.form;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordForm {
	private String password;
	
	private String token;
}
