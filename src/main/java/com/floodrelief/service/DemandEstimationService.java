package com.floodrelief.service;

import com.floodrelief.dao.SystemConfigDao;
import com.floodrelief.model.Camp;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demand Estimation Service based on Sphere Humanitarian Standards.
 * Computes standard relief requirement metrics:
 * - 15 Liters of drinking/survival water per person per day
 * - 2,100 kcal food nutrition metrics (rice, lentils, fortified oil, biscuits)
 * - ORS sachets and water purification tablets based on population and acute medical need
 * - WASH family hygiene packs
 * - Temporary shelter tarpaulins
 */
public class DemandEstimationService {
    private final SystemConfigDao configDao = new SystemConfigDao();

    public static class CampDemandReport {
        private final Camp camp;
        private final int days;
        private final int population;
        private final double totalWaterLiters;
        private final double totalRiceKg;
        private final double totalLentilsKg;
        private final double totalOilLiters;
        private final int totalBiscuitsPacks;
        private final int totalOrsSachets;
        private final int totalWaterPurificationTablets;
        private final int totalWashKits;
        private final int totalTarpaulins;

        public CampDemandReport(Camp camp, int days, int population, double totalWaterLiters,
                                double totalRiceKg, double totalLentilsKg, double totalOilLiters,
                                int totalBiscuitsPacks, int totalOrsSachets, int totalWaterPurificationTablets,
                                int totalWashKits, int totalTarpaulins) {
            this.camp = camp;
            this.days = days;
            this.population = population;
            this.totalWaterLiters = totalWaterLiters;
            this.totalRiceKg = totalRiceKg;
            this.totalLentilsKg = totalLentilsKg;
            this.totalOilLiters = totalOilLiters;
            this.totalBiscuitsPacks = totalBiscuitsPacks;
            this.totalOrsSachets = totalOrsSachets;
            this.totalWaterPurificationTablets = totalWaterPurificationTablets;
            this.totalWashKits = totalWashKits;
            this.totalTarpaulins = totalTarpaulins;
        }

        public Camp getCamp() { return camp; }
        public int getDays() { return days; }
        public int getPopulation() { return population; }
        public double getTotalWaterLiters() { return totalWaterLiters; }
        public double getTotalRiceKg() { return totalRiceKg; }
        public double getTotalLentilsKg() { return totalLentilsKg; }
        public double getTotalOilLiters() { return totalOilLiters; }
        public int getTotalBiscuitsPacks() { return totalBiscuitsPacks; }
        public int getTotalOrsSachets() { return totalOrsSachets; }
        public int getTotalWaterPurificationTablets() { return totalWaterPurificationTablets; }
        public int getTotalWashKits() { return totalWashKits; }
        public int getTotalTarpaulins() { return totalTarpaulins; }

        public Map<String, String> getFormattedMetrics() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("Drinking Water (15L/person/day)", String.format("%,.0f Liters", totalWaterLiters));
            map.put("Rice Rations (400g/person/day)", String.format("%,.1f kg (%,d 10kg bags)", totalRiceKg, (int) Math.ceil(totalRiceKg / 10.0)));
            map.put("Lentils / Pulses (80g/person/day)", String.format("%,.1f kg", totalLentilsKg));
            map.put("Fortified Cooking Oil (30g/day)", String.format("%,.1f Liters", totalOilLiters));
            map.put("High-Energy Biscuits", String.format("%,d packs", totalBiscuitsPacks));
            map.put("ORS Sachets (Oral Rehydration)", String.format("%,d sachets", totalOrsSachets));
            map.put("Water Purification Tablets", String.format("%,d tablets", totalWaterPurificationTablets));
            map.put("Family WASH Hygiene Kits", String.format("%,d kits", totalWashKits));
            map.put("Heavy-Duty Shelter Tarpaulins", String.format("%,d sheets", totalTarpaulins));
            return map;
        }
    }

    public CampDemandReport calculateDemand(Camp camp, int days) {
        if (camp == null) return null;
        int pop = Math.max(1, camp.getPopulation());
        int numFamilies = (int) Math.ceil(pop / 5.0);

        double waterPerPersonDay = configDao.getDouble("sphere.water_per_person_day", 15.0);
        double totalWater = pop * waterPerPersonDay * days;

        // Food calculations based on 2,100 kcal Sphere standard
        double riceKg = (pop * 0.400 * days);
        double lentilsKg = (pop * 0.080 * days);
        double oilLiters = (pop * 0.030 * days);
        int biscuitsPacks = (int) Math.ceil(pop * 0.5 * days);

        // Medical calculations: 2 sachets per person per week + 10 per reported medical need patient
        int medicalCases = Math.max(0, camp.getMedicalNeed());
        int orsBase = (int) Math.ceil((pop * (2.0 / 7.0)) * days);
        int orsMedical = medicalCases * 10;
        int totalOrs = orsBase + orsMedical;

        // 3 tablets per person per day for water chlorination
        int purifTablets = pop * 3 * days;

        // WASH kits: 1 kit per family per 14 days
        int washKits = (int) Math.ceil(numFamilies * (Math.max(1.0, (double) days / 14.0)));

        // Shelter: 1 per family if capacity is exceeded or high waterlogged days
        int tarpaulins = (camp.getWaterloggedDays() >= 7 || camp.getPopulation() > camp.getCapacity())
                ? numFamilies
                : (int) Math.ceil(numFamilies * 0.4);

        return new CampDemandReport(camp, days, pop, totalWater, riceKg, lentilsKg, oilLiters,
                biscuitsPacks, totalOrs, purifTablets, washKits, tarpaulins);
    }
}
