package org.example.fashionana.Repositorios.Inventario_Empleados;

import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
