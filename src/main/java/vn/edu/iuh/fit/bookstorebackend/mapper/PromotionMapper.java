package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePromotionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PromotionResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Promotion;
import vn.edu.iuh.fit.bookstorebackend.model.User;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "priceOrderActive", ignore = true)
    Promotion toPromotion(CreatePromotionRequest request);

    @Mapping(target = "createdById", source = "createdBy", qualifiedByName = "userToId")
    @Mapping(target = "createdByName", source = "createdBy", qualifiedByName = "userToDisplayName")
    @Mapping(target = "approvedById", source = "approvedBy", qualifiedByName = "userToId")
    @Mapping(target = "approvedByName", source = "approvedBy", qualifiedByName = "userToDisplayName")
    PromotionResponse toPromotionResponse(Promotion promotion);

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
