
package ms_core_banco.banco.repository;

import ms_core_banco.banco.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
