package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.Notification;
import vn.edu.iuh.fit.bookstorebackend.model.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverAndIsReadOrderByCreatedAtDesc(User receiver, boolean isRead);

    long countByReceiverAndIsRead(User receiver, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver = :receiver AND n.isRead = false")
    int markAllAsReadByReceiver(@Param("receiver") User receiver);
}
