package com.nubianlanguages.contentservice.controller;




import com.nubianlanguages.contentservice.dto.WordSentenceRequest;
import com.nubianlanguages.contentservice.entity.ContributorProgress;
import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import com.nubianlanguages.contentservice.repository.ContributorProgressRepository;
import com.nubianlanguages.contentservice.repository.WordSentenceCollectionRepository;
import com.nubianlanguages.contentservice.service.ContentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.nubianlanguages.contentservice.entity.LearnerProgress;
import  com.nubianlanguages.contentservice.repository.LearnerProgressRepository;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private ContentService ser;
    private final WordSentenceCollectionRepository wordSentenceCollectionRepository;
    private final ContributorProgressRepository contribProgressRepository;
    private  final LearnerProgressRepository learnerProgressRepository;
    public ContentController(ContentService s, WordSentenceCollectionRepository w,
                             ContributorProgressRepository c, LearnerProgressRepository learnerProgressRepository) {
        this.ser = s;
        this.wordSentenceCollectionRepository = w;
        this.contribProgressRepository = c;

        this.learnerProgressRepository = learnerProgressRepository;
    }
    @PostMapping("/progress/update")
    public void updateProgress(
            @RequestParam Long contributorId,
            @RequestParam int uploadedCount
    ) {
        System.out.println("callng progress update "+contributorId+" count "+uploadedCount);
        ContributorProgress progress = contribProgressRepository
                .findByContributorId(contributorId)
                .orElseGet(() -> {   ContributorProgress p = new ContributorProgress();
                    p.setContributorId(contributorId);
                    p.setNumberOfRecordings(0);     return p;   });
        progress.setNumberOfRecordings( progress.getNumberOfRecordings() + uploadedCount );

        contribProgressRepository.save(progress);
    }
    @GetMapping("/numberofrecordings")
    public int getNumberOfContributorRecordings( @RequestParam Long contributorId)

    {
        return   contribProgressRepository
                .findByContributorId(contributorId)
                .map(p->p.getNumberOfRecordings())
                .orElse(0);
    }
    @GetMapping("/words/next")
    public List<WordSentenceCollection> getNextWords(
            @RequestParam Long contributorId,
            @RequestParam int size
    ) {

        int alreadyRecorded = contribProgressRepository
                .findByContributorId(contributorId)
                .map(p->p.getNumberOfRecordings())
                .orElse(0);

        return wordSentenceCollectionRepository
                .findByIdGreaterThanOrderByIdAsc(
                        (long) alreadyRecorded,
                        PageRequest.of(0, size)
                );
    }
    @GetMapping("/words")
    public List<WordSentenceCollection> getWords(
            @RequestParam int start,
            @RequestParam int end
    ) {
        int size = end - start ;

        return wordSentenceCollectionRepository
                .findAll(PageRequest.of(start , size))
                .getContent();
    }
    @GetMapping("/words/range")
    public List<WordSentenceCollection> getWordsRange(
            @RequestParam Long start,
            @RequestParam Long end
    ) {
        return wordSentenceCollectionRepository.findByIdRange(start, end);
    }
    @GetMapping("/learner-progress")
    public LearnerProgress getProgress(
            @RequestParam String email,
            @RequestParam String dialect) {

        return learnerProgressRepository
                .findByLearnerEmailAndDialect(email, dialect)
                .orElseGet(() -> {
                    LearnerProgress progress = new LearnerProgress();
                    progress.setLearnerEmail(email);
                    progress.setDialect(dialect);
                    progress.setCompletedCount(0);
                    progress.setUpdatedAt(LocalDateTime.now());
                    return learnerProgressRepository.save(progress);
                });
    }
    @PostMapping("/learner-progress/increment")
    public LearnerProgress incrementProgress(
            @RequestParam String email,
            @RequestParam String dialect) {

        LearnerProgress progress = learnerProgressRepository
                .findByLearnerEmailAndDialect(email, dialect)
                .orElseGet(() -> {
                    LearnerProgress p = new LearnerProgress();
                    p.setLearnerEmail(email);
                    p.setDialect(dialect);
                    p.setCompletedCount(0);
                    return p;
                });

        progress.setCompletedCount(progress.getCompletedCount() + 1);
        progress.setUpdatedAt(LocalDateTime.now());

        return learnerProgressRepository.save(progress);
    }

    @GetMapping("/health")
    public String health() {
        return "Content Service is running";
    }
   @PostMapping("/word-sentence/bulk")
    public List<WordSentenceCollection> saveBulk(
            @RequestBody List<WordSentenceRequest> requests
    ) {
        List<WordSentenceCollection> items = requests.stream()
                .filter(req -> req.getEnglishWord() != null)
                .filter(req -> !req.getEnglishWord().isBlank())
                .map(req -> {

                    WordSentenceCollection item = new WordSentenceCollection();
                    item.setEnglishWord(req.getEnglishWord());
                    item.setEnglishSentence(req.getEnglishSentence());

System.out.println("ITEM ");
                    return item;
                }).toList();//.collect(Collectors.toList());;
        items.forEach(i ->
                System.out.println(
                        "WORD=[" + i.getEnglishWord() + "] " +
                                "SENTENCE=[" + i.getEnglishSentence() + "]"
                )
        );

        return  wordSentenceCollectionRepository.saveAll(items);
    }
}