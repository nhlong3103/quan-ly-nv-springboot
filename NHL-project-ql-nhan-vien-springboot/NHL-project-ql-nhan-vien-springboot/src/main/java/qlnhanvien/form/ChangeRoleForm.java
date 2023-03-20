package qlnhanvien.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import qlnhanvien.entity.Account.Role;

@Data
@NoArgsConstructor
public class ChangeRoleForm {
	private int id;

	private Role role;
}
