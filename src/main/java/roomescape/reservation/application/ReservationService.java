package roomescape.reservation.application;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.reservation.ui.dto.ReservationRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.domain.entity.Reservation;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTimeRepository;
import roomescape.theme.domain.ThemeRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationResponse.fromReservations(reservations);
    }

    public ReservationResponse findOne(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        return ReservationResponse.from(reservation);
    }

    public long make(ReservationRequest request) {
        validateRequest(request);
        return reservationRepository.save(
                request.getName(),
                request.getDate(),
                request.getTimeId(),
                request.getThemeId()
        );
    }

    private void validateRequest(ReservationRequest request) {
        validateDate(request.getDate());
        checkExistentTime(request.getTimeId());
        checkExistentTheme(request.getThemeId());
        checkDuplicated(request.getDate(), request.getTimeId(), request.getThemeId());
    }

    private void validateDate(String date) {
        LocalDate reservationDate;
        LocalDate now = LocalDate.now();

        try {
            reservationDate = LocalDate.parse(date);
        }
        catch (DateTimeParseException exception) {
            throw new BadRequestException("유효하지 않은 날짜 형식입니다.");
        }
        if (!reservationDate.isAfter(now)) {
            throw new BadRequestException("해당 날짜는 예약이 불가능합니다.");
        }
    }

    private void checkExistentTime(Long timeId) {
        try {
            reservationTimeRepository.findById(timeId);
        }
        catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("존재하지 않는 예약 시간입니다.");
        }
    }

    private void checkExistentTheme(Long themeId) {
        try {
            themeRepository.findById(themeId);
        }
        catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("존재하지 않는 테마입니다.");
        }
    }

    private void checkDuplicated(String date, Long timeId, Long themeId) {
        if (reservationRepository.countMatchWith(date, timeId, themeId) > 0) {
            throw new BadRequestException("해당 방탈출은 이미 예약되었습니다.");
        }
    }

    public void cancel(Long id) {
        long deleteCount = reservationRepository.deleteById(id);

        if (deleteCount == 0) {
            throw new NotFoundException("id와 일치하는 예약이 없습니다.");
        }
    }
}