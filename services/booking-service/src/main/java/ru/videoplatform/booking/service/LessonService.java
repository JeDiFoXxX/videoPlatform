package ru.videoplatform.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.videoplatform.booking.dto.LessonCreateDto;
import ru.videoplatform.booking.dto.LessonResponseDto;
import ru.videoplatform.booking.exception.SlotConflictException;
import ru.videoplatform.booking.model.Lesson;
import ru.videoplatform.booking.model.LessonStatus;
import ru.videoplatform.booking.repository.LessonRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    @Transactional
    public LessonResponseDto createLesson(LessonCreateDto dto) {
        boolean hasActiveLesson = lessonRepository.existsByStatusInAndStudentId(
                List.of(LessonStatus.SCHEDULED), dto.studentId()
        );

        if (hasActiveLesson) {
            throw new SlotConflictException("Сначала отмените текущий урок");
        }
        boolean isOccupied = lessonRepository.existsByStatusInAndEndTimeAfterAndStartTimeBefore(
                List.of(LessonStatus.SCHEDULED), dto.startTime(), dto.endTime()
        );

        if (isOccupied) {
            throw new SlotConflictException("Слот уже занят");
        }

        return LessonResponseDto.from(lessonRepository.save(dto.toEntity()));
    }

    @Transactional
    public LessonResponseDto cancelLesson(UUID id, String studentId) {
        var activeLesson = lessonRepository.findByStatusAndIdAndStudentId(LessonStatus.SCHEDULED, id, studentId)
                .orElseThrow(() -> new SlotConflictException("Не удалось отменить урок"));
        var canceledLesson = activeLesson.toBuilder()
                .status(LessonStatus.CANCELED)
                .build();
        return LessonResponseDto.from(lessonRepository.save(canceledLesson));
    }

    public Map<Instant, List<LessonResponseDto>> getLessonsTeacher(Instant from, Instant to) {
        var lessons = lessonRepository.findByStatusInAndStartTimeBetweenOrderByStartTimeAsc(
                List.of(LessonStatus.SCHEDULED), from, to
        );
        return groupLessonsByDay(lessons);
    }

    public Map<Instant, List<LessonResponseDto>> getLessonsStudent(String studentId, Instant from, Instant to) {
        var lessons = lessonRepository.findByStatusInAndStudentIdAndStartTimeBetweenOrderByStartTimeAsc(
                List.of(LessonStatus.SCHEDULED), studentId, from, to
        );
        return groupLessonsByDay(lessons);
    }

    private Map<Instant, List<LessonResponseDto>> groupLessonsByDay(List<Lesson> lessons) {
        return lessons.stream()
                .collect(Collectors.groupingBy(
                        lesson -> lesson.getStartTime().truncatedTo(ChronoUnit.DAYS),
                        Collectors.mapping(LessonResponseDto::from, Collectors.toList())
                ));
    }
}
