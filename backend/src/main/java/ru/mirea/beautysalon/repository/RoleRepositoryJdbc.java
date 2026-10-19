package ru.mirea.beautysalon.repository;

import org.springframework.stereotype.Repository;
import ru.mirea.beautysalon.model.Role;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RoleRepositoryJdbc implements RoleRepository {

    private final DataSource dataSource;

    public RoleRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Role> findAll() {
        String sql = "SELECT * FROM role ORDER BY name";
        List<Role> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении ролей: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Role> findById(UUID id) {
        String sql = "SELECT * FROM role WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске роли: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Role> findByName(String name) {
        String sql = "SELECT * FROM role WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске роли по имени: " + e.getMessage(), e);
        }
    }

    private Role mapRow(ResultSet rs) throws SQLException {
        return new Role((UUID) rs.getObject("id"), rs.getString("name"));
    }
}
