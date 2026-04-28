package vn.edu.iuh.fit.bookstorebackend.marketing.service;

import vn.edu.iuh.fit.bookstorebackend.marketing.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface BannerService {
    
    BannerResponse createBanner(BannerRequest request) throws IdInvalidException;
    
    List<BannerResponse> getAllBanners();
    
    BannerResponse getBannerById(Long bannerId) throws IdInvalidException;
    
    BannerResponse updateBanner(Long bannerId, BannerRequest request) throws IdInvalidException;
    
    void deleteBanner(Long bannerId) throws IdInvalidException;
}
