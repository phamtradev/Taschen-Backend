package vn.edu.iuh.fit.bookstorebackend.book.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.bookstorebackend.book.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.book.mapper.BookMapper;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.book.model.BookEmbedding;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookEmbeddingRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.book.service.BookEmbeddingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling embedding (vector representation) for books.
 * <p>
 * Workflow:
 * 1. Generate embedding when book is created/updated
 * 2. Find similar books using cosine similarity
 * 3. Delete embedding when book is deleted
 * <p>
 * All embedding generation runs ASYNCHRONOUSLY to avoid blocking user requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookEmbeddingServiceImpl implements BookEmbeddingService {

    // Record to hold embedding result + model name
    private record EmbeddingResult(List<Double> vector, String model) {
    }

    private final BookEmbeddingRepository embeddingRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Value("${ai.api.key}")
    private String aiApiKey;

    // Batch size for streaming embeddings (avoid loading all into RAM at once)
    private static final int BATCH_SIZE = 500;

    // Standard embedding dimension - Gemini returns 768
    private static final int VECTOR_DIMENSION = 768;

    @Override
    @Async("embeddingTaskExecutor")
    @Transactional
    public void generateEmbedding(Long bookId) throws IdInvalidException {
        log.info("Starting async embedding generation for bookId: {}", bookId);
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IdInvalidException("Book not found"));

            String textToEmbed = buildTextToEmbed(book);
            EmbeddingResult result = generateSimpleVector(textToEmbed);

            saveOrUpdateEmbedding(bookId, result.vector(), result.model(), textToEmbed);
            log.info("Completed async embedding generation for bookId: {}", bookId);
        } catch (Exception e) {
            log.error("Failed to generate embedding for bookId: {}", bookId, e);
        }
    }

    /**
     * Regenerate embedding when book is updated.
     */
    @Override
    @Async("embeddingTaskExecutor")
    @Transactional
    public void regenerateEmbedding(Long bookId) throws IdInvalidException {
        log.info("Starting async embedding regeneration for bookId: {}", bookId);
        generateEmbedding(bookId);
    }

    /**
     * Find similar books based on cosine similarity.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findSimilarBooks(Long bookId, int limit) throws IdInvalidException {
        BookEmbedding currentEmbedding = embeddingRepository.findByBookId(bookId)
                .orElseThrow(() -> new IdInvalidException("Embedding not found for this book"));

        List<Double> currentVector = parseVector(currentEmbedding.getVector());

        // Use top-k min-heap approach: only keep top 'limit' results in memory
        // Instead of loading all embeddings, we process in batches
        PriorityQueue<Map.Entry<Long, Double>> topK = new PriorityQueue<>(
                Map.Entry.<Long, Double>comparingByValue()
        );

        int totalProcessed = 0;

        // Process embeddings in batches to avoid OOM with large datasets
        int pageNum = 0;
        Page<BookEmbedding> page;
        do {
            Pageable pageable = PageRequest.of(pageNum, BATCH_SIZE);
            page = embeddingRepository.findAll(pageable);
            for (BookEmbedding embedding : page.getContent()) {
                if (embedding.getBookId().equals(bookId)) {
                    continue;
                }
                List<Double> otherVector = parseVector(embedding.getVector());
                double similarity = cosineSimilarity(currentVector, otherVector);

                topK.offer(Map.entry(embedding.getBookId(), similarity));
                if (topK.size() > limit) {
                    topK.poll();
                }
            }
            pageNum++;
            totalProcessed += page.getNumberOfElements();
        } while (page.hasNext());

        log.info("Processed {} embeddings for similarity search against bookId={}", totalProcessed, bookId);

        // Extract top-k results and sort descending
        List<Map.Entry<Long, Double>> topResults = new ArrayList<>(topK);
        topResults.sort(Map.Entry.<Long, Double>comparingByValue().reversed());

        // Fetch books in a single query
        List<Long> topBookIds = topResults.stream()
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, Book> bookMap = bookRepository.findAllById(topBookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        return topResults.stream()
                .map(entry -> bookMap.get(entry.getKey()))
                .filter(Objects::nonNull)
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> findSimilarByText(String queryText, int limit) {
        log.info("Finding similar books for text query: {}", queryText);

        // Embed the query text using Gemini
        EmbeddingResult queryEmbedding = generateSimpleVector(queryText.toLowerCase().trim());
        List<Double> queryVector = queryEmbedding.vector();

        // Use top-k min-heap
        PriorityQueue<Map.Entry<Long, Double>> topK = new PriorityQueue<>(
                Map.Entry.<Long, Double>comparingByValue()
        );

        int pageNum = 0;
        Page<BookEmbedding> page;
        do {
            Pageable pageable = PageRequest.of(pageNum, BATCH_SIZE);
            page = embeddingRepository.findAll(pageable);
            for (BookEmbedding embedding : page.getContent()) {
                List<Double> otherVector = parseVector(embedding.getVector());
                double similarity = cosineSimilarity(queryVector, otherVector);

                topK.offer(Map.entry(embedding.getBookId(), similarity));
                if (topK.size() > limit) {
                    topK.poll();
                }
            }
            pageNum++;
        } while (page.hasNext());

        List<Map.Entry<Long, Double>> topResults = new ArrayList<>(topK);
        topResults.sort(Map.Entry.<Long, Double>comparingByValue().reversed());

        List<Long> topBookIds = topResults.stream()
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, Book> bookMap = bookRepository.findAllById(topBookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        return topResults.stream()
                .map(entry -> bookMap.get(entry.getKey()))
                .filter(Objects::nonNull)
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete embedding when book is deleted.
     */
    @Override
    @Transactional
    public void deleteEmbedding(Long bookId) {
        log.info("Deleting embedding for bookId: {}", bookId);
        embeddingRepository.deleteByBookId(bookId);
    }

    /**
     * Build text from book properties for embedding.
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
     * Generate vector from text using Gemini API.
     * Uses local fallback algorithm if API fails.
     */
    private EmbeddingResult generateSimpleVector(String text) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Gemini embedding API - uses v1beta with X-goog-api-key header
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

            String requestBody = String.format("""
                    {
                      "content": {
                        "parts": [{"text": "%s"}]
                      }
                    }
                    """, text.replace("\"", "\\\"").replace("\n", " "));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", aiApiKey); // Using X-goog-api-key header

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("embedding")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> embedding = (Map<String, Object>) body.get("embedding");
                @SuppressWarnings("unchecked")
                List<Double> values = (List<Double>) embedding.get("values");
                if (values != null && !values.isEmpty()) {
                    log.info("Successfully generated embedding via Gemini API, dimension: {}", values.size());
                    return new EmbeddingResult(values, "gemini-embedding-001");
                }
            }

            log.warn("Gemini API returned empty embedding, using fallback");
            return new EmbeddingResult(generateFallbackVector(text), "fallback-local");

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}, using fallback algorithm", e.getMessage());
            return new EmbeddingResult(generateFallbackVector(text), "fallback-local");
        }
    }

    /**
     * Fallback: Generate simple vector from text using word frequency + hash.
     * Used when Gemini API fails.
     */
    private List<Double> generateFallbackVector(String text) {
        // Count word frequency
        String[] words = text.split("\\s+");
        Map<String, Integer> wordFreq = new HashMap<>();

        for (String word : words) {
            word = word.replaceAll("[^a-z0-9\\u00C0-\\u024F]", "");
            if (word.length() > 2) {
                wordFreq.merge(word, 1, (a, b) -> a + b);
            }
        }

        // Generate vector based on hash
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
     * Calculate cosine similarity between 2 vectors.
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
     * Save or update embedding to database.
     */
    @Transactional
    protected void saveOrUpdateEmbedding(Long bookId, List<Double> vector, String model, String textUsed) {
        Optional<BookEmbedding> existing = embeddingRepository.findByBookId(bookId);

        BookEmbedding embedding = existing.orElseGet(BookEmbedding::new);

        embedding.setBookId(bookId);
        embedding.setVector(vectorToJson(vector));
        embedding.setModel(model);
        embedding.setDimension(VECTOR_DIMENSION);
        embedding.setTextUsed(textUsed);

        embeddingRepository.save(embedding);
        log.debug("Saved embedding for bookId: {}", bookId);
    }

    /**
     * Parse vector from JSON string to List<Double>.
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
     * Convert List<Double> to JSON string.
     */
    private String vectorToJson(List<Double> vector) {
        return vector.toString();
    }
}
