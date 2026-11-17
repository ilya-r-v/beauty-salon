package ru.mirea.beautysalon.service;

import jakarta.validation.constraints.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class BookingNotes {

    private final BookingService bookings;
    private final JdbcTemplate db;

    public record Note(@NotBlank @Size(max = 2000) String body, boolean pinned) {}

    public List<Map<String, Object>> list(UUID booking) {
        bookings.getBookingById(booking);
        return db.queryForList(
            "select n.*,c.name as author from booking_note n join client c on c.id=n.author_id where booking_id=? order by pinned desc,created_at desc,n.id",
            booking
        );
    }

    @Transactional
    public UUID create(UUID booking, UUID author, Note note) {
        bookings.getBookingById(booking);
        return db.queryForObject(
            "insert into booking_note(booking_id,author_id,body,pinned) values(?,?,?,?) returning id",
            UUID.class,
            booking,
            author,
            note.body().strip(),
            note.pinned()
        );
    }

    @Transactional
    public void edit(UUID booking, UUID id, Note note) {
        bookings.getBookingById(booking);
        check(
            db.update(
                "update booking_note set body=?,pinned=?,updated_at=now() where id=? and booking_id=?",
                note.body().strip(),
                note.pinned(),
                id,
                booking
            )
        );
    }

    @Transactional
    public void remove(UUID booking, UUID id) {
        bookings.getBookingById(booking);
        check(db.update("delete from booking_note where id=? and booking_id=?", id, booking));
    }

    private void check(int rows) {
        if (rows == 0) throw new EntityNotFoundException("Заметка не найдена");
    }
}
