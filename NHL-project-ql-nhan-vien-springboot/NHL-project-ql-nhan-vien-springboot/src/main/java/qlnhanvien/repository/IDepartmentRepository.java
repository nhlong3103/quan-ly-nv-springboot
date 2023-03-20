package qlnhanvien.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import qlnhanvien.entity.Department;


public interface IDepartmentRepository
		extends JpaRepository<Department, Integer>, JpaSpecificationExecutor<Department> {

	public Department findByName(String name);

	boolean existsByName(String name);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Department WHERE id IN(:ids)")
	public void deleteByIds(@Param("ids") List<Integer> ids);
}
