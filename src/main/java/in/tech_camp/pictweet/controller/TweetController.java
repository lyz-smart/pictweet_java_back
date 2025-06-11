package in.tech_camp.pictweet.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.tech_camp.pictweet.custom_user.CustomUserDetail;
import in.tech_camp.pictweet.entity.TweetEntity;
// import in.tech_camp.pictweet.form.CommentForm;
import in.tech_camp.pictweet.form.SearchForm;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.repository.TweetRepository;
import in.tech_camp.pictweet.repository.UserRepository;
import in.tech_camp.pictweet.validation.ValidationOrder;
import lombok.AllArgsConstructor;

// @Controller
@RestController
@RequestMapping("/api/tweets")
@AllArgsConstructor
public class TweetController {
  private final TweetRepository tweetRepository;

  private final UserRepository userRepository;

  @GetMapping("/")
  public List<TweetEntity> showIndex() {
    List<TweetEntity> tweets = tweetRepository.findAll();
    return tweets;
  }

  // @GetMapping("/tweets/new")
  // public String showTweetNew(Model model){
  //   model.addAttribute("tweetForm", new TweetForm());
  //   return "tweets/new";
  // }
  
  @PostMapping("/")
  public ResponseEntity<?> createTweet(@RequestBody @Validated(ValidationOrder.class) TweetForm tweetForm,
                            BindingResult result, 
                            @AuthenticationPrincipal CustomUserDetail currentUser) {
    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      return ResponseEntity.badRequest().body(Map.of("messages", errorMessages));
    }

    TweetEntity tweet = new TweetEntity();
    tweet.setUser(userRepository.findById(currentUser.getId()));
    tweet.setText(tweetForm.getText());
    tweet.setImage(tweetForm.getImage());

    try {
      tweetRepository.insert(tweet);
      return ResponseEntity.ok().body(tweet);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return ResponseEntity.internalServerError().body(Map.of("messages", List.of("Internal Server Error")));
    }
    //  return "redirect:/";
  }

  @PostMapping("/{tweetId}/delete")
  public ResponseEntity<?> deleteTweet(@PathVariable("tweetId") Integer tweetId) {
    try {
      tweetRepository.deleteById(tweetId);
      return ResponseEntity.ok().body("");
    } catch (Exception e) {
      System.out.println("エラー: " + e);
      return ResponseEntity.internalServerError().body(Map.of("messages", List.of("Internal Server Error")));
    }
  }

  // @GetMapping("/tweets/{tweetId}/edit")
  // public String editTweet(@PathVariable("tweetId") Integer tweetId, Model model) {
  //   TweetEntity tweet = tweetRepository.findById(tweetId);

  //   TweetForm tweetForm = new TweetForm();
  //   tweetForm.setText(tweet.getText());
  //   tweetForm.setImage(tweet.getImage());

  //   model.addAttribute("tweetForm", tweetForm);
  //   model.addAttribute("tweetId", tweetId);
  //   return "tweets/edit";
  // }

  @PostMapping("/{tweetId}/update")
  public ResponseEntity<?> updateTweet(@RequestBody @Validated(ValidationOrder.class) TweetForm tweetForm,
                            BindingResult result,
                            @PathVariable("tweetId") Integer tweetId) {

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      return ResponseEntity.badRequest().body(Map.of("messages", errorMessages));
    }

    TweetEntity tweet = tweetRepository.findById(tweetId);
    tweet.setText(tweetForm.getText());
    tweet.setImage(tweetForm.getImage());

    try {
      tweetRepository.update(tweet);
      return ResponseEntity.ok().body(tweet);
    } catch (Exception e) {
        System.out.println("エラー：" + e);
        return ResponseEntity.internalServerError().body(Map.of("messages", List.of("Internal Server Error")));
    }
  }

  @GetMapping("/{tweetId}")
  public ResponseEntity<TweetEntity> showTweetDetail(@PathVariable("tweetId") Integer tweetId) {
    TweetEntity tweet = tweetRepository.findById(tweetId);

    if (tweet == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok().body(tweet);
  }

  @GetMapping("/search")
  public ResponseEntity<List<TweetEntity>> searchTweets(@RequestParam("query") String query) {
      List<TweetEntity> tweets = tweetRepository.findByTextContaining(query);
      return ResponseEntity.ok().body(tweets);
  }
}
