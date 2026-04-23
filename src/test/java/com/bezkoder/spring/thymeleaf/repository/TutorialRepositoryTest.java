package com.bezkoder.spring.thymeleaf.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;

@DataJpaTest
class TutorialRepositoryTest {

  @Autowired
  private TutorialRepository tutorialRepository;

  @Test
  void findByTitleContainingIgnoreCase_returnsMatchingTutorials() {
    tutorialRepository.save(new Tutorial("Spring Basics", "Intro", 1, false));
    tutorialRepository.save(new Tutorial("JAVA Advanced", "Deep dive", 3, true));

    List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("spring");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getTitle()).isEqualTo("Spring Basics");
  }

  @Test
  void updatePublishedStatus_updatesPublishedFlag() {
    Tutorial tutorial = tutorialRepository.save(new Tutorial("JUnit", "Testing", 2, false));

    tutorialRepository.updatePublishedStatus(tutorial.getId(), true);

    Tutorial updated = tutorialRepository.findById(tutorial.getId()).orElseThrow(IllegalStateException::new);
    assertThat(updated.isPublished()).isTrue();
  }
}
