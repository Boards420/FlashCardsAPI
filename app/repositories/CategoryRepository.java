package repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.CardDeck;
import models.Category;
import models.User;
import play.Logger;
import play.mvc.BodyParser;
import util.JsonKeys;
import util.RequestKeys;
import util.UrlParamHelper;
import util.UserOperations;
import util.exceptions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Widmann
 */
public class CategoryRepository {
    /**
     * Retrieves all Categories.
     *
     * @return HTTPResult
     */
    public static List<Category> getCategoryList() {
        if (UrlParamHelper.checkBool(RequestKeys.ROOT)) {
            List<Category> emptyGroups = Category.find.where().isNull(JsonKeys.CATEGORY_PARENT).findList();
            return emptyGroups;
        }
        return Category.find.all();
    }

    /**
     * Retrieves the Category with the specific id, if it does not exist, return notFound.
     *
     * @param id of a category
     * @return category
     */
    public static Category getCategory(Long id) {
        return Category.find.byId(id);
    }

    /**
     * Get all card decks in a category.
     *
     * @param id of a category
     * @return decks inside of the category
     */
    public static List<CardDeck> getCategoryCardDecks(Long id) {
        return Category.find.byId(id).getCardDecks();
    }

    /**
     * Returns the children of the specified carddeck.
     *
     * @param id of a category
     * @return list of children
     */
    public static List<Category> getChildren(Long id) {
        Category parent = Category.find.byId(id);
        List<Category> children=new ArrayList<>();
        if(parent!=null) {
            children = Category.find.where().eq(JsonKeys.CATEGORY_PARENT, parent).findList();
            children.forEach(c -> System.out.println("c=" + c));
        }
        return children;
    }

