package com.floodrelief.service;

import com.floodrelief.model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to generate operational summaries and export data to standard CSV files.
 */
public class ExportService {
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String escapeCsv(Object val) {
        if (val == null) return "";
        String s = String.valueOf(val);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static boolean exportCampsToCsv(List<Camp> camps, File targetFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("Camp ID,Name,District,Area,Population,Capacity,Waterlogged Days,Medical Need,Last Relief Date,Severity Score,Severity Label,Latitude,Longitude");
            for (Camp c : camps) {
                pw.printf("%s,%s,%s,%s,%d,%d,%d,%d,%s,%.1f,%s,%s,%s%n",
                        escapeCsv(c.getCampId()),
                        escapeCsv(c.getName()),
                        escapeCsv(c.getDistrict()),
                        escapeCsv(c.getArea()),
                        c.getPopulation(),
                        c.getCapacity(),
                        c.getWaterloggedDays(),
                        c.getMedicalNeed(),
                        escapeCsv(c.getLastReliefDate() != null ? c.getLastReliefDate().format(DT_FORMAT) : "NEVER"),
                        c.getSeverityScore(),
                        escapeCsv(c.getSeverityLabel().name()),
                        c.getLatitude(),
                        c.getLongitude()
                );
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting camps to CSV: {}", e.getMessage());
            return false;
        }
    }

    public static boolean exportInventoryToCsv(List<ReliefItem> items, File targetFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("Item ID,Name,Category,Unit,Expiry Date,Warehouse,Stock In,Reserved,Dispatched,Damaged,Expired,Available Stock,Low Stock Threshold");
            for (ReliefItem item : items) {
                pw.printf("%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%d%n",
                        escapeCsv(item.getItemId()),
                        escapeCsv(item.getName()),
                        escapeCsv(item.getCategory().name()),
                        escapeCsv(item.getUnit()),
                        escapeCsv(item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"),
                        escapeCsv(item.getWarehouseName()),
                        item.getStockIn(),
                        item.getReserved(),
                        item.getDispatched(),
                        item.getDamaged(),
                        item.getExpired(),
                        item.getAvailableStock(),
                        item.getLowStockThreshold()
                );
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting inventory to CSV: {}", e.getMessage());
            return false;
        }
    }

    public static boolean exportFamiliesToCsv(List<Family> families, File targetFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("Family ID,Camp ID,Camp Name,Household Head,Member Count,Vulnerability Notes,Registration Date");
            for (Family f : families) {
                pw.printf("%s,%s,%s,%s,%d,%s,%s%n",
                        escapeCsv(f.getFamilyId()),
                        escapeCsv(f.getCampId()),
                        escapeCsv(f.getCampName()),
                        escapeCsv(f.getHeadName()),
                        f.getMemberCount(),
                        escapeCsv(f.getVulnerabilityNotes()),
                        escapeCsv(f.getRegistrationDate() != null ? f.getRegistrationDate().format(DT_FORMAT) : "")
                );
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting families to CSV: {}", e.getMessage());
            return false;
        }
    }

    public static boolean exportShipmentsToCsv(List<Shipment> shipments, File targetFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("Shipment ID,Warehouse,Destination Camp,Status,Transport Mode,Driver Contact,Expected Delivery,Actual Delivery,Responsible User,Notes");
            for (Shipment s : shipments) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCsv(s.getShipmentId()),
                        escapeCsv(s.getWarehouseName()),
                        escapeCsv(s.getCampName()),
                        escapeCsv(s.getStatus().name()),
                        escapeCsv(s.getTransportMode()),
                        escapeCsv(s.getDriverContact()),
                        escapeCsv(s.getExpectedDeliveryTime() != null ? s.getExpectedDeliveryTime().format(DT_FORMAT) : "N/A"),
                        escapeCsv(s.getActualDeliveryTime() != null ? s.getActualDeliveryTime().format(DT_FORMAT) : "N/A"),
                        escapeCsv(s.getResponsibleUser()),
                        escapeCsv(s.getNotes())
                );
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting shipments to CSV: {}", e.getMessage());
            return false;
        }
    }

    public static boolean exportDistributionsToCsv(List<DistributionRecord> records, File targetFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("Record ID,Family ID,Head Name,Camp Name,Category,Item,Quantity,Date & Time,Recorded By,Override,Override Reason");
            for (DistributionRecord r : records) {
                pw.printf("%s,%s,%s,%s,%s,%s,%d,%s,%s,%s,%s%n",
                        escapeCsv(r.getRecordId()),
                        escapeCsv(r.getFamilyId()),
                        escapeCsv(r.getHeadName()),
                        escapeCsv(r.getCampName()),
                        escapeCsv(r.getItemCategory().name()),
                        escapeCsv(r.getItemName() != null ? r.getItemName() : r.getItemId()),
                        r.getQuantity(),
                        escapeCsv(r.getDateTime() != null ? r.getDateTime().format(DT_FORMAT) : ""),
                        escapeCsv(r.getRecordedBy()),
                        r.isOverride() ? "YES" : "NO",
                        escapeCsv(r.getOverrideReason() != null ? r.getOverrideReason() : "")
                );
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting distributions to CSV: {}", e.getMessage());
            return false;
        }
    }
}
