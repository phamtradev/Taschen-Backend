package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.NotificationResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Notification;
import vn.edu.iuh.fit.bookstorebackend.model.User;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "senderId", source = "sender", qualifiedByName = "userToId")
    @Mapping(target = "senderName", source = "sender", qualifiedByName = "userToDisplayName")
    @Mapping(target = "receiverId", source = "receiver", qualifiedByName = "userToId")
    @Mapping(target = "receiverName", source = "receiver", qualifiedByName = "userToDisplayName")
    NotificationResponse toNotificationResponse(Notification notification);

    @Named("userToId")
    default Long userToId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("userToDisplayName")
    default String userToDisplayName(User user) {
        if (user == null) {
            return null;
        }
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else if (user.getLastName() != null) {
            return user.getLastName();
        } else {
            return user.getEmail();
        }
    }
}
