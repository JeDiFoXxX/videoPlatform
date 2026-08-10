package ru.videoplatform.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.videoplatform.booking.dto.LessonCreateDto;
import ru.videoplatform.booking.dto.LessonResponseDto;
import ru.videoplatform.booking.exception.SlotConflictException;
import ru.videoplatform.booking.model.LessonStatus;
import ru.videoplatform.booking.repository.LessonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    @Transactional
    public LessonResponseDto createLesson(LessonCreateDto dto) {
        boolean isOccupied = lessonRepository.existsByStatusInAndEndTimeAfterAndStartTimeBefore(
                List.of(LessonStatus.SCHEDULED), dto.startTime(), dto.endTime()
        );

        if (isOccupied) {
            throw new SlotConflictException("Слот уже занят");
        }

        return LessonResponseDto.from(lessonRepository.save(dto.toEntity()));
    }
}
