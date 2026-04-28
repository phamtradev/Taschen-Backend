package vn.edu.iuh.fit.bookstorebackend.marketing.service;

import vn.edu.iuh.fit.bookstorebackend.shared.common.PromotionStatus;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.request.CreatePromotionRequest;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.response.PromotionResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface PromotionService {

    PromotionResponse createPromotion(CreatePromotionRequest request) throws IdInvalidException;

    PromotionResponse approvePromotion(Long promotionId) throws IdInvalidException;

    PromotionResponse deactivatePromotion(Long promotionId) throws IdInvalidException;

    PromotionResponse pausePromotion(Long promotionId) throws IdInvalidException;

    PromotionResponse resumePromotion(Long promotionId) throws IdInvalidException;

    List<PromotionResponse> searchPromotions(String name, String code, PromotionStatus status, Boolean isActive);

    List<PromotionResponse> getAllPromotions();

    PromotionResponse getPromotionById(Long promotionId) throws IdInvalidException;

    boolean validatePromotionCode(String code);

    void notifyPromotionPaused(Long promotionId) throws IdInvalidException;

    void notifyActiveCustomers(Long promotionId) throws IdInvalidException;

    void notifyPromotionResumed(Long promotionId) throws IdInvalidException;
}
