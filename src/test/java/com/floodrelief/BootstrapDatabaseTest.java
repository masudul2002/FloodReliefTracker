package com.floodrelief;

import com.floodrelief.service.DatabaseManager;
import com.floodrelief.service.SchemaInitializer;
import com.floodrelief.model.Camp;
import com.floodrelief.model.ReliefItem;
import com.floodrelief.model.User;
import com.floodrelief.service.AuthService;
import com.floodrelief.service.CampService;
import com.floodrelief.service.InventoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BootstrapDatabaseTest {

    @Test
    @DisplayName("Bootstrap database schema and verify seed records in Neon Cloud PostgreSQL")
    public void testSchemaAndSeedInitialization() {
        assertTrue(DatabaseManager.isConnected(), "Must connect to Cloud PostgreSQL");

        // Execute schema initialization & seed
        SchemaInitializer.initializeSchemaAndSeed();

        // Verify users seeded
        AuthService authService = new AuthService();
        List<User> users = authService.getAllUsers();
        assertFalse(users.isEmpty(), "Users table must be seeded");

        // Verify admin can login
        assertTrue(authService.login("admin", "admin123"), "Admin login must succeed");

        // Verify camps seeded
        CampService campService = new CampService();
        List<Camp> camps = campService.getAllCamps();
        assertFalse(camps.isEmpty(), "Camps table must be seeded");
        assertTrue(camps.size() >= 8, "Must have at least 8 Haor region camps");

        // Verify inventory items seeded
        InventoryService invService = new InventoryService();
        List<ReliefItem> items = invService.getAllItems();
        assertFalse(items.isEmpty(), "Relief items table must be seeded");
    }
}
