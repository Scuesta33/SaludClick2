package com.example.SaludClick.repository;
import com.example.SaludClick.model.Notificacion;
import com.example.SaludClick.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    //metodo para buscar notificaciones por el usuario(creador de la notificacion)
	List<Notificacion> findByUsuario(Usuario usuario);
    //metodo para buscar notificaciones por el destinatario(usuario que recibe la notificacion)
	List<Notificacion> findByDestinatario(Usuario destinatario);
}
