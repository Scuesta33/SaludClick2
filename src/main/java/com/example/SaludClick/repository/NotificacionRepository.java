package com.example.SaludClick.repository;
import com.example.SaludClick.model.Notificacion;
import com.example.SaludClick.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
	List<Notificacion> findByUsuario(Usuario usuario);
	List<Notificacion> findByDestinatario(Usuario destinatario);
}
