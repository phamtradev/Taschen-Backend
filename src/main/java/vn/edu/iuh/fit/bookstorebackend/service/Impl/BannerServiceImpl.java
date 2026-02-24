package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.BannerMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Banner;
import vn.edu.iuh.fit.bookstorebackend.repository.BannerRepository;
import vn.edu.iuh.fit.bookstorebackend.service.BannerService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    @Override
    @Transactional
    public BannerResponse createBanner(BannerRequest request) throws IdInvalidException {
        validateRequest(request);
        
        Banner banner = bannerMapper.toBanner(request);
        banner.setName(request.getName().trim());
        banner.setImageUrl(request.getImageUrl().trim());
        
        Banner savedBanner = bannerRepository.save(banner);
        return bannerMapper.toBannerResponse(savedBanner);
    }

    private void validateRequest(BannerRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("BannerRequest cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IdInvalidException("Banner name cannot be null or empty");
        }
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new IdInvalidException("Banner image URL cannot be null or empty");
        }
    }

    @Override
    public List<BannerResponse> getAllBanners() {
        List<Banner> banners = bannerRepository.findAll();
        return banners.stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BannerResponse getBannerById(Long bannerId) throws IdInvalidException {
        validateBannerId(bannerId);
        Banner banner = findBannerById(bannerId);
        return bannerMapper.toBannerResponse(banner);
    }

    private void validateBannerId(Long bannerId) throws IdInvalidException {
        if (bannerId == null || bannerId <= 0) {
            throw new IdInvalidException("Banner id is invalid: " + bannerId);
        }
    }

    private Banner findBannerById(Long bannerId) throws IdInvalidException {
        return bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IdInvalidException("Banner not found with id: " + bannerId));
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Long bannerId, BannerRequest request) throws IdInvalidException {
        validateBannerId(bannerId);
        validateRequest(request);
        
        Banner banner = findBannerById(bannerId);
        banner.setName(request.getName().trim());
        banner.setImageUrl(request.getImageUrl().trim());
        
        Banner updatedBanner = bannerRepository.save(banner);
        return bannerMapper.toBannerResponse(updatedBanner);
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) throws IdInvalidException {
        validateBannerId(bannerId);
        Banner banner = findBannerById(bannerId);
        bannerRepository.delete(banner);
    }
}
