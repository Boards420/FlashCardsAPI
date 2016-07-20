package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import play.data.validation.Constraints;
import util.JsonKeys;

import javax.persistence.*;
import java.net.URI;
import java.util.Date;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 *         on 17/06/16.
 */
@Entity
@JsonPropertyOrder({ JsonKeys.ANSWER_ID})
public class Answer extends Model {
    @Id @GeneratedValue
    @Column(name = JsonKeys.ANSWER_ID)
    @JsonProperty(JsonKeys.ANSWER_ID)
    private long id;

    @Constraints.Required
    @JsonProperty(JsonKeys.ANSWER_TEXT)
    private String answerText;
    @JsonProperty(JsonKeys.ANSWER_HINT)
    private String hintText;
    @JsonProperty(JsonKeys.URI)
    private URI mediaURI;

    // TODO: 11/07/16  Ist die Antwort richtig oder falsch?
   @ManyToOne
   @JoinColumn(name="author_id", referencedColumnName=JsonKeys.USER_ID)
   @JsonProperty(JsonKeys.AUTHOR)
    private User author;

    @ManyToOne
    @JoinColumn(name="parent_card_id")
    @JsonIgnore
    private FlashCard card;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z") @CreatedTimestamp
    @JsonProperty(JsonKeys.DATE_CREATED)
    private Date created;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z")
    @UpdatedTimestamp
    @JsonProperty(JsonKeys.DATE_UPDATED)
    private Date lastUpdated;
    @JsonProperty(JsonKeys.RATING)
    private int rating;

    public static Model.Finder<Long, Answer> find = new Model.Finder<Long, Answer>(Answer.class);

    public Answer(String answerText, String hintText, User author) {
        this.answerText = answerText;
        this.hintText = hintText;
        this.author = author;
    }


    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", answerText='" + answerText + '\'' +
                ", hintText='" + hintText + '\'' +
                ", mediaURI=" + mediaURI +
                ", author=" + author +
                ", card=" + card +
                ", created=" + created +
                ", rating=" + rating +
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

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public URI getMediaURI() {
        return mediaURI;
    }

    public void setMediaURI(URI mediaURI) {
        this.mediaURI = mediaURI;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
    @JsonIgnore
    public FlashCard getCard() {
        return card;
    }

    public Date getCreated() {
        return created;
    }

    public void setCard(FlashCard card) {
        this.card = card;
        this.update();
    }
}
