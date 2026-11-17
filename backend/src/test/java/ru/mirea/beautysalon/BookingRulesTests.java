package ru.mirea.beautysalon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.beautysalon.filter.BookingFilter;
import ru.mirea.beautysalon.repository.BookingRepository;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingRulesTests {
    @Autowired JdbcTemplate db;
    @Autowired BookingRepository bookings;
    @Test void pendingUnpaidBookingRequiresAttentionUntilPrepaid() {
        var id=db.queryForObject("select id from booking where status='PENDING' limit 1",java.util.UUID.class);
        db.update("update booking set prepaid=false where id=?",id);
        assertEquals("HIGH",db.queryForObject("select priority from booking where id=?",String.class,id));
        db.update("update booking set prepaid=true where id=?",id);
        assertEquals("NORMAL",db.queryForObject("select priority from booking where id=?",String.class,id));
    }
    @Test void searchCombinesTextPriceAndArchiveFlag() {
        var b=bookings.findAll().getFirst();
        b.setComment("Поиск-КР3"); b.setArchived(true); bookings.saveAndFlush(b);
        var query=new BookingFilter("поиск-кр3",null,null,null,null,null,null,true,b.getBookedPrice(),b.getBookedPrice(),null,null,null,null);
        assertEquals(java.util.List.of(b),bookings.findAll(query.specification()));
        b.setArchived(false); bookings.saveAndFlush(b);
        assertTrue(bookings.findAll(query.specification()).isEmpty());
    }
}
