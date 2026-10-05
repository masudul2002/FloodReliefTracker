package com.floodrelief.service;

import com.floodrelief.dao.DistributionDao;
import com.floodrelief.dao.FamilyDao;
import com.floodrelief.dao.SystemConfigDao;
import com.floodrelief.model.DistributionRecord;
import com.floodrelief.model.Family;
import com.floodrelief.model.ItemCategory;
import com.floodrelief.config.AuthContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages family registration and implements the Duplicate Relief Prevention Algorithm.
 */
public class DistributionService {
    private static final Logger logger = LoggerFactory.getLogger(DistributionService.class);
    private final DistributionDao recordDao = new DistributionDao();
    private final FamilyDao familyDao = new FamilyDao();
    private final SystemConfigDao configDao = new SystemConfigDao();

    public static class DuplicateCheckResult {
        private final boolean duplicate;
        private final DistributionRecord priorRecord;
        private final double daysSincePrior;
        private final int cooldownDays;
        private final String message;

        public DuplicateCheckResult(boolean duplicate, DistributionRecord priorRecord, double daysSincePrior, int cooldownDays, String message) {
            this.duplicate = duplicate;
            this.priorRecord = priorRecord;
            this.daysSincePrior = daysSincePrior;
            this.cooldownDays = cooldownDays;
            this.message = message;
        }

        public boolean isDuplicate() { return duplicate; }
        public DistributionRecord getPriorRecord() { return priorRecord; }
        public double getDaysSincePrior() { return daysSincePrior; }
        public int getCooldownDays() { return cooldownDays; }
        public String getMessage() { return message; }
    }

    public List<Family> getAllFamilies() {
        return familyDao.getAllFamilies();
    }

    public List<Family> getFamiliesByCamp(String campId) {
        return familyDao.getFamiliesByCamp(campId);
    }

    public List<Family> searchFamilies(String query, String campIdFilter) {
        return familyDao.searchFamilies(query, campIdFilter);
    }

    public boolean registerFamily(String familyId, String campId, String headName, int memberCount, String vulnerabilityNotes) {
        Family family = new Family(familyId.trim().toUpperCase(), campId, headName.trim(), memberCount, vulnerabilityNotes);
        return familyDao.create(family);
    }

    /**
     * Duplicate Relief Prevention Algorithm:
     * Scans family distribution history for the specified aid category within the configured cooldown period.
     */
    public DuplicateCheckResult checkEligibility(String familyId, ItemCategory category) {
        int cooldownDays = configDao.getInt("cooldown.days", 7);
        DistributionRecord prior = recordDao.getRecentDistribution(familyId, category, cooldownDays);

        if (prior == null) {
            return new DuplicateCheckResult(false, null, 0, cooldownDays,
                    "Eligible: No recent " + category.getDisplayName() + " distribution within the last " + cooldownDays + " days.");
        }

        double daysSince = 0.0;
        if (prior.getDateTime() != null) {
            long hours = Duration.between(prior.getDateTime(), LocalDateTime.now()).toHours();
            daysSince = Math.max(0.0, hours / 24.0);
        }

        double daysRemaining = Math.max(0.0, cooldownDays - daysSince);
        String msg = String.format("DUPLICATE DETECTED: Family received %s %.1f days ago (Cooldown: %d days). Eligible in %.1f days.",
                category.getDisplayName(), daysSince, cooldownDays, daysRemaining);

        return new DuplicateCheckResult(true, prior, daysSince, cooldownDays, msg);
    }

    /**
     * Records distribution with duplicate prevention verification and administrator override logging.
     */
    public synchronized boolean recordDistribution(String recordId, String familyId, String campId,
                                                   ItemCategory category, String itemId, int quantity,
                                                   String recordedBy, String shipmentId,
                                                   boolean isOverride, String overrideReason) {
        DuplicateCheckResult check = checkEligibility(familyId, category);

        if (check.isDuplicate()) {
            if (!isOverride) {
                logger.warn("Distribution blocked by duplicate check for family {}: {}", familyId, check.getMessage());
                return false;
            }

            // Verify override authorization
            if (!AuthContext.canOverrideDuplicateDistributions()) {
                throw new SecurityException("Unauthorized: Only Administrators and Relief Coordinators can override duplicate distribution limits.");
            }

            if (overrideReason == null || overrideReason.trim().isEmpty()) {
                throw new IllegalArgumentException("A mandatory override reason is required to bypass duplicate aid limits.");
            }
        }

        DistributionRecord record = new DistributionRecord(recordId, familyId, campId, category, itemId, quantity, recordedBy, shipmentId);
        record.setOverride(isOverride);
        record.setOverrideReason(isOverride ? overrideReason.trim() : null);

        boolean created = recordDao.create(record);
        if (created) {
            logger.info("Recorded distribution #{} for family {} (Override={})", recordId, familyId, isOverride);
        }
        return created;
    }

    public List<DistributionRecord> getAllRecords() {
        return recordDao.getAllRecords();
    }

    public List<DistributionRecord> getRecordsByFamily(String familyId) {
        return recordDao.getRecordsByFamily(familyId);
    }
}
