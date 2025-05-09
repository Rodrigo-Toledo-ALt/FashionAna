package org.example.fashionana.Servicios.Inventario_Empleados;

import org.example.fashionana.Modelos.Inventario_Empleados.Employee;
import org.example.fashionana.Repositorios.Inventario_Empleados.EmployeeRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Employee save(Employee employee) {
        // Hashear la contraseña antes de guardar
        String hashedPassword = BCrypt.hashpw(employee.getPassword(), BCrypt.gensalt());
        employee.setPassword(hashedPassword);
        return employeeRepository.save(employee);
    }

    // Método para verificar contraseñas
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    @Override
    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        // This would require a custom query method in the repository
        // For now, we'll implement a simple search
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getEmail().equals(email))
                .findFirst();
    }
}