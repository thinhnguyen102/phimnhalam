package com.aimovie.service;

import com.aimovie.dto.CountryDTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CountryService {

    CountryResponse createCountry(CountryCreateRequest request);

    CountryResponse updateCountry(Long id, CountryUpdateRequest request);

    CountryResponse getCountryById(Long id);

    List<CountryResponse> getAllCountries();

    List<CountrySummaryResponse> getAllActiveCountries();

    Page<CountryResponse> getAllCountriesPaginated(Pageable pageable);

    Page<CountryResponse> getActiveCountriesPaginated(Pageable pageable);

    List<CountryResponse> getCountriesOrderByMovieCount();

    Page<CountryResponse> getCountriesOrderByMovieCountPaginated(Pageable pageable);

    List<CountryResponse> searchCountriesByName(String name);

    Page<CountryResponse> searchCountriesByNamePaginated(String name, Pageable pageable);

    List<CountryResponse> getCountriesByMovieCountRange(Integer minCount, Integer maxCount);

    List<CountryResponse> getCountriesWithNoMovies();

    List<CountryResponse> getCountriesWithMovies();

    CountryWithMoviesResponse getCountryWithMovies(Long id);

    void deleteCountry(Long id);

    void activateCountry(Long id);

    void deactivateCountry(Long id);

    boolean isCountryNameAvailable(String name);

    boolean isCountryNameAvailableForUpdate(String name, Long id);

    Long getTotalActiveCountries();

    Long getTotalMovieCount();

    // Form-based operations
    CountryFormResponse createCountryFromForm(CountryFormCreateRequest request);

    CountryFormResponse updateCountryFromForm(Long id, CountryFormUpdateRequest request);

    // Bulk operations
    CountryBulkResponse createCountriesBulk(CountryBulkCreateRequest request);

    CountryBulkResponse updateCountriesBulk(CountryBulkUpdateRequest request);

    void deleteCountriesBulk(List<Long> ids);

    // Statistics
    List<CountrySummaryResponse> getCountryStatistics();
}
