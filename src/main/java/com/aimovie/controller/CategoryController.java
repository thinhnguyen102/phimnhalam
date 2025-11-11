package com.aimovie.controller;

import com.aimovie.dto.CategoryCRUD;
import com.aimovie.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<CategoryCRUD.Response>> list(
            @RequestParam(value = "q", required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(categoryService.list(q, pageable));
    }

    @GetMapping("/{name}")
    public ResponseEntity<CategoryCRUD.Response> get(@PathVariable String name) {
        CategoryCRUD.Response res = categoryService.getByName(name);
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CategoryCRUD.Response> create(
            @RequestParam("name") String name,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon) {
        try {
            CategoryCRUD.CreateRequest req = new CategoryCRUD.CreateRequest(
                    name,
                    displayName,
                    description,
                    icon
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(req));
        } catch (RuntimeException e) {
            log.warn("Create category failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{name}")
    public ResponseEntity<CategoryCRUD.Response> update(
            @PathVariable String name,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon) {
        try {
            CategoryCRUD.UpdateRequest req = new CategoryCRUD.UpdateRequest(
                    displayName,
                    description,
                    icon
            );
            return ResponseEntity.ok(categoryService.update(name, req));
        } catch (RuntimeException e) {
            log.warn("Update category failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        try {
            categoryService.delete(name);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Delete category failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{name}/assign/{movieId}")
    public ResponseEntity<Void> assignMovie(@PathVariable String name, @PathVariable Long movieId) {
        try {
            categoryService.assignMovie(name, movieId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Assign movie failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{name}/assign/{movieId}")
    public ResponseEntity<Void> unassignMovie(@PathVariable String name, @PathVariable Long movieId) {
        try {
            categoryService.unassignMovie(name, movieId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Unassign movie failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}


