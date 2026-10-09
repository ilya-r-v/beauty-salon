package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.ServiceEntity;
import ru.mirea.beautysalon.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServiceEntityRepositoryJdbc implements ServiceEntityRepository {

    @Override
    public ServiceEntity save(ServiceEntity service) {
        String sql = "INSERT INTO service (id, title, master_id, service_type_id) VALUES (?, ?, ?, ?)";
        UUID id = UUID.randomUUID();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setString(2, service.getTitle());
            ps.setObject(3, service.getMasterId());
            ps.setObject(4, service.getServiceTypeId());
            ps.executeUpdate();
            service.setId(id);
            return service;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceEntity> findAll() {
        String sql = "SELECT * FROM service ORDER BY title";
        List<ServiceEntity> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении услуг: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ServiceEntity> findById(UUID id) {
        String sql = "SELECT * FROM service WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceEntity update(ServiceEntity service) {
        String sql = "UPDATE service SET title = ?, master_id = ?, service_type_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service.getTitle());
            ps.setObject(2, service.getMasterId());
            ps.setObject(3, service.getServiceTypeId());
            ps.setObject(4, service.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Услуга не найдена: " + service.getId());
            return service;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM service WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Услуга не найдена: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении услуги: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceEntity> findByMaster(UUID masterId) {
        String sql = "SELECT * FROM service WHERE master_id = ? ORDER BY title";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, masterId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceEntity> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации услуг по мастеру: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceEntity> findByType(UUID serviceTypeId) {
        String sql = "SELECT * FROM service WHERE service_type_id = ? ORDER BY title";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, serviceTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ServiceEntity> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации услуг по типу: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM service WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки услуги: " + e.getMessage(), e);
        }
    }

    private ServiceEntity mapRow(ResultSet rs) throws SQLException {
        ServiceEntity s = new ServiceEntity();
        s.setId((UUID) rs.getObject("id"));
        s.setTitle(rs.getString("title"));
        s.setMasterId((UUID) rs.getObject("master_id"));
        s.setServiceTypeId((UUID) rs.getObject("service_type_id"));
        return s;
    }
}
