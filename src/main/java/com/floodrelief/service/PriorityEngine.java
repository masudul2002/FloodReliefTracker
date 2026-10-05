package com.floodrelief.service;

import com.floodrelief.dao.CampDao;
import com.floodrelief.dao.SystemConfigDao;
import com.floodrelief.model.Camp;
import com.floodrelief.model.SeverityLabel;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Priority Engine & Severity Scoring Model.
 * Calculates dynamic severity scores using a 4-factor normalization formula:
 * 1. Population (normalized against max capacity baseline)
 * 2. Waterlogged Days (0 to 21+ days)
 * 3. Medical Need (acute health distress)
 * 4. Days Since Last Relief assistance
 *
 * Utilizes a Java PriorityQueue with a custom Comparator to sort and triage camps by urgency.
 */
public class PriorityEngine {
    private static final Logger logger = LoggerFactory.getLogger(PriorityEngine.class);

    private static final double MAX_POPULATION_REF = 2500.0;
    private static final double MAX_WATERLOGGED_DAYS_REF = 21.0;
    private static final double MAX_MEDICAL_NEED_REF = 80.0;
    private static final double MAX_RELIEF_INTERVAL_DAYS_REF = 14.0;

    private final SystemConfigDao configDao = new SystemConfigDao();
    private final CampDao campDao = new CampDao();

    /**
     * Custom Comparator for Camp triage urgency.
     * Orders camps with highest severity score first, breaking ties by days without relief and medical need.
     */
    public static final Comparator<Camp> URGENCY_COMPARATOR = (c1, c2) -> {
        int scoreCompare = Double.compare(c2.getSeverityScore(), c1.getSeverityScore());
        if (scoreCompare != 0) return scoreCompare;

        // Tie-breaker 1: Days since last relief
        double days1 = getDaysSinceLastRelief(c1);
        double days2 = getDaysSinceLastRelief(c2);
        int daysCompare = Double.compare(days2, days1);
        if (daysCompare != 0) return daysCompare;

        // Tie-breaker 2: Medical need
        return Integer.compare(c2.getMedicalNeed(), c1.getMedicalNeed());
    };

    public double calculateSeverityScore(Camp camp) {
        if (camp == null) return 0.0;

        double wPop = configDao.getDouble("weight.population", 0.30);
        double wWater = configDao.getDouble("weight.waterlogged", 0.30);
        double wMed = configDao.getDouble("weight.medical", 0.30);
        double wRelief = configDao.getDouble("weight.last_relief", 0.10);

        // Normalize weights if their sum is not 1.0
        double totalWeight = wPop + wWater + wMed + wRelief;
        if (totalWeight <= 0) totalWeight = 1.0;
        wPop /= totalWeight;
        wWater /= totalWeight;
        wMed /= totalWeight;
        wRelief /= totalWeight;

        // Factor 1: Normalized Population [0.0 - 1.0]
        double normPop = Math.min(1.0, Math.max(0.0, camp.getPopulation() / MAX_POPULATION_REF));

        // Factor 2: Normalized Waterlogged Days [0.0 - 1.0]
        double normWater = Math.min(1.0, Math.max(0.0, camp.getWaterloggedDays() / MAX_WATERLOGGED_DAYS_REF));

        // Factor 3: Normalized Medical Need [0.0 - 1.0]
        double normMed = Math.min(1.0, Math.max(0.0, camp.getMedicalNeed() / MAX_MEDICAL_NEED_REF));

        // Factor 4: Normalized Days Since Last Relief [0.0 - 1.0]
        double daysSinceRelief = getDaysSinceLastRelief(camp);
        double normRelief = Math.min(1.0, Math.max(0.0, daysSinceRelief / MAX_RELIEF_INTERVAL_DAYS_REF));

        // Aggregate 4-factor score normalized to 0 - 100
        double rawScore = (wPop * normPop) +
                          (wWater * normWater) +
                          (wMed * normMed) +
                          (wRelief * normRelief);

        double score = Math.round(rawScore * 1000.0) / 10.0; // Round to 1 decimal place
        return Math.min(100.0, Math.max(0.0, score));
    }

    public static double getDaysSinceLastRelief(Camp camp) {
        if (camp.getLastReliefDate() == null) {
            return MAX_RELIEF_INTERVAL_DAYS_REF; // Never received relief -> treated as maximum urgency
        }
        Duration diff = Duration.between(camp.getLastReliefDate(), LocalDateTime.now());
        long hours = diff.toHours();
        return Math.max(0.0, hours / 24.0);
    }

    /**
     * Evaluates and updates severity score and label for a camp, saving to database.
     */
    public Camp evaluateAndUpdateCamp(Camp camp) {
        double score = calculateSeverityScore(camp);
        camp.setSeverityScore(score);
        camp.setSeverityLabel(SeverityLabel.fromScore(score));
        campDao.updateSeverity(camp.getCampId(), score, camp.getSeverityLabel());
        return camp;
    }

    /**
     * Recalculates severity for all camps and returns a PriorityQueue sorted by urgency.
     */
    public PriorityQueue<Camp> getTriagePriorityQueue(List<Camp> camps) {
        PriorityQueue<Camp> queue = new PriorityQueue<>(URGENCY_COMPARATOR);
        for (Camp c : camps) {
            double score = calculateSeverityScore(c);
            c.setSeverityScore(score);
            c.setSeverityLabel(SeverityLabel.fromScore(score));
            campDao.updateSeverity(c.getCampId(), score, c.getSeverityLabel());
            queue.offer(c);
        }
        return queue;
    }

    /**
     * Returns camps sorted by urgency in descending order.
     */
    public List<Camp> getCampsSortedByUrgency(List<Camp> camps) {
        PriorityQueue<Camp> queue = getTriagePriorityQueue(camps);
        List<Camp> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            sorted.add(queue.poll());
        }
        return sorted;
    }
}
