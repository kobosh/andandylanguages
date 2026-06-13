package com.nubianlanguages.contentservice.controller;




import com.nubianlanguages.contentservice.dto.WordSentenceRequest;
import com.nubianlanguages.contentservice.entity.ContributorProgress;
import com.nubianlanguages.contentservice.entity.ItemType;
import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import com.nubianlanguages.contentservice.repository.ContributorProgressRepository;
import com.nubianlanguages.contentservice.repository.WordSentenceCollectionRepository;
import com.nubianlanguages.contentservice.service.ContentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private ContentService ser;
    private final WordSentenceCollectionRepository wordSentenceCollectionRepository;
    private final ContributorProgressRepository contribProgressRepository;

    public ContentController(ContentService s, WordSentenceCollectionRepository w,
                             ContributorProgressRepository c) {
        this.ser = s;
        this.wordSentenceCollectionRepository = w;
        this.contribProgressRepository = c;
    }
    @PostMapping("/progress/update")
    public void updateProgress(
            @RequestParam Long contributorId,
            @RequestParam int uploadedCount
    ) {
        System.out.println("callng progress update "+contributorId+" count "+uploadedCount);
        ContributorProgress progress = contribProgressRepository
                .findByContributorId(contributorId)
                .orElseGet(() -> {
                    ContributorProgress p = new ContributorProgress();
                    p.setContributorId(contributorId);
                    p.setNumberOfRecordings(0);
                    return p;
                });

        progress.setNumberOfRecordings(
                progress.getNumberOfRecordings() + uploadedCount
        );

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
        int size = end - start + 1;

        return wordSentenceCollectionRepository
                .findAll(PageRequest.of(start - 1, size))
                .getContent();
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