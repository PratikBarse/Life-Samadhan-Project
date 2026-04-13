package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByStatus(String status);

    List<Location> findByDistrict(String district);

    @Query("SELECT DISTINCT l.country FROM Location l ORDER BY l.country")
    List<String> findDistinctCountries();

    @Query("SELECT DISTINCT l.state FROM Location l WHERE l.country = ?1 ORDER BY l.state")
    List<String> findDistinctStatesByCountry(String country);

    @Query("SELECT DISTINCT l.district FROM Location l WHERE l.state = ?1 ORDER BY l.district")
    List<String> findDistinctDistrictsByState(String state);

    @Query("SELECT DISTINCT l.pincode FROM Location l WHERE l.district = ?1 ORDER BY l.pincode")
    List<String> findDistinctPincodesByDistrict(String district);
}