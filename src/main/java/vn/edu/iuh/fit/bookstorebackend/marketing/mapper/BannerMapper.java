package vn.edu.iuh.fit.bookstorebackend.marketing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.marketing.model.Banner;

@Mapper(componentModel = "spring")
public interface BannerMapper {

    @Mapping(target = "id", ignore = true)
    Banner toBanner(BannerRequest request);

    BannerResponse toBannerResponse(Banner banner);
}
