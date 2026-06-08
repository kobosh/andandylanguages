package com.nubianlanguages.contentservice.controller;




import com.nubianlanguages.contentservice.dto.WordSentenceRequest;
import com.nubianlanguages.contentservice.entity.ItemType;
import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
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

    /*  public ContentController(WordSentenceCollectionRepository wordSentenceCollectionRepository) {
          this.wordSentenceCollectionRepository = wordSentenceCollectionRepository;
      }*/
    public ContentController(ContentService s, WordSentenceCollectionRepository w)
    {
        this.ser=s;
        this.wordSentenceCollectionRepository=w;
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
//    @GetMapping("/words")
//    public List<WordSentenceCollection> getWords() {
//        return wordSentenceCollectionRepository.findAll();
//    }
//    @PostMapping("/word-sentence/bulk")
//    public String saveBulk(
//            @RequestBody String body
//    ) {
//
//        System.out.println("RAW REQUEST:");
//        System.out.println(body);
//
//        return "OK";
//    }
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