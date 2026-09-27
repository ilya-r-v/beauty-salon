package ru.mirea.beautysalon.repository;

import ru.mirea.beautysalon.exception.EntityNotFoundException;
import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.BookingStatus;
import ru.mirea.beautysalon.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BookingRepositoryJdbc implements BookingRepository {

    @Override
    public Booking save(Booking booking) {
        String sql = "INSERT INTO booking (id, service_variant_id, status, booking_time, client_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        UUID id = UUID.randomUUID();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.setObject(2, booking.getServiceVariantId());
            ps.setString(3, booking.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(booking.getTime()));
            ps.setObject(5, booking.getClientId());

            ps.executeUpdate();
            booking.setId(id);
            return booking;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении записи: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> findAll() {
        String sql = "SELECT * FROM booking ORDER BY booking_time";
        List<Booking> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при чтении записей: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        String sql = "SELECT * FROM booking WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске записи по id: " + e.getMessage(), e);
        }
    }

    @Override
    public Booking update(Booking booking) {
        String sql = "UPDATE booking SET service_variant_id = ?, status = ?, " +
                "booking_time = ?, client_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, booking.getServiceVariantId());
            ps.setString(2, booking.getStatus().name());
            ps.setTimestamp(3, Timestamp.valueOf(booking.getTime()));
            ps.setObject(4, booking.getClientId());
            ps.setObject(5, booking.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Запись с id=" + booking.getId() + " не найдена");
            }
            return booking;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении записи: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM booking WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Запись с id=" + id + " не найдена");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении записи: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> searchByClientName(String namePart) {
        String sql = "SELECT b.* FROM booking b JOIN client c ON b.client_id = c.id " +
                "WHERE c.name ILIKE ? ORDER BY b.booking_time";
        return queryWithParam(sql, "%" + namePart + "%");
    }

    @Override
    public List<Booking> searchByServiceTitle(String titlePart) {
        String sql = "SELECT b.* FROM booking b " +
                "JOIN service_variant sv ON b.service_variant_id = sv.id " +
                "WHERE sv.title ILIKE ? ORDER BY b.booking_time";
        return queryWithParam(sql, "%" + titlePart + "%");
    }

    @Override
    public List<Booking> filterByStatus(BookingStatus status) {
        String sql = "SELECT * FROM booking WHERE status = ? ORDER BY booking_time";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации по статусу: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> filterByMaster(UUID masterId) {
        String sql = "SELECT b.* FROM booking b " +
                "JOIN service_variant sv ON b.service_variant_id = sv.id " +
                "JOIN service s ON sv.service_id = s.id " +
                "WHERE s.master_id = ? ORDER BY b.booking_time";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, masterId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации по мастеру: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Booking> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT * FROM booking WHERE booking_time BETWEEN ? AND ? ORDER BY booking_time";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации по датам: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsOverlapForServiceVariant(UUID serviceVariantId, LocalDateTime time) {
        // считаем слот занятым, если у мастера уже есть активная запись на тот же час.
        String sql = "SELECT COUNT(*) FROM booking b " +
                "JOIN service_variant booked_sv ON b.service_variant_id = booked_sv.id " +
                "JOIN service booked_s ON booked_sv.service_id = booked_s.id " +
                "JOIN service_variant requested_sv ON requested_sv.id = ? " +
                "JOIN service requested_s ON requested_sv.service_id = requested_s.id " +
                "WHERE booked_s.master_id = requested_s.master_id AND b.status <> 'CANCELLED' " +
                "AND b.booking_time = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, serviceVariantId);
            ps.setTimestamp(2, Timestamp.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки занятости мастера: " + e.getMessage(), e);
        }
    }

    private List<Booking> queryWithParam(String sql, String param) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> result = new ArrayList<>();
                while (rs.next()) result.add(mapRow(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска: " + e.getMessage(), e);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId((UUID) rs.getObject("id"));
        b.setServiceVariantId((UUID) rs.getObject("service_variant_id"));
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        b.setTime(rs.getTimestamp("booking_time").toLocalDateTime());
        b.setClientId((UUID) rs.getObject("client_id"));
        return b;
    }
}
