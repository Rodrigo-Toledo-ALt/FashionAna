package org.example.fashionana.Servicios.Inventario_Empleados;

import org.example.fashionana.Modelos.Inventario_Empleados.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> findAll();
    Optional<Employee> findById(Long id);
    Employee save(Employee employee);
    void deleteById(Long id);
    Optional<Employee> findByEmail(String email);
    boolean checkPassword(String plainPassword, String hashedPassword);
}