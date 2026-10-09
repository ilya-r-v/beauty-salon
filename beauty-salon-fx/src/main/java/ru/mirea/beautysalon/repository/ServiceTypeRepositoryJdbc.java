package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.model.ServiceType;
import ru.mirea.beautysalon.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServiceTypeRepositoryJdbc implements ServiceTypeRepository {

    @Override
    public ServiceType save(ServiceType type) {
        String sql = "INSERT INTO service_type (id, name) VALUES (?, ?)";
        UUID id = UUID.randomUUID();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setString(2, type.getName());
            ps.executeUpdate();
            type.setId(id);
            return type;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании типа услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceType> findAll() {
        String sql = "SELECT * FROM service_type ORDER BY name";
        List<ServiceType> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new ServiceType((UUID) rs.getObject("id"), rs.getString("name")));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении типов услуг: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ServiceType> findById(UUID id) {
        String sql = "SELECT * FROM service_type WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new ServiceType((UUID) rs.getObject("id"), rs.getString("name")));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске типа услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM service_type WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки типа услуги: " + e.getMessage(), e);
        }
    }
}
