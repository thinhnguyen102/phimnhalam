package com.aimovie.service;

import com.aimovie.dto.CategoryCRUD;
import com.aimovie.entity.Category;
import com.aimovie.entity.Movie;
import com.aimovie.repository.CategoryRepository;
import com.aimovie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public Page<CategoryCRUD.Response> list(String q, Pageable pageable) {
        Page<Category> page = (q == null || q.isBlank())
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(q, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryCRUD.Response getByName(String name) {
        return categoryRepository.findByName(name)
                .map(this::toResponse)
                .orElse(null);
    }

    public CategoryCRUD.Response create(CategoryCRUD.CreateRequest req) {
        String normalized = req.getName().trim().toLowerCase();
        if (categoryRepository.existsByName(normalized)) {
            throw new RuntimeException("Category name already exists");
        }

        Category cat = Category.builder()
                .name(normalized)
                .displayName(req.getDisplayName())
                .description(req.getDescription())
                .icon(req.getIcon())
                .build();
        return toResponse(categoryRepository.save(cat));
    }

    public CategoryCRUD.Response update(String name, CategoryCRUD.UpdateRequest req) {
        Category cat = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (req.getDisplayName() != null) cat.setDisplayName(req.getDisplayName());
        if (req.getDescription() != null) cat.setDescription(req.getDescription());
        if (req.getIcon() != null) cat.setIcon(req.getIcon());
        return toResponse(categoryRepository.save(cat));
    }

    public void delete(String name) {
        Category cat = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(cat);
    }

    public void assignMovie(String categoryName, Long movieId) {
        Category cat = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        cat.getMovies().add(movie);
        movie.getCategories().add(cat);
        categoryRepository.save(cat);
        movieRepository.save(movie);
    }

    public void unassignMovie(String categoryName, Long movieId) {
        Category cat = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        cat.getMovies().remove(movie);
        movie.getCategories().remove(cat);
        categoryRepository.save(cat);
        movieRepository.save(movie);
    }

    private CategoryCRUD.Response toResponse(Category c) {
        return CategoryCRUD.Response.builder()
                .id(c.getId())
                .name(c.getName())
                .displayName(c.getDisplayName())
                .description(c.getDescription())
                .icon(c.getIcon())
                .movieCount(c.getMovies() != null ? c.getMovies().size() : 0)
                .build();
    }
}


