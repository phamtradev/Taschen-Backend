package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface BannerService {
    
    BannerResponse createBanner(BannerRequest request) throws IdInvalidException;
    
    List<BannerResponse> getAllBanners();
    
    BannerResponse getBannerById(Long bannerId) throws IdInvalidException;
    
    BannerResponse updateBanner(Long bannerId, BannerRequest request) throws IdInvalidException;
    
    void deleteBanner(Long bannerId) throws IdInvalidException;
}
