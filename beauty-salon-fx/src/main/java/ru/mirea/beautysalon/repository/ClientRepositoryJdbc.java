package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientRepositoryJdbc implements ClientRepository {

    @Override
    public Client save(Client client) {
        String sql = "INSERT INTO client (id, name, phone, password_hash, role_id) VALUES (?, ?, ?, ?, ?)";
        UUID id = UUID.randomUUID();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.setString(2, client.getName());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getPasswordHash());
            ps.setObject(5, client.getRoleId());
            ps.executeUpdate();
            client.setId(id);
            return client;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании клиента: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT * FROM client ORDER BY name";
        List<Client> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении клиентов: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Client> findById(UUID id) {
        String sql = "SELECT * FROM client WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске клиента: " + e.getMessage(), e);
        }
    }

    @Override
    public Client update(Client client) {
        String sql = "UPDATE client SET name = ?, phone = ?, password_hash = ?, role_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getPasswordHash());
            ps.setObject(4, client.getRoleId());
            ps.setObject(5, client.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Клиент с id=" + client.getId() + " не найден");
            return client;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении клиента: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new EntityNotFoundException("Клиент с id=" + id + " не найден");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении клиента: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM client WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки существования клиента: " + e.getMessage(), e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId((UUID) rs.getObject("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setRoleId((UUID) rs.getObject("role_id"));
        return c;
    }
}
