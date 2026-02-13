package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.BookMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.BookEmbedding;
import vn.edu.iuh.fit.bookstorebackend.repository.BookEmbeddingRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.service.BookEmbeddingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý embedding (biểu diễn vector) cho sách.
 * 
 * Luồng hoạt động:
 * 1. Tạo embedding khi sách được tạo/cập nhật
 * 2. Tìm sách tương tự bằng cosine similarity
 * 3. Xóa embedding khi sách bị xóa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookEmbeddingServiceImpl implements BookEmbeddingService {

    private final BookEmbeddingRepository embeddingRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    /** Số chiều của vector (100 chiều) */
    private static final int VECTOR_DIMENSION = 100;

    @Override
    @Transactional
    public void generateEmbedding(Long bookId) throws IdInvalidException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IdInvalidException("Book not found"));

        String textToEmbed = buildTextToEmbed(book);
        List<Double> vector = generateSimpleVector(textToEmbed);

        saveOrUpdateEmbedding(bookId, vector, textToEmbed);
    }

    /**
     * Tạo lại embedding khi sách được cập nhật.
     */
    @Override
    @Transactional
    public void regenerateEmbedding(Long bookId) throws IdInvalidException {
        generateEmbedding(bookId);
    }

    /**
     * Tìm sách tương tự dựa trên cosine similarity.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findSimilarBooks(Long bookId, int limit) throws IdInvalidException {
        BookEmbedding currentEmbedding = embeddingRepository.findByBookId(bookId)
                .orElseThrow(() -> new IdInvalidException("Embedding not found for this book"));

        List<Double> currentVector = parseVector(currentEmbedding.getVector());

        List<BookEmbedding> allEmbeddings = embeddingRepository.findAll();

        List<BookEmbedding> candidates = allEmbeddings.stream()
                .filter(e -> !e.getBookId().equals(bookId))
                .collect(Collectors.toList());

        Map<Long, Double> similarities = new HashMap<>();

        for (BookEmbedding embedding : candidates) {
            List<Double> otherVector = parseVector(embedding.getVector());
            double similarity = cosineSimilarity(currentVector, otherVector);
            similarities.put(embedding.getBookId(), similarity);
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Book book = bookRepository.findById(entry.getKey()).orElse(null);
                    return book != null ? bookMapper.toBookResponse(book) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Xóa embedding khi sách bị xóa.
     */
    @Override
    @Transactional
    public void deleteEmbedding(Long bookId) {
        embeddingRepository.deleteByBookId(bookId);
    }

    /**
     * Ghép text từ các thuộc tính của sách.
     */
    private String buildTextToEmbed(Book book) {
        StringBuilder sb = new StringBuilder();
        
        if (book.getTitle() != null) {
            sb.append(book.getTitle()).append(" ");
        }
        if (book.getDescription() != null) {
            sb.append(book.getDescription()).append(" ");
        }
        if (book.getAuthor() != null) {
            sb.append(book.getAuthor()).append(" ");
        }
        if (book.getCategories() != null) {
            book.getCategories().forEach(cat -> sb.append(cat.getName()).append(" "));
        }

        return sb.toString().toLowerCase().trim();
    }

    /**
     * Tạo vector đơn giản từ text bằng word frequency + hash.
     */
    private List<Double> generateSimpleVector(String text) {
        // Đếm tần suất từ
        String[] words = text.split("\\s+");
        Map<String, Integer> wordFreq = new HashMap<>();

        for (String word : words) {
            word = word.replaceAll("[^a-z0-9\u00C0-\u024F]", "");
            if (word.length() > 2) {
                wordFreq.merge(word, 1, (a, b) -> a + b);
            }
        }

        // Tạo vector dựa trên hash
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            double sum = 0.0;
            for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                int hash = Math.abs(entry.getKey().hashCode() + i * 31);
                sum += entry.getValue() * (hash % 1000) / 1000.0;
            }
            vector.add(sum);
        }

        return normalizeVector(vector);
    }

    /**
     * Chuẩn hóa vector về đơn vị (độ dài = 1).
     */
    private List<Double> normalizeVector(List<Double> vector) {
        double magnitude = Math.sqrt(vector.stream()
                .mapToDouble(d -> d * d)
                .sum());
        
        if (magnitude == 0) {
            return vector;
        }
        
        return vector.stream()
                .map(d -> d / magnitude)
                .collect(Collectors.toList());
    }

    /**
     * Tính cosine similarity giữa 2 vector.
     */
    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        
        if (denominator == 0) {
            return 0.0;
        }

        return dotProduct / denominator;
    }

    /**
     * Lưu hoặc cập nhật embedding vào database.
     */
    private void saveOrUpdateEmbedding(Long bookId, List<Double> vector, String textUsed) {
        Optional<BookEmbedding> existing = embeddingRepository.findByBookId(bookId);

        BookEmbedding embedding = existing.orElseGet(BookEmbedding::new);
        
        embedding.setBookId(bookId);
        embedding.setVector(vectorToJson(vector));
        embedding.setModel("simple-frequency");
        embedding.setDimension(VECTOR_DIMENSION);
        embedding.setTextUsed(textUsed);

        embeddingRepository.save(embedding);
    }

    /**
     * Parse vector từ JSON string sang List<Double>.
     */
    private List<Double> parseVector(String json) {
        try {
            String trimmed = json.trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
                return new ArrayList<>();
            }
            String content = trimmed.substring(1, trimmed.length() - 1);
            List<Double> result = new ArrayList<>();
            if (content.isEmpty()) {
                return result;
            }
            for (String part : content.split(",")) {
                result.add(Double.parseDouble(part.trim()));
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Chuyển List<Double> thành JSON string.
     */
    private String vectorToJson(List<Double> vector) {
        return vector.toString();
    }
}
