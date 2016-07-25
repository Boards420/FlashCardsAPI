package util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Answer;
import models.Tag;
import models.User;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtil {
//	public final static String dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Wraps the object it receives in a json file
     *
     * @param o
     * @return
     */
    public static JsonNode getJson(Object o) {
        ObjectMapper mapper = new ObjectMapper();
//		SimpleDateFormat outputFormat = new SimpleDateFormat(dateformat);
//		mapper.setDateFormat(outputFormat);
//		Json.setObjectMapper(mapper);
        return Json.toJson(o);
    }

    /**
     * Generates an objectnode that will contain a given statuscode and the
     * description in human readable form.
     *
     * @param statuscode
     * @param description
     * @return
     */
    public static ObjectNode prepareJsonStatus(int statuscode,
                                               String description) {
        ObjectNode result = Json.newObject();
        result.put("statuscode", statuscode);
        result.put("description", description);
        return result;
    }

    /**
     * Generates an objectnode that will contain a given statuscode and the
     * description in human readable form.
     *
     * @param statuscode
     * @param description
     * @return
     */
    public static ObjectNode prepareJsonStatus(int statuscode, String description, int id) {
        ObjectNode result = Json.newObject();
        result.put("statuscode", statuscode);
        result.put("description", description);
        result.put("id",id);
        return result;
    }

    /**
     * Wraps the Map given as parameter in json.
     * @param data - in the form of <key, obj>
     * @return json representation of the given data
     */
    public static ObjectNode convertToJsonNode(Map<String, Object> data){
        ObjectNode result=Json.newObject();

        for (String key: data.keySet()) {
            result.set(key,getJson(data.get(key)));
        }
        return result;
    }

    /**
     * Reads all tags either via their id, or creates a new tag when it does not exist at the moment.
     *
     * @param json the root json object
     * @return a list of tags
     */
    public static List<Tag> retrieveTags(JsonNode json) {
        List<Tag> tags = new ArrayList<>();
        //get the specific nods in the json
        JsonNode tagNode = json.findValue(JsonKeys.FLASHCARD_TAGS);
        // Loop through all objects in the values associated with the
        // "users" key.
        for (JsonNode node : tagNode) {
            // when a user id is found we will get the object and add them to the userList.
            System.out.println("Node=" + node);
            if (node.has(JsonKeys.TAG_ID)) {
                Tag found = Tag.find.byId(node.get(JsonKeys.TAG_ID).asLong());
                System.out.println(">> tag: " + found);
                tags.add(found);
            } else {
                try {
                    Tag tmpT = parseTag(node);
                    System.out.println(">> tag: " + tmpT);
                    tags.add(tmpT);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return tags;
    }

    /**
     * Parses a question from the given JsonNode node.
     * @param node the json node to parse
     * @return a question object containing the information
     * @throws URISyntaxException
     */
    public static Tag parseTag(JsonNode node) throws URISyntaxException {
        User author=null;
        String tagText=null;

        if(node.has(JsonKeys.TAG_NAME)){
            tagText=node.get(JsonKeys.QUESTION_TEXT).asText();
        }
        Tag tag=new Tag(tagText);

        return tag;
    }

    /**
     * Reads all answers either via their id, or creates a new answer when it does not exist at the moment.
     *
     * @param json the root json object
     * @return a list of answers
     */
    public static List<Answer> retrieveAnswers(JsonNode json) {
        List<Answer> answers = new ArrayList<>();

        //get the specific nods in the json
        JsonNode answersNode = json.findValue(JsonKeys.FLASHCARD_ANSWERS);
        // Loop through all objects in the values associated with the
        // "users" key.
        for (JsonNode node : answersNode) {
            // when a user id is found we will get the object and add them to the userList.
            System.out.println("Node=" + node);
            if (node.has(JsonKeys.ANSWER_ID)) {
                Answer found = Answer.find.byId(node.get(JsonKeys.ANSWER_ID).asLong());
                System.out.println(">> answer: " + found);
                answers.add(found);
            } else {
                try {
                    Answer tmpA = JsonUtil.parseAnswer(node);
                    System.out.println(">> answer: " + tmpA);
                    answers.add(tmpA);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return answers;
    }

    /**
     * Parses answers from the given JsonNode node.
     * @param node the json node to parse
     * @return list of answers
     * @throws URISyntaxException
     */
    public static Answer parseAnswer(JsonNode node) throws URISyntaxException {
        User author=null;
        String answerText=null;
        String hintText=null;
        if(node.has(JsonKeys.ANSWER_HINT)){
            hintText=node.get(JsonKeys.ANSWER_HINT).asText();
        }
        if(node.has(JsonKeys.AUTHOR)){
            if(node.get(JsonKeys.AUTHOR).has(JsonKeys.USER_ID)){
                long uid=node.get(JsonKeys.AUTHOR).get(JsonKeys.USER_ID).asLong();
                author=User.find.byId(uid);
                System.out.println("Search for user with id="+uid+" details="+author);
            }
        }
        if(node.has(JsonKeys.ANSWER_TEXT)){
            answerText=node.get(JsonKeys.ANSWER_TEXT).asText();
        }
        Answer answer=new Answer(answerText,hintText,author);

        if(node.has(JsonKeys.URI)){
            answer.setMediaURI(new URI(node.get(JsonKeys.URI).asText()));
        }
        return answer;
    }

}
