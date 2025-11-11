package com.aimovie.serviceImpl;

import com.aimovie.dto.CountryDTOs.*;
import com.aimovie.entity.Country;
import com.aimovie.entity.Movie;
import com.aimovie.repository.CountryRepository;
import com.aimovie.repository.MovieRepository;
import com.aimovie.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;

    @Override
    public CountryResponse createCountry(CountryCreateRequest request) {
        if (countryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Country with name '" + request.getName() + "' already exists");
        }

        Country country = Country.builder()
                .name(request.getName())
                .flagUrl(request.getFlagUrl())
                .isActive(true)
                .build();

        Country savedCountry = countryRepository.save(country);
        return convertToCountryResponse(savedCountry);
    }

    @Override
    public CountryResponse updateCountry(Long id, CountryUpdateRequest request) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));

        if (!country.getName().equals(request.getName()) && 
            countryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Country with name '" + request.getName() + "' already exists");
        }

        country.setName(request.getName());
        country.setFlagUrl(request.getFlagUrl());
        if (request.getIsActive() != null) {
            country.setIsActive(request.getIsActive());
        }

        Country updatedCountry = countryRepository.save(country);
        return convertToCountryResponse(updatedCountry);
    }

    @Override
    @Transactional(readOnly = true)
    public CountryResponse getCountryById(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
        return convertToCountryResponse(country);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(this::convertToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountrySummaryResponse> getAllActiveCountries() {
        return countryRepository.findByIsActiveTrue().stream()
                .map(this::convertToCountrySummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountryResponse> getAllCountriesPaginated(Pageable pageable) {
        return countryRepository.findAll(pageable)
                .map(this::convertToCountryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountryResponse> getActiveCountriesPaginated(Pageable pageable) {
        return countryRepository.findByIsActiveTrue(pageable)
                .map(this::convertToCountryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> getCountriesOrderByMovieCount() {
        return countryRepository.findAllOrderByMovieCount().stream()
                .map(this::convertToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountryResponse> getCountriesOrderByMovieCountPaginated(Pageable pageable) {
        return countryRepository.findAllOrderByMovieCount(pageable)
                .map(this::convertToCountryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> searchCountriesByName(String name) {
        return countryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountryResponse> searchCountriesByNamePaginated(String name, Pageable pageable) {
        return countryRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToCountryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> getCountriesByMovieCountRange(Integer minCount, Integer maxCount) {
        return countryRepository.findByMovieCountBetween(minCount, maxCount).stream()
                .map(this::convertToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> getCountriesWithNoMovies() {
        return countryRepository.findByMovieCount(0).stream()
                .map(this::convertToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountryResponse> getCountriesWithMovies() {
        return countryRepository.findByMovieCountGreaterThan(0).stream()
                .map(this::convertToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CountryWithMoviesResponse getCountryWithMovies(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
        
        List<Movie> movies = movieRepository.findByCountry(country);
        
        return CountryWithMoviesResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .flagUrl(country.getFlagUrl())
                .isActive(country.getIsActive())
                .movieCount(movies.size())
                .movies(movies.stream()
                        .map(movie -> MovieSummaryDTO.builder()
                                .id(movie.getId())
                                .title(movie.getTitle())
                                .year(movie.getYear())
                                .posterUrl(movie.getPosterUrl())
                                .averageRating(movie.getAverageRating())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(country.getCreatedAt())
                .updatedAt(country.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteCountry(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
        
        // Check if country has movies
        if (movieRepository.countByCountry(country) > 0) {
            throw new RuntimeException("Cannot delete country with existing movies");
        }
        
        countryRepository.delete(country);
    }

    @Override
    public void activateCountry(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
        country.setIsActive(true);
        countryRepository.save(country);
    }

    @Override
    public void deactivateCountry(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
        country.setIsActive(false);
        countryRepository.save(country);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCountryNameAvailable(String name) {
        return !countryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCountryNameAvailableForUpdate(String name, Long id) {
        return !countryRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalActiveCountries() {
        return countryRepository.countByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalMovieCount() {
        return movieRepository.count();
    }

    @Override
    public CountryFormResponse createCountryFromForm(CountryFormCreateRequest request) {
        CountryResponse countryResponse = createCountry(CountryCreateRequest.builder()
                .name(request.getName())
                .flagUrl(request.getFlagUrl())
                .isActive(request.getIsActive())
                .build());

        return CountryFormResponse.builder()
                .id(countryResponse.getId())
                .name(countryResponse.getName())
                .flagUrl(countryResponse.getFlagUrl())
                .movieCount(countryResponse.getMovieCount())
                .isActive(countryResponse.getIsActive())
                .success(true)
                .message("Country created successfully")
                .build();
    }

    @Override
    public CountryFormResponse updateCountryFromForm(Long id, CountryFormUpdateRequest request) {
        CountryResponse countryResponse = updateCountry(id, CountryUpdateRequest.builder()
                .name(request.getName())
                .flagUrl(request.getFlagUrl())
                .isActive(request.getIsActive())
                .build());

        return CountryFormResponse.builder()
                .id(countryResponse.getId())
                .name(countryResponse.getName())
                .flagUrl(countryResponse.getFlagUrl())
                .movieCount(countryResponse.getMovieCount())
                .isActive(countryResponse.getIsActive())
                .success(true)
                .message("Country updated successfully")
                .build();
    }

    @Override
    public CountryBulkResponse createCountriesBulk(CountryBulkCreateRequest request) {
        List<CountryResponse> createdCountries = request.getCountries().stream()
                .map(countryRequest -> {
                    try {
                        return createCountry(countryRequest);
                    } catch (Exception e) {
                        log.error("Error creating country: {}", countryRequest.getName(), e);
                        return null;
                    }
                })
                .filter(country -> country != null)
                .collect(Collectors.toList());

        return CountryBulkResponse.builder()
                .totalProcessed(request.getCountries().size())
                .successCount(createdCountries.size())
                .failureCount(request.getCountries().size() - createdCountries.size())
                .createdCountries(createdCountries)
                .build();
    }

    @Override
    public CountryBulkResponse updateCountriesBulk(CountryBulkUpdateRequest request) {
        List<CountryResponse> updatedCountries = new ArrayList<>();
        for (CountryUpdateRequest countryRequest : request.getCountries()) {
            try {
                // For bulk update, we need to find the country by name since CountryUpdateRequest doesn't have ID
                Country existingCountry = countryRepository.findByName(countryRequest.getName())
                        .orElseThrow(() -> new RuntimeException("Country not found with name: " + countryRequest.getName()));
                
                CountryResponse updatedCountry = updateCountry(existingCountry.getId(), countryRequest);
                updatedCountries.add(updatedCountry);
            } catch (Exception e) {
                log.error("Error updating country: {}", countryRequest.getName(), e);
            }
        }

        return CountryBulkResponse.builder()
                .totalProcessed(request.getCountries().size())
                .successCount(updatedCountries.size())
                .failureCount(request.getCountries().size() - updatedCountries.size())
                .createdCountries(updatedCountries)
                .build();
    }

    @Override
    public void deleteCountriesBulk(List<Long> ids) {
        ids.forEach(this::deleteCountry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CountrySummaryResponse> getCountryStatistics() {
        return countryRepository.findAll().stream()
                .map(this::convertToCountrySummaryResponse)
                .collect(Collectors.toList());
    }

    private CountryResponse convertToCountryResponse(Country country) {
        return CountryResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .flagUrl(formatImageUrl(country.getFlagUrl()))
                .movieCount(country.getMovieCount())
                .isActive(country.getIsActive())
                .createdAt(country.getCreatedAt())
                .updatedAt(country.getUpdatedAt())
                .build();
    }

    private CountrySummaryResponse convertToCountrySummaryResponse(Country country) {
        return CountrySummaryResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .flagUrl(formatImageUrl(country.getFlagUrl()))
                .movieCount(country.getMovieCount())
                .isActive(country.getIsActive())
                .build();
    }

    private String formatImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (url.startsWith("/api/images/") || url.startsWith("http")) {
            return url;
        }
        return "/api/images/" + url;
    }
}