    /**
     * Creates a new category, returns either an error or a success message.
     *
     * @return the newly created category
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Category addCategory(JsonNode json) throws PartiallyModifiedException, ObjectNotFoundException {
        String information = "";
        ObjectMapper mapper = new ObjectMapper();

        Category receivedCategory = mapper.convertValue(json, Category.class);
        Category category = new Category(receivedCategory);
        Logger.debug("rcvd=" + receivedCategory);

        if (receivedCategory.getId() > 0) {
            receivedCategory.setId(0);
        }
        //retrieve the parent by id
        if (json.has(JsonKeys.CATEGORY_PARENT) && json.get(JsonKeys.CATEGORY_PARENT).has(JsonKeys.CATEGORY_ID)) {
            Long parentId = json.get(JsonKeys.CATEGORY_PARENT).get(JsonKeys.CATEGORY_ID).asLong();
            category.setParent(parseParent(parentId));
        } else
            category.setParent(null);

        //handle cardDeck list
        List<CardDeck> cardDeckList = new ArrayList<>();
        if (receivedCategory.getCardDecks() != null) {
            for (CardDeck cardDeck : receivedCategory.getCardDecks()) {
                CardDeck tmp = CardDeck.find.byId(cardDeck.getId());
                //add it to the list if it isnt already in and isnt null
                if (!cardDeckList.contains(tmp) && tmp != null && tmp.getCategory() == null) {
                    cardDeckList.add(cardDeck);
                }
                //if it is null we can't handle the request, thus we send a notFound to the user
                else if (tmp == null) {
                    throw new ObjectNotFoundException("One cardDeck could not be found.", cardDeck.getId());
                }
                //if it is just a duplicate, add it to the information flag we add onto the reply for the user to read.
                else if (cardDeckList.contains(tmp))
                    information += " Error adding cardDeck" + cardDeck.getId() + ", it was sent more than once.";
                else if (tmp.getCategory() != null) {
                    information += " Error adding cardDeck" + cardDeck.getId() + ", it already has a parent.";
                }
            }
            Logger.debug("deckList=" + cardDeckList);
            category.setCardDecks(cardDeckList);
        }

        Logger.debug("finishing=" + category);
        category.save();
        //cardDecks themselves can be re-set to a new category at the moment,
        for (CardDeck cardDeck : cardDeckList) {
            cardDeck.setCategory(category);
            cardDeck.update();
        }
        String msg = "Category has been created!";
        if (information != "") {
            throw new PartiallyModifiedException("Category has been created! Additional information: " + information, category.getId());
        }
        return category;


    }


    /**
     * Updates the category specified by the first parameter. Email is needed to check if the user has the rights to perform the action.
     * Json contains all the changes we want to make. Method is there to check if PUT/PATCH is required.
     *
     * @param id     of a category
     * @param email  of the creating user
     * @param json   content of the request body
     * @param method PUT/PATCH
     * @return newly updated category
     * @throws InvalidInputException      if the input contains problems
     * @throws ObjectNotFoundException    if the object does not exist
     * @throws PartiallyModifiedException if the request could be solved but problems have been found
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Category updateCategory(Long id, String email, JsonNode json, String method) throws InvalidInputException, ObjectNotFoundException, PartiallyModifiedException, NotAuthorizedException, DuplicateKeyException {
        String information = "";
        boolean append = UrlParamHelper.checkBool(RequestKeys.APPEND);
        Logger.debug("Appending? " + append);
        ObjectMapper mapper = new ObjectMapper();

        Category receivedCategory = mapper.convertValue(json, Category.class);
        Category category = Category.find.byId(id);

        User author = User.find.where().eq(JsonKeys.USER_EMAIL, email).findUnique();
        // if the user has no rights to edit a category:
        //  1. he uses put - this is not allowed as he cannot change things other than append decks.
        //  2. he uses patch but does not append - this is not allowed as he may not change anything besides the decks.
        if ((method.equals("PUT") || !append) && !author.hasPermission(UserOperations.EDIT_CATEGORY, category))
            throw new NotAuthorizedException("This user is not authorized to modify the category with this id.");

        //Check whether the request was a put and if it was check if a param is missing, if that is the case --> bad req.
        if (method.equals("PUT") && (!json.has(JsonKeys.CATEGORY_NAME) || !json.has(JsonKeys.CATEGORY_DECK) || !json.has(JsonKeys.CATEGORY_PARENT))) {
            throw new InvalidInputException("The Update method needs all details of the category, such as name, " +
                    "an array of carddeck (ids) and a parent (null or id of another category).");
        }

        if (json.has(JsonKeys.CATEGORY_NAME)) {
            category.setName(receivedCategory.getName());
        }

        //retrieve the parent by id
        if (json.has(JsonKeys.CATEGORY_PARENT)) {
            if (json.get(JsonKeys.CATEGORY_PARENT).has(JsonKeys.CATEGORY_ID)) {
                Long parentId = json.get(JsonKeys.CATEGORY_PARENT).get(JsonKeys.CATEGORY_ID).asLong();

                //do not allow self loops or loops between two objects
                if (!containsEndlessLoop(id, parentId))
                    category.setParent(parseParent(parentId));
                else
                    throw new DuplicateKeyException("This parent is not allowed as it would create an endless loop.", 1);
            }
        }
        if (json.has(JsonKeys.CATEGORY_DECK)) {
            //handle cardDeck list
            List<CardDeck> cardDeckList = new ArrayList<>();
            if (!append) {
                for (CardDeck cardDeck :
                        category.getCardDecks()) {
                    cardDeck.setCategory(null);
                    cardDeck.update();
                }
            }
            if (receivedCategory.getCardDecks() != null) {
                for (CardDeck cardDeck : receivedCategory.getCardDecks()) {
                    CardDeck tmp = CardDeck.find.byId(cardDeck.getId());
                    //add it to the list if it isn't already in and isn't null

                    if (!cardDeckList.contains(tmp) && tmp != null && tmp.getCategory() == null) {
                        cardDeckList.add(cardDeck);
                        cardDeck.setCategory(category);
                        cardDeck.update();
                    }
                    //if it is null we can't handle the request, thus we send a notFound to the user
                    else if (tmp == null) {
                        throw new ObjectNotFoundException("One cardDeck could not be found.", cardDeck.getId());
                    }
                    //if it is just a duplicate, add it to the information flag we add onto the reply for the user to read.
                    else if (cardDeckList.contains(tmp))
                        information += " Error adding cardDeck" + cardDeck.getId() + ", it was sent more than once.";
                    else if (tmp.getCategory() != null) {
                        information += " Error adding cardDeck" + cardDeck.getId() + ", it already has a parent.";
                    }
                }
                category.getCardDecks();
                Logger.debug("deckList=" + cardDeckList + " category list=" + category.getCardDecks());
            }
        }

        category.update();

        if (information != "") {
            throw new PartiallyModifiedException("Category has been updated! Additional information: " + information, category.getId());
        }
        return category;
    }

    /**
     * This method checks whether the Category with a given id is already a parent for the to-be parent with parentId.
     * e.g. 1<-2<-3<-4 are connected categories where cat.id. 4 has parent 3.
     * if we now wanted to set 1 as child to 4 this method would find the loop and deny the operation.
     * The result for the illegal operation would be 1<-2<-3<-4<-1.
     *
     * @param id       of the category
     * @param parentId of the future parent category
     * @return true if a loop would exist or false if none is contained.
     */
    private static boolean containsEndlessLoop(Long id, Long parentId) {
        Category child = Category.find.byId(id);
        Category parent = Category.find.byId(parentId);
        Category temp = parent;
        boolean hasLoop = false;
        Logger.debug("Checking if the current category is not already the parent to any nodes of our to-be parent.");
        while (temp != null) {
            Logger.debug("current id=" + temp.getId() + " parent=" + temp.getParent());
            if (temp.getId() == child.getId())
                hasLoop = true;
            temp = temp.getParent();
        }

        return hasLoop;
    }

/*    public static Result deleteCategory(Long id){
        try{
            Category.find.byId(id).delete();
            return noContent();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return notFound();
    }*/

    /**
     * Retrieves the parent category from the given category. If the id of the parent object cant be found in the database, throw the exception.
     *
     * @param id of the parent
     * @return the category from db or null if null is received
     * @throws ObjectNotFoundException if the object does not exist.
     */
    public static Category parseParent(Long id) throws ObjectNotFoundException {
        if (id > 0) {
            Category parent = Category.find.byId(id);
            Logger.debug("got parent=" + parent);
            if (parent != null) {
                return parent;
            } else
                throw new ObjectNotFoundException("Parent does not exist with the id=" + id);
        } else
            return null;
    }

    /**
     * Returns either the existing category or null
     * @param value
     * @return category if existing or null
     */
    public static Category findCategoryByName(String value) {
        return Category.find.where().eq(JsonKeys.CATEGORY_NAME,value).findUnique();
    }
}
