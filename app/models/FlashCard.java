/**
 *
 */
package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.PrivateOwned;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import models.statistics.CardStatistics;
import play.Logger;
import util.JsonKeys;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * @author Fabian Widmann
 */
@Entity
@Table(name = JsonKeys.FLASH_CARD_TABLE_NAME)
@JsonPropertyOrder({JsonKeys.FLASHCARD_ID})
public class FlashCard extends Model {
    public static Model.Finder<Long, FlashCard> find = new Model.Finder<Long, FlashCard>(FlashCard.class);
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.FLASHCARD_ID)
    @JsonProperty(JsonKeys.FLASHCARD_ID)
    private long id;
    @JsonProperty(JsonKeys.RATING)
    @Column(name = JsonKeys.RATING)
    private int rating;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    @Column(name = JsonKeys.DATE_CREATED)
    private Date created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @UpdatedTimestamp
    @Column(name = JsonKeys.DATE_UPDATED)
    @JsonProperty(JsonKeys.DATE_UPDATED)
    private Date lastUpdated;
    @ManyToMany/*(cascade = CascadeType.ALL)*/
    @JoinTable(name = JsonKeys.CARD_TAG_JOIN_TABLE,
            joinColumns = @JoinColumn(name = JsonKeys.FLASHCARD_ID, referencedColumnName = JsonKeys.FLASHCARD_ID),
            inverseJoinColumns = @JoinColumn(name = JsonKeys.TAG_ID, referencedColumnName = JsonKeys.TAG_ID))
    private List<Tag> tags;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = JsonKeys.QUESTION_ID, referencedColumnName = JsonKeys.QUESTION_ID)
    @PrivateOwned //this means, that if the element is deleted with its parent.
    @JsonProperty(JsonKeys.FLASHCARD_QUESTION)
    private Question question;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "card")
    @PrivateOwned
    @JsonProperty(JsonKeys.FLASHCARD_ANSWERS)
    @JsonIgnore
    private List<Answer> answers;
    @ManyToOne //OneToMany??
    @JoinColumn(name = JsonKeys.USER_ID, referencedColumnName = JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.AUTHOR)
    private User author;
    @ManyToOne
    @JoinColumn(name = JsonKeys.FLASHCARD_PARENT_ID)
    @JsonIgnore
    private CardDeck deck;
    @JsonProperty(JsonKeys.FLASHCARD_MULTIPLE_CHOICE)
    @Column(name = JsonKeys.FLASHCARD_MULTIPLE_CHOICE)
    private boolean multipleChoice;
    @Transient //not persistent.
    @JsonIgnore
    private boolean isSelected;
    @Transient //not persistent.
    @JsonIgnore
    private boolean isMarked;

    public FlashCard(User author, boolean multipleChoice, List<String> tags) {
        this.author = author;
        this.multipleChoice = multipleChoice;
    }

    public FlashCard(User author, List<Answer> answers, Question question, boolean multipleChoice) {
        this.author = author;
        if (answers != null) {
            this.answers = answers;
            for (Answer a : answers) {
                if (a != null) {
                    a.setCard(this);
                    a.update();
                }
            }
        }
        if (question != null) {
            this.question = question;
        }
        this.multipleChoice = multipleChoice;
    }

    public FlashCard(Date created, Question question, List<Answer> answers, User author, boolean multipleChoice) {
        this.created = created;
        if (answers != null) {
            this.answers = answers;
            for (Answer a : answers) {
                if (a != null) {
                    a.setCard(this);
                    a.update();
                }
            }
        }
        if (question != null) {
            this.question = question;
        }
        this.author = author;
        this.multipleChoice = multipleChoice;
    }

    public FlashCard(FlashCard requestObject) {
        this.author = requestObject.getAuthor();
        this.answers = requestObject.getAnswers();
        this.question = requestObject.getQuestion();
        this.multipleChoice = requestObject.isMultipleChoice();
        this.tags = requestObject.getTags();
    }

    /**
     * Adds one answer to this specific flashcard, updates the flashcards in the DB.
     *
     * @param answer to add
     */
    public void addAnswer(Answer answer) {
//        System.out.println("Flashcard: addAnswer a="+answer);
        if (answer != null && !this.answers.contains(answer)) {
            this.answers.add(answer);
            answer.setCard(this);
            this.update();
        }
    }

    @Override
    public String toString() {
        return "FlashCard{" +
                "id=" + id +
                ", rating=" + rating +
                ", created=" + created +
                ", lastUpdated=" + lastUpdated +
                ", author=" + author +
                ", multipleChoice=" + multipleChoice +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Question getQuestion() {
        return question;
    }

    /**
     * Sets the question of this card to a specific question object and updates the flashcard in the DB.
     *
     * @param question to set
     */
    public void setQuestion(Question question) {
//        System.out.println("Flashcard: setQuestionText q="+question);
        this.question = question;
    }

    @JsonIgnore
    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;

    }

    public CardDeck getDeck() {
        return deck;
    }

    public void setDeck(CardDeck deck) {
        this.deck = deck;
        this.update();
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    @JsonIgnore
    public boolean isSelected() {
        return isSelected;
    }

    @JsonIgnore
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @JsonIgnore
    public boolean isMarked() {
        return isMarked;
    }

    @JsonIgnore
    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            tag.addFlashCard(this);
        }
    }

    public List<Tag> getTags() {
//        for (Tag t: tags) {
//            System.out.print(t.getName());
//        }
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * Adds the given rating to the current rating, updates this instance and calls the function on the corresponding user.
     *
     * @param ratingModifier
     */
    public void updateRating(int ratingModifier) {
        this.rating += ratingModifier;
        this.update();
        //update user as well, work on the newest data from the db, not our local reference.
        User.find.byId(author.getId()).updateRating(ratingModifier);
    }

    @Override
    public void delete() {
        //Get all tags and unlink them from this card. Tag still exists to this point.
        for (Tag tmptag : tags) {
            tmptag.removeFlashCard(this);
            System.out.println("Removing link to tag=" + tmptag);
        }
        //answers.forEach(answer -> answer.delete());
        Question question=this.question;
        this.question=null;
        this.update();
        Logger.debug("q="+question);
        if(question!=null)question.delete();

        List<CardStatistics> cardStatisticsList=CardStatistics.finder.where().eq(JsonKeys.STATISTICS_CARD,this).findList();
        Logger.debug("statistics:"+ cardStatisticsList);
        cardStatisticsList.forEach(cardStatistics -> cardStatistics.delete());
        super.delete();
    }
}
