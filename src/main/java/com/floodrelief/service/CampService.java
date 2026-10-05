package com.floodrelief.service;

import com.floodrelief.dao.CampDao;
import com.floodrelief.model.Camp;
import com.floodrelief.model.SeverityLabel;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampService {
    private static final Logger logger = LoggerFactory.getLogger(CampService.class);
    private final CampDao campDao = new CampDao();
    private final PriorityEngine priorityEngine = new PriorityEngine();

    public List<Camp> getAllCamps() {
        return campDao.getAllCamps();
    }

    public List<Camp> getCampsSortedByPriority() {
        List<Camp> camps = campDao.getAllCamps();
        return priorityEngine.getCampsSortedByUrgency(camps);
    }

    public Camp getCampById(String campId) {
        return campDao.getById(campId);
    }

    public List<Camp> searchCamps(String keyword, String district, SeverityLabel severity) {
        return campDao.searchCamps(keyword, district, severity);
    }

    public boolean registerCamp(String campId, String name, String district, String area,
                                int population, int capacity, int waterloggedDays, int medicalNeed,
                                double latitude, double longitude) {
        Camp camp = new Camp();
        camp.setCampId(campId.trim().toUpperCase());
        camp.setName(name.trim());
        camp.setDistrict(district.trim());
        camp.setArea(area.trim());
        camp.setPopulation(population);
        camp.setCapacity(capacity);
        camp.setWaterloggedDays(waterloggedDays);
        camp.setMedicalNeed(medicalNeed);
        camp.setLastReliefDate(null); // Newly registered, never relieved
        camp.setLatitude(latitude);
        camp.setLongitude(longitude);

        // Calculate severity
        double score = priorityEngine.calculateSeverityScore(camp);
        camp.setSeverityScore(score);
        camp.setSeverityLabel(SeverityLabel.fromScore(score));

        boolean created = campDao.create(camp);
        if (created) {
            logger.info("Successfully registered camp {} [{}] with initial severity {}",
                    camp.getName(), camp.getCampId(), camp.getSeverityLabel());
        }
        return created;
    }

    public boolean updateCamp(Camp camp) {
        // Re-evaluate severity score
        double score = priorityEngine.calculateSeverityScore(camp);
        camp.setSeverityScore(score);
        camp.setSeverityLabel(SeverityLabel.fromScore(score));
        return campDao.update(camp);
    }

    public boolean recordReliefArrival(String campId) {
        return campDao.updateLastReliefDate(campId, LocalDateTime.now());
    }

    public boolean deleteCamp(String campId) {
        return campDao.delete(campId);
    }
}
