package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.ServiceVariant;
import ru.mirea.beautysalon.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServiceVariantRepositoryJdbc implements ServiceVariantRepository {

    @Override
    public ServiceVariant save(ServiceVariant variant) {
        String sql = "INSERT INTO service_variant (id, title, description, price, service_id) VALUES (?, ?, ?, ?, ?)";
        UUID id = UUID.randomUUID();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setString(2, variant.getTitle());
            ps.setString(3, variant.getDescription());
            ps.setBigDecimal(4, variant.getPrice());
            ps.setObject(5, variant.getServiceId());
            ps.executeUpdate();
            variant.setId(id);
            return variant;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании варианта услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceVariant> findAll() {
        String sql = "SELECT * FROM service_variant ORDER BY title";
        List<ServiceVariant> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении вариантов услуг: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ServiceVariant> findById(UUID id) {
        String sql = "SELECT * FROM service_variant WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске варианта услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceVariant update(ServiceVariant variant) {
        String sql = "UPDATE service_variant SET title = ?, description = ?, price = ?, service_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, variant.getTitle());
            ps.setString(2, variant.getDescription());
            ps.setBigDecimal(3, variant.getPrice());
            ps.setObject(4, variant.getServiceId());
            ps.setObject(5, variant.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Вариант услуги не найден: " + variant.getId());
            return variant;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении варианта услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM service_variant WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Вариант услуги не найден: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении варианта услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceVariant> findByService(UUID serviceId) {
        String sql = "SELECT * FROM service_variant WHERE service_id = ? ORDER BY title";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceVariant> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации вариантов по услуге: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceVariant> filterByPriceRange(BigDecimal min, BigDecimal max) {
        String sql = "SELECT * FROM service_variant WHERE price BETWEEN ? AND ? ORDER BY price";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, min);
            ps.setBigDecimal(2, max);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceVariant> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации вариантов по цене: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM service_variant WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки варианта услуги: " + e.getMessage(), e);
        }
    }

    private ServiceVariant mapRow(ResultSet rs) throws SQLException {
        ServiceVariant v = new ServiceVariant();
        v.setId((UUID) rs.getObject("id"));
        v.setTitle(rs.getString("title"));
        v.setDescription(rs.getString("description"));
        v.setPrice(rs.getBigDecimal("price"));
        v.setServiceId((UUID) rs.getObject("service_id"));
        return v;
    }
}
