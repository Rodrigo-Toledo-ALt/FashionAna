package org.example.fashionana.Repositorios.Pedidos;

import org.example.fashionana.Modelos.Pedidos.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
}
