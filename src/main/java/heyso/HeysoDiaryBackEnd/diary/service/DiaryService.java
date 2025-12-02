package heyso.HeysoDiaryBackEnd.diary.service;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiarySummaryResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateResponse;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiaryService {

    private final DiaryMapper diaryMapper;

    public DiaryService(DiaryMapper diaryMapper) {
        this.diaryMapper = diaryMapper;
    }

    public DiaryListResponse getDiaryList(DiaryListRequest request) {
        List<DiarySummary> diaries = diaryMapper.selectDiaryList(request);
        List<DiarySummaryResponse> responses = diaries.stream()
                .map(DiarySummaryResponse::from)
                .collect(Collectors.toList());
        return DiaryListResponse.of(responses);
    }

    public DiaryCreateResponse createDiary(DiaryCreateRequest request) {
        Diary diary = new Diary();
        diary.setUserId(request.getUserId());
        diary.setTitle(request.getTitle());
        diary.setContentMd(request.getContentMd());
        diary.setDiaryDate(request.getDiaryDate());

        diaryMapper.insertDiary(diary);
        return DiaryCreateResponse.of(diary.getDiaryId());
    }
}
