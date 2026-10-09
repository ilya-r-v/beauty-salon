package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.MasterProfile;
import ru.mirea.beautysalon.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MasterProfileRepositoryJdbc implements MasterProfileRepository {

    @Override
    public MasterProfile save(MasterProfile profile) {
        String sql = "INSERT INTO master_profile (client_id, avatar_url, description, rating) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, profile.getClientId());
            ps.setString(2, profile.getAvatarUrl());
            ps.setString(3, profile.getDescription());
            ps.setFloat(4, profile.getRating());
            ps.executeUpdate();
            return profile;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании профиля мастера: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MasterProfile> findAll() {
        String sql = "SELECT * FROM master_profile";
        List<MasterProfile> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении мастеров: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MasterProfile> findById(UUID clientId) {
        String sql = "SELECT * FROM master_profile WHERE client_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске мастера: " + e.getMessage(), e);
        }
    }

    @Override
    public MasterProfile update(MasterProfile profile) {
        String sql = "UPDATE master_profile SET avatar_url = ?, description = ?, rating = ? WHERE client_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.getAvatarUrl());
            ps.setString(2, profile.getDescription());
            ps.setFloat(3, profile.getRating());
            ps.setObject(4, profile.getClientId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Профиль мастера не найден: " + profile.getClientId());
            return profile;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении профиля мастера: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(UUID clientId) {
        String sql = "DELETE FROM master_profile WHERE client_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, clientId);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Профиль мастера не найден: " + clientId);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении профиля мастера: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(UUID clientId) {
        String sql = "SELECT 1 FROM master_profile WHERE client_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки профиля мастера: " + e.getMessage(), e);
        }
    }

    private MasterProfile mapRow(ResultSet rs) throws SQLException {
        MasterProfile p = new MasterProfile();
        p.setClientId((UUID) rs.getObject("client_id"));
        p.setAvatarUrl(rs.getString("avatar_url"));
        p.setDescription(rs.getString("description"));
        p.setRating(rs.getFloat("rating"));
        return p;
    }
}
