package com.nubianlanguages.contentservice.controller;




import com.nubianlanguages.contentservice.dto.WordSentenceRequest;
import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import com.nubianlanguages.contentservice.reposi.Myrepo;
import com.nubianlanguages.contentservice.service.ContentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private ContentService ser;
    private final Myrepo wordSentenceCollectionRepository;

  /*  public ContentController(WordSentenceCollectionRepository wordSentenceCollectionRepository) {
        this.wordSentenceCollectionRepository = wordSentenceCollectionRepository;
    }*/
    public ContentController(ContentService s, Myrepo w)
    {
        this.ser=s;
        this.wordSentenceCollectionRepository=w;
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
                .map(req -> {
                    WordSentenceCollection item = new WordSentenceCollection();
                    item.setEnglishWord(req.getEnglishWord());
                    item.setEnglishSentence(req.getEnglishSentence());
                    return item;
                }).collect(Collectors.toList());;
                //.toList();

        return  wordSentenceCollectionRepository.saveAll(items);
    }
}