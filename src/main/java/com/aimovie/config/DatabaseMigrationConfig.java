package com.aimovie.config;

import com.aimovie.entity.Director;
import com.aimovie.entity.Movie;
import com.aimovie.repository.DirectorRepository;
import com.aimovie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationConfig implements CommandLineRunner {

    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Running database migration...");
        
        Director defaultDirector = directorRepository.findByNameIgnoreCase("Unknown Director")
                .orElseGet(() -> {
                    Director newDirector = Director.builder()
                            .name("Unknown Director")
                            .isActive(true)
                            .build();
                    return directorRepository.save(newDirector);
                });
        
        log.info("Default director created/found with ID: {}", defaultDirector.getId());
        
        List<Movie> moviesWithInvalidDirector = movieRepository.findAll().stream()
                .filter(movie -> movie.getDirector() == null || movie.getDirector().getId() == null || movie.getDirector().getId() == 0)
                .toList();
        
        for (Movie movie : moviesWithInvalidDirector) {
            movie.setDirector(defaultDirector);
        }
        
        if (!moviesWithInvalidDirector.isEmpty()) {
            movieRepository.saveAll(moviesWithInvalidDirector);
            log.info("Updated {} movies with invalid director references", moviesWithInvalidDirector.size());
        } else {
            log.info("No movies with invalid director references found");
        }
        
        // Migrate videoDuration from minutes to seconds
        log.info("Migrating videoDuration from minutes to seconds...");
        List<Movie> allMovies = movieRepository.findAll();
        int updatedCount = 0;
        
        for (Movie movie : allMovies) {
            if (movie.getVideoDuration() != null && movie.getVideoDuration() > 0) {
                // Check if duration is likely in minutes (less than 500 suggests it's in minutes, not seconds)
                // A typical movie is 90-180 minutes, which is 5400-10800 seconds
                if (movie.getVideoDuration() < 500) {
                    int oldDuration = movie.getVideoDuration();
                    movie.setVideoDuration(oldDuration * 60); // Convert minutes to seconds
                    updatedCount++;
                    log.debug("Updated movie '{}' (ID: {}) duration from {} minutes to {} seconds", 
                            movie.getTitle(), movie.getId(), oldDuration, movie.getVideoDuration());
                }
            }
        }
        
        if (updatedCount > 0) {
            movieRepository.saveAll(allMovies);
            log.info("Migrated {} movies' videoDuration from minutes to seconds", updatedCount);
        } else {
            log.info("No movies needed videoDuration migration");
        }
        
        log.info("Database migration completed successfully");
    }
}
