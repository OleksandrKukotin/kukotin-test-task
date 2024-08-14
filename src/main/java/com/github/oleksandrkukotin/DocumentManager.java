package com.github.oleksandrkukotin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;


/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.id.isBlank()) {
            document.setId(UUID.randomUUID().toString());
        }
        storage.put(document.id, document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (isSearchRequestEmpty(request)) {
            return Collections.emptyList();
        }

        return storage.values().stream()
                .filter(document -> matchesContent(document, request))
                .filter(document -> matchesAuthor(document, request))
                .filter(document -> matchesTitlePrefix(document, request))
                .filter(document -> matchesCreatedDate(document, request))
                .toList();
    }

    private boolean isSearchRequestEmpty(SearchRequest request) {
        return isNullOrEmpty(request.getTitlePrefixes()) &&
                isNullOrEmpty(request.getContainsContents()) &&
                isNullOrEmpty(request.getAuthorIds()) &&
                request.getCreatedFrom() == null &&
                request.getCreatedTo() == null;
    }

    private boolean matchesContent(Document document, SearchRequest request) {
        return isNullOrEmpty(request.getContainsContents()) ||
                request.getContainsContents().stream().anyMatch(document.getContent()::contains);
    }

    private boolean matchesAuthor(Document document, SearchRequest request) {
        return isNullOrEmpty(request.getAuthorIds()) ||
                request.getAuthorIds().contains(document.getAuthor().getId());
    }

    private boolean matchesTitlePrefix(Document document, SearchRequest request) {
        return isNullOrEmpty(request.getTitlePrefixes()) ||
                request.getTitlePrefixes().stream().anyMatch(prefix -> document.getTitle().startsWith(prefix));
    }

    private boolean matchesCreatedDate(Document document, SearchRequest request) {
        boolean matchesFrom = request.getCreatedFrom() == null || document.getCreated().isAfter(request.getCreatedFrom());
        boolean matchesTo = request.getCreatedTo() == null || document.getCreated().isBefore(request.getCreatedTo());
        return matchesFrom && matchesTo;
    }

    private boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}